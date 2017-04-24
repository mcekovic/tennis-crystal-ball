package org.strangeforest.tcb.dataload

abstract class LoaderUtil {

	static retry(int count, Closure<Boolean> predicate, Closure<?> closure) {
		for (int i = 0; i <= count; i++) {
			try {
				return closure.run()
			}
			catch (Throwable th) {
				def rootCause = extractRootCause(th)
				if (i < count && predicate.curry(rootCause))
					println "Exception occurred: ${rootCause} [retry ${i + 1} follows]"
				else
					throw th
			}
		}
	}

	static List<Throwable> getThrowableList(Throwable th) {
		def ths = []
		for (Throwable t = th; t && !(t in ths); t = t.getCause())
			ths << t
		ths
	}

	static Throwable extractRootCause(Throwable th) {
		def ths = getThrowableList(th)
		ths ? ths[ths.size() - 1] : null
	}

	static <E> List<Collection<E>> tile(Collection<E> col) {
		int size = col.size()
		if (size <= 1)
			[col]
		else {
			def part1 = [], part2 = []
			def part1Size = (size + 1) / 2
			def i = 0
			col.each { (++i <= part1Size ? part1 : part2) << it }
			[part1, part2]
		}
	}
}
