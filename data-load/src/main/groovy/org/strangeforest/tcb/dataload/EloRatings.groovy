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
import static org.strangeforest.tcb.dataload.StartEloRatings.*
import static org.strangeforest.tcb.util.DateUtil.*

class EloRatings {

	final SqlPool sqlPool
	final LockManager<Integer> lockManager
	final LockManager<RankKey> rankLockManager
	final Map<Integer, CompletableFuture> playerMatchFutures
	final EloSurfaceFactors eloSurfaceFactors
	Map<Integer, EloRating> playerRatings
	Map<String, Map<Integer, EloRating>> playerRatingsByType
	BlockingQueue<MatchEloRating> matchRatings
	Map<RankKey, Integer> rankCache
	Map<Date, Map<Integer, Integer>> rankDateCache
	volatile Date lastDate
	AtomicInteger saves, rankFetches
	AtomicInteger progress
	ExecutorService rankExecutor, saveExecutor
	Date saveFromDate

	static final String QUERY_MATCHES = //language=SQL
		"SELECT m.match_id, m.winner_id, m.loser_id, tournament_end(CASE WHEN e.level = 'D' THEN m.date ELSE e.date END, e.level, e.draw_size) AS end_date, e.level, m.surface, m.indoor, m.round, m.best_of, m.outcome," +
		"  m.w_sets, m.l_sets, s.w_sv_gms - (s.w_bp_fc - s.w_bp_sv) AS w_sv_gms, s.l_sv_gms - (s.l_bp_fc - s.l_bp_sv) AS l_sv_gms, s.l_bp_fc - s.l_bp_sv AS w_rt_gms, s.w_bp_fc - s.w_bp_sv AS l_rt_gms, m.w_tbs, m.l_tbs, m.has_stats\n" +
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
		"  :rank_date, :player_id, :rank, :elo_rating," +
		"  :hard_rank, :hard_elo_rating, :clay_rank, :clay_elo_rating, :grass_rank, :grass_elo_rating, :carpet_rank, :carpet_elo_rating, :outdoor_rank, :outdoor_elo_rating, :indoor_rank, :indoor_elo_rating," +
		"  :set_rank, :set_elo_rating, :service_game_rank, :service_game_elo_rating, :return_game_rank, :return_game_elo_rating, :tie_break_rank, :tie_break_elo_rating" +
		")}"

	static final String DELETE_ALL = //language=SQL
		"DELETE FROM player_elo_ranking"

	static final String UPDATE_MATCH_ELO_RATINGS = //language=SQL
		"UPDATE match SET winner_elo_rating = :winner_elo_rating, winner_next_elo_rating = :winner_next_elo_rating, loser_elo_rating = :loser_elo_rating, loser_next_elo_rating = :loser_next_elo_rating\n" +
		"WHERE match_id = :match_id"

	static final List<String> RATING_TYPES = ['H', 'C', 'G', 'P', 'O', 'I', 's', 'sg', 'rg', 'tb']
	static final Date CARPET_END = toDate(LocalDate.of(2008, 1, 1))
	static final Date STATS_START = toDate(LocalDate.of(1991, 1, 1))
	static final Date TIE_BREAK_START = toDate(LocalDate.of(1970, 1, 1))
	static final int DEFAULT_MIN_MATCHES = 10
	static final Map<String, Integer> MIN_MATCHES = [H: 5, C: 5, G: 5, P: 5, O: 5, I: 5]
	static final int MIN_MATCHES_PERIOD = 365
	static final int MIN_MATCHES_IN_PERIOD = 3
	static final int PLAYERS_TO_SAVE = 200

	static final int MATCHES_FETCH_SIZE = 200
	static final int RANK_PRELOAD_FETCH_SIZE = 1000
	static final double SAVE_RANK_THREAD_RATIO = 1.0

	static final comparator = { a, b -> b <=> a }
	static final bestComparator = { a, b -> b.bestRating <=> a.bestRating }
	static final nullFuture = CompletableFuture.completedFuture(null)

	static final int MATCHES_PER_DOT = 1000
	static final int SAVES_PER_PLUS = 20
	static final int RANK_FETCHES_PER_QUESTION_MARK = 200
	static final int RANK_PRELOADS_PER_QUESTION_MARK = 20000
	static final int PROGRESS_LINE_WRAP = 100

	EloRatings(SqlPool sqlPool) {
		this.sqlPool = sqlPool
		lockManager = new LockManager<>()
		rankLockManager = new LockManager<>()
		playerMatchFutures = new HashMap<>()
		eloSurfaceFactors = new EloSurfaceFactors(sqlPool)
	}

	def compute(boolean save = false, boolean fullSave = true, Date saveFromDate = null, boolean preLoadRanks = true) {
		def stopwatch = Stopwatch.createStarted()
		int matches = 0
		playerRatings = new ConcurrentHashMap<>()
		playerRatingsByType = [:]
		RATING_TYPES.each { playerRatingsByType[it] = new ConcurrentHashMap<>()}
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
				deleteAll()
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
					processMatch(match, true, false)
					processMatch(match, false, true)
					processMatch(match, false, false, 's')
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
		println "\nElo Ratings computed in $stopwatch"
		println "Rank fetches: $rankFetches"
		if (save)
			println "Saves: $saves"
		playerRatings
	}

	def waitForAllMatchesToComplete() {
		CompletableFuture.allOf(playerMatchFutures.values().toArray(new CompletableFuture[playerMatchFutures.size()])).join()
		playerMatchFutures.clear()
	}

