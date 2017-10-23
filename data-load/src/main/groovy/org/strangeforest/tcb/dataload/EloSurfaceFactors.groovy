package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.util.DateUtil.*

class EloSurfaceFactors {

	static final String QUERY_SURFACE_RATIOS = //language=SQL
		"SELECT extract(YEAR FROM date) AS season,\n" +
			"  count(*) FILTER (WHERE surface = 'H')::REAL / count(*) AS hard_pct,\n" +
			"  count(*) FILTER (WHERE surface = 'C')::REAL / count(*) AS clay_pct,\n" +
			"  count(*) FILTER (WHERE surface = 'G')::REAL / count(*) AS grass_pct,\n" +
			"  count(*) FILTER (WHERE surface = 'P')::REAL / count(*) AS carpet_pct\n" +
			"FROM match\n" +
			"GROUP BY season\n" +
			"ORDER BY season"

	private Map<Integer, Double> hardFactors = new TreeMap<>()
	private Map<Integer, Double> clayFactors = new TreeMap<>()
	private Map<Integer, Double> grassFactors = new TreeMap<>()
	private Map<Integer, Double> carpetFactors = new TreeMap<>()

	EloSurfaceFactors(SqlPool sqlPool) {
		println 'Loading surface ratios'
		sqlPool.withSql  { sql ->
			sql.eachRow(QUERY_SURFACE_RATIOS) { row ->
				int season = row.season
				hardFactors[season] = pctToFactor(toDouble(row.hard_pct))
				clayFactors[season] = pctToFactor(toDouble(row.clay_pct))
				grassFactors[season] = pctToFactor(toDouble(row.grass_pct))
				carpetFactors[season] = pctToFactor(toDouble(row.carpet_pct))
			}
		}
		println hardFactors
		println clayFactors
		println grassFactors
		println carpetFactors
	}

	double surfaceKFactor(String surface, Date date) {
		switch (surface) {
//			case 'H': 1.5; break
//			case 'C': 1.6; break
//			case 'G': 2.1; break
//			case 'P': 2.3; break
//			case 'H': 1.75; break
//			case 'C': 1.85; break
//			case 'G': 2.40; break
//			case 'P': 2.90; break
			case 'H': return kFactor(hardFactors, date); break
			case 'C': return kFactor(clayFactors, date); break
			case 'G': return kFactor(grassFactors, date); break
			case 'P': return kFactor(carpetFactors, date); break
		}
	}

	private static double pctToFactor(double pct) {
		pct > 0 ? 1 + 1.5 * (1 - pct) : 10
	}

	private static double kFactor(Map<Integer, Double> surfaceFactors, Date date) {
		def season = toLocalDate(date).year
		def pct = surfaceFactors[season]
		while (!pct)
			pct = surfaceFactors[--season]
		pct
	}

	private static double toDouble(Number n) {
		n ? n.doubleValue() : 0.0
	}
}
