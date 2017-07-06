package org.strangeforest.tcb.model

class SetOutcome @JvmOverloads constructor(
	val pServe: Double,
	val pReturn: Double,
	val tieBreak: Boolean = true
)
	: DiffOutcome(6, 2, { gameNo -> if (gameNo % 2 == 0) GameOutcome(pServe).pWin() else GameOutcome(pReturn).pWin() }
) {

	override fun pDeuce(p1: Double, p2: Double): Double {
		if (tieBreak)
			return TieBreakOutcome(pServe, pReturn).pWin()
		else
			return super.pDeuce(p1, p2)
	}
}