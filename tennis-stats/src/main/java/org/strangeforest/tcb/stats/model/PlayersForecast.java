package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayersForecast {

	private final Set<String> results;
	private final Map<Integer, PlayerForecast> playerForecasts;

	PlayersForecast(List<PlayerForecast> players) {
		results = new LinkedHashSet<>();
		playerForecasts = new LinkedHashMap<>();
		for (PlayerForecast player : players)
			playerForecasts.put(player.getId(), new PlayerForecast(player));
	}

	public Set<String> getResults() {
		return results;
	}

	public String getFirstResult() {
		return results.iterator().next();
	}

	public KOResult getEntryRound() {
		KOResult firstResult = KOResult.valueOf(getFirstResult());
		return firstResult.hasPrev() ? firstResult.prev() : firstResult;
	}

	public Collection<PlayerForecast> getPlayerForecasts() {
		return playerForecasts.values();
	}

	public PlayerForecast getOtherPlayer(int index) {
		int otherIndex = index + (index % 2 == 0 ? 1 : -1);
		return otherIndex < playerForecasts.size() ? new ArrayList<>(playerForecasts.values()).get(otherIndex) : null;
	}

	public double getStrength(int fromIndex, int count) {
		return playerForecasts.values().stream().skip(fromIndex).limit(count).mapToDouble(PlayerForecast::winProbability).sum();
	}

	public List<MatchPlayerEx> getKnownPlayers(String result) {
		return playerForecasts.values().stream().filter(player -> player.getId() > 0 && player.probability(result) > 0.0)
			.sorted(comparing(MatchPlayerEx::getSeed, nullsLast(naturalOrder())).thenComparing(MatchPlayerEx::getName, nullsLast(naturalOrder())))
			.collect(toList());
	}

	void addResult(int playerId, String result, double probability) {
		playerForecasts.get(playerId).addForecast(result, probability);
		results.add(result);
	}

	void removePlayersWOResults() {
		new HashMap<>(playerForecasts).forEach((playerId, playerForecast) -> {
			if (playerForecast.isEmpty())
				playerForecasts.remove(playerId);
		});
	}

	void removePlayersWORemainingResults() {
		new HashMap<>(playerForecasts).forEach((playerId, playerForecast) -> {
			if (!playerForecast.hasAnyResult(results))
				playerForecasts.remove(playerId);
		});
	}

	void removePastRounds() {
		for (String result : new ArrayList<>(results)) {
			if (!"W".equals(result) && isPastRound(result))
				results.remove(result);
		}
	}

	private boolean isPastRound(String result) {
		for (PlayerForecast playerForecast : getPlayerForecasts()) {
			double probability = playerForecast.probability(result);
			if (probability > 0.0 && probability < PCT)
				return false;
		}
		return true;
	}
}
