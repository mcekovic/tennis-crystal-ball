package org.strangeforest.tcb.stats.service;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.postgresql.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class RecordsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 100;

	private static final String RECORDS_TABLE_QUERY = //language=SQL
		"SELECT r.record_id, p.player_id, p.name, p.country_id, p.active, r.detail\n" +
		"FROM player_record r\n" +
		"INNER JOIN player_v p ON p.player_id = r.player_id\n" +
		"WHERE r.rank = 1 AND r.record_id = ANY(?)%1$s";

	private static final String PLAYER_RECORDS_CONDITION = //language=SQL
		"\n AND EXISTS(SELECT TRUE FROM player_record r2 WHERE r2.record_id = r.record_id AND r2.player_id = ? AND r2.rank = 1)";

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

	private static final String DELETE_ALL_RECORDS = //language=SQL
		"DELETE FROM %1$s";

	private static final String IS_RECORD_SAVED = //language=SQL
		"SELECT record_id FROM saved_record\n" +
		"WHERE record_id = :recordId AND active_players = :activePlayers";

	private static final String MARKED_RECORD_SAVED = //language=SQL
		"INSERT INTO saved_record\n" +
		"(record_id, active_players, infamous)\n" +
		"VALUES\n" +
		"(:recordId, :activePlayers, :infamous)";

	private static final String DELETE_ALL_SAVED_RECORDS = //language=SQL
		"DELETE FROM saved_record";


	@Cacheable("Records.Table")
	public BootgridTable<RecordRow> getRecordsTable(RecordFilter filter, int pageSize, int currentPage) {
		List<Record> records = getRecords(filter);
		int offset = (currentPage - 1) * pageSize;
		List<RecordRow> recordRows = records.stream()
			.skip(offset).limit(pageSize)
			.map(RecordRow::new)
			.collect(toList());
		jdbcTemplate.getJdbcOperations().query(
			format(RECORDS_TABLE_QUERY, ""),
			ps -> {
				bindStringArray(ps, 1, recordRows.stream().map(RecordRow::getId).toArray(String[]::new));
			},
			rs -> {
				addRecordHolders(recordRows, rs);
			}
		);
		BootgridTable<RecordRow> table = new BootgridTable<>(currentPage, records.size());
		recordRows.forEach(table::addRow);
		return table;
	}

	@Cacheable("PlayerRecords.Table")
	public BootgridTable<PlayerRecordRow> getPlayerRecordsTable(RecordFilter filter, int playerId, int pageSize, int currentPage) {
		List<Record> records = getRecords(filter);
		List<PlayerRecordRow> recordRows = records.stream()
			.map(record -> new PlayerRecordRow(record, playerId))
			.collect(toList());
		jdbcTemplate.getJdbcOperations().query(
			format(RECORDS_TABLE_QUERY, PLAYER_RECORDS_CONDITION),
			ps -> {
				bindStringArray(ps, 1, recordRows.stream().map(PlayerRecordRow::getId).toArray(String[]::new));
				ps.setInt(2, playerId);
			},
			rs -> {
				addRecordHolders(recordRows, rs);
			}
		);
		List<PlayerRecordRow> playerRecordRows = recordRows.stream().filter(PlayerRecordRow::hasHolders).collect(toList());
		BootgridTable<PlayerRecordRow> table = new BootgridTable<>(currentPage, playerRecordRows.size());
		int offset = (currentPage - 1) * pageSize;
		playerRecordRows.stream().skip(offset).limit(pageSize).forEach(table::addRow);
		return table;
	}

	private static List<Record> getRecords(RecordFilter filter) {
		return Records.getRecords().stream()
			.filter(filter::predicate)
			.collect(toList());
	}

	private static void addRecordHolders(List<? extends RecordRow> recordRows, ResultSet rs) throws SQLException {
		String recordId = rs.getString("record_id");
		int playerId = rs.getInt("player_id");
		String name = rs.getString("name");
		String countryId = rs.getString("country_id");
		boolean active = rs.getBoolean("active");
		Record record = Records.getRecord(recordId);
		RecordDetail detail = getDetail(record, rs.getString("detail"));
		RecordHolderRow recordHolder = new RecordHolderRow(playerId, name, countryId, active, String.valueOf(detail.getValue()), detail.toDetailString());
		Optional<RecordRow> recordRow = findRecordRow((List)recordRows, recordId);
		recordRow.orElseThrow(
			() -> new IllegalStateException("Cannot find record: " + recordId)
		).addRecordHolder(recordHolder);
	}

	private static <R extends RecordRow> Optional<R> findRecordRow(List<R> recordRows, String recordId) {
		return recordRows.stream().filter(row -> row.getId().equals(recordId)).findFirst();
	}

	@Transactional
	@Cacheable("Record.Table")
	public BootgridTable<RecordDetailRow> getRecordTable(String recordId, boolean activePlayers, int pageSize, int currentPage) {
		Record record = Records.getRecord(recordId);
		ensureSaveRecord(record, activePlayers);
		BootgridTable<RecordDetailRow> table = new BootgridTable<>(currentPage);
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
					RecordDetail detail = getDetail(record, rs.getString("detail"));
					table.addRow(new RecordDetailRow(rank, playerId, name, countryId, active, detail));
				}
			}
		);
		table.setTotal(offset + players.get());
		return table;
	}

	private void ensureSaveRecord(Record record, boolean activePlayers) {
		if (!isRecordSaved(record, activePlayers)) {
			deleteRecord(record, activePlayers);
			saveRecord(record, activePlayers);
			markRecordSaved(record, activePlayers);
		}
	}

	private static RecordDetail getDetail(Record record, String json) throws SQLDataException {
		try {
			return record.getDetailFactory().createDetail(json);
		}
		catch (IOException ex) {
			throw new SQLDataException("Unable to parse JSON value: " + json, PSQLState.DATA_ERROR.getState(), ex);
		}
	}

	@Transactional
	public void refreshRecord(String recordId, boolean activePlayers) {
		Record record = Records.getRecord(recordId);
		deleteRecord(record, activePlayers);
		saveRecord(record, activePlayers);
		if (!isRecordSaved(record, activePlayers))
			markRecordSaved(record, activePlayers);
	}

	private void saveRecord(Record record, boolean activePlayers) {
		jdbcTemplate.update(
			format(SAVE_RECORD, record.getSql(), record.getRankOrder(), record.getDisplayOrder(), record.getColumns(), activePlayers ? ACTIVE_CONDITION : "", getTableName(activePlayers)),
			params("recordId", record.getId()).addValue("maxPlayers", MAX_PLAYER_COUNT)
		);
	}

	private void deleteRecord(Record record, boolean activePlayers) {
		jdbcTemplate.update(
			format(DELETE_RECORD, getTableName(activePlayers)),
			params("recordId", record.getId())
		);
	}

	private boolean isRecordSaved(Record record, boolean activePlayers) {
		return jdbcTemplate.query(
			IS_RECORD_SAVED,
			params("recordId", record.getId())
				.addValue("activePlayers", activePlayers),
			ResultSet::next
		);
	}

	private void markRecordSaved(Record record, boolean activePlayers) {
		jdbcTemplate.update(
			MARKED_RECORD_SAVED,
			params("recordId", record.getId())
				.addValue("activePlayers", activePlayers)
				.addValue("infamous", record.isInfamous())
		);
	}

	@Transactional
	public void clearRecords() {
		deleteAllRecords(false);
		deleteAllRecords(true);
		deleteAllSavedRecords();
	}

	private void deleteAllRecords(boolean activePlayers) {
		jdbcTemplate.getJdbcOperations().update(format(DELETE_ALL_RECORDS, getTableName(activePlayers)));
	}

	private void deleteAllSavedRecords() {
		jdbcTemplate.getJdbcOperations().update(DELETE_ALL_SAVED_RECORDS);
	}

	private static String getTableName(boolean activePlayers) {
		return activePlayers ? "active_player_record" : "player_record";
	}
}
