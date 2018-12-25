package org.strangeforest.tcb.stats.model.elo;

import java.sql.*;
import java.time.*;
import java.time.temporal.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.*;
import java.util.concurrent.atomic.*;
import javax.sql.*;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.*;
import org.strangeforest.tcb.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static java.lang.Math.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.elo.EloCalculator.*;
import static org.strangeforest.tcb.stats.model.elo.StartEloRatings.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

public class EloRatingsManager {

	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate txTemplate;
	private final EloSurfaceFactors eloSurfaceFactors;
	private final LockManager<Integer> lockManager;
	private Map<String, Map<Integer, EloRating>> playerRatings;
	private Set<LocalDate> dates;
	private Map<LocalDate, Map<Integer, Integer>> rankingTables;
	private Map<String, PredictionResult> predictionResults;
	private LocalDate saveFromDate;
	private ExecutorService saveExecutor;
	private BlockingQueue<MatchEloRating> matchRatingsForSave;
	private ProgressTicker matchTicker, saveTicker;

	// Factors and tennis constants
	private static final List<String> RATING_TYPES = asList("E", "R", "H", "C", "G", "P", "O", "I", "s", "g", "sg", "rg", "tb");
	private static final LocalDate CARPET_END = LocalDate.of(2008, 1, 1);
	private static final LocalDate STATS_START = LocalDate.of(1991, 1, 1);
	private static final LocalDate TIE_BREAK_START = LocalDate.of(1970, 1, 1);
	private static final LocalDate PREDICTION_START_DATE = LocalDate.of(2005, 1, 1);
	private static final int DEFAULT_MIN_MATCHES = 10;
	private static final Map<String, Integer> MIN_MATCHES = ImmutableMap.<String, Integer>builder()
		.put("R", 5).put("H", 5).put("C", 5).put("G", 5).put("P", 5).put("O", 5).put("I", 5)
		.put("s", 5).put("g", 2).put("sg", 3).put("rg", 3)
	.build();
	private static final int DEFAULT_MIN_MATCHES_PERIOD = 365;
	private static final Map<String, Integer> MIN_MATCHES_PERIOD = ImmutableMap.<String, Integer>builder().put("R", 90).build();
	private static final int MIN_MATCHES_IN_PERIOD = 3;

	// Player counts
	private static final int PLAYERS_TO_SAVE = 200;

	// Technical
	private static final int MATCHES_FETCH_SIZE = 200;
	private static final int RANK_PRELOAD_FETCH_SIZE = 2000;
	private static final int SAVE_EXECUTOR_QUEUE_CAPACITY = 200;
	private static final int MATCH_RATINGS_FOR_SAVE_QUEUE_CAPACITY = 10000;

	// Progress tracking
	private static final int MATCHES_PER_DOT = 1000;
	private static final int SAVES_PER_PLUS = 20;
	private static final int RANK_PRELOADS_PER_DOT = 50000;


	private static final String LAST_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS last_date FROM player_elo_ranking";

	private static final String RANKING_DATES_QUERY = //language=SQL
		"SELECT DISTINCT rank_date FROM player_elo_ranking";

	private static final String DELETE_RANKING_FOR_DATE = //language=SQL
		"DELETE FROM player_elo_ranking WHERE rank_date = ?";

	private static final String PLAYER_RANKS_QUERY = //language=SQL
		"SELECT player_id, rank_date, rank\n" +
		"FROM player_ranking\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY rank_date, player_id";

	private static final String MATCHES_QUERY = //language=SQL
		"SELECT m.match_id, m.winner_id, m.loser_id, tournament_end(CASE WHEN e.level = 'D' THEN m.date ELSE e.date END, e.level, e.draw_size) AS end_date, e.level, m.best_of, m.surface, m.indoor, m.round, m.outcome,\n" +
		"  m.w_sets, m.l_sets, m.w_games, m.l_games, s.w_sv_gms - (s.w_bp_fc - s.w_bp_sv) AS w_sv_gms, s.l_sv_gms - (s.l_bp_fc - s.l_bp_sv) AS l_sv_gms, s.l_bp_fc - s.l_bp_sv AS w_rt_gms, s.w_bp_fc - s.w_bp_sv AS l_rt_gms, m.w_tbs, m.l_tbs, m.has_stats\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN match_stats s ON s.match_id = m.match_id AND s.set = 0\n" +
		"WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B', 'D', 'T')\n" +
		"ORDER BY end_date, m.round, m.match_num, m.winner_id, m.loser_id";

