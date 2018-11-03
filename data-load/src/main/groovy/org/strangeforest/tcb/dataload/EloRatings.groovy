package org.strangeforest.tcb.dataload

import java.time.*
import java.time.temporal.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

import org.strangeforest.tcb.util.*

import com.google.common.base.*
import groovy.sql.*
import groovy.transform.*

import static java.lang.Math.*
import static java.lang.String.*
import static org.strangeforest.tcb.dataload.StartEloRatings.*
import static org.strangeforest.tcb.stats.util.PercentageUtil.*
import static org.strangeforest.tcb.util.DateUtil.*

class EloRatings {

	final SqlPool sqlPool
	final LockManager<Integer> lockManager
	final LockManager<RankKey> rankLockManager
	final Map<Integer, CompletableFuture> playerMatchFutures
	EloSurfaceFactors eloSurfaceFactors
	Map<Integer, EloRating> playerRatings
	Map<String, Map<Integer, EloRating>> playerRatingsByType
	Set<LocalDate> dates
	PredictionResult predictionResult
	Map<String, PredictionResult> predictionResultByType
	BlockingQueue<MatchEloRating> matchRatings
	Map<RankKey, Integer> rankCache
	Map<Date, Map<Integer, Integer>> rankDateCache
	volatile Date lastDate
	AtomicInteger saves, rankFetches
	AtomicInteger progress
	ExecutorService rankExecutor, saveExecutor
	Date saveFromDate

	static final String QUERY_MATCHES = //language=SQL
		"SELECT m.match_id, m.winner_id, m.loser_id, tournament_end(CASE WHEN e.level = 'D' THEN m.date ELSE e.date END, e.level, e.draw_size) AS end_date, e.level, m.surface, m.indoor, m.round, m.best_of, m.outcome,\n" +
		"  m.w_sets, m.l_sets, m.w_games, m.l_games, s.w_sv_gms - (s.w_bp_fc - s.w_bp_sv) AS w_sv_gms, s.l_sv_gms - (s.l_bp_fc - s.l_bp_sv) AS l_sv_gms, s.l_bp_fc - s.l_bp_sv AS w_rt_gms, s.w_bp_fc - s.w_bp_sv AS l_rt_gms, m.w_tbs, m.l_tbs, m.has_stats\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN match_stats s ON s.match_id = m.match_id AND s.set = 0\n" +
		"WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B', 'D', 'T')\n" +
		"ORDER BY end_date, m.round, m.winner_id, m.loser_id, m.match_num"

	static final String QUERY_LAST_DATE = //language=SQL
		"SELECT max(rank_date) AS last_date FROM player_elo_ranking"

	static final String QUERY_PLAYER_RANK = //language=SQL
		"SELECT player_rank(?, ?) AS rank"

	static final String QUERY_ALL_RANKS = //language=SQL
		"SELECT player_id, rank_date, rank\n" +
		"FROM player_ranking\n" +
		"WHERE rank <= :maxRank\n" +
		"ORDER BY rank_date, player_id"

	static final String MERGE_ELO_RANKING = //language=SQL
		"{call merge_elo_ranking(" +
		"  :rank_date, :player_id, :rank, :elo_rating, :recent_rank, :recent_elo_rating, " +
		"  :hard_rank, :hard_elo_rating, :clay_rank, :clay_elo_rating, :grass_rank, :grass_elo_rating, :carpet_rank, :carpet_elo_rating, :outdoor_rank, :outdoor_elo_rating, :indoor_rank, :indoor_elo_rating, " +
		"  :set_rank, :set_elo_rating, :game_rank, :game_elo_rating, :service_game_rank, :service_game_elo_rating, :return_game_rank, :return_game_elo_rating, :tie_break_rank, :tie_break_elo_rating" +
		")}"

	static final String QUERY_RANKING_DATES = //language=SQL
		"SELECT DISTINCT rank_date FROM player_elo_ranking"

	static final String DELETE_FOR_DATE = //language=SQL
		"DELETE FROM player_elo_ranking WHERE rank_date = :rank_date"

