package org.strangeforest.tcb.dataload

import org.jsoup.*

abstract class LoaderUtil {

	private static final int TIMEOUT = 30 * 1000
	private static final int RETRY_COUNT = 5
	private static final long RETRY_DELAY = 1000L

	static retriedGetDoc(String url) {
		retry(RETRY_COUNT, RETRY_DELAY, { th -> th instanceof HttpStatusException }, {
			Jsoup.connect(url).timeout(TIMEOUT).get()
		})
	}

	static retry(int count, long delay, Closure<Boolean> predicate, Closure<Object> closure) {
		for (int retry = 0; retry <= count; retry++) {
			try {
				return closure.call(retry)
			}
			catch (Throwable th) {
				th.printStackTrace()
				def rootCause = extractRootCause(th)
				if (retry < count && predicate.curry(rootCause)) {
					println "Exception occurred: ${rootCause} [retry ${retry + 1} follows]"
					Thread.sleep(delay)
				}
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
