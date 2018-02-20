package org.strangeforest.tcb.util;

import java.util.*;
import java.util.function.*;

import static java.lang.Integer.max;
import static java.util.Collections.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

public abstract class CompareUtil {

	private static final Comparator NULLS_LAST_COMPARATOR = nullsLast(naturalOrder());

	public static <T extends Comparable<T>> int nullsLastCompare(T t1, T t2) {
		return Objects.compare(t1, t2, NULLS_LAST_COMPARATOR);
	}

	public static <T extends Comparable<T>> int compareLists(List<T> list1, List<T> list2) {
		int size1 = list1.size();
		int size2 = list2.size();
		for (int i = 0, count = max(size1, size2); i < count; i++) {
			T item1 = i < size1 ? list1.get(i) : null;
			T item2 = i < size2 ? list2.get(i) : null;
			int result = nullsLastCompare(item1, item2);
			if (result != 0)
				return result;
		}
		return 0;
	}

	public static <T, R> List<R> mapList(List<T> items, Function<? super T, ? extends R> mapper) {
		return items.stream().map(mapper).collect(toList());
	}

	public static <T, R extends Comparable> List<R> mapSortList(List<T> items, Function<? super T, ? extends R> mapper) {
		List<R> list = mapList(items, mapper);
		sort(list);
		return list;
	}
}
