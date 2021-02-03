package org.strangeforest.tcb.stats.model.records.details;

import static com.google.common.base.Strings.*;

public abstract class RecordDetailUtil {

	public static String resultURLParam(String result) {
		var sb = new StringBuilder();
		if (!(isNullOrEmpty(result) || result.equals("RR"))) {
			sb.append("&result=").append(result);
			if (!result.equals("W"))
				sb.append("%2B");
		}
		return sb.toString();
	}
}
