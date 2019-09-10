package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import org.slf4j.*;
import org.springframework.aop.framework.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import static com.google.common.base.Strings.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.FeaturedContent.*;

@Service
public class ContentService implements HasCache {

	@Autowired private PlayerService playerService;
	@Autowired private RankingsService rankingsService;
	@Autowired private TournamentService tournamentService;
	@Autowired private RecordsService recordsService;
	@Autowired private GOATLegendService goatLegendService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	private final Random rnd = new Random();

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentService.class);

	private static final String FEATURED_CONTENT_QUERY =
		"SELECT type, value, description FROM featured_content";

	private static final String PLAYER_OF_THE_WEEK_QUERY =
		"SELECT r.player_id, e.tournament_event_id\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN event_participation p USING (tournament_event_id)\n" +
		"WHERE result = 'W' AND e.date >= current_date - INTERVAL '1 month'\n" +
		"ORDER BY date_trunc('week', e.date) DESC, e.level, p.strength DESC NULLS LAST LIMIT 1";


	private final Memoizer<PlayerOfTheWeek> playerOfTheWeek = Memoizer.of(this::doGetPlayerOfTheWeek, TimeUnit.HOURS.toMillis(1L));
	private final Memoizer<RecordOfTheDay> recordOfTheDay = Memoizer.of(this::doGetRecordOfTheDay, TimeUnit.HOURS.toMillis(1L));

	@Cacheable(value = "Global", key = "'FeaturedContent'")
	public List<FeaturedContent> getFeaturedContent() {
		return jdbcTemplate.query(FEATURED_CONTENT_QUERY, (rs, rowNum) -> new FeaturedContent(Type.valueOf(rs.getString("type")), rs.getString("value"), rs.getString("description")));
	}

	public PlayerOfTheWeek getPlayerOfTheWeek() {
		return playerOfTheWeek.get();
	}

	private PlayerOfTheWeek doGetPlayerOfTheWeek() {
		FeaturedContent playerContent = getFeaturedContent(Type.PLAYER);
		String playerId = playerContent.getValue();
		if (playerId != null) {
			String eventId = playerContent.getContent();
			TournamentEvent event = !isNullOrEmpty(eventId) ? tournamentService.getTournamentEvent(Integer.parseInt(eventId)) : null;
			return new PlayerOfTheWeek(playerService.getPlayer(Integer.parseInt(playerId)), event);
		}
		else
			return findPlayerOfTheWeek();
	}

	private PlayerOfTheWeek findPlayerOfTheWeek() {
		Integer[] playerIdEventId = jdbcTemplate.query(PLAYER_OF_THE_WEEK_QUERY, rs ->
			rs.next() ? new Integer [] {rs.getInt("player_id"), rs.getInt("tournament_event_id")} : null
		);
		if (playerIdEventId != null)
			return new PlayerOfTheWeek(playerService.getPlayer(playerIdEventId[0]), tournamentService.getTournamentEvent(playerIdEventId[1]));
		List<PlayerRanking> rankingsTopN = rankingsService.getRankingsTopN(RankType.RANK, 1);
		if (rankingsTopN.size() < 1)
			throw new NotFoundException("PlayerOfTheWeek", null);
		return new PlayerOfTheWeek(playerService.getPlayer(rankingsTopN.get(0).getPlayerId()));
	}

	public RecordOfTheDay getRecordOfTheDay() {
		return recordOfTheDay.get();
	}
	
	private RecordOfTheDay doGetRecordOfTheDay() {
		FeaturedContent recordContent = getFeaturedContent(Type.RECORD);
		String recordId = recordContent.getValue();
		if (recordId != null)
			return recordOfTheDay(recordId);
		else
			return getRecordOfTheDay(LocalDate.now().get(ChronoField.DAY_OF_YEAR));
	}

	public RecordOfTheDay getRecordOfTheDay(int currentDay) {
		Map<Record, Integer> recordWeights = getRecordWeights(null);
		for (Surface surface : Surface.values())
			recordWeights.putAll(getRecordWeights(surface.getCode()));
		int totalWeight = recordWeights.values().stream().mapToInt(v -> v).sum();
		int weightPoint = new Random(currentDay).nextInt(totalWeight);
		int weight = 0;
		for (Map.Entry<Record, Integer> recordWeight : recordWeights.entrySet()) {
			weight += recordWeight.getValue();
			if (weight >= weightPoint) {
				Record record = recordWeight.getKey();
				LOGGER.info("Record of the Day [{}, Records: {}, Total weight: {}, Current day: {}, Weight point: {}]", record.getName(), recordWeights.size(), totalWeight, currentDay, weightPoint);
				return recordOfTheDay(record);
			}
		}
		throw new IllegalStateException();
	}

	private Map<Record, Integer> getRecordWeights(String surface) {
		Map<Record, String> records = goatLegendService.getRecordsGOATPoints(surface).values().stream().collect(Collector.of(LinkedHashMap::new, Map::putAll, (a, i) -> { a.putAll(i); return a;	}));
		return records.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> {
			String v = e.getValue();
			int pos = v.indexOf(',');
			if (pos > 0)
				v = v.substring(0, pos);
			return Integer.parseInt(v);
		}));
	}

	private RecordOfTheDay recordOfTheDay(String recordId) {
		return recordOfTheDay(Records.getRecord(recordId));
	}

	private RecordOfTheDay recordOfTheDay(Record record) {
		List<RecordDetailRow> rows = recordsService.getRecordTable(record.getId(), false, 10, 1).getRows();
		int holders = 0;
		for (RecordDetailRow row : rows) {
			if (row.getRank() == 1)
				holders++;
			else
				break;
		}
		return holders > 0 ? new RecordOfTheDay(record, rows.get(rnd.nextInt(holders))) : null;
	}

	public FeaturedContent getFeaturedBlogPost() {
		return getFeaturedContent(Type.BLOG);
	}

	public FeaturedContent getFeaturedPage() {
		return getFeaturedContent(Type.PAGE);
	}

	private FeaturedContent getFeaturedContent(Type type) {
		return getAOPProxy().getFeaturedContent().stream().filter(c -> c.isOfType(type)).findAny().orElse(type.empty());
	}

	private ContentService getAOPProxy() {
		return (ContentService)AopContext.currentProxy();
	}

	@Override public int clearCache() {
		playerOfTheWeek.clear();
		recordOfTheDay.clear();
		return 2;
	}
}
