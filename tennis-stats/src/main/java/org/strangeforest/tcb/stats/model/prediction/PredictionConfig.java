package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;
import java.util.Optional;
import java.util.regex.*;
import java.util.stream.*;

import com.google.common.base.*;

//TODO Make PredictionConfig instantiable class, automatically add equivalent config results
public class PredictionConfig {

	private static volatile Properties config = new Properties();

	private static final Pattern AREA_PATTERN = Pattern.compile("area\\.\\w+");
	private static final Pattern ITEM_PATTERN = Pattern.compile("item\\.\\w+\\[\\w+\\]");

	public static Properties get() {
		Properties config = new Properties(PredictionConfig.config);
		for (PredictionArea area : PredictionArea.values()) {
			setProperty(config, "area." + area, String.valueOf(area.getWeight()));
			for (PredictionItem item : area.getItems())
				setProperty(config, "item." + item, String.valueOf(item.getWeight()));
		}
		return config;
	}

	private static void setProperty(Properties config, String key, String value) {
		config.setProperty(key.intern(), value.intern());
	}

	public static void set(Properties config) {
		for (String name : config.stringPropertyNames()) {
			String value = config.getProperty(name);
			if (Strings.isNullOrEmpty(value))
				continue;
			if (AREA_PATTERN.matcher(name).matches()) {
				String areaName = name.substring(5);
				double weight = Double.parseDouble(value);
				PredictionArea.valueOf(areaName).setWeight(weight);
			}
			else if (ITEM_PATTERN.matcher(name).matches()) {
				int pos = name.indexOf('[', 5);
				String areaName = name.substring(5, pos);
				String itemName = name.substring(5);
				double weight = Double.parseDouble(value);
				Stream.of(PredictionArea.valueOf(areaName).getItems()).filter(item -> item.toString().equals(itemName)).findFirst().get().setWeight(weight);
			}
		}
		PredictionConfig.config = config;
	}

	public static Optional<Integer> getIntegerProperty(String name) {
		String value = config.getProperty(name);
		return !Strings.isNullOrEmpty(value) ? Optional.of(Integer.parseInt(value)) : Optional.empty();
	}
}
