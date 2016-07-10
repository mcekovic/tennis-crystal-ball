package org.strangeforest.tcb.stats.service;

import java.io.*;
import java.sql.*;
import java.util.concurrent.atomic.*;

import org.postgresql.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class RecordsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	public static final int MAX_PLAYER_COUNT = 100;

	private static final String RECORD_TABLE_QUERY = //language=SQL
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, r.detail\n" +
		"FROM %1$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.record_id = :recordId\n" +
		"ORDER BY r.sort_order, p.name OFFSET :offset";

	private static final String SAVE_RECORD = //language=SQL
		"WITH player_record AS (\n" +
		"  %1$s\n" +
		"), player_record_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY %2$s) AS rank, rank() OVER (ORDER BY %3$s) AS order, player_id, %4$s\n" +
		"  FROM player_record r LEFT JOIN player p USING (player_id)\n" +
		"  WHERE NOT lower(p.last_name) LIKE '%%unknown%%'%5$s\n" +
		")\n" +
		"INSERT INTO %6$s\n" +
		"SELECT :recordId AS record_id, row_number() OVER (ORDER BY r.order, p.name) AS sort_order, r.rank, player_id,\n" +
		"  (SELECT row_to_json(d) FROM (SELECT %4$s) AS d) AS detail\n" +
		"FROM player_record_ranked r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank <= :maxPlayers\n" +
		"ORDER BY r.order, p.name";

	private static final String ACTIVE_CONDITION = /* language=SQL */ " AND p.active";

	private static final String DELETE_RECORD = //language=SQL
		"DELETE FROM %1$s\n" +
		"WHERE record_id = :recordId";

	private static final String DELETE_RECORDS = //language=SQL
		"DELETE FROM %1$s";


	@Cacheable("Records.Table")
	public BootgridTable<RecordRow> getRecordTable(String recordId, boolean activePlayers, int pageSize, int currentPage) {
		Record record = Records.getRecord(recordId);
		BootgridTable<RecordRow> table = new BootgridTable<>(currentPage);
		AtomicInteger players = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(RECORD_TABLE_QUERY, getTableName(activePlayers)),
			params("recordId", recordId)
				.addValue("offset", offset),
			rs -> {
				if (players.incrementAndGet() <= pageSize) {
					int rank = rs.getInt("rank");
					int playerId = rs.getInt("player_id");
					String name = rs.getString("name");
					String countryId = rs.getString("country_id");
					Boolean active = !activePlayers ? rs.getBoolean("active") : null;
//					RecordDetail detail = record.getDetailFactory().createDetail();
//					populateDetail(detail, rs.getString("detail"));
					RecordDetail detail = getDetail(record.getDetailFactory().getDetailClass(), rs.getString("detail"));
					table.addRow(new RecordRow(rank, playerId, name, countryId, active, detail));
				}
			}
		);
		table.setTotal(offset + players.get());
		return table;
	}

	private RecordDetail getDetail(Class<? extends RecordDetail> detailClass, String json) throws SQLDataException {
		try {
			return JSON_MAPPER.readValue(json, detailClass);
		}
		catch (IOException ex) {
			throw new SQLDataException("Unable to parse JSON value: " + json, PSQLState.DATA_ERROR.getState(), ex);
		}
	}

	private void populateDetail(RecordDetail detail, String json) throws SQLDataException {
		try {
			JSON_MAPPER.readerForUpdating(detail).readValue(json);
		}
		catch (IOException ex) {
			throw new SQLDataException("Unable to parse JSON value.", PSQLState.DATA_ERROR.getState(), ex);
		}
	}

	@Transactional
	public void updateRecord(String recordId, boolean activePlayers) {
		Record record = Records.getRecord(recordId);
		jdbcTemplate.update(format(DELETE_RECORD, getTableName(activePlayers)), params("recordId", recordId));
		jdbcTemplate.update(
			format(SAVE_RECORD, record.getSql(), record.getRankOrder(), record.getDisplayOrder(), record.getColumns(), activePlayers ? ACTIVE_CONDITION : "", getTableName(activePlayers)),
			params("recordId", recordId).addValue("maxPlayers", MAX_PLAYER_COUNT)
		);
	}

	public void deleteRecords(boolean activePlayers) {
		jdbcTemplate.getJdbcOperations().update(format(DELETE_RECORDS, getTableName(activePlayers)));
	}

	private static String getTableName(boolean activePlayers) {
		return activePlayers ? "active_player_record" : "player_record";
	}
}
