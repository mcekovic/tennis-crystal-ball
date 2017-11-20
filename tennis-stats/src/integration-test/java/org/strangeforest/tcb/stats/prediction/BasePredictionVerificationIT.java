package org.strangeforest.tcb.stats.prediction;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.test.context.testng.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.service.*;

import com.google.common.util.concurrent.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

public abstract class BasePredictionVerificationIT extends AbstractTestNGSpringContextTests {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MatchPredictionService predictionService;

	private static final double MIN_PREDICTABILITY = 0.0;
	private static final String PRICE_SOURCE = "B365";
	private static final boolean BET_ON_OUTSIDER = false;
	private static final boolean KELLY_STAKE = true;

	private static final int THREADS = 8;
	private static final int MATCHES_PER_TICK = 1000;
	private static final int PROGRESS_LINE_WRAP = 100;

	private static final String MATCHES_QUERY = //language=SQL
		"SELECT m.winner_id, m.loser_id, m.date, m.tournament_id, m.tournament_event_id, m.level, m.best_of, m.surface, m.indoor, m.round, p.winner_price, p.loser_price\n" +
		"FROM match_for_stats_v m\n" +
		"LEFT JOIN match_price p ON p.match_id = m.match_id AND source = :source\n" +
		"WHERE m.date BETWEEN :date1 AND :date2\n" +
		"ORDER BY m.date";


	protected PredictionResult verifyPrediction(LocalDate fromDate, LocalDate toDate) throws InterruptedException {
		System.out.printf("\nVerifying prediction from %1$s to %2$s and weights:\n", fromDate, toDate);
		printWeights();
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		AtomicInteger total = new AtomicInteger();
		AtomicInteger predicted = new AtomicInteger();
		AtomicInteger hits = new AtomicInteger();
		AtomicInteger hasPrice = new AtomicInteger();
		AtomicInteger profitable = new AtomicInteger();
		AtomicInteger beatingPrice = new AtomicInteger();
		AtomicDouble profit = new AtomicDouble();
		AtomicInteger ticks = new AtomicInteger();
		for (MatchForVerification match : matches(fromDate, toDate)) {
			executor.execute(() -> {
				MatchPrediction prediction = predictionService.predictMatch(match.winnerId, match.loserId, match.date, match.tournamentId, match.tournamentEventId, false, match.surface, match.indoor, match.level, match.bestOf, match.round);
				if (prediction.getPredictability1() > MIN_PREDICTABILITY) {
					predicted.incrementAndGet();
					double winnerProbability = prediction.getWinProbability1();
					double loserProbability = prediction.getWinProbability2();
					Double winnerPrice = match.winnerPrice;
					Double loserPrice = match.loserPrice;
					if (winnerProbability > 0.5)
						hits.incrementAndGet();
					if (winnerPrice != null || loserPrice != null)
						hasPrice.incrementAndGet();
					if (winnerProbability > 0.5 || BET_ON_OUTSIDER) {
						if (winnerPrice != null && winnerProbability > 1.0 / winnerPrice) {
							beatingPrice.incrementAndGet();
							profitable.incrementAndGet();
							double stake = KELLY_STAKE ? kellyStake(winnerProbability, winnerPrice) : 1.0;
							profit.addAndGet(stake * (winnerPrice - 1.0));
						}
					}
					if (loserProbability > 0.5 || BET_ON_OUTSIDER) {
						if (loserPrice != null && loserProbability > 1.0 / loserPrice) {
							beatingPrice.incrementAndGet();
							double stake = KELLY_STAKE ? kellyStake(loserProbability, loserPrice) : 1.0;
							profit.addAndGet(-stake);
						}
					}
				}
				if (total.incrementAndGet() % MATCHES_PER_TICK == 0) {
					System.out.print('.');
					if (ticks.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
						System.out.println();
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(1L, TimeUnit.HOURS);
		double predictablePct = 100.0 * predicted.get() / total.get();
		double predictionRate = 100.0 * hits.get() / predicted.get();
		System.out.printf("\nPredictable: %1$.3f%%\n", predictablePct);
		System.out.printf("Prediction rate: %1$.3f%%\n", predictionRate);
		double profitPct = 0.0;
		if (hasPrice.get() > 0.0) {
			double hasPricePct = 100.0 * hasPrice.get() / predicted.get();
			double beatingPriceRate = 100.0 * beatingPrice.get() / predicted.get();
			double profitablePct = 100.0 * profitable.get() / beatingPrice.get();
			profitPct = 100.0 * profit.get() / beatingPrice.get();
			System.out.printf("Has price: %1$.3f%%\n", hasPricePct);
			System.out.printf("Beating price rate: %1$.3f%%\n", beatingPriceRate);
			System.out.printf("Profitable: %1$.3f%%\n", profitablePct);
			System.out.printf("Profit: %1$.3f%%\n", profitPct);
		}
		return new PredictionResult(predictablePct, predictionRate, profitPct);
	}

	private static double kellyStake(double probability, double price) {
		return (probability * price - 1) / (price - 1);
	}

	private List<MatchForVerification> matches(LocalDate date1, LocalDate date2) {
		return jdbcTemplate.query(
			MATCHES_QUERY,
			params("source", PRICE_SOURCE).addValue("date1", date1).addValue("date2", date2),
			(rs, rowNum) -> match(rs)
		);
	}

	private static MatchForVerification match(ResultSet rs) throws SQLException {
		return new MatchForVerification(
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			toLocalDate(rs.getDate("date")),
			rs.getInt("tournament_id"),
			rs.getInt("tournament_event_id"),
			rs.getString("level"),
			rs.getShort("best_of"),
			rs.getString("surface"),
			rs.getBoolean("indoor"),
			rs.getString("round"),
			getDouble(rs,"winner_price"),
			getDouble(rs,"loser_price")
		);
	}

	protected static void setWeights(double weight) {
		setWeights(weight, weight);
	}

	protected static void setWeights(double areaWeight, double itemWeight) {
		for (PredictionArea area : PredictionArea.values())
			area.setWeights(areaWeight, itemWeight);
	}

	protected static void printWeights() {
		for (PredictionArea area : PredictionArea.values()) {
			double areaWeight = area.getWeight();
			if (areaWeight > 0.0) {
				System.out.printf("%1$s: %2$s %3$s\n", area, areaWeight,
					Stream.of(area.getItems()).filter(item -> item.getWeight() > 0.0).map(item -> format("%1$s: %2$s", item, item.getWeight())).collect(toList())
				);
			}
		}
	}
}
