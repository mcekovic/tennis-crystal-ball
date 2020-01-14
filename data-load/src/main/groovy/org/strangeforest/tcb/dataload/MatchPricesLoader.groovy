package org.strangeforest.tcb.dataload

import java.sql.*
import java.text.*

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
		'CALL load_match_prices(' +
			':season, :location, :tournament, :date, :surface, :round, :winner, :loser, :B365W, :B365L, :EXW, :EXL, :LBW, :LBL, :PSW, :PSL' +
		')'
	}

	int batchSize() { 100 }

	Map params(record, Connection conn) {
		def params = [:]

		params.season = season
		params.location = mapTournament record.Location.trim()
		params.tournament = mapTournament record.Tournament.trim()
		params.date = date record.Date
		params.surface = mapSurface record.Surface

		params.round = mapRound record.Round
		params.winner = mapPlayer lastName(record.Winner)
		params.loser = mapPlayer lastName(record.Loser)

		params.B365W = safeDecimal safeProperty(record, 'B365W')
		params.B365L = safeDecimal safeProperty(record, 'B365L')
		params.EXW = safeDecimal safeProperty(record, 'EXW')
		params.EXL = safeDecimal safeProperty(record, 'EXL')
		params.LBW = safeDecimal safeProperty(record, 'LBW')
		params.LBL = safeDecimal safeProperty(record, 'LBL')
		params.PSW = safeDecimal safeProperty(record, 'PSW')
		params.PSL = safeDecimal safeProperty(record, 'PSL')

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
		d ? new Date(new SimpleDateFormat('M/d/yyyy').parse(d).time) : null
	}

	String mapTournament(String name) {
		switch (name) {
			case 'Indian Wells': return 'Indian Wells Masters'
			case 'Miami': return 'Miami Masters'
			case 'VTR Open': return 'Santiago'
			case 'Portugal Open': return 'Estoril'
			case 'Movistar Open': return season <= 2009 ? 'Viña del Mar' : name
			case 'Portschach': return 'Poertschach'
			case 'Madrid': return 'Madrid Masters'
			case 'Hamburg': return season <= 2008 ? 'Hamburg Masters' : name
			case 'Rome': return 'Rome Masters'
			case 'Stuttgart': return season == 2000 as short ? 'Stuttgart Masters' : name
			case 'St. Polten': return 'St. Poelten'
			case 'French Open': return 'Roland Garros'
			case 'AEGON Championships': return 'London'
			case 'Queens Club': return season <= 2017 ? 'Queen\'s Club' : (season >= 2018 ? 'London' : name)
			case '\'s-Hertogenbosch': return season >= 2008 ? 's-Hertogenbosch' : name
			case 'Salvador': return 'Costa Do Sauipe'
			case 'Sopot': return season == 2001 as short ? 'Sopot - WS' : name
			case 'Winston-Salem':
			case 'Winston-Salem Open': return season >= 2018 ? 'Winston Salem' : name
			case 'Rogers Masters': case 'Rogers Cup': case 'Toronto': case 'Montreal': return 'Canada Masters'
			case 'Western & Southern Financial Group Masters': case 'Cincinnati': return 'Cincinnati Masters'
			case 'Ho Chi Min City': return 'Ho Chi Minh City'
			case 'St. Petersburg Open': return 'St.Petersburg'
			case 'Paris': return 'Paris Masters'
			case 'Masters Cup': return season >= 2009 ? 'Tour Finals': name
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
