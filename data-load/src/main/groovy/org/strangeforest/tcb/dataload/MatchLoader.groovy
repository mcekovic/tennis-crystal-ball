package org.strangeforest.tcb.dataload

import java.sql.*

class MatchLoader extends BaseCSVLoader {

	MatchLoader(SqlPool sqlPool) {
		super(sqlPool)
	}

	int threadCount() { 1 }

	String loadSql() {
		'{call load_match(' +
			':ext_tournament_id, :season, :tournament_date, :tournament_name, :event_name, :tournament_level, :surface, :indoor, :draw_type, :draw_size, :rank_points, ' +
			':match_num, :date, :round, :best_of, ' +
			':ext_winner_id, :winner_seed, :winner_entry, :winner_rank, :winner_rank_points, :winner_age, :winner_country_id, :winner_name, :winner_height, :winner_hand, ' +
			':ext_loser_id, :loser_seed, :loser_entry, :loser_rank, :loser_rank_points, :loser_age, :loser_country_id, :loser_name, :loser_height, :loser_hand, ' +
			':score, :outcome, :w_sets, :l_sets, :w_games, :l_games, :w_set_games, :l_set_games, :w_set_tb_pt, :l_set_tb_pt, :minutes, ' +
			':w_ace, :w_df, :w_sv_pt, :w_1st_in, :w_1st_won, :w_2nd_won, :w_sv_gms, :w_bp_sv, :w_bp_fc, ' +
			':l_ace, :l_df, :l_sv_pt, :l_1st_in, :l_1st_won, :l_2nd_won, :l_sv_gms, :l_bp_sv, :l_bp_fc' +
		')}'
	}

	int batchSize() { 100 }

