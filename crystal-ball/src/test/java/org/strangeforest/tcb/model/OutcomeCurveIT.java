package org.strangeforest.tcb.model;

import org.junit.*;

public class OutcomeCurveIT {

	@Test @Ignore
	public void testCurve() {
		System.out.println("Point    Game     TieBreak Set      NoTB Set BestOf3  BestOf5  BestOf5TB");
		for (double p = 0.0; p <= 1.0; p += 0.01) {
			System.out.printf("%1$f %2$f %3$f %4$f %5$f %6$f %7$f %8$f\n",
				p,
				new GameOutcome(p).pWin(),
				new TieBreakOutcome(p, p).pWin(),
				new SetOutcome(p, p).pWin(),
				new SetOutcome(p, p, false).pWin(),
				new MatchOutcome(p, p, 3).pWin(),
				new MatchOutcome(p, p, 5).pWin(),
				new MatchOutcome(p, p, 5, true).pWin()
			);
		}
	}
}
