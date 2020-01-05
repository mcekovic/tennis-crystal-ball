package org.strangeforest.tcb.dataload

import java.sql.*

class MatchLoader extends BaseCSVLoader {

	MatchLoader(SqlPool sqlPool) {
		super(sqlPool)
	}

	int threadCount() { 1 }

	String loadSql() {
		XMLMatchLoader.LOAD_SQL
	}

	int batchSize() { 100 }

	Map params(record, Connection conn) {
		def params = [:]

		def tourneyId = string record.tourney_id
		if (!tourneyId) {
			System.err.println "WARN: Invalid record: $record"
			return null
		}
		if (tourneyId[4] != '-')
			throw new IllegalArgumentException("Invalid tourney_id: $tourneyId")
		def extTourneyId = string tourneyId.substring(5)
		if (extTourneyId.startsWith('0'))
			extTourneyId = extTourneyId.substring(1)
		def level = string record.tourney_level
		def name = string(record.tourney_name).trim()
		def season = smallint tourneyId.substring(0, 4)
		if (isInvalid(season, name))
			return null
		params.season = season
		def date = date record.tourney_date
		params.tournament_date = date
		params.date = date

		def drawSize = smallint record.draw_size
		def mappedLevel = mapLevel(level, name, season, extTourneyId)
		if (mappedLevel == 'C' || mappedLevel == 'U')
			return null
		def dcInfo = level == 'D' ? extractDCTournamentInfo(record, season) : null
		params.ext_tournament_id = mapExtTournamentId(season, extTourneyId, mappedLevel, dcInfo)
		def eventName = mapEventName(name, mappedLevel, season, dcInfo)
		params.tournament_name = mappedLevel != 'O' ? eventName : 'Olympics'
		params.event_name = eventName
		params.tournament_level = mappedLevel
		def surface = string record.surface
		params.surface = mapSurface surface
		params.indoor = mapIndoor(surface, eventName, season)
		params.draw_type = mapDrawType(mappedLevel, season, eventName)
		params.draw_size = mapDrawSize(drawSize, mappedLevel, season)
		params.rank_points = mapRankPoints mappedLevel

		def matchNum = record.match_num
		params.match_num = level != 'D' ? smallint(matchNum) : smallint(dcMatchNum(extTourneyId, season, matchNum))
		def round = string record.round
		params.round = level != 'D' ? mapRound(round) : dcInfo.round
		params.best_of = smallint record.best_of

		params.ext_winner_id = integer record.winner_id
		params.winner_seed = extractSeed(record, 'winner_')
		params.winner_entry = extractEntry(record, 'winner_')
		params.winner_rank = integer mapRank(record.winner_rank)
		params.winner_rank_points = integer record.winner_rank_points
		params.winner_age = real record.winner_age
		params.winner_country_id = country record.winner_ioc
		params.winner_name = string record.winner_name
		params.winner_height = smallint record.winner_ht
		params.winner_hand = hand record.winner_hand
		
		params.ext_loser_id = integer record.loser_id
		params.loser_seed = extractSeed(record, 'loser_')
		params.loser_entry = extractEntry(record, 'loser_')
		params.loser_rank = integer mapRank(record.loser_rank)
		params.loser_rank_points = integer record.loser_rank_points
		params.loser_age = real record.loser_age
		params.loser_country_id = country record.loser_ioc
		params.loser_name = string record.loser_name
		params.loser_height = smallint record.loser_ht
		params.loser_hand = hand record.loser_hand

		def score = record.score.trim()
		def matchScore = MatchScoreParser.parse(score)
		params.score = matchScore?.toString()
		params.outcome = matchScore?.outcome
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
		params.w_games = matchScore?.w_games
		params.l_games = matchScore?.l_games
		params.w_tbs = matchScore?.w_tbs
		params.l_tbs = matchScore?.l_tbs
		params.w_set_games = matchScore ? shortArray(conn, matchScore.w_set_games) : null
		params.l_set_games = matchScore ? shortArray(conn, matchScore.l_set_games) : null
		params.w_set_tb_pt = matchScore ? shortArray(conn, matchScore.w_set_tb_pt) : null
		params.l_set_tb_pt = matchScore ? shortArray(conn, matchScore.l_set_tb_pt) : null
		params.w_set_tbs = matchScore ? shortArray(conn, matchScore.w_set_tbs) : null
		params.l_set_tbs = matchScore ? shortArray(conn, matchScore.l_set_tbs) : null
		def bestOf = matchScore?.bestOf
		if (bestOf)
			params.best_of = bestOf
		
		params.minutes = smallint record.minutes

		params.w_ace = smallint record.w_ace
		params.w_df = smallint record.w_df
		params.w_sv_pt = smallint record.w_svpt
		params.w_1st_in = smallint record.w_1stIn
		params.w_1st_won = smallint record.w_1stWon
		params.w_2nd_won = smallint record.w_2ndWon
		params.w_sv_gms = smallint record.w_SvGms
		params.w_bp_sv = smallint record.w_bpSaved
		params.w_bp_fc = smallint record.w_bpFaced

		params.l_ace = smallint record.l_ace
		params.l_df = smallint record.l_df
		params.l_sv_pt = smallint record.l_svpt
		params.l_1st_in = smallint record.l_1stIn
		params.l_1st_won = smallint record.l_1stWon
		params.l_2nd_won = smallint record.l_2ndWon
		params.l_sv_gms = smallint record.l_SvGms
		params.l_bp_sv = smallint record.l_bpSaved
		params.l_bp_fc = smallint record.l_bpFaced

		short totalPoints = (params.w_sv_pt ?: 0) + (params.l_sv_pt ?: 0)
		if (totalPoints > 0 && matchScore) {
			short totalGames = (matchScore.w_games ?: 0) + (matchScore.l_games ?: 0)
			if (totalGames > 1) {
				if (params.w_sv_gms == 0)
					params.w_sv_gms = (totalGames / 2).shortValue()
				if (params.l_sv_gms == 0)
					params.l_sv_gms = (totalGames / 2).shortValue()
			}
		}

		return params
	}