	static final String UPDATE_MATCH_ELO_RATINGS = //language=SQL
		"UPDATE match SET winner_elo_rating = :winner_elo_rating, winner_next_elo_rating = :winner_next_elo_rating, loser_elo_rating = :loser_elo_rating, loser_next_elo_rating = :loser_next_elo_rating\n" +
		"WHERE match_id = :match_id"

	
	// Factors and tennis constants
	static final List<String> RATING_TYPES = ['r', 'H', 'C', 'G', 'P', 'O', 'I', 's', 'g', 'sg', 'rg', 'tb']
	static final Date CARPET_END = toDate(LocalDate.of(2008, 1, 1))
	static final Date STATS_START = toDate(LocalDate.of(1991, 1, 1))
	static final Date TIE_BREAK_START = toDate(LocalDate.of(1970, 1, 1))
	static final int DEFAULT_MIN_MATCHES = 10
	static final Map<String, Integer> MIN_MATCHES = [r: 5, H: 5, C: 5, G: 5, P: 5, O: 5, I: 5, s: 5, g: 2, sg: 3, rg: 3, tb: 10]
	static final int DEFAULT_MIN_MATCHES_PERIOD = 365
	static final Map<String, Integer> MIN_MATCHES_PERIOD = [r: 90]
	static final int MIN_MATCHES_IN_PERIOD = 3
	static final int DEFAULT_INACTIVITY_ADJ_PERIOD = 365
	static final Map<String, Integer> INACTIVITY_ADJ_PERIOD = [r: 90]
	static final List<String> BEST_OF_INDEPENDENT = ['s', 'g', 'sg', 'rg', 'tb']
	static final double RECENT_K_FACTOR = 2.0d
	static final double SET_K_FACTOR = 0.5d
	static final double GAME_K_FACTOR = 0.0556d
	static final double SERVICE_GAME_K_FACTOR = 0.1667d
	static final double RETURN_GAME_K_FACTOR = 0.1667d
	static final double TIE_BREAK_K_FACTOR = 1.5d

	// Player counts
	static final int PLAYERS_TO_SAVE = 200

	// Technical
	static final int MATCHES_FETCH_SIZE = 200
	static final int RANK_PRELOAD_FETCH_SIZE = 1000
	static final Double SAVE_RANK_THREAD_RATIO = 1.0d

	// Progress tracking
	static final int MATCHES_PER_DOT = 1000
	static final int SAVES_PER_PLUS = 20
	static final int RANK_FETCHES_PER_QUESTION_MARK = 200
	static final int RANK_PRELOADS_PER_QUESTION_MARK = 20000
	static final int PROGRESS_LINE_WRAP = 100

	// Functions
	static final comparator = { a, b -> b <=> a }
	static final bestComparator = { a, b -> b.bestRating <=> a.bestRating }
	static final nullFuture = CompletableFuture.completedFuture(null)

	EloRatings(SqlPool sqlPool) {
		this.sqlPool = sqlPool
		lockManager = new LockManager<>()
		rankLockManager = new LockManager<>()
		playerMatchFutures = new HashMap<>()
		sqlPool.withSql { sql -> eloSurfaceFactors = new EloSurfaceFactors(sql) }
	}

