package org.strangeforest.tcb.util;

import static java.lang.String.*;

public abstract class EnumUtil {

	public static IllegalArgumentException unknownEnum(Enum e) {
		return new IllegalArgumentException(format("Invalid %1$s value: %2$s", e.getClass().getName(), e));
	}
}
