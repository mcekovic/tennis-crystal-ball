package org.strangeforest.tcb.dataload

import groovy.transform.EqualsAndHashCode

import java.time.*
import java.time.temporal.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

import org.strangeforest.tcb.util.*

import com.google.common.base.*
import groovy.sql.*

import static java.lang.Math.*
import static org.strangeforest.tcb.dataload.StartEloRatings.*
import static org.strangeforest.tcb.util.DateUtil.*

//TODO Cache player start rankings, optionally batch rankings fetch for the whole day
class EloRatings {

	final SqlPool sqlPool
	final LockManager<Integer> lockManager
	final LockManager<RankKey> rankLockManager
	final Map<Integer, CompletableFuture> playerMatchFutures
	Map<Integer, EloRating> playerRatings
	Map<String, Map<Integer, EloRating>> surfacePlayerRatings
	BlockingQueue<MatchEloRating> matchRatings
	Map<RankKey, Integer> rankCache
	Map<Date, Map<Integer, Integer>> rankDateCache
	volatile Date lastDate
	AtomicInteger saves, rankFetches
	AtomicInteger progress
	ExecutorService rankExecutor, saveExecutor
	Date saveFromDate

	static final String QUERY_MATCHES = //language=SQL
		"SELECT m.match_id, m.winner_id, m.loser_id, tournament_end(e.date, e.level, e.draw_size) AS end_date, e.level, e.surface, m.round, m.best_of, m.outcome\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'B', 'D', 'T')\n" +
		"AND (m.outcome IS NULL OR m.outcome <> 'ABD')\n" +
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
		"{call merge_elo_ranking(:rank_date, :player_id, :rank, :elo_rating, :hard_rank, :hard_elo_rating, :clay_rank, :clay_elo_rating, :grass_rank, :grass_elo_rating, :carpet_rank, :carpet_elo_rating)}"

	static final String DELETE_ALL = //language=SQL
		"DELETE FROM player_elo_ranking"

	static final String UPDATE_MATCH_ELO_RATINGS = //language=SQL
		"UPDATE match SET winner_elo_rating = :winner_elo_rating, loser_elo_rating = :loser_elo_rating WHERE match_id = :match_id"