	static isInvalid(int season, String name) {
		(season == 1971 && name == 'Toronto WCT')	||
		(season == 1982 && name == 'Itaparica')
	}

	static mapExtTournamentId(int season, String extTourneyId, String level, DavisCupTournamentInfo dcInfo) {
		switch (level) {
			case 'D': return dcInfo.extId
			case 'O': return level
			default: switch (extTourneyId) {
				case 'M001': return '338'
				case 'M004': return '807'
				case 'M006': return '404'
				case 'M007': return '403'
				case 'M009': return '416'
				case 'M010': return '440'
				case 'M014': return '438'
				case 'M015': return '747'
				case 'M016': return '741'
				case 'M020': return '339'
				case 'M021': return '1536'
				case 'M024': return '422'
				case 'M035': return '418'
				case 'M052': return '6932'
				case '306': return season == 2009 ? '319' : extTourneyId
				default:	return extTourneyId
			}
		}
	}

	static mapEventName(String name, String level, int season, DavisCupTournamentInfo dcInfo) {
		switch (level) {
			case 'G': switch (name) {
				case 'Us Open': return 'US Open'
				default: return name
			}
			case 'F': return season >= 2016 ? 'Tour Finals' : name
			case 'M': return season >= 1990 && !name.endsWith(' Masters') ? name + ' Masters' : name
			case 'O': return season == 2016 ? 'Rio Olympics' : name
			case 'D': return dcInfo.name
			default:
				if (name.equals('Santiago') && season in 2012..2013)
					return 'Vina del Mar'
				else if (name.equals('Cabo San Lucas') && season == 2018)
					return 'Los Cabos'
				else
					return name
		}
	}

