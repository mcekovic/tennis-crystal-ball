package org.strangeforest.tcb.util;

import org.strangeforest.tcb.stats.util.*;

import static java.lang.String.*;

public abstract class EnumUtil {

	public static InvalidArgumentException unknownEnum(Enum e) {
		return new InvalidArgumentException(format("Invalid %1$s value: %2$s", e.getClass().getName(), e));
	}
}
