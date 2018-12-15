package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public abstract class AdjustedH2H {

	public static H2H surfaceAdjustedH2H(PlayerPerformance h2hPerf, PlayerPerformance perf1, PlayerPerformance perf2) {
		H2H h2h = H2H.EMPTY;
		int h2hMatches = h2hPerf.getMatches().getTotal();
		double hardPct = pct(h2hPerf.getHardMatches().getTotal(), h2hMatches);
		double clayPct = pct(h2hPerf.getClayMatches().getTotal(), h2hMatches);
		double grassPct = pct(h2hPerf.getGrassMatches().getTotal(), h2hMatches);
		double carpetPct = pct(h2hPerf.getCarpetMatches().getTotal(), h2hMatches);
		int matches1 = perf1.getMatches().getTotal();
		int matches2 = perf2.getMatches().getTotal();
		if (hardPct > 0.0) {
			double hardPct1 = pct(perf1.getHardMatches().getTotal(), matches1);
			double hardPct2 = pct(perf2.getHardMatches().getTotal(), matches2);
			h2h = h2h.add(new H2H(h2hPerf.getHardMatches()).scale((hardPct1 + hardPct2) / hardPct));
		}
		if (clayPct > 0.0) {
			double clayPct1 = pct(perf1.getClayMatches().getTotal(), matches1);
			double clayPct2 = pct(perf2.getClayMatches().getTotal(), matches2);
			h2h = h2h.add(new H2H(h2hPerf.getClayMatches()).scale((clayPct1 + clayPct2) / clayPct));
		}
		if (grassPct > 0.0) {
			double grassPct1 = pct(perf1.getGrassMatches().getTotal(), matches1);
			double grassPct2 = pct(perf2.getGrassMatches().getTotal(), matches2);
			h2h = h2h.add(new H2H(h2hPerf.getGrassMatches()).scale((grassPct1 + grassPct2) / grassPct));
		}
		if (carpetPct > 0.0) {
			double carpetPct1 = pct(perf1.getCarpetMatches().getTotal(), matches1);
			double carpetPct2 = pct(perf2.getCarpetMatches().getTotal(), matches2);
			h2h = h2h.add(new H2H(h2hPerf.getCarpetMatches()).scale((carpetPct1 + carpetPct2) / carpetPct));
		}
		return h2h.scale(h2hPerf.getMatches().getTotal() / h2h.getTotal());
	}

	public static H2H importanceAdjustedH2H(int playerId1, int playerId2, PlayerPerformance h2hPerf, List<Match> matches, List<BigWinMatchFactor> matchFactors) {
		H2H h2h = H2H.EMPTY;
		for (Match match : matches) {
			int winnerId = match.getWinner().getId();
			h2h = h2h.add(new H2H(winnerId == playerId1 ? 1 : 0, winnerId == playerId2 ? 1 : 0).scale(matchFactor(match, matchFactors)));
		}
		return h2h.scale(h2hPerf.getMatches().getTotal() / h2h.getTotal());
	}

	private static double matchFactor(Match match, List<BigWinMatchFactor> matchFactors) {
		String level = match.getLevel();
		String round = match.getRound();
		return matchFactors.stream().filter(f -> Objects.equals(f.getLevel(), level) && Objects.equals(f.getRound(), round)).findFirst()
				.map(f -> (double)f.getMatchFactor()).orElse(0.5);
	}
}
