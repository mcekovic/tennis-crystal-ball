package org.strangeforest.tcb.stats.prediction;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.service.*;
import org.testng.annotations.*;

import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionVerificationIT extends AbstractTestNGSpringContextTests {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MatchPredictionService predictionService;

	private static final int THREADS = 8;

	private static final String MATCHES_QUERY = //language=SQL
		"SELECT winner_id, loser_id, date, level, surface, round, best_of\n" +
		"FROM match_for_stats_v\n" +
		"WHERE date BETWEEN :date1 AND :date2\n" +
		"ORDER BY date";

	@Test
	public void verifyPrediction() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		AtomicInteger total = new AtomicInteger();
		AtomicInteger predicted = new AtomicInteger();
		AtomicInteger hits = new AtomicInteger();
		for (MatchForVerification match : matches(LocalDate.of(2000, 1, 1), LocalDate.now())) {
			executor.execute(() -> {
				MatchPrediction prediction = predictionService.predictMatch(match.winnerId, match.loserId, match.date, match.surface, match.level, match.round, match.best_of);
				if (!prediction.isEmpty()) {
					predicted.incrementAndGet();
					if (prediction.getWinProbability1() > 0.5)
						hits.incrementAndGet();
				}
				if (total.incrementAndGet() % 100 == 0)
					System.out.print('.');
			});
		}
		executor.shutdown();
		executor.awaitTermination(1L, TimeUnit.HOURS);
		System.out.printf("\nPrediction rate: %1$.2f%%\n", 100.0 * hits.get() / predicted.get());
		System.out.printf("Predicted: %1$.2f%%\n", 100.0 * predicted.get() / total.get());
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
}
