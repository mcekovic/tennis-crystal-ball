package org.strangeforest.tcb.dataload

import org.jsoup.HttpStatusException
import org.junit.jupiter.api.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

class LoaderUtilTest {

	@Test
	void 'Retry test'() {
		def i = 0
		retry(5, 0L) {
			if (++i <= 2) throw new RuntimeException('Booom!!!', new HttpStatusException('Bad request', 401, ''))
		}
	}

	@Test
	void 'Collection tile test'() {
		assert tile([1])[0] == [1]

		assert tile([1, 2])[0] == [1]
		assert tile([1, 2])[1] == [2]

		assert tile([1, 2, 3])[0] == [1, 2]
		assert tile([1, 2, 3])[1] == [3]

		assert tile([1, 2, 3, 4])[0] == [1, 2]
		assert tile([1, 2, 3, 4])[1] == [3, 4]
	}
}
