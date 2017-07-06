package org.strangeforest.tcb.model

class TieBreakOutcome(
	pServe: Double,
	pReturn: Double
)
	: DiffOutcome(7, 2, { pointNo -> if (pointNo / 2 % 2 == 0) pServe else pReturn }
)
