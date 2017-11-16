package org.strangeforest.tcb.dataload

import org.junit.*

class MatchScoreTest {

	@Test
	void "Test best-of-3 match score"() {
		MatchScore score = MatchScoreParser.parse('6-3 0-6 7-5')

		assert score.outcome == null
		assert score.w_sets == 2
		assert score.l_sets == 1
		assert score.w_games == 13
		assert score.l_games == 14
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 3
		assert score.setScores[0] == new SetScore(w_games: 6, l_games: 3)
		assert score.setScores[1] == new SetScore(w_games: 0, l_games: 6)
		assert score.setScores[2] == new SetScore(w_games: 7, l_games: 5)
		assert score.toString() == '6-3 0-6 7-5'
	}

	@Test
	void "Test best-of-5 match score"() {
		MatchScore score = MatchScoreParser.parse('2-6 6-1 6-0 5-7 12-10')

		assert score.outcome == null
		assert score.w_sets == 3
		assert score.l_sets == 2
		assert score.w_games == 31
		assert score.l_games == 24
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 5
		assert score.setScores[0] == new SetScore(w_games: 2, l_games: 6)
		assert score.setScores[1] == new SetScore(w_games: 6, l_games: 1)
		assert score.setScores[2] == new SetScore(w_games: 6, l_games: 0)
		assert score.setScores[3] == new SetScore(w_games: 5, l_games: 7)
		assert score.setScores[4] == new SetScore(w_games: 12, l_games: 10)
		assert score.toString() == '2-6 6-1 6-0 5-7 12-10'
	}

	@Test
	void "Test match score with tie-break"() {
		MatchScore score = MatchScoreParser.parse('7-6(4) 6-7(0) 7-6(12)')

		assert score.outcome == null
		assert score.w_sets == 2
		assert score.l_sets == 1
		assert score.w_games == 18
		assert score.l_games == 18
		assert score.w_tbs == 2
		assert score.l_tbs == 1
		assert score.setScores.size() == 3
		assert score.setScores[0] == new SetScore(w_games: 7, l_games: 6, w_tb_pt: 7, l_tb_pt: 4, w_tbs: 1)
		assert score.setScores[1] == new SetScore(w_games: 6, l_games: 7, w_tb_pt: 0, l_tb_pt: 7, l_tbs: 1)
		assert score.setScores[2] == new SetScore(w_games: 7, l_games: 6, w_tb_pt: 14, l_tb_pt: 12, w_tbs: 1)
		assert score.toString() == '7-6(4) 6-7(0) 7-6(12)'
	}

	@Test
	void "Test match score to 9 games"() {
		MatchScore score = MatchScoreParser.parse('6-3 8-9 9-8')

		assert score.outcome == null
		assert score.w_sets == 2
		assert score.l_sets == 1
		assert score.w_games == 23
		assert score.l_games == 20
		assert score.w_tbs == 1
		assert score.l_tbs == 1
		assert score.setScores.size() == 3
		assert score.setScores[0] == new SetScore(w_games: 6, l_games: 3)
		assert score.setScores[1] == new SetScore(w_games: 8, l_games: 9, l_tbs: 1)
		assert score.setScores[2] == new SetScore(w_games: 9, l_games: 8, w_tbs: 1)
		assert score.toString() == '6-3 8-9 9-8'
	}

	@Test
	void "Test match score 6-5 games"() {
		MatchScore score = MatchScoreParser.parse('6-5 5-6 6-4')

		assert score.outcome == null
		assert score.w_sets == 2
		assert score.l_sets == 1
		assert score.w_games == 17
		assert score.l_games == 15
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 3
		assert score.setScores[0] == new SetScore(w_games: 6, l_games: 5)
		assert score.setScores[1] == new SetScore(w_games: 5, l_games: 6)
		assert score.setScores[2] == new SetScore(w_games: 6, l_games: 4)
		assert score.toString() == '6-5 5-6 6-4'
	}

	@Test
	void "Test walk-over"() {
		MatchScore score = MatchScoreParser.parse('W/O')

		assert score.outcome == 'W/O'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.w_games == 0
		assert score.l_games == 0
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.isEmpty()
		assert score.toString() == 'W/O'
	}

	@Test
	void "Test walk-over 2"() {
		MatchScore score = MatchScoreParser.parse('RET')

		assert score.outcome == 'W/O'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.w_games == 0
		assert score.l_games == 0
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.isEmpty()
		assert score.toString() == 'W/O'
	}

	@Test
	void "Test walk-over 3"() {
		MatchScore score = MatchScoreParser.parse('(W/O)')

		assert score.outcome == 'W/O'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.w_games == 0
		assert score.l_games == 0
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.isEmpty()
		assert score.toString() == 'W/O'
	}

	@Test
	void "Test retired"() {
		MatchScore score = MatchScoreParser.parse('6-4 3-0 RET')

		assert score.outcome == 'RET'
		assert score.w_sets == 1
		assert score.l_sets == 0
		assert score.w_games == 9
		assert score.l_games == 4
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 2
		assert score.setScores[0] == new SetScore(w_games: 6, l_games: 4)
		assert score.setScores[1] == new SetScore(w_games: 3, l_games: 0)
		assert score.toString() == '6-4 3-0 RET'
	}

	@Test
	void "Test retired 2"() {
		MatchScore score = MatchScoreParser.parse('6-6 (RET)')

		assert score.outcome == 'RET'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.w_games == 6
		assert score.l_games == 6
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 1
		assert score.setScores[0] == new SetScore(w_games: 6, l_games: 6)
		assert score.toString() == '6-6 RET'
	}

	@Test
	void "Test retired 3"() {
		MatchScore score = MatchScoreParser.parse('7-5 RET')

		assert score.outcome == 'RET'
		assert score.w_sets == 1
		assert score.l_sets == 0
		assert score.w_games == 7
		assert score.l_games == 5
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 1
		assert score.setScores[0] == new SetScore(w_games: 7, l_games: 5)
		assert score.toString() == '7-5 RET'
	}

	@Test
	void "Test retired missing"() {
		MatchScore score = MatchScoreParser.parse('6-4 6-3 3-6 1-6 2-3')

		assert score.outcome == 'RET'
		assert score.w_sets == 2
		assert score.l_sets == 2
		assert score.w_games == 18
		assert score.l_games == 22
		assert score.w_tbs == 0
		assert score.l_tbs == 0
		assert score.setScores.size() == 5
		assert score.setScores[4] == new SetScore(w_games: 2, l_games: 3)
		assert score.toString() == '6-4 6-3 3-6 1-6 2-3 RET'
	}

	@Test
	void "Test score missing"() {
		MatchScore score = MatchScoreParser.parse('')

		assert score == null
	}
}
