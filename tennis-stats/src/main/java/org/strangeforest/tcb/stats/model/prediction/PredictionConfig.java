package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;
import java.util.Optional;
import java.util.regex.*;
import java.util.stream.*;

import com.google.common.base.*;

public class PredictionConfig {

	private static volatile Properties props = new Properties();

	private static final Pattern AREA_PATTERN = Pattern.compile("area\\.\\w+");
	private static final Pattern ITEM_PATTERN = Pattern.compile("item\\.\\w+\\.\\w+");

	public static void set(Properties props) {
		for (String name : props.stringPropertyNames()) {
			String value = props.getProperty(name);
			if (Strings.isNullOrEmpty(value))
				continue;
			if (AREA_PATTERN.matcher(name).matches()) {
				String areaName = name.substring(5);
				double weight = Double.parseDouble(value);
				PredictionArea.valueOf(areaName).setWeight(weight);
			}
			else if (ITEM_PATTERN.matcher(name).matches()) {
				int pos = name.indexOf('.', 5);
				String areaName = name.substring(5, pos);
				String itemName = name.substring(pos + 1);
				double weight = Double.parseDouble(value);
				Stream.of(PredictionArea.valueOf(areaName).getItems()).filter(item -> item.toString().equals(itemName)).findFirst().get().setWeight(weight);
			}
		}
		PredictionConfig.props = props;
	}

	public static Optional<Integer> getIntegerProperty(String name) {
		String value = props.getProperty(name);
		return !Strings.isNullOrEmpty(value) ? Optional.of(Integer.parseInt(value)) : Optional.empty();
	}
}