	static final List<String> SURFACES = ['H', 'C', 'G', 'P']
	static final Date CARPET_EOL = toDate(LocalDate.of(2008, 1, 1))
	static final Map<String, Integer> MIN_MATCHES = [(null): 10, H: 5, C: 5, G: 5, P: 5]
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
	}

	def compute(boolean save = false, boolean fullSave = true, Date saveFromDate = null, boolean preLoadRanks = true) {
		def stopwatch = Stopwatch.createStarted()
		int matches = 0
		playerRatings = new ConcurrentHashMap<>()
		surfacePlayerRatings = [:]
		SURFACES.each { surfacePlayerRatings[it] = new ConcurrentHashMap<>()}
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
			saveExecutor = Executors.newFixedThreadPool(saveThreads)
			println "Using $saveThreads saving threads"
			if (fullSave)
				deleteAll()
			else
				this.saveFromDate = saveFromDate ? saveFromDate : lastDate()
		}
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
					processMatch(match, false)
					processMatch(match, true)
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

	def current(int count, Date date = new Date(), String surface = null) {
		Date minDate = toDate(toLocalDate(date).minusYears(1))
		def i = 0
		getRatings(surface).values().findAll { it.matches >= MIN_MATCHES[surface] && it.lastDate >= minDate && it.getDaysSpan(date) <= MIN_MATCHES_PERIOD }
			.sort(comparator)
			.findAll { ++i <= count }
	}

	def peak(int count) {
		def i = 0
		playerRatings.values().findAll {	it.bestRating }
			.sort(bestComparator)
			.findAll { ++i <= count }
	}

	def processMatch(match, boolean forSurface) {
		if (forSurface && !match.surface)
			return
		long matchId = match.match_id
		int winnerId = match.winner_id
		int loserId = match.loser_id
		def playerId1 = min(winnerId, loserId)
		def playerId2 = max(winnerId, loserId)
		String level = match.level
		String surface = forSurface ? match.surface : null
		String round = match.round
		short bestOf = match.best_of
		String outcome = match.outcome
		Date date = match.end_date
		lockManager.withLock(playerId1, playerId2) {
			def winnerRating = getRating(surface, winnerId, date)
			def loserRating = getRating(surface, loserId, date)
			boolean schedule = false
			if (!(winnerRating && loserRating)) {
				if (rankExecutor)
					schedule = true
				else {
					winnerRating = winnerRating ?: newEloRating(winnerId, surface)
					loserRating = loserRating ?: newEloRating(loserId, surface)
				}
			}
			if (schedule) {
				def future = CompletableFuture.allOf(playerMatchFutures.get(playerId1) ?: nullFuture, playerMatchFutures.get(playerId2) ?: nullFuture).thenRunAsync({
					lockManager.withLock(playerId1, playerId2) {
						winnerRating = getRating(surface, winnerId, date) ?: newEloRating(winnerId, surface)
						loserRating = getRating(surface, loserId, date) ?: newEloRating(loserId, surface)
						def deltaRating = deltaRating(winnerRating.rating, loserRating.rating, level, surface, round, bestOf, outcome)
						putNewRatings(matchId, surface, winnerId, loserId, winnerRating, loserRating, deltaRating, date)
					}
				}, rankExecutor)
				playerMatchFutures.put(playerId1, future)
				playerMatchFutures.put(playerId2, future)
			}
			else {
				def deltaRating = deltaRating(winnerRating.rating, loserRating.rating, level, surface, round, bestOf, outcome)
				putNewRatings(matchId, surface, winnerId, loserId, winnerRating, loserRating, deltaRating, date)
			}
		}
	}

	private EloRating newEloRating(int playerId, String surface) {
		new EloRating(playerId, playerRank(playerId, lastDate), surface)
	}

	private getRatings(String surface) {
		surface == null ? playerRatings : surfacePlayerRatings[surface]
	}

	private getRating(String surface, int playerId, Date date) {
		def rating = getRatings(surface).get(playerId)
		if (rating)
			rating.adjustRating(date)
		rating
	}

	private putNewRatings(long matchId, String surface, int winnerId, int loserId, EloRating winnerRating, EloRating loserRating, double deltaRating, Date date) {
		def ratings = getRatings(surface)
		ratings.put(winnerId, winnerRating.newRating(deltaRating, date, surface))
		ratings.put(loserId, loserRating.newRating(-deltaRating, date, surface))
		if (!surface)
			matchRatings.put new MatchEloRating(matchId: matchId, winnerRating: winnerRating.rating, loserRating: loserRating.rating)
	}

	private static double deltaRating(double winnerRating, double loserRating, String level, String surface, String round, short bestOf, String outcome) {
		double winnerQ = pow(10, winnerRating / 400)
		double loserQ = pow(10, loserRating / 400)
		double loserExpectedScore = loserQ / (winnerQ + loserQ)
		kFactor(level, surface, round, bestOf, outcome) * loserExpectedScore
	}

	private static double kFactor(String level, String surface, String round, short bestOf, String outcome) {
		double kFactor = 100
		switch (level) {
			case 'G': break
			case 'F': kFactor *= 0.9; break
			case 'M': kFactor *= 0.8; break
			case 'A': kFactor *= 0.7; break
			default: kFactor *= 0.6; break
		}
		switch (surface) {
			case 'H': kFactor *= 1.5; break
			case 'C': kFactor *= 1.6; break
			case 'G': kFactor *= 2.2; break
			case 'P': kFactor *= 2.2; break
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

	static class EloRating implements Comparable<EloRating> {

		volatile int playerId
		volatile double rating
		volatile int matches
		volatile Deque<Date> dates
		volatile EloRating bestRating

		EloRating() {}

		EloRating(int playerId, Integer rank, String surface) {
			this.playerId = playerId
			rating = startRating(rank, surface)
		}

		EloRating newRating(double delta, Date date, String surface) {
			def newRating = new EloRating(playerId: playerId, rating: rating + delta * kFunction(), matches: matches + 1, dates: new ArrayDeque<>(dates ?: []))
			newRating.bestRating = bestRating(newRating, surface)
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

		/**
		 * K-Function returns values from 1/2 to 1.
		 * For rating 0-1800 returns 1
		 * For rating 1800-2000 returns linearly decreased values from 1 to 1/2. For example, for 1900 return 3/4
		 * For rating 2000+ returns 1/2
		 * @return values from 1/2 to 1, depending on current rating
		 */
		private double kFunction() {
			if (rating <= 1800)
				1.0
			else if (rating <= 2000)
				1.0 - (rating - 1800) / 400.0
			else
				0.5
		}

		def adjustRating(Date date) {
			def lastDate = this.lastDate
			if (lastDate) {
				def daysSinceLastMatch = ChronoUnit.DAYS.between(toLocalDate(lastDate), toLocalDate(date))
				if (daysSinceLastMatch > 365)
					rating = ratingAdjusted(daysSinceLastMatch)
			}
		}

		private ratingAdjusted(long daysSinceLastMatch) {
			max(START_RATING, rating - (daysSinceLastMatch - 365) * 200 / 365)
		}

		def bestRating(EloRating newRating, String surface) {
			if (matches >= MIN_MATCHES[surface])
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
		println 'Preloading ranks...'
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
					progressTick '?'
			}
		}
		println "\nRanks preloaded in $stopwatch"
		progress = new AtomicInteger()
	}

	private saveCurrentRatings() {
		if (saveExecutor && playerRatings && (!saveFromDate || lastDate >= saveFromDate)) {
			def eloRatings = setRanks(current(Integer.MAX_VALUE, lastDate).collect { new EloRatingValue(it) })
			def playerRatings = new LinkedHashMap<Integer, EloRatingValue>(eloRatings.collectEntries({[(it.playerId): it]}))
			SURFACES.each { surface ->
				def surfaceEloRatings = current(PLAYERS_TO_SAVE, lastDate, surface).collect { new EloRatingValue(it, surface) }
				mergeRatings(playerRatings, setRanks(surfaceEloRatings, surface))
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

	private static setRanks(Collection<EloRatingValue> eloRatings, String surface = null) {
		int rank = 0
		for (def eloRating : eloRatings)
			eloRating.ranks[surface] = ++rank
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
					boolean isCarpetUsed = date < CARPET_EOL
					params.carpet_rank = isCarpetUsed ? ranks['P'] : null
					params.carpet_elo_rating = isCarpetUsed ? intRound(ratings['P']) : null
					ps.addBatch(params)
				}
			}
		}
		List<MatchEloRating> matchRatingsBatch = new ArrayList<>(matchRatings.size())
		matchRatings.drainTo(matchRatingsBatch)
		sqlPool.withSql { sql ->
			sql.withBatch(UPDATE_MATCH_ELO_RATINGS) { ps ->
				matchRatingsBatch.each { matchEloRatings ->
					Map params = [:]
					params.match_id = matchEloRatings.matchId
					params.winner_elo_rating = matchEloRatings.winnerRating
					params.loser_elo_rating = matchEloRatings.loserRating
					ps.addBatch(params)
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

		EloRatingValue(EloRating eloRating, String surface = null) {
			playerId = eloRating.playerId
			ratings[surface] = eloRating.rating
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
		int loserRating
	}
}
