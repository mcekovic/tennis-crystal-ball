package org.strangeforest.tcb.model

import org.junit.*

class OutcomeCurveIT {

	@Test	@Ignore
	fun testCurve() {
		println("Point    Game     TieBreak Set      NoTB Set BestOf3  BestOf5  BestOf5TB")
		var p = 0.0
		while (p <= 1.0) {
			System.out.printf("%1\$f %2\$f %3\$f %4\$f %5\$f %6\$f %7\$f %8\$f\n",
				p,
				GameOutcome(p).pWin(),
				TieBreakOutcome(p, p).pWin(),
				SetOutcome(p, p).pWin(),
				SetOutcome(p, p, false).pWin(),
				MatchOutcome(p, p, 3).pWin(),
				MatchOutcome(p, p, 5).pWin(),
				MatchOutcome(p, p, 5, true).pWin()
			)
			p += 0.01
		}
	}
}
