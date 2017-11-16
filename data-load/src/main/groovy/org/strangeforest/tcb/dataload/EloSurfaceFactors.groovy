package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.util.DateUtil.*

class EloSurfaceFactors {

	static final String QUERY_SURFACE_RATIOS = //language=SQL
		"SELECT extract(YEAR FROM date) AS season,\n" +
			"  count(*) FILTER (WHERE surface = 'H')::REAL / count(*) AS hard_pct,\n" +
			"  count(*) FILTER (WHERE surface = 'C')::REAL / count(*) AS clay_pct,\n" +
			"  count(*) FILTER (WHERE surface = 'G')::REAL / count(*) AS grass_pct,\n" +
			"  count(*) FILTER (WHERE surface = 'P')::REAL / count(*) AS carpet_pct,\n" +
			"  count(*) FILTER (WHERE NOT indoor)::REAL / count(*) AS outdoor_pct,\n" +
			"  count(*) FILTER (WHERE indoor)::REAL / count(*) AS indoor_pct\n" +
			"FROM match\n" +
			"GROUP BY season\n" +
			"ORDER BY season"

	private Map<Integer, Double> hardFactors = new TreeMap<>()
	private Map<Integer, Double> clayFactors = new TreeMap<>()
	private Map<Integer, Double> grassFactors = new TreeMap<>()
	private Map<Integer, Double> carpetFactors = new TreeMap<>()
	private Map<Integer, Double> outdoorFactors = new TreeMap<>()
	private Map<Integer, Double> indoorFactors = new TreeMap<>()

	EloSurfaceFactors(SqlPool sqlPool) {
		println 'Loading surface ratios'
		sqlPool.withSql  { sql ->
			sql.eachRow(QUERY_SURFACE_RATIOS) { row ->
				int season = row.season
				hardFactors[season] = (1.5 + pctToFactor(toDouble(row.hard_pct))) / 2
				clayFactors[season] = (1.6 + pctToFactor(toDouble(row.clay_pct))) / 2
				grassFactors[season] = (2.2 + pctToFactor(toDouble(row.grass_pct))) / 2
				carpetFactors[season] = (2.5 + pctToFactor(toDouble(row.carpet_pct))) / 2
				outdoorFactors[season] = (1.1 + pctToFactor(toDouble(row.outdoor_pct))) / 2
				indoorFactors[season] = (2.4 + pctToFactor(toDouble(row.indoor_pct))) / 2
			}
		}
	}

	double surfaceKFactor(String surface, Date date) {
		switch (surface) {
			case 'H': return kFactor(hardFactors, date); break
			case 'C': return kFactor(clayFactors, date); break
			case 'G': return kFactor(grassFactors, date); break
			case 'P': return kFactor(carpetFactors, date); break
			case 'O': return kFactor(outdoorFactors, date); break
			case 'I': return kFactor(indoorFactors, date); break
		}
	}

	private static double pctToFactor(double pct) {
		pct > 0 ? Double.max(1.0, 2.4 - 1.5 * pct) : 10
	}

	private static double kFactor(Map<Integer, Double> surfaceFactors, Date date) {
		def season = toLocalDate(date).year
		def pct = surfaceFactors[season]
		if (!pct)
			pct = surfaceFactors[season - 1]
		if (!pct)
			pct = surfaceFactors[season + 1]
		pct ?: 0
	}

	private static double toDouble(Number n) {
		n ? n.doubleValue() : 0.0
	}
}