	private static final String MERGE_ELO_RANKING = //language=SQL
		"INSERT INTO player_elo_ranking\n" +
		"(rank_date, player_id, rank, elo_rating, recent_rank, recent_elo_rating,\n" +
		" hard_rank, hard_elo_rating, clay_rank, clay_elo_rating, grass_rank, grass_elo_rating, carpet_rank, carpet_elo_rating, outdoor_rank, outdoor_elo_rating, indoor_rank, indoor_elo_rating,\n" +
		" set_rank, set_elo_rating, game_rank, game_elo_rating, service_game_rank, service_game_elo_rating, return_game_rank, return_game_elo_rating, tie_break_rank, tie_break_elo_rating)\n" +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_MATCH_ELO_RATINGS = //language=SQL
		"UPDATE match SET winner_elo_rating = ?, winner_next_elo_rating = ?, loser_elo_rating = ?, loser_next_elo_rating = ?\n" +
		"WHERE match_id = ?";


	public EloRatingsManager(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		txTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
		eloSurfaceFactors = new EloSurfaceFactors(jdbcTemplate);
		lockManager = new LockManager<>();
		playerRatings = new HashMap<>();
		predictionResults = new HashMap<>();
		txTemplate.execute(s -> {
			loadRanks();
			return null;
		});
	}

	public Map<String, PredictionResult> compute(boolean save, boolean fullSave, LocalDate saveFromDate, int saveThreads) throws InterruptedException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		for (String type : RATING_TYPES) {
			playerRatings.put(type, new ConcurrentHashMap<>());
			predictionResults.put(type, new PredictionResult());
		}

		if (save) {
			if (fullSave)
				dates = new ConcurrentSkipListSet<>(fetchRankingDates());
			else
				this.saveFromDate = saveFromDate != null ? saveFromDate : fetchLastDate().plusDays(1);
			saveExecutor = new ThreadPoolExecutor(saveThreads, saveThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(SAVE_EXECUTOR_QUEUE_CAPACITY), new CallerRunsPolicy());
			matchRatingsForSave = new LinkedBlockingDeque<>(MATCH_RATINGS_FOR_SAVE_QUEUE_CAPACITY);
			System.out.printf("Processing matches using %1$d saving threads\n", saveThreads);
		}
		else
			System.out.println("Processing matches");

		ProgressTicker newLineTicker = ProgressTicker.newLineTicker();
		matchTicker = new ProgressTicker('.', MATCHES_PER_DOT).withDownstreamTicker(newLineTicker);
		saveTicker = new ProgressTicker('+', SAVES_PER_PLUS).withDownstreamTicker(newLineTicker);
		try {
			AtomicReference<LocalDate> lastDateRef = new AtomicReference<>();
			txTemplate.execute(s -> {
				jdbcTemplate.query(MATCHES_QUERY, ps -> ps.setFetchSize(MATCHES_FETCH_SIZE), rs -> {
					MatchForElo match = mapMatch(rs);
					LocalDate lastDate = lastDateRef.get();
					if (lastDate != null && !lastDate.equals(match.endDate))
						saveCurrentRatings(lastDate);
					processMatch(match, "E");
					processMatch(match, "R");
					processMatch(match, true, false);
					processMatch(match, false, true);
					processMatch(match, "s");
					processMatch(match, "g");
					if (match.hasStats) {
						processMatch(match, "sg");
						processMatch(match, "rg");
					}
					processMatch(match, "tb");
					lastDateRef.set(match.endDate);
					matchTicker.tick();
				});
				return null;
			});
			saveCurrentRatings(lastDateRef.get());
		}
		finally {
			if (save) {
				saveExecutor.shutdown();
				saveExecutor.awaitTermination(1L, TimeUnit.DAYS);
			}
		}
		System.out.println();