	def compute(boolean save = false, boolean fullSave = true, Date saveFromDate = null, boolean preLoadRanks = true) {
		def stopwatch = Stopwatch.createStarted()
		int matches = 0
		playerRatings = new ConcurrentHashMap<>()
		playerRatingsByType = [:]
		dates = []
		predictionResult = new PredictionResult()
		predictionResultByType = [:]
		RATING_TYPES.each {
			playerRatingsByType[it] = new ConcurrentHashMap<>()
			predictionResultByType[it] = new PredictionResult()
		}
		matchRatings = new LinkedBlockingDeque<>()
		lastDate = null
		saves = new AtomicInteger()
		rankFetches = new AtomicInteger()
		progress = new AtomicInteger()
		if (preLoadRanks)
			loadRanks()
		else
			rankCache = [:]
		def remainingPoolSize = sqlPool.size() - 1
		def useRankExecutor = SAVE_RANK_THREAD_RATIO != null && !preLoadRanks
		int saveThreads = save ? (useRankExecutor ? remainingPoolSize * SAVE_RANK_THREAD_RATIO / (1 + SAVE_RANK_THREAD_RATIO) : remainingPoolSize) : 0
		int rankThreads = useRankExecutor ? remainingPoolSize - saveThreads : 0
		if (rankThreads) {
			println "Using $rankThreads rank threads"
			rankExecutor = Executors.newFixedThreadPool(rankThreads)
		}
		if (save) {
			if (fullSave)
				fetchRankingDates()
			else
				this.saveFromDate = saveFromDate ? saveFromDate : lastDate()
			saveExecutor = Executors.newFixedThreadPool(saveThreads)
			println "Processing matches using $saveThreads saving threads"
		}
		else
			println 'Processing matches'

		sqlPool.withSql { sql ->
			try {
				sql.withStatement { st -> st.fetchSize = MATCHES_FETCH_SIZE }
				sql.eachRow(QUERY_MATCHES) { match ->
					def date = match.end_date
					if (lastDate && date != lastDate) {
						waitForAllMatchesToComplete()
						saveCurrentRatings()
					}
					processMatch(match, false, false)
					processMatch(match, false, false, 'r')
					processMatch(match, true, false)
					processMatch(match, false, true)
					processMatch(match, false, false, 's')
					processMatch(match, false, false, 'g')
					if (match.has_stats) {
						processMatch(match, false, false, 'sg')
						processMatch(match, false, false, 'rg')
					}
					processMatch(match, false, false, 'tb')
					lastDate = date
					if (++matches % MATCHES_PER_DOT == 0)
						progressTick '.'
				}
				waitForAllMatchesToComplete()
				saveCurrentRatings()
			}
			finally {
				rankExecutor?.shutdownNow()
				saveExecutor?.shutdown()
				saveExecutor?.awaitTermination(1L, TimeUnit.DAYS)
			}
		}
		if (fullSave && dates) {
			println "\nDeleting remaining dates: ${dates.size()}"
			for (LocalDate date : new ArrayList<>(dates)) {
				deleteForDate(toDate(date))
				println date
			}
		}
		println "\nElo Ratings computed in $stopwatch"
		println "Rank fetches: $rankFetches"
		if (save)
			println "Saves: $saves"
		predictionResult.complete()
		RATING_TYPES.each { predictionResultByType[it].complete() }
		println predictionResult
		println predictionResultByType
		playerRatings
	}

	def waitForAllMatchesToComplete() {
		CompletableFuture.allOf(playerMatchFutures.values().toArray(new CompletableFuture[playerMatchFutures.size()])).join()
		playerMatchFutures.clear()
	}

	def current(int count, Date date = new Date(), String type = null) {
		Date minDate = toDate(toLocalDate(date).minusYears(1))
		def i = 0
		getRatings(type).values().findAll { it.rating >= START_RATING && it.matches >= minMatches() && it.lastDate >= minDate && it.getDaysSpan(date) <= minMatchesPeriod(type) }
			.sort(comparator)
			.findAll { ++i <= count }
	}

	static def minMatches(String type) {
		type ? (MIN_MATCHES[type] ?: DEFAULT_MIN_MATCHES) : DEFAULT_MIN_MATCHES
	}

	static def minMatchesPeriod(String type) {
		type ? (MIN_MATCHES_PERIOD[type] ?: DEFAULT_MIN_MATCHES_PERIOD) : DEFAULT_MIN_MATCHES_PERIOD
	}

	static def inactivityAdjustmentPeriod(String type) {
		type ? (INACTIVITY_ADJ_PERIOD[type] ?: DEFAULT_INACTIVITY_ADJ_PERIOD) : DEFAULT_INACTIVITY_ADJ_PERIOD
	}

	def peak(int count) {
		def i = 0
		playerRatings.values().findAll {	it.bestRating }
			.sort(bestComparator)
			.findAll { ++i <= count }
	}

