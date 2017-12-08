package org.strangeforest.tcb.model

abstract class DiffOutcome	protected constructor(
	private val maxItems: Int,
	private val itemsDiff: Int,
	private val pItemWin: (Int) -> Double
) {

	fun pWin(): Double {
		return pWin(0, 0)
	}

	fun pWin(items1: Int, items2: Int): Double {
		if (items1 == maxItems) {
			val diff = items1 - items2
			return if (diff >= itemsDiff)
				1.0
			else {
				val nextItem = items1 + items2 + 1
				when (diff) {
					0 -> pDeuce(pItemWin(nextItem), pItemWin(nextItem + 1))
					1 -> {
						val p = pItemWin(nextItem)
						p + (1 - p) * pDeuce(pItemWin(nextItem + 1), pItemWin(nextItem + 2))
					}
					else -> throw IllegalStateException()
				}
			}
		}
		if (items2 >= maxItems) {
			val diff = items2 - items1
			return if (diff >= itemsDiff)
				0.0
			else {
				val nextItem = items1 + items2 + 1
				when (diff) {
					0 -> pDeuce(pItemWin(nextItem), pItemWin(nextItem + 1))
					1 -> {
						val p = pItemWin(nextItem)
						p * pDeuce(pItemWin(nextItem + 1), pItemWin(nextItem + 2))
					}
					else -> throw IllegalStateException()
				}
			}
		}
		val p = pItemWin(items1 + items2 + 1)
		return p * pWin(items1 + 1, items2) + (1 - p) * pWin(items1, items2 + 1)
	}

	protected open fun pDeuce(p1: Double, p2: Double): Double {
		val p12 = p1 * p2
		return p12 / (1.0 - p1 - p2 + 2 * p12)
	}
}
