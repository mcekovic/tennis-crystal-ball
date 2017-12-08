package org.strangeforest.tcb.model

fun maxSets(bestOf: Int): Int {
	return when (bestOf) {
		3 -> 2
		5 -> 3
		else -> throw IllegalStateException()
	}
}

fun finalSetTieBreaker(bestOf: Int): Boolean {
	return when (bestOf) {
		3 -> true
		5 -> false
		else -> throw IllegalStateException()
	}
}


