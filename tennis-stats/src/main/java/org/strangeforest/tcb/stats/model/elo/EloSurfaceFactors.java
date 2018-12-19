package org.strangeforest.tcb.stats.model.elo;

import java.sql.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;

import javax.sql.*;

import org.springframework.jdbc.core.*;

import com.google.common.base.*;

public class EloSurfaceFactors {

	private Map<Integer, Double> hardFactors = new TreeMap<>();
	private Map<Integer, Double> clayFactors = new TreeMap<>();
	private Map<Integer, Double> grassFactors = new TreeMap<>();
	private Map<Integer, Double> carpetFactors = new TreeMap<>();
	private Map<Integer, Double> outdoorFactors = new TreeMap<>();
	private Map<Integer, Double> indoorFactors = new TreeMap<>();

	private static final int START_SEASON = 1968;

	private static final String SURFACE_RATIOS_QUERY = //language=SQL
		"SELECT extract(YEAR FROM date) AS season,\n" +
		"  count(*) FILTER (WHERE surface = 'H')::REAL / count(*) AS hard_pct,\n" +
		"  count(*) FILTER (WHERE surface = 'C')::REAL / count(*) AS clay_pct,\n" +
		"  count(*) FILTER (WHERE surface = 'G')::REAL / count(*) AS grass_pct,\n" +
		"  count(*) FILTER (WHERE surface = 'P')::REAL / count(*) AS carpet_pct,\n" +
		"  count(*) FILTER (WHERE NOT indoor)::REAL / count(*) AS outdoor_pct,\n" +
		"  count(*) FILTER (WHERE indoor)::REAL / count(*) AS indoor_pct\n" +
		"FROM match\n" +
		"WHERE date >= ? AND date < ?\n" +
		"GROUP BY season\n" +
		"ORDER BY season";


	public EloSurfaceFactors(DataSource dataSource, int fromSeason) {
		this(new JdbcTemplate(dataSource), fromSeason);
	}

	public EloSurfaceFactors(JdbcTemplate jdbcTemplate) {
		this(jdbcTemplate, START_SEASON);
	}

	private EloSurfaceFactors(JdbcTemplate jdbcTemplate, int fromSeason) {
		System.out.print("Loading Elo Ratings surface ratios");
		Stopwatch stopwatch = Stopwatch.createStarted();
		LocalDate fromDate = LocalDate.of(fromSeason, 1, 1);
		LocalDate toDate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
		jdbcTemplate.query(SURFACE_RATIOS_QUERY, ps -> {
			ps.setObject(1, fromDate, Types.DATE);
			ps.setObject(2, toDate, Types.DATE);
		}, rs -> {
			int season = rs.getInt("season");
			hardFactors.put(season, (1.4 + pctToFactor(rs.getDouble("hard_pct"))) / 2.0);
			clayFactors.put(season, (1.45 + pctToFactor(rs.getDouble("clay_pct"))) / 2.0);
			grassFactors.put(season, (2.05 + pctToFactor(rs.getDouble("grass_pct"))) / 2.0);
			carpetFactors.put(season, (2.3 + pctToFactor(rs.getDouble("carpet_pct"))) / 2.0);
			outdoorFactors.put(season, (1.05 + pctToFactor(rs.getDouble("outdoor_pct"))) / 2.0);
			indoorFactors.put(season, (1.95 + pctToFactor(rs.getDouble("indoor_pct"))) / 2.0);
		});
		System.out.println(" " + stopwatch);
	}

	private static double pctToFactor(double pct) {
		return pct > 0.0 ? Double.max(1.0, 2.4 - 1.5 * pct) : 2.4;
	}

	public double surfaceKFactor(String surface, int season) {
		switch (surface) {
			case "H": return kFactor(hardFactors, season);
			case "C": return kFactor(clayFactors, season);
			case "G": return kFactor(grassFactors, season);
			case "P": return kFactor(carpetFactors, season);
			case "O": return kFactor(outdoorFactors, season);
			case "I": return kFactor(indoorFactors, season);
			default: throw new IllegalStateException();
		}
	}

	private static double kFactor(Map<Integer, Double> surfaceFactors, int season) {
		Double pct = surfaceFactors.get(season);
		if (pct != null)
			return pct;
		pct = surfaceFactors.get(season - 1);
		if (pct != null)
			return pct;
		pct = surfaceFactors.get(season + 1);
		return pct != null ? pct : 0.0;
	}
}