	static mapLevel(String level, String name, int season, String extTournamentId) {
		switch (level) {
			case 'G': return 'G'
			case 'F':
				if (name.contains("WCT"))
					return 'A'
				else if (name.startsWith('Next Gen'))
					return 'H'
				else
					return 'F'
			case 'M':
				if (name.startsWith('Masters'))
					return 'F'
				else if (
					(name.equals('Cincinnati') && season in 1968..1980) ||
					(name.equals('Delray Beach') && season == 1985) ||
					(name.equals('Montreal / Toronto') && (season in 1969..1971 || season in 1976..1977)) ||
					(name.equals('Rome') && season in 1968..1969)
				)
					return 'B'
				else
					return 'M'
			case 'A':
				if (name.startsWith('Australian Open') && season == 1977)
					return 'G'
				else if (extTournamentId == '605' && season >= 2016)
					return 'F'
				else if ( // Alternative Tour Finals
					(name.equals('Tennis Champions Classic') && season in 1970..1971) || // Tennis Champions Classic
					(name.startsWith('Dallas') && extTournamentId == '610' && season in 1971..1989) || // WCT Finals
					(name.equals('Grand Slam Cup') && season in 1990..1999)
				)
					return 'L'
				else if (
					(name.startsWith('Boston') && season in 1970..1977) ||
					(name.startsWith('Forest Hills') && season in 1982..1985) ||
					(name.equals('Hamburg') && season in 1978..1989) ||
					(name.equals('Indianapolis') && season in 1974..1977) ||
					(name.startsWith('Johannesburg') && extTournamentId == '426' && season in 1972..1973)||
					(name.startsWith('Johannesburg') && extTournamentId == '254' && season == 1974)||
					(name.startsWith('Las Vegas') && season in 1972..1981 && extTournamentId == '413') ||
					(name.startsWith('Los Angeles') && season in 1970..1973 && extTournamentId == '423') ||
					(name.startsWith('Monte Carlo') && season in 1970..1989) ||
					(name.startsWith('Paris') && season == 1989) ||
					(name.startsWith('Philadelphia') && season in 1970..1986) ||
					(name.equals('Sydney Outdoor') && season == 1971) ||
					(name.equals('Stockholm') && (season in 1972..1980 || season in 1984..1989)) ||
					(name.equals('Stockholm Open') && season in 1970..1971) ||
					(name.equals('Tokyo Indoor') && season in 1978..1988) ||
					(name.startsWith('Washington') && extTournamentId.equals('418') && season in 1975..1977) ||
					(name.equals('Wembley') && (season in 1970..1971 || season in 1976..1983))
				)
					return 'M'
				else if (name.contains('Olympics'))
					return 'O'
				else if (
					// Real ATP 500
					(name.equals('Acapulco') && season >= 2001) ||
					(name.equals('Antwerp') && season in 1996..1998) ||
					(name.equals('Barcelona') && season >= 1990) ||
					(name.equals('Basel') && season >= 2009) ||
					(name.equals('Beijing') && season >= 2009) ||
					(name.equals('Brussels') && season in 1990..1992) ||
					(name.equals('Dubai') && season >= 2001) ||
					(name.equals('Halle') && season >= 2015) ||
					(name.equals('Hamburg') && season >= 2009) ||
					(name.equals('Indianapolis') && season in 1990..2002) ||
					(name.equals('Kitzbuhel') && season in 1999..2008) ||
					(name.equals('London') && (season in 1998..2000 || season >= 2018)) ||
					(name.equals('Memphis') && (season in 1990..1992 || season in 1994..2013)) ||
					(name.equals('Mexico City') && season == 2000) ||
					(name.equals('Milan') && season in 1991..1997) ||
					(name.equals('New Haven') && season in 1990..1998) ||
					(name.equals('Queen\'s Club') && (season in 2015..2017)) ||
					(name.equals('Philadelphia') && season in 1990..1998) ||
					(name.equalsIgnoreCase('Rio de Janeiro') && season >= 2014) ||
					(name.equals('Rotterdam') && season >= 1999) ||
					(name.equals('Singapore') && season in 1997..1999) ||
					(name.equals('Stuttgart Outdoor') && season in 1990..2000) ||
					(name.equals('Stuttgart') && (season == 2001 || season in 2003..2008)) ||
					(name.equals('Stuttgart Indoor') && season in 1990..1995) ||
					(name.equals('Sydney Indoor') && season in 1990..1994) ||
					(name.equals('Tokyo') && season >= 1996) ||
					(name.equals('Tokyo Indoor') && season in 1990..1995) ||
					(name.equals('Tokyo Outdoor') && season in 1990..1995) ||
					(name.equals('Toronto Indoor') && season == 1990) ||
					(name.equals('Valencia') && season in 2009..2014) ||
					(name.equals('Vienna') && (season in 1996..2008 || season >= 2015)) ||
					(name.equals('Washington') && (season in 1990..2002 || season >= 2009)) ||
					 // Other distinguished tournaments
					(name.equals('Pepsi Grand Slam') && season in 1976..1981) ||
					(name.equals('WCT Challenge Cup') && season in 1976..1980) ||
					(name.equals('Rome WCT') && season == 1972) ||
					(name.equals('Naples Finals WCT') && season == 1982) ||
					(name.equals('Detroit WCT') && season == 1983)
				)
					return 'A'
				else if (
					(name.equals('Dusseldorf') && extTournamentId == '615') ||
					(name.equals('Nations Cup') && extTournamentId == '615')
				)
					return 'T'
				else if (name.equals('Barranquilla'))
					return 'H'
				else
					return 'B'
			case 'D': return 'D'
			case 'C': return 'C'
			default: throw new IllegalArgumentException("Unknown tournament level: $level")
		}
	}

