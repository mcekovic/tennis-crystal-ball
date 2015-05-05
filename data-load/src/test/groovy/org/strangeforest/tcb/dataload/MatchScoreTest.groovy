package org.strangeforest.tcb.dataload

import org.junit.*

class MatchScoreTest {

	@Test
	void testBestOf3MatchScore() {
		MatchScore score = MatchScore.parse('6-3 0-6 7-5')

		assert score.outcome == null
		assert score.w_sets == 2
		assert score.l_sets == 1
		assert score.setScores.size() == 3
		assert score.setScores[0] == new SetScore(w_gems: 6, l_gems: 3)
		assert score.setScores[1] == new SetScore(w_gems: 0, l_gems: 6)
		assert score.setScores[2] == new SetScore(w_gems: 7, l_gems: 5)
	}

	@Test
	void testBestOf5MatchScore() {
		MatchScore score = MatchScore.parse('2-6 6-1 6-0 5-7 12-10')

		assert score.outcome == null
		assert score.w_sets == 3
		assert score.l_sets == 2
		assert score.setScores.size() == 5
		assert score.setScores[0] == new SetScore(w_gems: 2, l_gems: 6)
		assert score.setScores[1] == new SetScore(w_gems: 6, l_gems: 1)
		assert score.setScores[2] == new SetScore(w_gems: 6, l_gems: 0)
		assert score.setScores[3] == new SetScore(w_gems: 5, l_gems: 7)
		assert score.setScores[4] == new SetScore(w_gems: 12, l_gems: 10)
	}

	@Test
	void testMatchScoreWithTieBreak() {
		MatchScore score = MatchScore.parse('7-6(4) 6-7(0) 7-6(12)')

		assert score.outcome == null
		assert score.w_sets == 2
		assert score.l_sets == 1
		assert score.setScores.size() == 3
		assert score.setScores[0] == new SetScore(w_gems: 7, l_gems: 6, w_tb_pt: 7, l_tb_pt: 4)
		assert score.setScores[1] == new SetScore(w_gems: 6, l_gems: 7, w_tb_pt: 0, l_tb_pt: 7)
		assert score.setScores[2] == new SetScore(w_gems: 7, l_gems: 6, w_tb_pt: 14, l_tb_pt: 12)
	}

	@Test
	void testWalkOver() {
		MatchScore score = MatchScore.parse('W/O')

		assert score.outcome == 'W/O'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.setScores.isEmpty()
	}

	@Test
	void testWalkOver2() {
		MatchScore score = MatchScore.parse('RET')

		assert score.outcome == 'W/O'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.setScores.isEmpty()
	}

	@Test
	void testRetired() {
		MatchScore score = MatchScore.parse('6-4 3-0 RET')

		assert score.outcome == 'RET'
		assert score.w_sets == 1
		assert score.l_sets == 0
		assert score.setScores.size() == 2
		assert score.setScores[0] == new SetScore(w_gems: 6, l_gems: 4)
		assert score.setScores[1] == new SetScore(w_gems: 3, l_gems: 0)
	}

	@Test
	void testRetired2() {
		MatchScore score = MatchScore.parse('6-6 RET')

		assert score.outcome == 'RET'
		assert score.w_sets == 0
		assert score.l_sets == 0
		assert score.setScores.size() == 1
		assert score.setScores[0] == new SetScore(w_gems: 6, l_gems: 6)
	}
}