	def processMatch(match, boolean forSurface, boolean forIndoor, String forType = null) {
		if (forSurface && !match.surface)
			return
		int winnerId = match.winner_id
		int loserId = match.loser_id
		def playerId1 = min(winnerId, loserId)
		def playerId2 = max(winnerId, loserId)
		String type = null
		if (forSurface)
			type = match.surface
		else if (forIndoor)
			type = match.indoor ? 'I' : 'O'
		else if (forType)
			type = forType
		String loserType = loserType(type)
		Date date = match.end_date
		lockManager.withLock(playerId1, playerId2) {
			def winnerRating = getRating(type, winnerId, date)
			def loserRating = getRating(loserType, loserId, date)
			boolean schedule = false
			if (!(winnerRating && loserRating)) {
				if (rankExecutor)
					schedule = true
				else {
					winnerRating = winnerRating ?: newEloRating(winnerId, type)
					loserRating = loserRating ?: newEloRating(loserId, loserType)
				}
			}
			if (schedule) {
				def future = CompletableFuture.allOf(playerMatchFutures.get(playerId1) ?: nullFuture, playerMatchFutures.get(playerId2) ?: nullFuture).thenRunAsync({
					lockManager.withLock(playerId1, playerId2) {
						winnerRating = getRating(type, winnerId, date) ?: newEloRating(winnerId, type)
						loserRating = getRating(loserType, loserId, date) ?: newEloRating(loserId, loserType)
						calculateAndPutNewRatings(winnerRating, loserRating, match, type, loserType, date, forSurface, forIndoor, forType)
					}
				}, rankExecutor)
				playerMatchFutures.put(playerId1, future)
				playerMatchFutures.put(playerId2, future)
			}
			else
				calculateAndPutNewRatings(winnerRating, loserRating, match, type, loserType, date, forSurface, forIndoor, forType)
		}
	}

	private EloRating newEloRating(int playerId, String type) {
		new EloRating(playerId, playerRank(playerId, lastDate), type)
	}

	private getRatings(String type) {
		type ? playerRatingsByType[type] : playerRatings
	}

	private getRating(String type, int playerId, Date date) {
		def rating = getRatings(type).get(playerId)
		if (rating) // TODO Potential side effect
			rating.adjustRating(date, type)
		rating
	}

	private static String loserType(String type) {
		switch (type) {
			case 'sg': return 'rg'
			case 'rg': return 'sg'
			default: return type
		}
	}

	private predictionResult(String type) {
		type ? predictionResultByType[type] : predictionResult
	}

	private calculateAndPutNewRatings(EloRating winnerRating, EloRating loserRating, match, String type, String loserType, Date date, boolean forSurface, boolean forIndoor, String forType) {
		long matchId = match.match_id
		String level = match.level
		String round = match.round
		short bestOf = BEST_OF_INDEPENDENT.contains(type) ? (short)5 : match.best_of
		String outcome = match.outcome
		def delta = deltaRating(winnerRating.rating, loserRating.rating, level, round, bestOf, outcome)
		if (forSurface || forIndoor)
			delta *= eloSurfaceFactors.surfaceKFactor(type, date)
		else if (forType) {
			def wDelta = delta
			def lDelta = deltaRating(loserRating.rating, winnerRating.rating, level, round, bestOf, outcome)
			switch (type) {
				case 'r':
					delta = RECENT_K_FACTOR * wDelta
					break
				case 's':
					delta = SET_K_FACTOR * (wDelta * (match.w_sets ?: 0d) - lDelta * (match.l_sets ?: 0d))
					break
				case 'g':
					delta = GAME_K_FACTOR * (wDelta * (match.w_games ?: 0d) - lDelta * (match.l_games ?: 0d))
					break
				case 'sg':
					delta = SERVICE_GAME_K_FACTOR * (wDelta * (match.w_sv_gms ?: 0d) * returnToServeRatio(match.surface) - lDelta * (match.l_rt_gms ?: 0d))
					break
				case 'rg':
					delta = RETURN_GAME_K_FACTOR * (wDelta * (match.w_rt_gms ?: 0d) - lDelta * (match.l_sv_gms ?: 0d) * returnToServeRatio(match.surface))
					break
				case 'tb':
					def wTBs = match.w_tbs ?: 0d
					def lTBs = match.l_tbs ?: 0d
					if (lTBs > wTBs) {
						def t = wDelta; wDelta = lDelta; lDelta = t
					}
					delta = TIE_BREAK_K_FACTOR * (wDelta * wTBs - lDelta * lTBs)
					break
			}
		}

		def winnerNextRating = winnerRating
		def loserNextRating = loserRating
		if (outcome != 'ABD') {
			predictionResult(type).newMatch(type, match, winnerRating.rating, loserRating.rating)
			winnerNextRating = winnerRating.newRating(delta, date, type)
			getRatings(type).put(winnerRating.playerId, winnerNextRating)
			loserNextRating = loserRating.newRating(-delta, date, loserType)
			getRatings(loserType).put(loserRating.playerId, loserNextRating)
		}
		if (saveExecutor && !type && (!saveFromDate || lastDate >= saveFromDate))
			matchRatings.put new MatchEloRating(matchId: matchId, winnerRating: winnerRating.rating, winnerNextRating: winnerNextRating.rating, loserRating: loserRating.rating, loserNextRating: loserNextRating.rating)
	}