	static mapSurface(String surface) {
		switch (surface) {
			case 'Hard': return 'H'
			case 'Clay': return 'C'
			case 'Grass': return 'G'
			case 'Carpet': return 'P'
			default: return null
		}
	}

	static mapIndoor(String surface, String name, int season) {
		BaseATPTourTournamentLoader.mapIndoor(mapSurface(surface), name, season)
	}

	static mapDrawType(String level, int season, String name) {
		switch (level) {
			case 'F': return season in 1982..1985 ? 'KO' : 'RR'
			case 'H': return name == 'Next Gen Finals' ? 'RR' : 'KO'
			default: return 'KO'
		}
	}

	static Short mapDrawSize(Short drawSize, String level, int season) {
		switch (level) {
			case 'F': return season >= 1987 ? 8 : drawSize
			default: return drawSize
		}
	}

	static mapRound(String round) {
		switch (round) {
			case 'R63': return 'R64'
			case 'R31': return 'R32'
			case 'R15': return 'R16'
			case 'R7': return 'QF'
			case 'R3': return 'SF'
			case 'R1': return 'F'
			default: return round
		}
	}

	static extractSeed(def record, String prefix) {
		def seed = record[prefix + 'seed']?.toString()
		isNumeric(seed) ? smallint(seed) : null
	}

	static extractEntry(def record, String prefix) {
		def entry = mapEntry(string(record[prefix + 'entry']))
		if (entry)
			return entry
		def seed = record[prefix + 'seed']?.toString()
		seed && !isNumeric(seed) ? mapEntry(string(seed)) : null
	}

	static boolean isNumeric(String s) {
		if (!s)
			return false
		try {
			Integer.parseInt(s)
			return true
		}
		catch (NumberFormatException ex) {
			return false
		}
	}

	static mapEntry(String entry) {
		if (entry) {
			switch (entry) {
				case 'S': return 'SE'
				case 'Alt':
				case 'ALT': return 'AL'
			}
		}
		entry
	}

	static mapRank(String rank) {
		switch (rank) {
			case 'UNR': return ''
			default: return rank
		}
	}

	static mapRankPoints(String level) {
		switch (level) {
			case 'G': return 2000
			case 'F': return 1500
			case 'M': return 1000
			case 'O': return 750
			default: return null
		}
	}


	// Davis Cup

