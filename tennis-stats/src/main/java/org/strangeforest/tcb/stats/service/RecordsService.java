package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class RecordsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	public static final int MAX_PLAYER_COUNT = 100;

	private static final String RECORD_QUERY = //language=SQL
		"WITH player_record AS (\n" +
		"  %1$s\n" +
		"), player_record_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY %2$s) AS rank, rank() OVER (ORDER BY %3$s) AS order, player_id, %4$s\n" +
		"  FROM player_record r LEFT JOIN player p USING (player_id)\n" +
		"  WHERE NOT lower(p.last_name) LIKE '%%unknown%%'%5$s\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, %4$s\n" +
		"FROM player_record_ranked r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank <= :maxPlayers\n" +
		"ORDER BY r.order, p.name OFFSET :offset LIMIT :limit";

	private static final String ACTIVE_CONDITION = /* language=SQL */ " AND p.active";


	@Cacheable("Records.Table")
	public BootgridTable<RecordRow> getRecordTable(String recordId, boolean activePlayers, int pageSize, int currentPage) {
		Record record = Records.getRecord(recordId);
		BootgridTable<RecordRow> table = new BootgridTable<>(currentPage, MAX_PLAYER_COUNT);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(RECORD_QUERY, record.getSql(), record.getRankOrder(), record.getDisplayOrder(), record.getColumns(), activePlayers ? ACTIVE_CONDITION : ""),
			params("maxPlayers", MAX_PLAYER_COUNT)
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !activePlayers ? rs.getBoolean("active") : null;
				RecordRow row = record.getRowFactory().createRow(rank, playerId, name, countryId, active);
				row.readValues(rs);
				table.addRow(row);
			}
		);
		return table;
	}
}