	static double deltaRating(double winnerRating, double loserRating, String level, String round, short bestOf, String outcome) {
		if (outcome == 'ABD')
			return 0d
		double delta = 1d / (1d + pow(10d, (winnerRating - loserRating) / 400d))
		kFactor(level, round, bestOf, outcome) * delta
	}

	static double kFactor(String level, String round, short bestOf, String outcome) {
		double kFactor = 32d
		switch (level) {
			case 'G': break
			case 'F': kFactor *= 0.90d; break
			case 'L': kFactor *= 0.85d; break
			case 'M': kFactor *= 0.85d; break
			case 'O': kFactor *= 0.80d; break
			case 'A': kFactor *= 0.75d; break
			default: kFactor *= 0.70d; break
		}
		switch (round) {
			case 'F': break
			case 'BR': kFactor *= 0.95d; break
			case 'SF': kFactor *= 0.90d; break
			case 'QF': kFactor *= 0.85d; break
			case 'R16': kFactor *= 0.80d; break
			case 'R32': kFactor *= 0.80d; break
			case 'R64': kFactor *= 0.75d; break
			case 'R128': kFactor *= 0.75d; break
			case 'RR': kFactor *= 0.85d; break
		}
		if (bestOf < 5) kFactor *= 0.90d
		if (outcome == 'W/O') kFactor *= 0.50d
		kFactor
	}

	/**
	 * K-Function returns values from 1 to 10 depending on current rating.
	 * It stabilizes ratings at the top, while allows fast progress of lower rated players.
	 * For high ratings returns 1
	 * For low ratings return 10
	 * @return values from 1 to 10, depending on current rating
	 */
	static double kFunction(double rating, String type = null) {
		1d + 18d / (1d + pow(2d, (ratingFromType(rating, type) - START_RATING) / 63d))
	}

	static final Map<String, Double> RATING_TYPE_FACTOR = [
		'r': 1.1d,
		's': 0.8d,
		'g': 0.25d,
		'sg': 0.4d,
		'rg': 0.4d,
		'tb': 0.4d
	]

	static double ratingForType(double rating, String type) {
		Double factor = RATING_TYPE_FACTOR[type]
		factor ? START_RATING + (rating - START_RATING) * factor : rating
	}

	static double ratingFromType(double typeRating, String type) {
		Double factor = RATING_TYPE_FACTOR[type]
		factor ? START_RATING + (typeRating - START_RATING) / factor : typeRating
	}

	static double ratingDiffForType(double ratingDiff, String type) {
		Double factor = RATING_TYPE_FACTOR[type]
		factor ? ratingDiff * factor : ratingDiff
	}

	static double newRating(double rating, double delta, String type) {
		rating + capDeltaRating(delta * kFunction(rating, type), type)
	}

	static double capDeltaRating(double delta, String type) {
		signum(delta) * min(abs(delta), ratingDiffForType(200d, type))
	}

	static double returnToServeRatio(String surface) {
		switch (surface) {
			case 'H': return 0.281d
			case 'C': return 0.365d
			case 'G': return 0.227d
			case 'P': return 0.243d
			default: return 0.297d
		}
	}

	static class EloRating implements Comparable<EloRating> {

		volatile int playerId
		volatile double rating
		volatile int matches
		volatile Deque<Date> dates
		volatile EloRating bestRating

		EloRating() {}

		EloRating(int playerId, Integer rank, String type = null) {
			this.playerId = playerId
			rating = ratingForType(startRating(rank), type)
		}

		EloRating newRating(double delta, Date date, String type) {
			def newRating = new EloRating(playerId: playerId, rating: newRating(rating, delta, type), matches: matches + 1, dates: new ArrayDeque<>(dates ?: []))
			newRating.bestRating = bestRating(newRating, type)
			newRating.addDate(date)
			newRating
		}

		Date getLastDate() {
			dates.peekLast()
		}

