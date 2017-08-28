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
				if (!hasColumn(orderBy, defaultOrder))
					orderBy += ", " + defaultOrder;
			}
			else
				orderBy = defaultOrder.toString();
		}
		return orderBy;
	}

	private static String findSortBy(Map<String, String> params, String attrName, final String columnName) {
		String sort = getSort(params, attrName);
		return sort != null ? OrderBy.addSort(columnName, sort.toUpperCase()) : null;
	}

	private static String getSort(Map<String, String> params, String attrName) {
		return params.get("sort[" + attrName + "]");
	}

	private static boolean hasColumn(String orderBy, OrderBy order) {
		String column = order.getColumn();
		return orderBy.contains(column + ' ') || orderBy.contains(column + ',');
	}

	public static <T> Comparator<T> getComparator(Map<String, String> params, Map<String, Comparator<T>> orderMap, Comparator<T> defaultComparator) {
		Comparator<T> orderBy = null;
		for (Map.Entry<String, Comparator<T>> order : orderMap.entrySet())
			orderBy = chain(orderBy, findComparator(params, order.getKey(), order.getValue()));
		return chain(orderBy, defaultComparator);
	}

	private static <T> Comparator<T> findComparator(Map<String, String> params, String attrName, Comparator<T> comparator) {
		String sort = getSort(params, attrName);
		return sort != null ? (sort.toUpperCase().equals("DESC") ? comparator.reversed() : comparator ) : null;
	}

	private static <T> Comparator<T> chain(Comparator<T> c1, Comparator<T> c2) {
		return c2 != null ? (c1 != null ? c1.thenComparing(c2) : c2) : c1;
	}
}
