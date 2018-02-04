package org.strangeforest.tcb.stats.model.prediction;

import java.io.*;
import java.util.*;
import java.util.Objects;
import java.util.regex.*;
import java.util.stream.*;

import org.strangeforest.tcb.stats.util.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;

public class PredictionConfig {

	private static final Pattern AREA_PATTERN = Pattern.compile("area\\.\\w+");
	private static final Pattern ITEM_PATTERN = Pattern.compile("item\\.\\w+\\.\\w+");
	private static final Pattern MATCH_RECENT_PERIOD_PATTERN = Pattern.compile("recentPeriod\\.match\\.\\w+");
	private static final Pattern SET_RECENT_PERIOD_PATTERN = Pattern.compile("recentPeriod\\.set\\.\\w+");
	private static final Pattern LAST_MATCHES_COUNT_PATTERN = Pattern.compile("lastMatchesCount\\.\\w+");


	// Factory

	private static PredictionConfig defaultConfig;

	public static synchronized PredictionConfig defaultConfig() {
		if (defaultConfig == null)
			loadConfig("/prediction/default-prediction.properties");
		return defaultConfig;
	}

	public static void loadConfig(String configName) {
		Properties props = new Properties();
		try {
			InputStream in = PredictionConfig.class.getResourceAsStream(configName);
			if (in == null)
				throw new TennisStatsException("Cannot load find prediction config: " + configName);
			props.load(in);
		}
		catch (IOException ex) {
			throw new TennisStatsException("Cannot load prediction config.", ex);
		}
		defaultConfig = new PredictionConfig(props);
	}

	public static final PredictionConfig EMPTY = new PredictionConfig();
	public static final PredictionConfig EQUAL_WEIGHTS = new PredictionConfig(1.0);

	public static PredictionConfig areaEqualWeights(PredictionArea area) {
		return new PredictionConfig(area, 1.0);
	}


	// Instance

	private final Map<PredictionArea, Double> areaWeights = new HashMap<>();
	private final Map<PredictionArea, Double> areaAdjustedWeights = new HashMap<>();
	private double totalAreasWeight;
	private final Map<PredictionItem, Double> itemWeights = new HashMap<>();
	private final Map<PredictionArea, Integer> matchRecentPeriods = new HashMap<>();
	private final Map<PredictionArea, Integer> setRecentPeriods = new HashMap<>();
	private final Map<PredictionArea, Integer> lastMatchesCounts = new HashMap<>();

	private PredictionConfig() {}
	
	public PredictionConfig(Properties props) {
		fromProperties(props);
		calculateAreaTotalAndAdjustedWeights();
	}
	
	public PredictionConfig(double weight) {
		this(weight, weight);
	}

	public PredictionConfig(double areaWeight, double itemWeight) {
		for (PredictionArea area : PredictionArea.values())
			setAreaWeights(area, areaWeight, itemWeight);
		calculateAreaTotalAndAdjustedWeights();
	}

	public PredictionConfig(PredictionArea area, double weight) {
		this(area, weight, weight);
	}
	
	public PredictionConfig(PredictionArea area, double areaWeight, double itemWeight) {
		setAreaWeights(area, areaWeight, itemWeight);
		calculateAreaTotalAndAdjustedWeights();
	}

	private void setAreaWeights(PredictionArea area, double areaWeight, double itemWeight) {
		areaWeights.put(area, areaWeight);
		for (PredictionItem item : area.getItems())
			itemWeights.put(item, itemWeight);
	}

	public PredictionConfig(PredictionArea area, double areaWeight, PredictionItem item, double itemWeight) {
		areaWeights.put(area, areaWeight);
		itemWeights.put(item, itemWeight);
		calculateAreaTotalAndAdjustedWeights();
	}

	public PredictionConfig(PredictionConfig config, PredictionArea area, double weight) {
		this(config);
		areaWeights.put(area, weight);
		calculateAreaTotalAndAdjustedWeights();
	}

	public PredictionConfig(PredictionConfig config, PredictionItem item, double weight) {
		this(config);
		itemWeights.put(item, weight);
		calculateAreaTotalAndAdjustedWeights();
	}

	private PredictionConfig(PredictionConfig config) {
		areaWeights.putAll(config.areaWeights);
		itemWeights.putAll(config.itemWeights);
		matchRecentPeriods.putAll(config.matchRecentPeriods);
		setRecentPeriods.putAll(config.setRecentPeriods);
		lastMatchesCounts.putAll(config.lastMatchesCounts);
	}

