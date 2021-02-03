package org.strangeforest.tcb.model

fun maxSets(bestOf: Int): Int = when (bestOf) {
	3 -> 2
	5 -> 3
	else -> throw IllegalStateException()
}

fun finalSetTieBreaker(bestOf: Int): Int? = when (bestOf) {
	3 -> 6
	5 -> null
	else -> throw IllegalStateException()
}


