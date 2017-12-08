package org.strangeforest.tcb.model

class SetOutcome @JvmOverloads constructor(
	private val pServe: Double,
	private val pReturn: Double,
	private val tieBreak: Boolean = true
)
	: DiffOutcome(6, 2, { gameNo -> if (gameNo % 2 == 0) GameOutcome(pServe).pWin() else GameOutcome(pReturn).pWin() }
) {

	override fun pDeuce(p1: Double, p2: Double): Double {
		return if (tieBreak)
			TieBreakOutcome(pServe, pReturn).pWin()
		else
			super.pDeuce(p1, p2)
	}
}