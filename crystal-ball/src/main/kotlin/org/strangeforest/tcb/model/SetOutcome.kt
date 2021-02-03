package org.strangeforest.tcb.model

class SetOutcome (
	val pServe: Double,
	val pReturn: Double,
	val tieBreakAt: Int? = 6
) : DiffOutcome(6, 2, { gameNo -> if (gameNo % 2 == 0) GameOutcome(pServe).pWin() else GameOutcome(pReturn).pWin() }) {

	override fun pDeuce(p1: Double, p2: Double, items1: Int, items2: Int): Double =
		if (tieBreakAt == items1 && tieBreakAt == items2)
			TieBreakOutcome(pServe, pReturn).pWin()
		else
			super.pDeuce(p1, p2, items1, items2)
}