	private void calculateAreaTotalAndAdjustedWeights() {
		for (PredictionArea area : PredictionArea.values())
			areaAdjustedWeights.put(area, calculateAreaAdjustedWeight(area));
		totalAreasWeight = Stream.of(PredictionArea.values()).mapToDouble(this::getAreaWeight).sum();
	}

	private double calculateAreaAdjustedWeight(PredictionArea area) {
		double areaItemWeights = Stream.of(area.getItems()).mapToDouble(this::getItemWeight).sum();
		return areaItemWeights > 0.0 ? getAreaWeight(area) / areaItemWeights : 0.0;
	}

	public double getAreaWeight(PredictionArea area) {
		return areaWeights.getOrDefault(area, 0.0);
	}

	public double getAreaAdjustedWeight(PredictionArea area) {
		return areaAdjustedWeights.getOrDefault(area, 0.0);
	}

	public double getItemWeight(PredictionItem item) {
		return itemWeights.getOrDefault(item, 0.0);
	}

	public double getTotalAreasWeight() {
		return totalAreasWeight;
	}

	public boolean isAreaEnabled(PredictionArea area) {
		return getAreaWeight(area) > 0.0 && getAreaAdjustedWeight(area) > 0.0;
	}

	public boolean isAnyAreaEnabled() {
		for (PredictionArea area : PredictionArea.values()) {
			if (isAreaEnabled(area))
				return true;
		}
		return false;
	}

	public int getMatchRecentPeriod(PredictionArea area, int defaultPeriod) {
		return matchRecentPeriods.getOrDefault(area, defaultPeriod);
	}

	public int getSetRecentPeriod(PredictionArea area, int defaultPeriod) {
		return setRecentPeriods.getOrDefault(area, defaultPeriod);
	}

	public int getLastMatchesCount(PredictionArea area, int defaultCount) {
		return lastMatchesCounts.getOrDefault(area, defaultCount);
	}

	public Properties asProperties() {
		Properties config = new Properties();
		for (PredictionArea area : PredictionArea.values()) {
			config.setProperty("area." + area, String.valueOf(getAreaWeight(area)));
			for (PredictionItem item : area.getItems())
				config.setProperty("item." + area + '.' + item, String.valueOf(getItemWeight(item)));
		}
		return config;
	}

	private void fromProperties(Properties props) {
		for (String name : props.stringPropertyNames()) {
			String value = props.getProperty(name);
			if (isNullOrEmpty(value))
				continue;
			if (AREA_PATTERN.matcher(name).matches()) {
				String areaName = name.substring(5);
				double weight = Double.parseDouble(value);
				areaWeights.put(PredictionArea.valueOf(areaName), weight);
			}
			else if (ITEM_PATTERN.matcher(name).matches()) {
				int pos = name.indexOf('.', 5);
				String areaName = name.substring(5, pos);
				String itemName = name.substring(pos + 1);
				double weight = Double.parseDouble(value);
				itemWeights.put(PredictionArea.valueOf(areaName).getItem(itemName), weight);
			}
			else if (MATCH_RECENT_PERIOD_PATTERN.matcher(name).matches()) {
				String areaName = name.substring(19);
				int recentPeriod = Integer.parseInt(value);
				matchRecentPeriods.put(PredictionArea.valueOf(areaName), recentPeriod);
			}
			else if (SET_RECENT_PERIOD_PATTERN.matcher(name).matches()) {
				String areaName = name.substring(17);
				int recentPeriod = Integer.parseInt(value);
				setRecentPeriods.put(PredictionArea.valueOf(areaName), recentPeriod);
			}
			else if (LAST_MATCHES_COUNT_PATTERN.matcher(name).matches()) {
				String areaName = name.substring(15);
				int matchCount = Integer.parseInt(value);
				lastMatchesCounts.put(PredictionArea.valueOf(areaName), matchCount);
			}
		}
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PredictionConfig config = (PredictionConfig)o;
		return areaWeights.equals(config.areaWeights) && itemWeights.equals(config.itemWeights)
			&& matchRecentPeriods.equals(config.matchRecentPeriods) && setRecentPeriods.equals(config.setRecentPeriods) && lastMatchesCounts.equals(config.lastMatchesCounts);
	}

	@Override public int hashCode() {
		return Objects.hash(areaWeights, itemWeights, matchRecentPeriods, setRecentPeriods, lastMatchesCounts);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("weights", areaWeights)
			.add("itemWeights", itemWeights)
			.add("matchRecentPeriods", matchRecentPeriods)
			.add("setRecentPeriods", setRecentPeriods)
			.add("lastMatchesCounts", lastMatchesCounts)
		.toString();
	}
}
