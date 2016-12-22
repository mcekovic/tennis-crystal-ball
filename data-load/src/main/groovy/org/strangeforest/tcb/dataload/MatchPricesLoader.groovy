package org.strangeforest.tcb.dataload

import java.sql.*

class MatchPricesLoader extends BaseCSVLoader {

	short season

	MatchPricesLoader(SqlPool sqlPool) {
		super(sqlPool)
	}

	def loadFile(String file) {
		def start = file.lastIndexOf(File.separator)
		start = start < 0 ? 0 : start + 1
		def fileName = file.substring(start, file.indexOf('.', start))
		fileName = fileName.startsWith('match_prices_') ? fileName.substring(13) : fileName
		season = Short.parseShort(fileName)
		super.loadFile(file)
	}

	String loadSql() {
		'{call load_match_prices(' +
			':season, :location, :tournament, :date, :surface, :round, :winner, :loser, :B365W, :B365L, :EXW, :EXL, :LBW, :LBL, :PSW, :PSL' +
		')}'
	}

	int batchSize() { 100 }

	Map params(def record, Connection conn) {
		def params = [:]

		params.season = season
		params.location = mapTournament record.Location.trim()
		params.tournament = mapTournament record.Tournament.trim()
		params.date = date record.Date
		params.surface = mapSurface record.Surface

		params.round = mapRound record.Round
		params.winner = mapPlayer lastName(record.Winner)
		params.loser = mapPlayer lastName(record.Loser)

		params.B365W = decimal record.B365W
		params.B365L = decimal record.B365L
		params.EXW = decimal record.EXW
		params.EXL = decimal record.EXL
		params.LBW = decimal record.LBW
		params.LBL = decimal record.LBL
		params.PSW = decimal record.PSW
		params.PSL = decimal record.PSL

		return params
	}

	static mapSurface(String surface) {
		switch (surface) {
			case 'Hard': return 'H'
			case 'Clay': return 'C'
			case 'Grass': return 'G'
			case 'Carpet': return 'P'
			default: throw new IllegalArgumentException("Unknown surface: $surface")
		}
	}

	static mapRound(String round) {
		switch (round) {
			case 'Round Robin': return 'RR'
			case 'Quarterfinals': return 'QF'
			case 'Semifinals': return 'SF'
			case 'The Final': return 'F'
			default: return null
		}
	}

	static String lastName(String name) {
		while (true) {
			if (name.endsWith('.'))
				name = name.substring(0, name.length() - 1)
			int pos = name.lastIndexOf(' ')
			if (pos > 0)
				name = name.substring(0, pos)
			else
				break
		}
		name.replace('-', ' ')
	}

	static Date date(String d) {
		d ? new Date(java.util.Date.parse('M/d/yyyy', d).time) : null
	}

	String mapTournament(String name) {
		switch (name) {
			case 'BNP Paribas Open': return 'Indian Wells Masters'
			case 'Sony Ericsson Open': return 'Miami Masters'
			case 'Mutua Madrid Open': return 'Madrid Masters'
			case 'Internazionali BNL d\'Italia': return 'Rome Masters'
			case 'French Open': return 'Roland Garros'
			case 'AEGON Championships': return 'London'
			case 'Topshelf Open': return 's-Hertogenbosch'
			case 'Rogers Masters': return 'Canada Masters'
			case 'Western & Southern Financial Group Masters': return 'Cincinnati Masters'
			case 'St. Petersburg Open': return 'St.Petersburg'
			case 'Paris': return season == 2016 as short ? 'Paris' : 'Paris Masters'
			case 'Masters Cup': return season == 2016 as short ? 'Barclays ATP World Tour Finals' : 'Tour Finals'
			default: return name
		}
	}

	static String mapPlayer(String name) {
		switch (name) {
			case 'Zayid': return 'Harrasi'
			case 'Ramos Vinolas': return 'Ramos'
			case 'Roger Vasselin': return 'Vasselin'
			default: return name
		}
	}
}