	def current(int count, Date date = new Date(), String type = null) {
		Date minDate = toDate(toLocalDate(date).minusYears(1))
		def i = 0
		getRatings(type).values().findAll { it.matches >= minMatches() && it.lastDate >= minDate && it.getDaysSpan(date) <= MIN_MATCHES_PERIOD }
			.sort(comparator)
			.findAll { ++i <= count }
	}

	static def minMatches(String type) {
		type ? (MIN_MATCHES[type] ?: DEFAULT_MIN_MATCHES) : DEFAULT_MIN_MATCHES
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
		if (rating)
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

	private calculateAndPutNewRatings(EloRating winnerRating, EloRating loserRating, match, String type, String loserType, Date date, boolean forSurface, boolean forIndoor, String forType) {
		long matchId = match.match_id
		String level = match.level
		String round = match.round
		short bestOf = forType ? (short)5 : match.best_of
		String outcome = match.outcome
		def delta = deltaRating(winnerRating.rating, loserRating.rating, level, round, bestOf, outcome)
		if (forSurface || forIndoor)
			delta *= eloSurfaceFactors.surfaceKFactor(type, date)
		else if (forType) {
			def wDelta = delta
			def lDelta = deltaRating(loserRating.rating, winnerRating.rating, level, round, bestOf, outcome)
			switch (type) {
				case 's':
					delta = 0.5 * (wDelta * (match.w_sets ?: 0) - lDelta * (match.l_sets ?: 0))
					break
				case 'sg':
					delta = 0.1 * (wDelta * (match.w_sv_gms ?: 0) - lDelta * (match.l_rt_gms ?: 0))
					break
				case 'rg':
					delta = 0.1 * (wDelta * (match.w_rt_gms ?: 0) - lDelta * (match.l_sv_gms ?: 0))
					break
				case 'tb':
					delta = 2.0 * (wDelta * (match.w_tbs ?: 0) - lDelta * (match.l_tbs ?: 0))
					break
			}
		}

		def winnerNextRating = winnerRating
		def loserNextRating = loserRating
		if (outcome != 'ABD') {
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
			return 0.0
		double winnerQ = pow(10, winnerRating / 400f)
		double loserQ = pow(10, loserRating / 400f)
		double loserExpectedScore = loserQ / (winnerQ + loserQ)
		kFactor(level, round, bestOf, outcome) * loserExpectedScore
	}

	static double kFactor(String level, String round, short bestOf, String outcome) {
		double kFactor = 100
		switch (level) {
			case 'G': break
			case 'F': kFactor *= 0.9; break
			case 'L': kFactor *= 0.8; break
			case 'M': kFactor *= 0.8; break
			case 'O': kFactor *= 0.75; break
			case 'A': kFactor *= 0.7; break
			default: kFactor *= 0.6; break
		}
		switch (round) {
			case 'F': break
			case 'BR': kFactor *= 0.975; break
			case 'SF': kFactor *= 0.95; break
			case 'QF': kFactor *= 0.90; break
			case 'R16': kFactor *= 0.85; break
			case 'R32': kFactor *= 0.80; break
			case 'R64': kFactor *= 0.75; break
			case 'R128': kFactor *= 0.70; break
			case 'RR': kFactor *= 0.90; break
		}
		if (bestOf < 5) kFactor *= 0.9
		if (outcome == 'W/O') kFactor *= 0.5
		kFactor
	}

	/**
	 * K-Function returns values from 1/2 to 1 depending on current rating.
	 * It stabilizes ratings at the top, while allows fast progress of lower rated players.
	 * For rating 0-1800 returns 1
	 * For rating 1800-2000 returns linearly decreased values from 1 to 1/2. For example, for 1900 return 3/4
	 * For rating 2000+ returns 1/2
	 * @return values from 1/2 to 1, depending on current rating
	 */
	static double kFunction(double rating, String type = null) {
		if (rating <= ratingForType(1800, type))
			1.0f
		else if (rating <= ratingForType(2000, type))
			1.0f - (rating - ratingForType(1800, type)) / ratingDiffForType(400, type)
		else
			0.5f
	}

	static final Map<String, Double> RATING_TYPE_FACTOR = [
		's': 0.75f,
		'sg': 0.45f,
		'rg': 0.10f,
		'tb': 0.5f
	]

	static int ratingForType(double rating, String type) {
		Double factor = RATING_TYPE_FACTOR[type]
		factor ? START_RATING + factor * (rating - START_RATING) : rating
	}

	static int ratingDiffForType(double ratingDiff, String type) {
		Double factor = RATING_TYPE_FACTOR[type]
		factor ? factor * ratingDiff : ratingDiff
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
			def newRating = new EloRating(playerId: playerId, rating: rating + delta * kFunction(rating, type), matches: matches + 1, dates: new ArrayDeque<>(dates ?: []))
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
				if (daysSinceLastMatch > 365)
					rating = ratingAdjusted(daysSinceLastMatch, type)
			}
		}

		private ratingAdjusted(long daysSinceLastMatch, String type) {
			max(START_RATING, rating - (daysSinceLastMatch - 365) * ratingDiffForType(200, type) / 365)
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

	private deleteAll() {
		println 'Deleting all Elo ratings'
		sqlPool.withSql { sql ->
			sql.execute(DELETE_ALL)
		}
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
}
