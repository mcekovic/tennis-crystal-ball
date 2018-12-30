package org.strangeforest.tcb.model

class MatchOutcome @JvmOverloads constructor(
	pServe: Double,
	pReturn: Double,
	bestOf: Int,
	finalSetTieBreaker: Int? = finalSetTieBreaker(bestOf)
)
	: DiffOutcome(maxSets(bestOf), 1, { setNo -> SetOutcome(pServe, pReturn, if (setNo < maxSets(bestOf)) finalSetTieBreaker else 5).pWin() }
)