	static DavisCupTournamentInfo extractDCTournamentInfo(match, int season) {
		String name = match.tourney_name
		String[] parts = name.split(' ')
		if (parts.length < 4)
			throw new IllegalArgumentException("Invalid Davis Cup tournament name: $name")
		def group = parts[2]
		def round = parts[3]
		if (round.endsWith(':'))
			round = round.substring(0, round.length() - 1)
		new DavisCupTournamentInfo(extId: 'D' + group, name: 'Davis Cup ' + group, round: mapDCRound(round, group, match, season))
	}

	static mapDCRound(String round, String group, match, int season) {
		switch (season) {
			case 1968:
				if (isDCTieBetween(match, 'USA', 'AUS')) return 'F'
				if (isDCTieBetween(match, 'IND', 'SRI')) return 'RR'
				if (isDCTieBetween(match, 'USA', 'IND')) return 'RR'
				break
			case 1969:
				if (isDCTieBetween(match, 'USA', 'ROU')) return 'F'
				if (isDCTieBetween(match, 'IND', 'SRI')) return 'RR'
				break
			case 1970:
				if (isDCTieBetween(match, 'USA', 'GER')) return 'F'
				if (isDCTieBetween(match, 'IND', 'SRI')) return 'RR'
				if (isDCTieBetween(match, 'AUS', 'IND')) return 'RR'
				break
			case 1971:
				if (isDCTieBetween(match, 'USA', 'ROU')) return 'F'
				if (isDCTieBetween(match, 'IND', 'SRI')) return 'RR'
				break
			case 1972:
				if (isDCTieBetween(match, 'AUS', 'IND')) return 'RR'
				if (isDCTieBetween(match, 'IND', 'SRI')) return 'RR'
				break
			case 1973:
				if (isDCTieBetween(match, 'AUS', 'IND')) return 'SF'
				break
			case 1974:
				if (isDCTieBetween(match, 'AUS', 'IND')) return 'RR'
				break
			case 1980:
				if (isDCTieBetween(match, 'USA', 'ITA')) return 'SF'
				break
			case 1985:
				if (isDCTieBetween(match, 'NZL', 'KOR')) return 'RR'
				if (isDCTieBetween(match, 'GBR', 'KOR')) return 'RR'
				break
		}
		switch (round) {
			case 'F': return 'F'
			case 'SF': return 'SF'
			case 'QF': return 'QF'
			case 'R1': return group == 'WG' ? 'R16' : 'RR'
			default: return 'RR'
		}
	}

	static final Map dcMatchNumMap = [:]

	static synchronized dcMatchNum(int season) {
		def matchNum = dcMatchNumMap[season]
		matchNum = matchNum ? ++matchNum : 1
		dcMatchNumMap[season] = matchNum
	}

	static dcMatchNum(String extTourneyId, int season, String matchNum) {
		if (extTourneyId.startsWith('D'))
			extTourneyId = extTourneyId.substring(1)
		if (extTourneyId.startsWith('M-DC'))
			10 * dcMatchNum(season) + matchNum
		else
			extTourneyId + matchNum
	}

	static class DavisCupTournamentInfo {
		String extId
		String name
		String round
	}

	static isDCTieBetween(match, String country1, String country2) {
		String winnerCountry = country match.winner_ioc
		String loserCountry = country match.loser_ioc
		(winnerCountry == country1 && loserCountry == country2) || (winnerCountry == country2 && loserCountry == country1)
	}

	static final SAME_TOURNAMENT_MAP = [
		'2013': '393',
		'2041': '393',
		'6116': '409',
		'0891': '891',
		'0451': '451',
		'3937': '301',
		'1720': '334',
		'3944': '316',
		'712': '650',
		'725': '650',
		'6718': '359',
		'417': '80',
		'3943': '80',
		'3938': '347',
		'3942': '313',
		'506': '303',
		'3939': '344',
		'2063': '360',
		'803': '499',
		'805': '437',
		'661': '615',
		'6710': '615',
		'7290': '468',
		'820': '405',
		'7163': '2027',
		'5012': '426',
	]
}
