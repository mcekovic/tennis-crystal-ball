package org.strangeforest.tcb.stats.model.records.details;

public abstract class RecordDetailUtil {

	public static String resultURLParam(String result) {
		StringBuilder sb = new StringBuilder();
		if (!result.equals("RR")) {
			sb.append("&result=").append(result);
			if (!result.equals("W"))
				sb.append("%2B");
		}
		return sb.toString();
	}
}
