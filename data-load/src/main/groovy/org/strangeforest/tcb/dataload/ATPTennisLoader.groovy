package org.strangeforest.tcb.dataload

import java.util.concurrent.*

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoadParams.*
import static org.strangeforest.tcb.dataload.SqlPool.*

class ATPTennisLoader {

	private final boolean full
	private String baseDir

	ATPTennisLoader() {
		full = getBooleanProperty(FULL_LOAD_PROPERTY, FULL_LOAD_DEFAULT)
	}

	def loadPlayers(loader) {
		println 'Loading players'
		loader.loadFile(baseDir() + 'atp_players.csv', true)
		println()
	}

	def loadRankings(loader) {
		println 'Loading rankings'
		load {
			def rows = 0
			if (full) {
				for (decade in ['70s', '80s', '90s', '00s', '10s', '20s'])
					rows += loader.loadFile(baseDir() + "atp_rankings_${decade}.csv")
			}
			rows += loader.loadFile(baseDir() + "atp_rankings_current.csv", true)
		}
		println()
	}

	def loadMatches(loader) {
		println 'Loading matches'
		load {
			def rows = 0
			if (full) {
				for (year in 1968..2020)
					rows += loader.loadFile(baseDir() + "atp_matches_${year}.csv")
			}
			def year = 2021
			rows += loader.loadFile(baseDir() + "atp_matches_${year}.csv")
		}
		println()
	}

	def loadMatchPrices(loader) {
		println 'Loading match prices'
		load {
			def rows = 0
			if (full) {
				for (year in 2001..2018)
					rows += loader.loadFile(baseDir() + "match_prices_${year}.csv")
			}
			def year = 2019
			rows += loader.loadFile(baseDir() + "match_prices_${year}.csv")
		}
		println()
	}

	private static load(loader) {
		def stopwatch = Stopwatch.createStarted()

		def rows = loader()

		stopwatch.stop()
		def seconds = stopwatch.elapsed(TimeUnit.SECONDS)
		int rowsPerSecond = seconds ? rows / seconds : 0
		println "Total rows: $rows in $stopwatch ($rowsPerSecond row/s)"
	}

	private baseDir() {
		if (!baseDir) {
			baseDir = System.properties[BASE_DIR_PROPERTY]
			if (!baseDir)
				throw new IllegalArgumentException("No Tennis data base directory is set, please specify it in $BASE_DIR_PROPERTY system property.")
			if (!baseDir.endsWith(File.separator))
				baseDir += File.separator
		}
		return baseDir
	}

	def loadAdditionalPlayerData(Sql sql) {
		if (full) {
			println 'Loading additional player data'
			new AdditionalPlayerDataLoader(sql).loadFile('classpath:/player-data.xml')

			println 'Adding player aliases and missing players...'
			executeSQLFile(sql, '/player-aliases-missing-players.sql')
		}
	}

	def loadAdditionalRankingData(Sql sql) {
		if (full) {
			println 'Fixing rank points...'
			executeSQLFile(sql, '/fix-rank-points.sql')
			println 'Loading pre-ATP rankings...'
			executeSQLFile(sql, '/rankings-pre-atp.sql')
		}
	}