	Map params(record, Connection conn) {
		def params = [:]

		def tourneyId = string record.tourney_id
		if (tourneyId[4] != '-')
			throw new IllegalArgumentException("Invalid tourney_id: $tourneyId")
		def extTourneyId = string tourneyId.substring(5)
		if (extTourneyId.startsWith('0'))
			extTourneyId = extTourneyId.substring(1)
		def level = string record.tourney_level
		def name = string record.tourney_name
		def season = smallint tourneyId.substring(0, 4)
		params.season = season
		def date = date record.tourney_date
		params.tournament_date = date
		params.date = date

		def drawSize = smallint record.draw_size
		def mappedLevel = mapLevel(level, drawSize, name, season, extTourneyId)
		if (mappedLevel == 'C' || mappedLevel == 'U')
			return null
		def dcInfo = level == 'D' ? extractDCTournamentInfo(record, season) : null
		params.ext_tournament_id = mapExtTournamentId(extTourneyId, mappedLevel, dcInfo)
		def eventName = mapEventName(name, mappedLevel, season, dcInfo)
		params.tournament_name = mappedLevel != 'O' ? eventName : 'Olympics'
		params.event_name = eventName
		params.tournament_level = mappedLevel
		def surface = string record.surface
		params.surface = mapSurface surface
		params.indoor = mapIndoor surface
		params.draw_type = mapDrawType(mappedLevel, season)
		params.draw_size = mapDrawSize(drawSize, mappedLevel, season)
		params.rank_points = mapRankPoints mappedLevel

		def matchNum = record.match_num
		params.match_num = level != 'D' ? smallint(matchNum) : smallint(dcMatchNum(extTourneyId, season, matchNum))
		def round = string record.round
		params.round = level != 'D' ? mapRound(round) : dcInfo.round
		params.best_of = smallint record.best_of

		params.ext_winner_id = integer record.winner_id
		params.winner_seed = smallint record.winner_seed
		params.winner_entry = mapEntry(string(record.winner_entry))
		params.winner_rank = integer mapRank(record.winner_rank)
		params.winner_rank_points = integer record.winner_rank_points
		params.winner_age = real record.winner_age
		params.winner_country_id = country record.winner_ioc
		params.winner_name = string record.winner_name
		params.winner_height = smallint record.winner_ht
		params.winner_hand = hand record.winner_hand
		
		params.ext_loser_id = integer record.loser_id
		params.loser_seed = smallint record.loser_seed
		params.loser_entry = mapEntry(string(record.loser_entry))
		params.loser_rank = integer mapRank(record.loser_rank)
		params.loser_rank_points = integer record.loser_rank_points
		params.loser_age = real record.loser_age
		params.loser_country_id = country record.loser_ioc
		params.loser_name = string record.loser_name
		params.loser_height = smallint record.loser_ht
		params.loser_hand = hand record.loser_hand

		def score = record.score.trim()
		def matchScore = MatchScoreParser.parse(score)
		params.score = string score
		params.outcome = matchScore?.outcome
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
		params.w_games = matchScore?.w_games
		params.l_games = matchScore?.l_games
		params.w_set_games = matchScore ? shortArray(conn, matchScore.w_set_games) : null
		params.l_set_games = matchScore ? shortArray(conn, matchScore.l_set_games) : null
		params.w_set_tb_pt = matchScore ? shortArray(conn, matchScore.w_set_tb_pt) : null
		params.l_set_tb_pt = matchScore ? shortArray(conn, matchScore.l_set_tb_pt) : null

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

	static mapExtTournamentId(String extTourneyId, String level, DavisCupTournamentInfo dcInfo) {
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
				case 'M020': return '339'
				case 'M021': return '1536'
				case 'M024': return '422'
				case 'M035': return '418'
				case 'M052': return '6932'
				default:	return extTourneyId
			}
		}
	}

	static mapEventName(String name, String level, int season, DavisCupTournamentInfo dcInfo) {
		switch (level) {
			case 'F': return season == 2016 ? 'Tour Finals' : name
			case 'M': return season >= 1990 && !name.endsWith(' Masters') ? name + ' Masters' : name
			case 'O': return season == 2016 ? 'Rio Olympics' : name
			case 'D': return dcInfo.name
			default: return name
		}
	}

	static mapLevel(String level, short drawSize, String name, int season, String extTournamentId) {
		switch (level) {
			case 'G': return 'G'
			case 'F': return name.contains("WCT") ? 'A' : 'F'
			case 'M':
				if (name.startsWith('Masters') && drawSize <= 16)
					return 'F'
				else if (
					(name.equals('Cincinnati') && (1968..1980).contains(season)) ||
					(name.equals('Delray Beach') && season == 1985) ||
					(name.equals('Montreal / Toronto') && ((1969..1971).contains(season) || (1976..1977).contains(season))) ||
					(name.equals('Rome') && (1968..1969).contains(season))
				)
					return 'B'
				else
					return 'M'
			case 'A':
				if (name == 'London' && season == 2016)
					return 'F'
				else if (name.contains('Olympics'))
					return 'O'
				else if (name.startsWith('Australian Open') && season == 1977)
					return 'G'
				else if (
					(name.startsWith('Boston') && (1970..1977).contains(season)) ||
					(name.startsWith('Forest Hills') && (1982..1985).contains(season)) ||
					(name.equals('Hamburg') && (1978..1989).contains(season)) ||
					(name.equals('Indianapolis') && (1974..1977).contains(season)) ||
					(name.startsWith('Johannesburg') && extTournamentId == '426' && (1972..1973).contains(season))||
					(name.startsWith('Johannesburg') && extTournamentId == '254' && season == 1974)||
					(name.startsWith('Las Vegas') && (1972..1981).contains(season) && drawSize >= 32) ||
					(name.startsWith('Los Angeles') && (1970..1973).contains(season) && drawSize >= 64) ||
					(name.startsWith('Monte Carlo') && (1970..1989).contains(season)) ||
					(name.startsWith('Paris') && season == 1989) ||
					(name.startsWith('Philadelphia') && (1970..1986).contains(season)) ||
					(name.equals('Sydney Outdoor') && season == 1971) ||
					(name.equals('Stockholm') && ((1972..1980).contains(season) || (1984..1989).contains(season))) ||
					(name.equals('Stockholm Open') && (1970..1971).contains(season)) ||
					(name.equals('Tokyo Indoor') && (1978..1988).contains(season)) ||
					(name.startsWith('Washington') && extTournamentId.equals('418') && (1975..1977).contains(season)) ||
					(name.equals('Wembley') && ((1970..1971).contains(season) || (1976..1983).contains(season)))
				)
					return 'M'
				else if (
					name.equals('Barranquilla')
				)
					return 'H'
				else if (
					(name.equals('Dusseldorf') && extTournamentId == '615') ||
					(name.equals('Nations Cup') && extTournamentId == '615')
				)
					return 'T'
				else if (
					// Real ATP 500
					(name.equals('Acapulco') && (2001..2016).contains(season)) ||
					(name.equals('Antwerp') && (1996..1998).contains(season)) ||
					(name.equals('Barcelona') && (1990..2016).contains(season)) ||
					(name.equals('Basel') && (2009..2016).contains(season)) ||
					(name.equals('Beijing') && (2009..2016).contains(season)) ||
					(name.equals('Brussels') && (1990..1992).contains(season)) ||
					(name.equals('Dubai') && (2001..2016).contains(season)) ||
					(name.equals('Halle') && (2015..2016).contains(season)) ||
					(name.equals('Hamburg') && (2009..2016).contains(season)) ||
					(name.equals('Indianapolis') && (1990..2002).contains(season)) ||
					(name.equals('Kitzbuhel') && (1999..2008).contains(season)) ||
					(name.equals('London') && ((1998..2000).contains(season) || (2015..2016).contains(season))) ||
					(name.equals('Memphis') && ((1990..1992).contains(season) || (1994..2013).contains(season))) ||
					(name.equals('Mexico City') && season == 2000) ||
					(name.equals('Milan') && (1991..1997).contains(season)) ||
					(name.equals('New Haven') && (1990..1998).contains(season)) ||
					(name.equals('Philadelphia') && (1990..1998).contains(season)) ||
					(name.equals('Rio de Janeiro') && (2014..2016).contains(season)) ||
					(name.equals('Rotterdam') && (1999..2016).contains(season)) ||
					(name.equals('Singapore') && (1997..1999).contains(season)) ||
					(name.equals('Stuttgart Outdoor') && (1990..2000).contains(season)) ||
					(name.equals('Stuttgart') && (season == 2001 || (2003..2008).contains(season))) ||
					(name.equals('Stuttgart Indoor') && (1990..1995).contains(season)) ||
					(name.equals('Sydney Indoor') && (1990..1994).contains(season)) ||
					(name.equals('Tokyo') && (1996..2016).contains(season)) ||
					(name.equals('Tokyo Indoor') && (1990..1995).contains(season)) ||
					(name.equals('Tokyo Outdoor') && (1990..1995).contains(season)) ||
					(name.equals('Toronto Indoor') && season == 1990) ||
					(name.equals('Valencia') && (2009..2014).contains(season)) ||
					(name.equals('Vienna') && ((1996..2008).contains(season) || (2015..2016).contains(season))) ||
					(name.equals('Washington') && ((1990..2002).contains(season) || (2009..2016).contains(season))) ||
					 // Other distinguished tournaments
					(name.startsWith('Dallas') && extTournamentId == '610' && (1971..1989).contains(season)) || // WCT Finals
					(name.equals('Grand Slam Cup') && (1990..1999).contains(season)) ||
					(name.equals('Pepsi Grand Slam') && (1976..1981).contains(season)) ||
					(name.equals('WCT Challenge Cup') && (1976..1980).contains(season))
				)
					return 'A'
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

	static mapIndoor(String surface) {
		switch (surface) {
			case 'Carpet': return true
			default: return false
		}
	}

	static mapDrawType(String level, int season) {
		switch (level) {
			case 'F': return (1982..1985).contains(season) ? 'KO' : 'RR'
			default: return 'KO'
		}
	}

	static short mapDrawSize(short drawSize, String level, int season) {
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

	static mapEntry(String entry) {
		if (entry) {
			if (entry == 'S')
				return 'SE'
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