		Date getFirstDate() {
			dates.peekFirst()
		}

		private addDate(Date date) {
			dates.addLast(date)
			while (dates.size() > MIN_MATCHES_IN_PERIOD)
				dates.removeFirst()
		}

		long getDaysSpan(Date date) {
			ChronoUnit.DAYS.between(toLocalDate(firstDate), toLocalDate(date))
		}

		def adjustRating(Date date, String type) {
			def lastDate = this.lastDate
			if (lastDate) {
				def daysSinceLastMatch = ChronoUnit.DAYS.between(toLocalDate(lastDate), toLocalDate(date))
				def adjustmentPeriod = inactivityAdjustmentPeriod(type)
				if (daysSinceLastMatch > adjustmentPeriod) {
					rating = ratingAdjusted(daysSinceLastMatch, adjustmentPeriod, type)
					if (daysSinceLastMatch > adjustmentPeriod * 5)
						matches = 0
				}
			}
		}

		private ratingAdjusted(long daysSinceLastMatch, int adjustmentPeriod, String type) {
			max(START_RATING, rating - (daysSinceLastMatch - adjustmentPeriod) * ratingDiffForType(200d, type) / adjustmentPeriod)
		}

		def bestRating(EloRating newRating, String type) {
			if (matches >= minMatches(type))
				bestRating && bestRating >= newRating ? bestRating : newRating
			else
				null
		}

		String toString() {
			round rating
		}

		int compareTo(EloRating eloRating) {
			rating <=> eloRating.rating
		}
	}


	private Date lastDate() {
		sqlPool.withSql { sql ->
			sql.firstRow(QUERY_LAST_DATE).last_date
		}
	}

	private fetchRankingDates() {
		print 'Fetching all Elo ranking dates'
		def stopwatch = Stopwatch.createStarted()
		sqlPool.withSql { sql ->
			sql.eachRow(QUERY_RANKING_DATES) { date -> dates << date.rank_date.toLocalDate()	}
		}
		println " $stopwatch"
	}

	private deleteForDate(Date date) {
		def sqlDate = new java.sql.Date(date.time)
		sqlPool.withSql { sql ->
			sql.execute([rank_date: sqlDate], DELETE_FOR_DATE)
		}
		dates.remove(sqlDate.toLocalDate())
	}

	private Integer playerRank(int playerId, Date date) {
		if (rankDateCache) {
			Map.Entry<Date, Map<Integer, Integer>> prevCachedDateEntry = null
			for (Map.Entry<Date, Map<Integer, Integer>> cachedDateEntry : rankDateCache.entrySet()) {
				if (date < cachedDateEntry.key)
					break
				else
					prevCachedDateEntry = cachedDateEntry
			}
			prevCachedDateEntry && date <= toDate(toLocalDate(prevCachedDateEntry.key).plusYears(1)) ? prevCachedDateEntry.value[playerId] : null
		}
		else {
			def rankKey = new RankKey(playerId: playerId, date: date)
			rankLockManager.withLock(rankKey) {
				Integer rank = rankCache[rankKey]
				if (rank)
					return rank
				rank = sqlPool.withSql { Sql sql ->
					sql.firstRow(QUERY_PLAYER_RANK, [playerId, date]).rank
				}
				rankCache[rankKey] = rank
				if (rankFetches.incrementAndGet() % RANK_FETCHES_PER_QUESTION_MARK == 0)
					progressTick '?'
				rank
			}
		}
	}

	private loadRanks() {
		print 'Preloading ranks'
		def stopwatch = Stopwatch.createStarted()
		rankDateCache = new LinkedHashMap<>()
		def rankPreloads = 0
		sqlPool.withSql { sql ->
			sql.withStatement { st -> st.fetchSize = RANK_PRELOAD_FETCH_SIZE }
			sql.eachRow(QUERY_ALL_RANKS, [maxRank: START_RATING_RANK]) { rankRecord ->
				def date = rankRecord.rank_date
				def rankTable = rankDateCache[date]
				if (!rankTable) {
					rankTable = [:]
					rankDateCache[date] = rankTable
				}
				rankTable[rankRecord.player_id] = rankRecord.rank
				if (++rankPreloads % RANK_PRELOADS_PER_QUESTION_MARK == 0)
					progressTick '.'
			}
		}
		println " $stopwatch"
		progress = new AtomicInteger()
	}