	def loadAdditionalTournamentData(Sql sql) {
		if (full) {
			println 'Loading additional match data'

			def atpTourMatchLoader = new ATPTourTournamentLoader(sql)
			atpTourMatchLoader.loadTournament(1968, 'bloemfontein', 9343, false, 'B', 'H', '1968-01-08', ['R32', 'R64'], 'Bloemfontein')
			atpTourMatchLoader.loadTournament(1968, 'hobart', 713, false, 'B', null, null, ['R16', 'R32', 'R64'])
			atpTourMatchLoader.loadTournament(1968, 'durban', 260, false, 'B', 'H', '1968-01-14', ['R32', 'R64'], 'Durban')
			atpTourMatchLoader.loadTournament(1968, 'sydney', 9295, false, 'B', 'P', '1968-01-20', [], 'Sydney WCT')
			atpTourMatchLoader.loadTournament(1968, 'auckland', 301, false, 'B', null, null, ['R16', 'R32'])
			atpTourMatchLoader.loadTournament(1968, 'richmond', 802, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'shreveport', 9297, false, 'B', 'H', '1968-02-08', [], 'Shreveport WCT')
			atpTourMatchLoader.loadTournament(1968, 'miami', 681, false, 'B', 'P', '1968-02-09', [], 'Miami WCT')
			atpTourMatchLoader.loadTournament(1968, 'salisbury', 355, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'houston', 9298, false, 'B', 'P', '1968-02-13', [], 'Houston WCT')
			atpTourMatchLoader.loadTournament(1968, 'new-orleans', 9299, false, 'B', 'P', '1968-02-16', [], 'New Orleans WCT')
			atpTourMatchLoader.loadTournament(1968, 'orlando', 9300, false, 'B', 'P', '1968-02-20', [], 'Orlando WCT')
			atpTourMatchLoader.loadTournament(1968, 'macon', 2066, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'kingston', 9204, false, 'B', 'H', '1968-02-26', ['R16', 'R32'], 'Kingston')
			atpTourMatchLoader.loadTournament(1968, 'tulsa', 9301, false, 'B', 'P', '1968-02-27', [], 'Tulsa WCT')
			atpTourMatchLoader.loadTournament(1968, 'barranquilla', 3940, false, 'B', 'C', '1968-03-04', ['R32'], 'Barranquilla')
			atpTourMatchLoader.loadTournament(1968, 'caracas', 344, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'sao-paulo', 9302, false, 'B', 'C', '1968-03-18', [], 'Sao Paulo NTL 1')
			atpTourMatchLoader.loadTournament(1968, 'willemstad', 9277, false, 'B', 'H', '1968-03-18', ['R32'], 'Curacao')
			atpTourMatchLoader.loadTournament(1968, 'san-diego', 9303, false, 'B', 'C', '1968-03-22', [], 'San Diego WCT')
			atpTourMatchLoader.loadTournament(1968, 'buenos-aires', 9304, false, 'B', 'C', '1968-03-23', [], 'Buenos Aires NTL')
			atpTourMatchLoader.loadTournament(1968, 'new-york', 2058, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'rome', 9337, false, 'B', 'C', '1968-03-25', ['R32', 'R64'], 'Parioli')
			atpTourMatchLoader.loadTournament(1968, 'bogota', 9305, false, 'B', 'C', '1968-03-27', [], 'Bogota NTL')
			atpTourMatchLoader.loadTournament(1968, 'los-altos-hills', 9306, false, 'B', 'P', '1968-03-28', [], 'Los Altos Hills WCT')
			atpTourMatchLoader.loadTournament(1968, 'bakersfield', 9307, false, 'B', 'P', '1968-04-01', [], 'Bakersfield WCT')
			atpTourMatchLoader.loadTournament(1968, 'san-juan', 6817, false, 'B', null, null, ['R32'])
			atpTourMatchLoader.loadTournament(1968, 'fresno', 9308, false, 'B', 'P', '1968-04-05', [], 'Fresno WCT')
			atpTourMatchLoader.loadTournament(1968, 'catania', 9338, false, 'B', 'C', '1968-04-08', ['R32'], 'Catania')
			atpTourMatchLoader.loadTournament(1968, 'st-petersburg', 6818, false, 'B', null, null, ['R32'])
			atpTourMatchLoader.loadTournament(1968, 'tampa', 9333, false, 'B', 'C', '1968-04-08', ['R16'], 'Tampa')
			atpTourMatchLoader.loadTournament(1968, 'hollywood', 9309, false, 'B', 'C', '1968-04-10', ['R16'], 'Hollywood NTL')
			atpTourMatchLoader.loadTournament(1968, 'charlotte', 436, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'houston', 405, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'palermo', 325, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'paris', 9310, false, 'B', 'H', '1968-04-16', [], 'Paris NTL')
			atpTourMatchLoader.loadTournament(1968, 'wembley', 9311, false, 'B', 'H', '1968-04-17', [], 'Wembley NTL 1')
			atpTourMatchLoader.loadTournament(1968, 'evansville', 9312, false, 'B', 'P', '1968-04-20', [], 'Evansville WCT')
			atpTourMatchLoader.loadTournament(1968, 'naples', 9339, false, 'B', 'C', '1968-04-22', ['R64'], 'Naples')
			atpTourMatchLoader.loadTournament(1968, 'wembley', 9313, false, 'B', 'H', '1968-05-02', [], 'Wembley NTL 2')
			atpTourMatchLoader.loadTournament(1968, 'minneapolis', 9314, false, 'B', 'P', '1968-05-04', [], 'Minneapolis WCT')
			atpTourMatchLoader.loadTournament(1968, 'buffalo', 9315, false, 'B', 'P', '1968-05-10', [], 'Buffalo WCT')
			atpTourMatchLoader.loadTournament(1968, 'new-york', 9282, false, 'B', 'P', '1968-05-15', [], 'New York NTL')
			atpTourMatchLoader.loadTournament(1968, 'berlin', 9240, false, 'B', 'C', '1968-05-29', ['R32'], 'Berlin')
			atpTourMatchLoader.loadTournament(1968, 'sacramento', 654, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1968, 'baltimore', 9316, false, 'B', 'P', '1968-05-31', [], 'Baltimore WCT')
			atpTourMatchLoader.loadTournament(1968, 'beckenham', 7310, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'lugano', 9334, false, 'B', 'C', '1968-06-10', ['R64'], 'Lugano')
			atpTourMatchLoader.loadTournament(1968, 'boston', 417, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'paris', 9317, false, 'B', 'C', '1968-07-07', [], 'Paris 2')
			atpTourMatchLoader.loadTournament(1968, 'bastad', 316, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'indianapolis', 9335, false, 'B', 'C', '1968-07-08', [], 'Indianapolis')
			atpTourMatchLoader.loadTournament(1968, 'milwaukee', 717, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'los-angeles', 6811, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'haverford', 2030, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'hilversum', 317, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1968, 'bastad', 9318, false, 'B', 'C', '1968-07-26', [], 'Bastad WCT')
			atpTourMatchLoader.loadTournament(1968, 'south-orange', 707, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'munich', 308, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1968, 'cannes', 9319, false, 'B', 'H', '1968-08-01', [], 'Cannes WCT')
			atpTourMatchLoader.loadTournament(1968, 'southampton', 9288, false, 'B', 'G', '1968-08-05 ', ['R64'], 'Southampton')
			atpTourMatchLoader.loadTournament(1968, 'binghamton', 9287, false, 'B', 'H', '1968-08-09', [], 'Binghamton NTL')
			atpTourMatchLoader.loadTournament(1968, 'newport', 9291, false, 'B', 'G', '1968-08-12', [], 'Newport WCT')
			atpTourMatchLoader.loadTournament(1968, 'toronto', 421, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1968, 'fort-worth', 653, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'berkeley', 424, false, 'B', null, null, ['R32', 'R64', 'R128'])
			atpTourMatchLoader.loadTournament(1968, 'pretoria', 9320, false, 'B', 'C', '1968-09-26', [], 'Pretoria WCT')
			atpTourMatchLoader.loadTournament(1968, 'midland', 6832, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'johannesburg', 9321, false, 'B', 'H', '1968-10-02', [], 'Johannesburg WCT')
			atpTourMatchLoader.loadTournament(1968, 'la-jolla', 9336, false, 'B', 'H', '1968-10-02', ['R32'], 'La Jolla')
			atpTourMatchLoader.loadTournament(1968, 'corpus-christi', 6816, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'durban', 9322, false, 'B', 'H', '1968-10-06', [], 'Durban WCT')
			atpTourMatchLoader.loadTournament(1968, 'east-london', 9323, false, 'B', 'H', '1968-10-09', [], 'East London WCT')
			atpTourMatchLoader.loadTournament(1968, 'port-elizabeth', 9324, false, 'B', 'H', '1968-10-10', [], 'Port Elizabeth WCT')
			atpTourMatchLoader.loadTournament(1968, 'cape-town', 9325, false, 'B', 'H', '1968-10-11', [], 'Cape Town WCT')
			atpTourMatchLoader.loadTournament(1968, 'kimberley', 9326, false, 'B', 'C', '1968-10-16', [], 'Kimberley WCT')
			atpTourMatchLoader.loadTournament(1968, 'sao-paulo', 9327, false, 'B', 'C', '1968-10-21', [], 'Sao Paulo NTL 2')
			atpTourMatchLoader.loadTournament(1968, 'la-paz', 9328, false, 'B', 'C', '1968-10-27', [], 'La Paz NTL')
			atpTourMatchLoader.loadTournament(1968, 'lima', 9329, false, 'B', 'C', '1968-10-31', [], 'Lima NTL')
			atpTourMatchLoader.loadTournament(1968, 'vienna', 9330, false, 'B', 'H', '1968-11-07', [], 'Vienna WCT')
			atpTourMatchLoader.loadTournament(1968, 'london', 9332, false, 'B', 'H', '1968-11-11', [], 'London 2')
			atpTourMatchLoader.loadTournament(1968, 'wembley', 430, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'london', 2016, false, 'B')
			atpTourMatchLoader.loadTournament(1968, 'new-york', 9206, false, 'B', 'P', '1968-11-28', [], 'New York 3')
			atpTourMatchLoader.loadTournament(1968, 'nashville', 9331, false, 'B', 'P', '1968-12-07', [], 'Nashville')
			atpTourMatchLoader.loadTournament(1968, 'brisbane', 9341, false, 'B', 'G', '1968-12-09', ['R32', 'R64'], 'Brisbane')
			atpTourMatchLoader.loadTournament(1968, 'new-orleans', 9278, false, 'B', 'P', '1968-12-27', [], 'New Orleans 2')
			atpTourMatchLoader.loadTournament(1969, 'omaha', 2036, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'richmond', 802, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'salisbury', 355, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'hollywood', 681, false, 'B', 'C', '1969-02-13', [], 'Hollywood')
			atpTourMatchLoader.loadTournament(1969, 'macon', 2066, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1969, 'oakland', 9280, false, 'B', 'P', '1969-02-23', [], 'Oakland')
			atpTourMatchLoader.loadTournament(1969, 'kingston', 9204, false, 'B', 'H', '1969-02-24', ['R32'], 'Kingston')
			atpTourMatchLoader.loadTournament(1969, 'willemstad', 9277, false, 'B', 'H', '1969-02-24', ['R16'], 'Curacao')
			atpTourMatchLoader.loadTournament(1969, 'los-angeles', 6811, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'barranquilla', 3940, false, 'B', 'C', '1969-03-10', ['R32', 'R64'], 'Barranquilla')
			atpTourMatchLoader.loadTournament(1969, 'st-petersburg', 6818, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1969, 'san-juan', 6817, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1969, 'charlotte', 436, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'houston', 405, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'dallas', 388, false, 'B', null, null, ['R32'])
			atpTourMatchLoader.loadTournament(1969, 'anaheim', 9292, false, 'B', 'P', '1969-04-25', [], 'Anaheim')
			atpTourMatchLoader.loadTournament(1969, 'osaka-tokyo-and-nagoya', 9281, false, 'B', 'P', '1969-05-01', [], 'Japanese Championships')
			atpTourMatchLoader.loadTournament(1969, 'new-york', 9282, false, 'B', 'P', '1969-05-15', [], 'New York 2')
			atpTourMatchLoader.loadTournament(1969, 'berlin', 9240, false, 'B', 'C', '1969-05-19', ['R32', 'R64'], 'Berlin')
			atpTourMatchLoader.loadTournament(1969, 'london', 9284, false, 'B', 'H', '1969-05-21', [], 'London 2')
			atpTourMatchLoader.loadTournament(1969, 'amsterdam', 9283, false, 'B', 'C', '1969-05-22', [], 'Amsterdam')
			atpTourMatchLoader.loadTournament(1969, 'sacramento', 654, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'beckenham', 7310, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1969, 'aix-en-provence', 342, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1969, 'milwaukee', 9285, false, 'B', 'C', '1969-07-18', [], 'Milwaukee')
			atpTourMatchLoader.loadTournament(1969, 'munich', 308, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1969, 'south-orange', 707, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1969, 'portschach', 9286, false, 'B', 'C', '1969-07-30', [], 'Portschach')
			atpTourMatchLoader.loadTournament(1969, 'st-louis', 652, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'southampton', 9288, false, 'B', 'G', '1969-08-04', [], 'Southampton')
			atpTourMatchLoader.loadTournament(1969, 'binghamton', 9287, false, 'B', 'H', '1969-08-08', [], 'Binghamton')
			atpTourMatchLoader.loadTournament(1969, 'haverford', 2030, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'kitzbuhel', 319, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1969, 'fort-worth', 653, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'newport', 9291, false, 'B', 'G', '1969-08-20', [], 'Newport')
			atpTourMatchLoader.loadTournament(1969, 'baltimore', 9293, false, 'B', 'G', '1969-08-21', [], 'Baltimore')
			atpTourMatchLoader.loadTournament(1969, 'chicago', 818, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'atlanta', 412, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'berkeley', 424, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1969, 'midland', 6832, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'tucson', 6833, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'denver', 389, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'cologne', 332, false, 'B', 'H', '1969-10-18', [], 'Cologne')
			atpTourMatchLoader.loadTournament(1969, 'barcelona', 9294, false, 'B', 'C', '1969-11-07', [], 'Barcelona 2')
			atpTourMatchLoader.loadTournament(1969, 'london', 2016, false, 'B')
			atpTourMatchLoader.loadTournament(1969, 'vienna', 9330, false, 'B', 'H', '1969-11-13', [], 'Vienna')
			atpTourMatchLoader.loadTournament(1969, 'brisbane', 9341, false, 'B', 'G', '1969-12-01', ['R32', 'R64'], 'Brisbane')
			atpTourMatchLoader.loadTournament(1969, 'new-orleans', 9278, false, 'B', 'P', '1969-12-26', [], 'New Orleans')
			atpTourMatchLoader.deleteTournament(1969, '7302') // Paris
			atpTourMatchLoader.loadTournament(1970, 'australian-round-robin', 9290, false, 'B', 'G', '1970-01-04', [], 'Australian Round Robin')
			atpTourMatchLoader.loadTournament(1970, 'melbourne', 3936, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'auckland', 301, false, 'B', null, null, ['R32'])
			atpTourMatchLoader.loadTournament(1970, 'omaha', 2036, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'new-york', 2058, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'caracas', 344, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'barranquilla', 3940, false, 'B', 'C', '1970-03-16', ['R32', 'R64'], 'Barranquilla')
			atpTourMatchLoader.loadTournament(1970, 'willemstad', 9277, false, 'B', 'H', '1970-03-23', ['R32'], 'Curacao')
			atpTourMatchLoader.loadTournament(1970, 'jacksonville', 2047, false, 'B', null, null, ['R32'])
			atpTourMatchLoader.loadTournament(1970, 'kingston', 9204, false, 'B', 'H', '1970-04-06', ['R32'], 'Kingston')
			atpTourMatchLoader.loadTournament(1970, 'kansas-city', 9238, false, 'B', 'H', '1970-05-01', [], 'Kansas City')
			atpTourMatchLoader.loadTournament(1970, 'sacramento', 654, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'berlin', 9240, false, 'B', 'C', '1970-05-11', ['R64'], 'Berlin')
			atpTourMatchLoader.loadTournament(1970, 'beckenham', 7310, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'tennis-champions-classic', 9289, false, 'L', 'P', '1970-07-16', [], 'Tennis Champions Classic')
			atpTourMatchLoader.loadTournament(1970, 'columbus', 343, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'newport', 9291, false, 'B', 'G', '1970-08-26', [], 'Newport 2')
			atpTourMatchLoader.loadTournament(1970, 'denver', 389, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'brisbane', 9341, false, 'B', 'G', '1970-11-09', ['R16', 'R32', 'R64'], 'Brisbane')
			atpTourMatchLoader.loadTournament(1970, 'london', 2016, false, 'B')
			atpTourMatchLoader.loadTournament(1970, 'adelaide', 7308, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1970, 'new-orleans', 9278, false, 'B', 'P', '1970-12-26', [], 'New Orleans')
			atpTourMatchLoader.loadTournament(1971, 'washington', 9211, false, 'B', 'P', '1971-02-04', [], 'Washington')
			atpTourMatchLoader.loadTournament(1971, 'tennis-champions-classic', 9289, false, 'L', 'P', '1971-03-19', [], 'Tennis Champions Classic')
			atpTourMatchLoader.loadTournament(1971, 'san-juan', 6817, false, 'B', null, null, ['R32'])
			atpTourMatchLoader.loadTournament(1971, 'st-petersburg', 6818, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.loadTournament(1971, 'manchester', 583, false, 'B', 'G', '1971-05-31', ['R32', 'R64', 'R128'], 'Manchester')
			atpTourMatchLoader.loadTournament(1971, 'beckenham', 7310, false, 'B')
			atpTourMatchLoader.loadTournament(1971, 'london', 2016, false, 'B')
			atpTourMatchLoader.loadTournament(1971, 'brisbane', 9341, false, 'B', 'G', '1971-12-06', ['R64'], 'Brisbane')
			atpTourMatchLoader.deleteTournament(1971, '820') // Houston
			atpTourMatchLoader.loadTournament(1972, 'san-juan', 6817, false, 'B')
			atpTourMatchLoader.loadTournament(1972, 'manchester', 583, false, 'B', 'G', '1972-06-05', ['R64', 'R128'], 'Manchester')
			atpTourMatchLoader.loadTournament(1972, 'beckenham', 7310, false, 'B', null, null, ['R64'])
			atpTourMatchLoader.loadTournament(1972, 'adelaide', 2013, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.deleteTournament(1972, '339') // Adelaide
			atpTourMatchLoader.deleteTournament(1972, '9036') // Rotterdam
			atpTourMatchLoader.deleteTournament(1972, '2060') // Sydney N.S.W.
			atpTourMatchLoader.loadTournament(1973, 'san-juan', 6817, false, 'B')
			atpTourMatchLoader.loadTournament(1973, 'caracas', 344, false, 'B')
			atpTourMatchLoader.loadTournament(1973, 'washington', 2040, false, 'B')
			atpTourMatchLoader.loadTournament(1973, 'manchester', 583, false, 'B', 'G', '1973-06-04', ['R32', 'R64'], 'Manchester')
			atpTourMatchLoader.loadTournament(1973, 'beckenham', 7310, false, 'B')
			atpTourMatchLoader.loadTournament(1973, 'hilton-head', 9340, false, 'B', 'P', '1973-09-10', [], 'World Invitational Tennis Classic')
			atpTourMatchLoader.loadTournament(1973, 'adelaide', 7308, false, 'B', null, null, ['R32', 'R64'])
			atpTourMatchLoader.deleteTournament(1973, '305') // Nice
			atpTourMatchLoader.deleteTournament(1973, '428') // Tokyo
			atpTourMatchLoader.loadTournament(1974, 'caracas', 344, false, 'B')
			atpTourMatchLoader.loadTournament(1974, 'beckenham', 7310, false, 'B')
			atpTourMatchLoader.loadTournament(1974, 'hilton-head', 9340, false, 'B', 'P', '1974-10-29', [], 'World Invitational Tennis Classic')
			atpTourMatchLoader.deleteTournament(1974, '2041') // Adelaide
			atpTourMatchLoader.deleteTournament(1974, '9037') // Tokyo
			atpTourMatchLoader.loadTournament(1975, 'hilton-head', 9340, false, 'B', 'C', '1975-10-20', [], 'World Invitational Tennis Classic')
			atpTourMatchLoader.loadTournament(1976, 'hilton-head', 9340, false, 'B', 'C', '1976-10-12', [], 'World Invitational Tennis Classic')
			atpTourMatchLoader.loadTournament(1977, 'hilton-head', 9340, false, 'B', 'C', '1977-09-26', [], 'World Invitational Tennis Classic')
			atpTourMatchLoader.loadTournament(1979, 'masters', 605, false, 'F', null, null, [], 'Masters')
			atpTourMatchLoader.loadTournament(1983, 'cleveland', 320, false, 'B')

			def xmlMatchLoader = new XMLMatchLoader(sql)
			xmlMatchLoader.loadFile('classpath:/tournaments/1968-paris-indoor.xml')
			xmlMatchLoader.loadFile('classpath:/tournaments/1976-wct-challenge-cup.xml')
			xmlMatchLoader.loadFile('classpath:/tournaments/1981-monte-carlo-masters+.xml')
			xmlMatchLoader.loadFile('classpath:/tournaments/1984-memphis+.xml')
			atpTourMatchLoader.deleteTournament(2003, '1536') // Madrid Masters
			xmlMatchLoader.loadFile('classpath:/tournaments/2003-madrid-masters.xml') // Reload: Matches missing stats
			atpTourMatchLoader.deleteTournament(2007, '505') // Vina del Mar
			xmlMatchLoader.loadFile('classpath:/tournaments/2007-vina-del-mar.xml') // Reload: Matches missing stats
			atpTourMatchLoader.deleteTournament(2008, '438') // Moscow
			xmlMatchLoader.loadFile('classpath:/tournaments/2008-moscow.xml') // Reload: Matches missing stats
			xmlMatchLoader.loadFile('classpath:/tournaments/2017-next-gen-finals.xml')
			xmlMatchLoader.loadFile('classpath:/tournaments/2019-next-gen-finals.xml')

			println()
		}
	}

	static loadAdditionalTournament(SqlPool sqlPool, String file) {
		sqlPool.withSql { sql ->
			new XMLMatchLoader(sql).loadFile(file)
		}
	}

	def correctDataFull(sql) {
		def stopwatch = Stopwatch.createStarted()
		print 'Correcting data (full)'
		executeSQLFile(sql, '/correct-data-full.sql')
		println " finished in $stopwatch"

		println 'Updating tournament event surfaces...'
		executeSQLFile(sql, '/tournament-event-surfaces.sql')

		println 'Loading team tournament winners...'
		executeSQLFile(sql, '/team-tournament-winners.sql')
	}

	def correctData(sql) {
		def stopwatch = Stopwatch.createStarted()
		print 'Correcting data (delta)'
		executeSQLFile(sql, '/correct-data-delta.sql')
		println " finished in $stopwatch"

		println 'Updating tournament event map properties...'
		executeSQLFile(sql, '/tournament-map-properties.sql')
	}

	def refreshMaterializedViews(Sql sql) {
		refreshMaterializedViews(sql,
			'event_participation',
			'player_tournament_event_result', 'player_titles', 'player_team_titles',
			'player_current_rank', 'player_best_rank', 'player_best_rank_points', 'player_year_end_rank',
			'player_current_elo_rank', 'player_best_elo_rank', 'player_best_elo_rating', 'player_season_best_elo_rating', 'player_year_end_elo_rank',
			'player_season_weeks_at_no1', 'player_weeks_at_no1', 'player_season_weeks_at_elo_topn', 'player_weeks_at_elo_topn', 'player_season_weeks_at_surface_elo_topn', 'player_weeks_at_surface_elo_topn',
			'player_season_performance', 'player_tournament_performance', 'player_performance',
			'player_season_surface_stats', 'player_season_stats', 'player_surface_stats', 'player_stats',
			'event_stats', 'player_h2h', 'title_difficulty',
			'player_win_streak', 'player_level_win_streak', 'player_best_of_win_streak', 'player_surface_win_streak', 'player_indoor_win_streak',
			'player_vs_no1_win_streak', 'player_vs_top5_win_streak', 'player_vs_top10_win_streak',
			'player_tournament_win_streak', 'player_tournament_level_win_streak',
			'player_season_goat_points', 'player_goat_points', 'player_surface_season_goat_points', 'player_surface_goat_points', 'player_tournament_goat_points'
		)
	}

	def refreshMaterializedViews(Sql sql, String... materializedViews) {
		def stopwatch = Stopwatch.createStarted()
		for (String materializedView : materializedViews)
			refreshMaterializedView(sql, materializedView)
		if (materializedViews.length > 1)
			println "Materialized views refreshed in $stopwatch"
	}

	def refreshMaterializedView(Sql sql, String viewName) {
		def stopwatch = Stopwatch.createStarted()
		print "Refreshing materialized view '$viewName'"
		withTx sql, { Sql s ->
			s.execute("REFRESH MATERIALIZED VIEW $viewName".toString())
		}
		println " finished in $stopwatch"
	}

	def installExtensions(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Installing extensions...'
		executeSQLFile(sql, '/create-extensions.sql')

		println "Extensions installed in $stopwatch"
	}

	def createDatabase(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Creating types...'
		executeSQLFile(sql, '/create-types.sql')

		println 'Creating tables...'
		executeSQLFile(sql, '/create-tables.sql')

		println 'Creating functions...'
		executeSQLFile(sql, '/create-functions.sql')

		println 'Creating views...'
		executeSQLFile(sql, '/create-views.sql')

		println 'Loading initial data...'
		executeSQLFile(sql, '/initial-load.sql')

		println 'Creating load functions...'
		executeSQLFile(sql, '/load-functions.sql')

		println "Database created in $stopwatch"
	}

	def dropDatabase(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Dropping load functions...'
		executeSQLFile(sql, '/drop-load-functions.sql')

		println 'Dropping views...'
		executeSQLFile(sql, '/drop-views.sql')

		println 'Dropping functions...'
		executeSQLFile(sql, '/drop-functions.sql')

		println 'Dropping tables...'
		executeSQLFile(sql, '/drop-tables.sql')

		println 'Dropping types...'
		executeSQLFile(sql, '/drop-types.sql')

		println "Database dropped in $stopwatch"
	}

	def vacuum(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Vacuuming tables and materialized views...'
		def tables = sql.rows('SELECT tablename FROM pg_tables WHERE schemaname IN (\'public\', \'tcb\') ORDER BY tablename')
			.collect { row -> row.tablename }
		def matViews = sql.rows('SELECT matviewname FROM pg_matviews WHERE schemaname IN (\'public\', \'tcb\') ORDER BY matviewname')
			.collect { row -> row.matviewname }
		tables.addAll matViews
		sql.connection.autoCommit = true
		try {
			tables.each { name ->
				sql.execute('VACUUM FULL VERBOSE ANALYSE ' + name)
			}
		}
		finally {
			sql.connection.autoCommit = false
		}

		println "Vacuuming finished in $stopwatch"
	}

	private executeSQLFile(Sql sql, String file) {
		withTx sql, { Sql s ->
			s.execute(getClass().getResourceAsStream(file).text)
		}
	}
}
