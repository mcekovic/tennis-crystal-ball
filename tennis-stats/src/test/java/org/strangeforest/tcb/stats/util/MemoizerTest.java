package org.strangeforest.tcb.stats.util;

import java.util.function.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.strangeforest.tcb.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoizerTest {

	@Mock private Supplier<String> supplier;

	@Test
	void valueIsMemoized() {
		when(supplier.get()).thenReturn("Pera");
		Supplier<String> memoizer = Memoizer.of(supplier);

		assertThat(memoizer.get()).isEqualTo("Pera");
		verify(supplier).get();

		assertThat(memoizer.get()).isEqualTo("Pera");
		verifyNoMoreInteractions(supplier);
	}

	@Test
	void valueIsExpired() throws InterruptedException {
		when(supplier.get()).thenReturn("Pera");
		Supplier<String> memoizer = Memoizer.of(supplier, 1L);

		assertThat(memoizer.get()).isEqualTo("Pera");
		verify(supplier).get();

		Thread.sleep(2L);

		assertThat(memoizer.get()).isEqualTo("Pera");
		verify(supplier, times(2)).get();
	}

	@Test
	void valueIsMemoizedAndCleared() {
		when(supplier.get()).thenReturn("Pera");
		Memoizer<String> memoizer = Memoizer.of(supplier);

		assertThat(memoizer.get()).isEqualTo("Pera");
		verify(supplier).get();

		memoizer.clear();

		assertThat(memoizer.get()).isEqualTo("Pera");
		verify(supplier, times(2)).get();
	}
}