	private saveCurrentRatings() {
		if (saveExecutor && playerRatings && (!saveFromDate || lastDate >= saveFromDate)) {
			def eloRatings = setRanks(current(Integer.MAX_VALUE, lastDate).collect { new EloRatingValue(it) })
			def playerRatings = new LinkedHashMap<Integer, EloRatingValue>(eloRatings.collectEntries({[(it.playerId): it]}))
			RATING_TYPES.each { type ->
				def typeEloRatings = current(PLAYERS_TO_SAVE, lastDate, type).collect { new EloRatingValue(it, type) }
				mergeRatings(playerRatings, setRanks(typeEloRatings, type))
			}
			def ratingsForSave = playerRatings.values().findAll { it.forSave }
			def date = lastDate
			saveExecutor.execute { saveRatings(ratingsForSave, date) }
		}
	}

	private static mergeRatings(Map<Integer, EloRatingValue> playerRatings, Collection<EloRatingValue> ratings) {
		for (EloRatingValue aRating : ratings) {
			def playerId = aRating.playerId
			def rating = playerRatings[playerId]
			if (rating)
				rating.merge(aRating)
			else
				playerRatings[playerId] = aRating
		}
	}

	private static setRanks(Collection<EloRatingValue> eloRatings, String type = null) {
		int rank = 0
		for (def eloRating : eloRatings)
			eloRating.ranks[type] = ++rank
		eloRatings
	}

	private saveRatings(Collection<EloRatingValue> eloRatings, Date date) {
		deleteForDate(date)
		if (eloRatings.empty)
			return
		sqlPool.withSql { sql ->
			sql.withBatch(MERGE_ELO_RANKING) { ps ->
				eloRatings.each { eloRating ->
					Map params = [:]
					params.rank_date = new java.sql.Date(date.time)
					params.player_id = eloRating.playerId
					def ranks = eloRating.ranks
					def ratings = eloRating.ratings
					params.rank = ranks[null]
					params.elo_rating = intRound ratings[null]
					params.recent_rank = ranks['r']
					params.recent_elo_rating = intRound ratings['r']
					params.hard_rank = ranks['H']
					params.hard_elo_rating = intRound ratings['H']
					params.clay_rank = ranks['C']
					params.clay_elo_rating = intRound ratings['C']
					params.grass_rank = ranks['G']
					params.grass_elo_rating = intRound ratings['G']
					boolean isCarpetUsed = date < CARPET_END
					params.carpet_rank = isCarpetUsed ? ranks['P'] : null
					params.carpet_elo_rating = isCarpetUsed ? intRound(ratings['P']) : null
					params.outdoor_rank = ranks['O']
					params.outdoor_elo_rating = intRound ratings['O']
					params.indoor_rank = ranks['I']
					params.indoor_elo_rating = intRound ratings['I']
					params.set_rank = ranks['s']
					params.set_elo_rating = intRound ratings['s']
					params.game_rank = ranks['g']
					params.game_elo_rating = intRound ratings['g']
					boolean areStatsUsed = date >= STATS_START
					params.service_game_rank = areStatsUsed ? ranks['sg'] : null
					params.service_game_elo_rating = areStatsUsed ? intRound(ratings['sg']) : null
					params.return_game_rank = areStatsUsed ? ranks['rg'] : null
					params.return_game_elo_rating = areStatsUsed ? intRound(ratings['rg']) : null
					boolean isTieBreakUsed = date >= TIE_BREAK_START
					params.tie_break_rank = isTieBreakUsed ? ranks['tb'] : null
					params.tie_break_elo_rating = isTieBreakUsed ? intRound(ratings['tb']) : null
					ps.addBatch(params)
				}
			}
		}
		List<MatchEloRating> matchRatingsBatch = new ArrayList<>(matchRatings.size())
		matchRatings.drainTo(matchRatingsBatch)
		if (matchRatingsBatch) {
			sqlPool.withSql { sql ->
				sql.withBatch(UPDATE_MATCH_ELO_RATINGS) { ps ->
					matchRatingsBatch.each { matchEloRatings ->
						Map params = [:]
						params.match_id = matchEloRatings.matchId
						params.winner_elo_rating = intRound matchEloRatings.winnerRating
						params.winner_next_elo_rating = intRound matchEloRatings.winnerNextRating
						params.loser_elo_rating = intRound matchEloRatings.loserRating
						params.loser_next_elo_rating = intRound matchEloRatings.loserNextRating
						ps.addBatch(params)
					}
				}
			}
		}
		if (saves.incrementAndGet() % SAVES_PER_PLUS == 0)
			progressTick '+'
	}

