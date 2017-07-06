package org.strangeforest.tcb.model

class MatchOutcome @JvmOverloads constructor(
	pServe: Double,
	pReturn: Double,
	bestOf: Int,
	finalSetTieBreaker: Boolean = finalSetTieBreaker(bestOf)
)
	: DiffOutcome(maxSets(bestOf), 1, { setNo -> SetOutcome(pServe, pReturn, finalSetTieBreaker || setNo < maxSets(bestOf)).pWin() }
)
