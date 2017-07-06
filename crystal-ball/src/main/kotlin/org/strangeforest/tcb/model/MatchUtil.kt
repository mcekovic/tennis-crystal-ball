package org.strangeforest.tcb.model

fun maxSets(bestOf: Int): Int {
	when (bestOf) {
		3 -> return 2
		5 -> return 3
		else -> throw IllegalStateException()
	}
}

fun finalSetTieBreaker(bestOf: Int): Boolean {
	when (bestOf) {
		3 -> return true
		5 -> return false
		else -> throw IllegalStateException()
	}
}


