package org.strangeforest.tcb.stats.prediction;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.test.context.testng.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.service.*;
import org.testng.annotations.*;

import com.google.common.base.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

public abstract class BasePredictionVerificationIT extends AbstractTestNGSpringContextTests {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MatchPredictionService predictionService;
	private ExecutorService executor;

	private static final TuningSetLevel TUNING_SET_LEVEL = TuningSetLevel.SURFACE;
	private static final double MIN_PREDICTABILITY = 0.25;
	private static final String PRICE_SOURCE = "B365";
	private static final boolean BET_ON_OUTSIDER = false;
	private static final boolean KELLY_STAKE = true;

	private static final String MATCHES_QUERY = //language=SQL
		"SELECT m.winner_id, m.loser_id, m.date, m.tournament_id, m.tournament_event_id, m.level, m.best_of, m.surface, m.indoor, m.round, m.winner_rank, m.loser_rank, p.winner_price, p.loser_price\n" +
		"FROM match_for_stats_v m\n" +
		"LEFT JOIN match_price p ON p.match_id = m.match_id AND source = :source\n" +
		"WHERE (m.date BETWEEN :date1 AND :date2)%1$s\n" +
		"ORDER BY m.date";


	@BeforeClass
	public void setUp() {
		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	@AfterClass
	public void tearDown() {
		executor.shutdown();
	}

	protected PredictionVerificationResult verifyPrediction(LocalDate fromDate, LocalDate toDate) throws InterruptedException {
		return verifyPrediction(fromDate, toDate, null, TuningSet.OVERALL);
	}

	protected PredictionVerificationResult verifyPrediction(LocalDate fromDate, LocalDate toDate, TuningSet tuningSet) throws InterruptedException {
		return verifyPrediction(fromDate, toDate, null, tuningSet);
	}

	protected PredictionVerificationResult verifyPrediction(LocalDate fromDate, LocalDate toDate, PredictionConfig config) throws InterruptedException {
		return verifyPrediction(fromDate, toDate, config, TuningSet.OVERALL);
	}

	protected PredictionVerificationResult verifyPrediction(LocalDate fromDate, LocalDate toDate, PredictionConfig config, TuningSet tuningSet) throws InterruptedException {
		System.out.printf("\nVerifying prediction from %1$s to %2$s and weights: ", fromDate, toDate);
		printWeights(config, tuningSet, true);
		Stopwatch stopwatch = Stopwatch.createStarted();
		PredictionVerificationResult verificationResult = new PredictionVerificationResult(config);
		List<MatchForVerification> matches = matches(fromDate, toDate, tuningSet.getCondition());
		CountDownLatch matchCount = new CountDownLatch(matches.size());
		for (MatchForVerification match : matches) {
			executor.execute(() -> {
				try {
					Surface surface = match.surface;
					boolean indoor = match.indoor;
					TournamentLevel level = match.level;
					short bestOf = match.bestOf;
					PredictionConfig matchConfig = config != null ? config : PredictionConfig.defaultConfig(TUNING_SET_LEVEL.select(surface, indoor, level, bestOf));
					MatchPrediction prediction = predictionService.predictMatch(match.winnerId, match.loserId, match.date, match.tournamentId, match.tournamentEventId, false, surface, indoor, level, bestOf, match.round, matchConfig);
					boolean predictable = false, predicted = false, withPrice = false, beatingPrice = false, profitable = false;
					double winnerProbability = 0.0, stake = 0.0, return_ = 0.0;
					if (prediction.getPredictability1() > MIN_PREDICTABILITY) {
						predictable = true;
						winnerProbability = prediction.getWinProbability1();
						double loserProbability = prediction.getWinProbability2();
						Double winnerPrice = match.winnerPrice;
						Double loserPrice = match.loserPrice;
						if (winnerProbability > 0.5)
							predicted = true;
						if (winnerPrice != null || loserPrice != null) {
							withPrice = true;
							if (winnerProbability > 0.5 || BET_ON_OUTSIDER) {
								if (winnerPrice != null && winnerProbability > 1.0 / winnerPrice) {
									beatingPrice = true;
									profitable = true;
									stake = KELLY_STAKE ? kellyStake(winnerProbability, winnerPrice) : 1.0;
									return_ = stake * winnerPrice;
								}
							}
							if (loserProbability > 0.5 || BET_ON_OUTSIDER) {
								if (loserPrice != null && loserProbability > 1.0 / loserPrice) {
									beatingPrice = true;
									stake = KELLY_STAKE ? kellyStake(loserProbability, loserPrice) : 1.0;
								}
							}
						}
					}
					verificationResult.newMatch(match, predictable, winnerProbability, predicted, withPrice, beatingPrice, profitable, stake, return_);
				}
				finally {
					matchCount.countDown();
				}
			});
		}
		matchCount.await();
		verificationResult.complete();
		PredictionResult result = verificationResult.getResult();
		System.out.printf("%1$s in %2$s\n", result, stopwatch);
		return verificationResult;
	}

	private static double kellyStake(double probability, double price) {
		return (probability * price - 1) / (price - 1);
	}

	private List<MatchForVerification> matches(LocalDate date1, LocalDate date2, String condition) {
		return jdbcTemplate.query(
			format(MATCHES_QUERY,  condition),
			params("source", PRICE_SOURCE).addValue("date1", date1).addValue("date2", date2),
			(rs, rowNum) -> match(rs)
		);
	}

	private static MatchForVerification match(ResultSet rs) throws SQLException {
		return new MatchForVerification(
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			getLocalDate(rs, "date"),
			rs.getInt("tournament_id"),
			rs.getInt("tournament_event_id"),
			rs.getString("level"),
			rs.getShort("best_of"),
			rs.getString("surface"),
			rs.getBoolean("indoor"),
			rs.getString("round"),
			getInteger(rs,"winner_rank"),
			getInteger(rs,"loser_rank"),
			getDouble(rs,"winner_price"),
			getDouble(rs,"loser_price")
		);
	}

	protected static void printWeights(PredictionConfig config, TuningSet tuningSet, boolean compact) {
		if (config == null && tuningSet.getLevel() != TuningSetLevel.TOP)
			config = PredictionConfig.defaultConfig(tuningSet);
		if (config != null)
			printWeights(config, compact);
		else
			System.out.println("Using variable tuning set weights");
	}

	protected static void printWeights(PredictionConfig config, boolean compact) {
		for (PredictionArea area : PredictionArea.values()) {
			double areaWeight = config.getAreaWeight(area);
			if (areaWeight > 0.0) {
				System.out.printf("%1$s: %2$s %3$s%4$s", area, areaWeight,
					Stream.of(area.getItems()).filter(item -> config.getItemWeight(item) > 0.0).map(item -> format("%1$s: %2$s", item, config.getItemWeight(item))).collect(toList()),
					compact ? "; " : "\n"
				);
			}
		}
		if (compact)
			System.out.println();
	}

	protected static void printResultDistribution(PredictionVerificationResult result) {
		System.out.println(result.getProbabilityRangeResults());
		System.out.println(result.getSurfaceResults());
		System.out.println(result.getLevelResults());
		System.out.println(result.getRankRangeResults());
	}
}