	private static intRound(Double d) {
		d ? (int)round(d) : null
	}

	private progressTick(tick) {
		print tick
		if (progress.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
			println()
	}

	private static class EloRatingValue {

		int playerId
		Map<String, Integer> ranks = [:]
		Map<String, Double> ratings = [:]

		EloRatingValue(EloRating eloRating, String type = null) {
			playerId = eloRating.playerId
			ratings[type] = eloRating.rating
		}

		def merge(EloRatingValue eloRating) {
			ranks.putAll(eloRating.ranks)
			ratings.putAll(eloRating.ratings)
		}

		boolean isForSave() {
			if (!ranks[null])
				return false
			for (Integer rank : ranks.values()) {
				if (rank <= PLAYERS_TO_SAVE)
					return true
			}
			false
		}

		@Override boolean equals(o) {
			if (this.is(o)) return true
			if (getClass() != o.class) return false
			playerId == ((EloRatingValue)o).playerId
		}

		@Override int hashCode() {
			return playerId
		}
	}

	@EqualsAndHashCode(includeFields = true)
	private static class RankKey {
		int playerId
		Date date
	}

	private static class MatchEloRating {
		long matchId
		int winnerRating
		int winnerNextRating
		int loserRating
		int loserNextRating
	}

	private static class PredictionResult {

		int total
		int predicted
		double p
		double pDelta2
		double pLog

		double predictionRate
		double brier
		double logLoss
		double score
		double calibration

		void newMatch(String type, def match, double winnerRating, double loserRating) {
			if (winnerRating == loserRating || match.outcome != null || toLocalDate(match.end_date) < LocalDate.of(2005, 1, 1))
				return

			def winnerScore, loserScore
			switch (type) {
				case 's':
					winnerScore = match.w_sets ?: 0d
					loserScore = match.l_sets ?: 0d
					break
				case 'g':
					winnerScore = match.w_games ?: 0d
					loserScore = match.l_games ?: 0d
					break
				case 'sg':
					winnerScore = (match.w_sv_gms ?: 0d) * returnToServeRatio(match.surface)
					loserScore = match.l_rt_gms ?: 0d
					break
				case 'rg':
					winnerScore = match.w_rt_gms ?: 0d
					loserScore = (match.l_sv_gms ?: 0d) * returnToServeRatio(match.surface)
					break
				case 'tb':
					winnerScore = match.w_tbs ?: 0d
					loserScore = match.l_tbs ?: 0d
					break
				default:
					winnerScore = 1d
					loserScore = 0d
					break
			}

			if (winnerScore == loserScore)
				return
			double totalScore = winnerScore + loserScore
			if (totalScore > 0d) {
				def winnerProbability = 1d / (1d + pow(10d, (loserRating - winnerRating) / 400d))
				def loserProbability = 1d - winnerProbability
				def probability
				if (winnerScore > loserScore) {
					++total
					if (winnerProbability > 0.5d) {
						++predicted
						probability = winnerProbability
					}
					else
						probability = loserProbability
				}
				else {
					++total
					if (winnerProbability < 0.5d) {
						++predicted
						probability = loserProbability
					}
					else
						probability = winnerProbability
				}
				p += probability
				pLog += log(winnerProbability)
				def pDelta = winnerScore / totalScore - winnerProbability
				pDelta2 += pDelta * pDelta
			}
		}

		def complete() {
			predictionRate = pct(predicted, total)
			brier = pDelta2 / total
			logLoss = -pLog / total
			score = predicted / (total * logLoss) // = Prediction Rate / Log-Loss
			calibration = p / predicted
		}

		@Override String toString() {
			return format('Rate=%1$.3f%%, Brier=%2$.5f, LogLoss=%3$.5f, Score=%4$.5f, Calibration=%5$.5f, Matches=%6$d', predictionRate, brier, logLoss, score, calibration, total)
		}
	}
}
