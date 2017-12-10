package org.strangeforest.tcb.util;

import java.util.*;

public abstract class ObjectUtil {

	public static <T> T nullIf(T obj, T value) {
		return Objects.equals(obj, value) ? null : obj;
	}
}
