package org.strangeforest.tcb.util;

public abstract class ObjectUtil {

	public static <T extends Comparable<? super T>> int compare(T o1, T o2) {
		return o1 == o2 ? 0 : (o1 == null ? -1 : (o2 == null ? 1 : o1.compareTo(o2)));
	}
}
