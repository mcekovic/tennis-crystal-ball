package org.strangeforest.tcb.stats.util;

import java.util.*;

public abstract class BootgridUtil {

	public static String getOrderBy(Map<String, String> params, Map<String, String> orderMap, OrderBy... defaultOrders) {
		String orderBy = null;
		for (Map.Entry<String, String> order : orderMap.entrySet()) {
			String sort = findSortBy(params, order.getKey(), order.getValue());
			if (sort != null) {
				if (orderBy != null)
					orderBy += ", " + sort;
				else
					orderBy = sort;
			}
		}
		for (OrderBy defaultOrder : defaultOrders) {
			if (orderBy != null) {
				if (!orderBy.contains(defaultOrder.getColumn()))
					orderBy += ", " + defaultOrder;
			}
			else
				orderBy = defaultOrder.toString();
		}
		return orderBy;
	}

	private static String findSortBy(Map<String, String> params, String attrName, final String columnName) {
		String sort = params.get("sort[" + attrName + "]");
		return sort != null ? columnName + " " + sort.toUpperCase() : null;
	}
}
