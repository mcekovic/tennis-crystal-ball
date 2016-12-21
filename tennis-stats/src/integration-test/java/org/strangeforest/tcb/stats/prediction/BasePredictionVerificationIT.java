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

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.params;

public abstract class BasePredictionVerificationIT extends AbstractTestNGSpringContextTests {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MatchPredictionService predictionService;

	private static final int THREADS = 8;
	private static final int MATCHES_PER_TICK = 1000;
	private static final int PROGRESS_LINE_WRAP = 100;

	private static final String MATCHES_QUERY = //language=SQL
		"SELECT winner_id, loser_id, date, level, surface, round, best_of\n" +
		"FROM match_for_stats_v\n" +
		"WHERE date BETWEEN :date1 AND :date2\n" +
		"ORDER BY date";


	protected void verifyPrediction(LocalDate fromDate, LocalDate toDate) throws InterruptedException {
		System.out.printf("\nVerifying prediction from %1$s to %2$s and weights:\n", fromDate, toDate);
		printWeights();
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		AtomicInteger total = new AtomicInteger();
		AtomicInteger predicted = new AtomicInteger();
		AtomicInteger hits = new AtomicInteger();
		AtomicInteger ticks = new AtomicInteger();
		for (MatchForVerification match : matches(fromDate, toDate)) {
			executor.execute(() -> {
				MatchPrediction prediction = predictionService.predictMatch(match.winnerId, match.loserId, match.date, match.surface, match.level, match.round, match.best_of);
				if (!prediction.isEmpty()) {
					predicted.incrementAndGet();
					if (prediction.getWinProbability1() > 0.5)
						hits.incrementAndGet();
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
		System.out.printf("\nPredicted: %1$.2f%%\n", 100.0 * predicted.get() / total.get());
		System.out.printf("Prediction rate: %1$.2f%%\n", 100.0 * hits.get() / predicted.get());
	}

	private List<MatchForVerification> matches(LocalDate date1, LocalDate date2) {
		return jdbcTemplate.query(
			MATCHES_QUERY,
			params("date1", date1).addValue("date2", date2),
			(rs, rowNum) -> match(rs)
		);
	}

	private static MatchForVerification match(ResultSet rs) throws SQLException {
		return new MatchForVerification(
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			rs.getDate("date"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getString("round"),
			rs.getShort("best_of")
		);
	}

	private static void printWeights() {
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