		if (save && fullSave && !dates.isEmpty()) {
			System.out.printf("Deleting remaining dates: %1$d\n", dates.size());
			for (LocalDate date : new ArrayList<>(dates)) {
				deleteForDate(date);
				System.out.println(date);
			}
		}

		System.out.printf("Elo Ratings for %1$d matches computed in %2$s\n", matchTicker.getTicks(), stopwatch);
		if (save)
			System.out.printf("Saves: %1$d\n", saveTicker.getTicks());
		PredictionResult bySurfaceResult = new PredictionResult().add(getPredictionResult("H")).add(getPredictionResult("C")).add(getPredictionResult("G")).add(getPredictionResult("P"));
		for (String type : RATING_TYPES)
			printPredictionResult(type, getPredictionResult(type));
		printPredictionResult("S", bySurfaceResult);
		return predictionResults;
	}

	public List<EloRating> getRatings(String type, int count, LocalDate date) {
		LocalDate minDate = date.minusYears(1);
		int minMatches = minMatches(type);
		int minMatchesPeriod = minMatchesPeriod(type);
		return getRatings(type).values().stream()
			.filter(r ->r.rating >= START_RATING && r.matches >= minMatches && !r.getLastDate().isBefore(minDate) && r.getDaysSpan(date) <= minMatchesPeriod)
			.sorted(reverseOrder())
			.limit(count)
			.collect(toList());
	}

	private static int minMatches(String type) {
		return MIN_MATCHES.getOrDefault(type, DEFAULT_MIN_MATCHES);
	}

	private static int minMatchesPeriod(String type) {
		return MIN_MATCHES_PERIOD.getOrDefault(type, DEFAULT_MIN_MATCHES_PERIOD);
	}

	private void processMatch(MatchForElo match, String forType) {
		processMatch(match, false, false, forType);
	}

	private void processMatch(MatchForElo match, boolean forSurface, boolean forIndoor) {
		processMatch(match, forSurface, forIndoor, null);
	}

	private void processMatch(MatchForElo match, boolean forSurface, boolean forIndoor, String forType) {
		if (forSurface && match.surface == null)
			return;
		int winnerId = match.winnerId;
		int loserId = match.loserId;
		int playerId1 = min(winnerId, loserId);
		int playerId2 = max(winnerId, loserId);
		String type;
		if (forSurface)
			type = match.surface;
		else if (forIndoor)
			type = match.indoor ? "I" : "O";
		else
			type = forType;
		String loserType = loserType(type);
		LocalDate date = match.endDate;
		lockManager.runLocked(playerId1, playerId2, () -> {
			EloRating winnerRating = getRating(type, winnerId, date);
			EloRating loserRating = getRating(loserType, loserId, date);
			EloRating winnerNextRating = winnerRating;
			EloRating loserNextRating = loserRating;
			if (!Objects.equals(match.outcome, "ABD")) {
				getPredictionResult(type).newMatch(type, match, winnerRating.rating, loserRating.rating);
				double delta = deltaRating(eloSurfaceFactors, winnerRating.rating, loserRating.rating, match, type, forSurface || forIndoor);
				winnerNextRating = winnerRating.newRating(delta, date);
				getRatings(type).put(winnerRating.playerId, winnerNextRating);
				loserNextRating = loserRating.newRating(-delta, date);
				getRatings(loserType).put(loserRating.playerId, loserNextRating);
			}
			if (saveExecutor != null && type.equals("E") && (saveFromDate == null || !match.endDate.isBefore(saveFromDate))) {
				MatchEloRating matchEloRating = new MatchEloRating(match.matchId, winnerRating.rating, winnerNextRating.rating, loserRating.rating, loserNextRating.rating);
				try {
					matchRatingsForSave.put(matchEloRating);
				}
				catch (InterruptedException ignored) {}
			}
		});
	}

	private Map<Integer, EloRating> getRatings(String type) {
		return playerRatings.get(type);
	}

	private EloRating getRating(String type, int playerId, LocalDate date) {
		EloRating rating = getRatings(type).get(playerId);
		return rating != null ? rating.adjustRating(date) : new EloRating(playerId, type, getPlayerRank(playerId, date));
	}

	private Integer getPlayerRank(int playerId, LocalDate date) {
		Map.Entry<LocalDate, Map<Integer, Integer>> prevRankingTable = null;
		for (Map.Entry<LocalDate, Map<Integer, Integer>> rankingTable : rankingTables.entrySet()) {
			if (date.isBefore(rankingTable.getKey()))
				break;
			else
				prevRankingTable = rankingTable;
		}
		return prevRankingTable != null && !date.isAfter(prevRankingTable.getKey().plusYears(1)) ? prevRankingTable.getValue().get(playerId) : null;
	}

	private static String loserType(String type) {
		switch (type) {
			case "sg": return "rg";
			case "rg": return "sg";
			default: return type;
		}
	}

	private PredictionResult getPredictionResult(String type) {
		return predictionResults.get(type);
	}

	private static void printPredictionResult(String type, PredictionResult predictionResult) {
		predictionResult.complete();
		System.out.printf("%1$s: %2$s\n", type, predictionResult);
	}

	public static final class EloRating implements Comparable<EloRating> {

		private final int playerId;
		private final String type;
		private final double rating;
		private final int matches;
		private final RingBuffer<LocalDate> dates;

		private EloRating(int playerId, String type, Integer rank) {
			this(playerId, type, ratingForType(startRating(rank), type), 0, newDates());
		}

		private EloRating(int playerId, String type, double rating, int matches, RingBuffer<LocalDate> dates) {
			this.playerId = playerId;
			this.type = type;
			this.rating = rating;
			this.matches = matches;
			this.dates = dates;
		}

		private static RingBuffer<LocalDate> newDates() {
			return new RingBuffer<>(MIN_MATCHES_IN_PERIOD);
		}

		public int getPlayerId() {
			return playerId;
		}

		public String getType() {
			return type;
		}

		public double getRating() {
			return rating;
		}

		public int getMatches() {
			return matches;
		}

		private LocalDate getLastDate() {
			return dates.peekLast();
		}

		private LocalDate getFirstDate() {
			return dates.peekFirst();
		}

		private long getDaysSpan(LocalDate date) {
			return ChronoUnit.DAYS.between(getFirstDate(), date);
		}

		private int getDaysSinceLastMatch(LocalDate date) {
			return (int)ChronoUnit.DAYS.between(getLastDate(), date);
		}

		private EloRating newRating(double delta, LocalDate date) {
			double newRating = EloCalculator.newRating(rating, delta, type);
			RingBuffer<LocalDate> newDates = dates.copy();
			newDates.push(date);
			return new EloRating(playerId, type, newRating, matches + 1, newDates);
		}

		private EloRating adjustRating(LocalDate date) {
			if (!dates.isEmpty()) {
				int daysSinceLastMatch = getDaysSinceLastMatch(date);
				if (daysSinceLastMatch > INACTIVITY_ADJ_NO_PENALTY_PERIOD) {
					if (daysSinceLastMatch <= INACTIVITY_ADJ_PERIOD * 4) {
						double newRating = EloCalculator.adjustRating(rating, daysSinceLastMatch, type);
						return new EloRating(playerId, type, newRating, matches, dates.copy());
					}
					else
						return new EloRating(playerId, type, START_RATING, 0, newDates());
				}
			}
			return this;
		}

		@Override public String toString() {
			return valueOf(round(rating));
		}

		@Override public int compareTo(EloRating eloRating) {
			return Double.compare(rating, eloRating.rating);
		}
	}


	private LocalDate fetchLastDate() {
		return jdbcTemplate.queryForObject(LAST_DATE_QUERY, LocalDate.class);
	}

	private List<LocalDate> fetchRankingDates() {
		System.out.print("Fetching all Elo ranking dates");
		Stopwatch stopwatch = Stopwatch.createStarted();
		List<LocalDate> dates = jdbcTemplate.queryForList(RANKING_DATES_QUERY, LocalDate.class);
		System.out.println(" " + stopwatch);
		return dates;
	}

	private void deleteForDate(LocalDate date) {
		if (dates != null) {
			jdbcTemplate.update(DELETE_RANKING_FOR_DATE, ps -> ps.setObject(1, date, Types.DATE));
			dates.remove(date);
		}
	}

	private void loadRanks() {
		System.out.print("Preloading ranks");
		Stopwatch stopwatch = Stopwatch.createStarted();
		rankingTables = new LinkedHashMap<>();
		ProgressTicker ticker = new ProgressTicker('.', RANK_PRELOADS_PER_DOT).withDownstreamTicker(ProgressTicker.newLineTicker());
		jdbcTemplate.query(PLAYER_RANKS_QUERY, ps -> {
			ps.setInt(1, START_RATING_RANK);
			ps.setFetchSize(RANK_PRELOAD_FETCH_SIZE);
		}, rs -> {
			LocalDate date = getLocalDate(rs, "rank_date");
			int playerId = rs.getInt("player_id");
			int rank = rs.getInt("rank");
			rankingTables.computeIfAbsent(date, d -> new HashMap<>()).put(playerId, rank);
			ticker.tick();
		});
		System.out.println(" " + stopwatch);
	}

	private static MatchForElo mapMatch(ResultSet rs) throws SQLException {
		return new MatchForElo(
			rs.getInt("match_id"),
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			getLocalDate(rs, "end_date"),
			rs.getString("level"),
			rs.getShort("best_of"),
			rs.getString("surface"),
			rs.getBoolean("indoor"),
			rs.getString("round"),
			rs.getString("outcome"),
			rs.getInt("w_sets"),
			rs.getInt("l_sets"),
			rs.getInt("w_games"),
			rs.getInt("l_games"),
			rs.getInt("w_sv_gms"),
			rs.getInt("l_sv_gms"),
			rs.getInt("w_rt_gms"),
			rs.getInt("l_rt_gms"),
			rs.getInt("w_tbs"),
			rs.getInt("l_tbs"),
			rs.getBoolean("has_stats")
		);
	}

	private void saveCurrentRatings(LocalDate date) {
		if (saveExecutor != null && (saveFromDate == null || !date.isBefore(saveFromDate))) {
			Map<Integer, EloRatingValue> playerRatingsForSave = new LinkedHashMap<>();
			for (String type : RATING_TYPES) {
				AtomicInteger rank = new AtomicInteger();
				getRatings(type, type.equals("E") ? Integer.MAX_VALUE : PLAYERS_TO_SAVE, date).stream()
					.map(e -> new EloRatingValue(e, rank.incrementAndGet())).forEach(e -> playerRatingsForSave.compute(e.playerId, (p, e2) -> {
						if (e2 != null) {
							e2.merge(e);
							return e2;
						}
						else
							return e;
					}));
			}
			List<EloRatingValue> ratingsForSave = playerRatingsForSave.values().stream().filter(EloRatingValue::isForSave).collect(toList());
			saveExecutor.execute(() -> txTemplate.execute(s -> {
				saveRatings(ratingsForSave, date);
				return null;
			}));
		}
	}

	private void saveRatings(List<EloRatingValue> eloRatings, LocalDate date) {
		deleteForDate(date);
		if (eloRatings.isEmpty())
			return;
		jdbcTemplate.batchUpdate(MERGE_ELO_RANKING, new BatchPreparedStatementSetter() {
			@Override public void setValues(PreparedStatement ps, int i) throws SQLException {
				EloRatingValue eloRating = eloRatings.get(i);
				Map<String, Integer> ranks = eloRating.ranks;
				Map<String, Double> ratings = eloRating.ratings;
				ps.setObject(1, date, Types.DATE);
				ps.setInt(2, eloRating.playerId);
				ps.setInt(3, ranks.get("E"));
				ps.setInt(4, intRound(ratings.get("E")));
				setInteger(ps, 5, ranks.get("R"));
				setInteger(ps, 6, intRound(ratings.get("R")));
				setInteger(ps, 7, ranks.get("H"));
				setInteger(ps, 8, intRound(ratings.get("H")));
				setInteger(ps, 9, ranks.get("C"));
				setInteger(ps, 10, intRound(ratings.get("C")));
				setInteger(ps, 11, ranks.get("G"));
				setInteger(ps, 12, intRound(ratings.get("G")));
				boolean isCarpetUsed = date.isBefore(CARPET_END);
				setInteger(ps, 13, isCarpetUsed ? ranks.get("P") : null);
				setInteger(ps, 14, isCarpetUsed ? intRound(ratings.get("P")) : null);
				setInteger(ps, 15, ranks.get("O"));
				setInteger(ps, 16, intRound(ratings.get("O")));
				setInteger(ps, 17, ranks.get("I"));
				setInteger(ps, 18, intRound(ratings.get("I")));
				setInteger(ps, 19, ranks.get("s"));
				setInteger(ps, 20, intRound(ratings.get("s")));
				setInteger(ps, 21, ranks.get("g"));
				setInteger(ps, 22, intRound(ratings.get("g")));
				boolean areStatsUsed = !date.isBefore(STATS_START);
				setInteger(ps, 23, areStatsUsed ? ranks.get("sg") : null);
				setInteger(ps, 24, areStatsUsed ? intRound(ratings.get("sg")) : null);
				setInteger(ps, 25, areStatsUsed ? ranks.get("rg") : null);
				setInteger(ps, 26, areStatsUsed ? intRound(ratings.get("rg")) : null);
				boolean isTieBreakUsed = !date.isBefore(TIE_BREAK_START);
				setInteger(ps, 27, isTieBreakUsed ? ranks.get("tb") : null);
				setInteger(ps, 28, isTieBreakUsed ? intRound(ratings.get("tb")) : null);
			}
			@Override public int getBatchSize() {
				return eloRatings.size();
			}
		});
		List<MatchEloRating> matchRatingsBatch = new ArrayList<>(matchRatingsForSave.size());
		matchRatingsForSave.drainTo(matchRatingsBatch);
		if (!matchRatingsBatch.isEmpty()) {
			jdbcTemplate.batchUpdate(UPDATE_MATCH_ELO_RATINGS, new BatchPreparedStatementSetter() {
				@Override public void setValues(PreparedStatement ps, int i) throws SQLException {
					MatchEloRating m = matchRatingsBatch.get(i);
					setInteger(ps, 1, m.winnerRating);
					setInteger(ps, 2, m.winnerNextRating);
					setInteger(ps, 3, m.loserRating);
					setInteger(ps, 4, m.loserNextRating);
					ps.setLong(5, m.matchId);

				}
				@Override public int getBatchSize() {
					return matchRatingsBatch.size();
				}
			});
		}
		saveTicker.tick();
	}

	private static Integer intRound(Double d) {
		return d != null ? (int)round(d) : null;
	}

	private static final class EloRatingValue {

		private final int playerId;
		private final Map<String, Integer> ranks = new HashMap<>();
		private final Map<String, Double> ratings = new HashMap<>();

		EloRatingValue(EloRating eloRating, int rank) {
			playerId = eloRating.playerId;
			ranks.put(eloRating.type, rank);
			ratings.put(eloRating.type, eloRating.rating);
		}

		private void merge(EloRatingValue eloRating) {
			ranks.putAll(eloRating.ranks);
			ratings.putAll(eloRating.ratings);
		}

		private boolean isForSave() {
			if (!ranks.containsKey("E"))
				return false;
			for (Integer rank : ranks.values()) {
				if (rank <= PLAYERS_TO_SAVE)
					return true;
			}
			return false;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			EloRatingValue value = (EloRatingValue)o;
			return playerId == value.playerId;
		}

		@Override public int hashCode() {
			return Objects.hash(playerId);
		}
	}

	private static final class MatchEloRating {

		private final long matchId;
		private final int winnerRating;
		private final int winnerNextRating;
		private final int loserRating;
		private final int loserNextRating;

		private MatchEloRating(long matchId, double winnerRating, double winnerNextRating, double loserRating, double loserNextRating) {
			this.matchId = matchId;
			this.winnerRating = intRound(winnerRating);
			this.winnerNextRating = intRound(winnerNextRating);
			this.loserRating = intRound(loserRating);
			this.loserNextRating = intRound(loserNextRating);
		}
	}

	public static final class PredictionResult {

		private int total;
		private int predicted;
		private double p;
		private double pDelta2;
		private double pLog;

		private double predictionRate;
		private double brier;
		private double logLoss;
		private double score;
		private double calibration;

		public int getTotal() {
			return total;
		}

		public int getPredicted() {
			return predicted;
		}

		public double getPredictionRate() {
			return predictionRate;
		}

		public double getBrier() {
			return brier;
		}

		public double getLogLoss() {
			return logLoss;
		}

		public double getScore() {
			return score;
		}

		public double getCalibration() {
			return calibration;
		}

		private void newMatch(String type, MatchForElo match, double winnerRating, double loserRating) {
			if (winnerRating == loserRating || match.outcome != null || match.endDate.compareTo(PREDICTION_START_DATE) < 0)
				return;

			double winnerScore, loserScore;
			switch (type) {
				case "s":
					winnerScore = match.wSets;
					loserScore = match.lSets;
					break;
				case "g":
					winnerScore = match.wGames;
					loserScore = match.lGames;
					break;
				case "sg":
					winnerScore = match.wSvGms * returnToServeRatio(match.surface);
					loserScore = match.lRtGms;
					break;
				case "rg":
					winnerScore = match.wRtGms;
					loserScore = match.lSvGms * returnToServeRatio(match.surface);
					break;
				case "tb":
					winnerScore = match.wTbs;
					loserScore = match.lTbs;
					break;
				default:
					winnerScore = 1.0;
					loserScore = 0.0;
					break;
			}

			if (winnerScore == loserScore)
				return;
			double totalScore = winnerScore + loserScore;
			if (totalScore > 0.0) {
				double winnerProbability = 1.0 / (1.0 + pow(10.0, (loserRating - winnerRating) / 400.0));
				double loserProbability = 1.0 - winnerProbability;
				double probability;
				if (winnerScore > loserScore) {
					++total;
					if (winnerProbability > 0.5) {
						++predicted;
						probability = winnerProbability;
					}
					else
						probability = loserProbability;
				}
				else {
					++total;
					if (winnerProbability < 0.5) {
						++predicted;
						probability = loserProbability;
					}
					else
						probability = winnerProbability;
				}
				p += probability;
				pLog += log(winnerProbability);
				double pDelta = winnerScore / totalScore - winnerProbability;
				pDelta2 += pDelta * pDelta;
			}
		}

		private PredictionResult add(PredictionResult result) {
			total += result.total;
			predicted += result.predicted;
			p += result.p;
			pDelta2 += result.pDelta2;
			pLog += result.pLog;
			return this;
		}

		private void complete() {
			predictionRate = pct(predicted, total);
			brier = pDelta2 / total;
			logLoss = -pLog / total;
			score = predicted / (total * logLoss); // = Prediction Rate / Log-Loss
			calibration = p / predicted;
		}

		@Override public String toString() {
			return format("Rate=%1$.3f%%, Brier=%2$.5f, LogLoss=%3$.5f, Score=%4$.5f, Calibration=%5$.5f, Matches=%6$d", predictionRate, brier, logLoss, score, calibration, total);
		}
	}
}
