package org.strangeforest.tcb.dataload

import java.text.*

import groovy.sql.*
import groovy.xml.*

class XMLTournamentExporter {

	static final String FETCH_TOURNAMENT_EVENT_SQL = //language=SQL
		'SELECT e.tournament_event_id, t.name tournament_name, e.name, e.date, e.level, e.surface, e.indoor, e.draw_type, e.draw_size\n' +
		'FROM tournament_event e\n' +
		'INNER JOIN tournament_mapping m USING (tournament_id)\n' +
		'INNER JOIN tournament t USING (tournament_id)\n' +
		'WHERE m.ext_tournament_id = :extId AND e.season = :season'

	static final String FETCH_TOURNAMENT_EVENT_PLAYERS_SQL = //language=SQL
		'SELECT DISTINCT m.winner_seed seed, m.winner_entry entry, pw.name, m.winner_country_id country_id\n' +
		'FROM match m\n' +
		'INNER JOIN player_v pw ON m.winner_id = pw.player_id\n' +
		'WHERE m.tournament_event_id = :tournamentEventId\n' +
		'UNION DISTINCT\n' +
	   'SELECT DISTINCT m.loser_seed seed, m.loser_entry entry, pl.name, m.loser_country_id country_id\n' +
		'FROM match m\n' +
		'INNER JOIN player_v pl ON m.loser_id = pl.player_id\n' +
		'WHERE m.tournament_event_id = :tournamentEventId\n' +
		'ORDER BY seed, entry, name'

	static final String FETCH_TOURNAMENT_EVENT_MATCHES_SQL = //language=SQL
		'SELECT m.match_id, m.match_num, m.round, m.best_of, pw.name winner, pl.name loser, m.score, m.outcome, m.has_stats\n' +
		'FROM match m\n' +
		'INNER JOIN player_v pw ON m.winner_id = pw.player_id\n' +
		'INNER JOIN player_v pl ON m.loser_id = pl.player_id\n' +
		'WHERE m.tournament_event_id = :tournamentEventId\n' +
		'ORDER BY m.round, m.match_num'

	static final String FETCH_MATCH_STATS_SQL = //language=SQL
		'SELECT minutes, w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n' +
		'                l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc\n' +
		'FROM match_stats m\n' +
		'WHERE match_id = :matchId'


	Sql sql

	XMLTournamentExporter(Sql sql) {
		this.sql = sql
	}

	def exportTournament(String extId, int season) {
		def event = sql.firstRow([extId: extId, season: season], FETCH_TOURNAMENT_EVENT_SQL)
		def players = sql.rows(FETCH_TOURNAMENT_EVENT_PLAYERS_SQL, [tournamentEventId: event.tournament_event_id])
		def matches = sql.rows(FETCH_TOURNAMENT_EVENT_MATCHES_SQL, [tournamentEventId: event.tournament_event_id])

		def writer = new FileWriter("data-load/src/main/resources/tournaments/${season}-${event.name.toLowerCase().replace(' ', '-')}.xml")
		def xml = new MarkupBuilder(writer)
		xml.doubleQuotes = true
		xml.omitNullAttributes = true
		xml.omitEmptyAttributes = true
		xml.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8')
		xml.'tournament-data'(xmlns: 'https://www.strangeforest.org/schema/tcb') {
			def date = formatDate(event.date)
			tournament(season: season, 'tournament-name': event.tournament_name, name: event.name, date: date, 'ext-id': extId) {
				level(event.level)
				surface(event.surface)
				indoor(event.indoor)
				'draw-type'(event.draw_type)
				'draw-size'(event.draw_size)
				for (def p : players)
					player(seed: p.seed, entry: p.entry, name: p.name, country: p.country_id)
				def round
				for (def m : matches) {
					def s = m.has_stats ? sql.firstRow([matchId: m.match_id], FETCH_MATCH_STATS_SQL) : [:]
					if (m.round != round) {
						round = m.round
						xml.mkp.yield '\n    '
						xml.mkp.comment round
					}
					match('match-num': m.match_num, round: m.round, 'best-of': m.best_of, winner: m.winner, loser: m.loser, score: m.score, outcome: m.outcome, minutes: s.minutes) {
						if (s) {
							'winner-stats'(ace: s.w_ace, df: s.w_df, 'sv-pt': s.w_sv_pt, 'fst-in': s.w_1st_in, 'fst-won': s.w_1st_won, 'snd-won': s.w_2nd_won, 'sv-gms': s.w_sv_gms, 'bp-sv': s.w_bp_sv, 'bp-fc': s.w_bp_fc)
							'loser-stats'(ace: s.l_ace, df: s.l_df, 'sv-pt': s.l_sv_pt, 'fst-in': s.l_1st_in, 'fst-won': s.l_1st_won, 'snd-won': s.l_2nd_won, 'sv-gms': s.l_sv_gms, 'bp-sv': s.l_bp_sv, 'bp-fc': s.l_bp_fc)
						}
					}
				}
			}
		}

		println "Tournament $extId for season $season exported."
	}

	private static String formatDate(Date date) {
		return new SimpleDateFormat('yyyy-MM-dd').format(date)
	}
}
