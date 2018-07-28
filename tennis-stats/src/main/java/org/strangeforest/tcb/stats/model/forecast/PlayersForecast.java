package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

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

	public PlayerForecast getOpponent(int index) {
		int opponentIndex = index + (index % 2 == 0 ? 1 : -1);
		return opponentIndex < playerForecasts.size() ? playerForecasts.get(opponentIndex) : null;
	}

	public List<PlayerForecast> getOpponents(int index, int rounds) {
		int drawFactor = 2 << rounds;
		int startIndex = index - index % drawFactor;
		int endIndex = startIndex + drawFactor - 1;
		if (2 * index < startIndex + endIndex)
			startIndex += drawFactor >> 1;
		else
			endIndex -= drawFactor >> 1;
		List<PlayerForecast> opponents = new ArrayList<>(endIndex - startIndex + 1);
		for (int opponentIndex = startIndex; opponentIndex <= endIndex; opponentIndex++) {
			if (opponentIndex < playerForecasts.size())
				opponents.add(playerForecasts.get(opponentIndex));
		}
		return opponents;
	}

	public Optional<Integer> findIndex(int playerId) {
		for (int i = 0, size = playerForecasts.size(); i < size; i++) {
			if (playerForecasts.get(i).getId() == playerId)
				return Optional.of(i);
		}
		return Optional.empty();
	}

	public double getStrength(int fromIndex, int count) {
		return playerForecasts.stream().skip(fromIndex).limit(count).mapToDouble(PlayerForecast::getWinProbability).sum();
	}

	public List<MatchPlayer> getKnownPlayers() {
		String result = results.iterator().next();
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

	public void setEloRatings(int playerId, Integer eloRating, Integer nextEloRating, Integer recentEloRating, Integer nextRecentEloRating,
	                          Integer surfaceEloRating, Integer nextSurfaceEloRating, Integer inOutEloRating, Integer nextInOutEloRating, Integer setEloRating, Integer nextSetEloRating) {
		PlayerForecast playerForecast = playerForecastMap.get(playerId);
		if (playerForecast != null) {
			playerForecast.setEloRatings(eloRating, nextEloRating);
			playerForecast.setRecentEloRatings(recentEloRating, nextRecentEloRating);
			playerForecast.setSurfaceEloRatings(surfaceEloRating, nextSurfaceEloRating);
			playerForecast.setInOutEloRatings(inOutEloRating, nextInOutEloRating);
			playerForecast.setSetEloRatings(setEloRating, nextSetEloRating);
		}
	}
}
