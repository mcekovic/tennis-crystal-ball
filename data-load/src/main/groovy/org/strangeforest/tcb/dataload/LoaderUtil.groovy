package org.strangeforest.tcb.dataload

abstract class LoaderUtil {

	static <E> List<Collection<E>> tile(Collection<E> col) {
		int size = col.size()
		if (size == 1)
			[col]
		else {
			def part1 = [], part2 = []
			def part1Size = size / 2
			def i = 0
			col.each { (++i <= part1Size ? part1 : part2) << it }
			[part1, part2]
		}
	}
}
