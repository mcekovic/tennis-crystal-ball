package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

public class PlayersForecast {

	private final Set<String> results;
	private final List<PlayerForecast> playerForecasts;
	private final Map<Integer, PlayerForecast> playerForecastMap;

	PlayersForecast(List<PlayerForecast> players) {
		results = new LinkedHashSet<>();
		playerForecasts = new ArrayList<>(players.size());
		playerForecastMap = new HashMap<>();
		for (PlayerForecast player : players) {
			PlayerForecast playerForecast = new PlayerForecast(player);
			playerForecasts.add(playerForecast);
			playerForecastMap.put(player.getId(), playerForecast);
		}
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
		return playerForecasts;
	}

	public PlayerForecast getPlayerForecast(int playerId) {
		return playerForecastMap.get(playerId);
	}

	public PlayerForecast getOtherPlayer(int index) {
		int otherIndex = index + (index % 2 == 0 ? 1 : -1);
		return otherIndex < playerForecasts.size() ? playerForecasts.get(otherIndex) : null;
	}

	public double getStrength(int fromIndex, int count) {
		return playerForecasts.stream().skip(fromIndex).limit(count).mapToDouble(PlayerForecast::getWinProbability).sum();
	}

	public List<MatchPlayer> getKnownPlayers(String result) {
		return playerForecasts.stream().filter(player -> player.getId() > 0 && player.getRawProbability(result) > 0.0)
			.sorted(comparing(MatchPlayer::getSeed, nullsLast(naturalOrder())).thenComparing(MatchPlayer::getName, nullsLast(naturalOrder())))
			.collect(toList());
	}

	void addResult(int playerId, String result, double probability) {
		playerForecastMap.get(playerId).addForecast(result, probability);
		results.add(result);
	}

	void removePlayersWOResults() {
		for (Iterator<PlayerForecast> iter = playerForecasts.iterator(); iter.hasNext(); ) {
			PlayerForecast playerForecast = iter.next();
			if (playerForecast.isEmpty()) {
				iter.remove();
				playerForecastMap.remove(playerForecast.getId());
			}
		}
	}

	void removePlayersWORemainingResults() {
		for (Iterator<PlayerForecast> iter = playerForecasts.iterator(); iter.hasNext(); ) {
			PlayerForecast playerForecast = iter.next();
			if (!playerForecast.hasAnyResult(results)) {
				iter.remove();
				playerForecastMap.remove(playerForecast.getId());
			}
		}
	}

	void removePastRounds() {
		for (String result : new ArrayList<>(results)) {
			if (!"W".equals(result) && isPastRound(result))
				results.remove(result);
		}
	}

	private boolean isPastRound(String result) {
		for (PlayerForecast playerForecast : getPlayerForecasts()) {
			double probability = playerForecast.getRawProbability(result);
			if (probability > 0.0 && probability < 1.0)
				return false;
		}
		return true;
	}

	public void setEloRatings(int playerId, Integer eloRating, Integer nextEloRating) {
		PlayerForecast playerForecast = playerForecastMap.get(playerId);
		if (playerForecast != null) {
			playerForecast.setEloRating(eloRating);
			playerForecast.setNextEloRating(nextEloRating);
		}
	}
}
