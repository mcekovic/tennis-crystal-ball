package org.strangeforest.tcb.stats.model;

public class WeeksAtRank {

	public static final WeeksAtRank EMPTY = new WeeksAtRank(false);

	private final boolean forSeason;
	private double atNo1;
	private double inTop2;
	private double inTop3;
	private double inTop4;
	private double inTop5;
	private double inTop10;
	private double inTop20;
	private double inTop50;
	private double inTop100;
	private double inTop200;
	private double atNo2;
	private double atNo3;
	private double atNo4;
	private double atNo5;
	private double atNo6_10;
	private double atNo11_20;
	private double atNo21_50;
	private double atNo51_100;
	private double atNo101_200;

	public WeeksAtRank(boolean forSeason) {
		this.forSeason = forSeason;
	}

	public int getAtNo1() {
		return round(atNo1);
	}

	public int getInTop2() {
		return round(inTop2);
	}

	public int getInTop3() {
		return round(inTop3);
	}

	public int getInTop4() {
		return round(inTop4);
	}

	public int getInTop5() {
		return round(inTop5);
	}

	public int getInTop10() {
		return round(inTop10);
	}

	public int getInTop20() {
		return round(inTop20);
	}

	public int getInTop50() {
		return round(inTop50);
	}

	public int getInTop100() {
		return round(inTop100);
	}

	public int getInTop200() {
		return round(inTop200);
	}

	public int getAtNo2() {
		return round(atNo2);
	}

	public int getAtNo3() {
		return round(atNo3);
	}

	public int getAtNo4() {
		return round(atNo4);
	}

	public int getAtNo5() {
		return round(atNo5);
	}

	public int getAtNo6_10() {
		return round(atNo6_10);
	}

	public int getAtNo11_20() {
		return round(atNo11_20);
	}

	public int getAtNo21_50() {
		return round(atNo21_50);
	}

	public int getAtNo51_100() {
		return round(atNo51_100);
	}

	public int getAtNo101_200() {
		return round(atNo101_200);
	}

	public boolean isNo1First() {
		return atNo1 > 0.0;
	}

	public boolean isTop2First() {
		return atNo1 == 0.0 && inTop2 > 0.0;
	}

	public boolean isTop3First() {
		return inTop2 == 0.0 && inTop3 > 0.0;
	}

	public boolean isTop4First() {
		return inTop3 == 0.0 && inTop4 > 0.0;
	}

	public boolean isTop5First() {
		return inTop4 == 0.0 && inTop5 > 0.0;
	}

	public boolean isTop10First() {
		return inTop5 == 0.0 && inTop10 > 0.0;
	}

	public boolean isTop20First() {
		return inTop10 == 0.0 && inTop20 > 0.0;
	}

	public boolean isTop50First() {
		return inTop20 == 0.0 && inTop50 > 0.0;
	}

	public boolean isTop100First() {
		return inTop50 == 0.0 && inTop100 > 0.0;
	}

	public boolean isTop200First() {
		return inTop100 == 0.0 && inTop200 > 0.0;
	}

	private int round(double weeks) {
		return (int)(forSeason ? Math.round(weeks) : Math.ceil(weeks));
	}

	public void processWeeksAt(int rank, double weeks) {
		if (weeks >= 53.0)
			return;
		if (rank == 1)
			atNo1 += weeks;
		if (rank <= 2)
			inTop2 += weeks;
		if (rank <= 3)
			inTop3 += weeks;
		if (rank <= 4)
			inTop4 += weeks;
		if (rank <= 5)
			inTop5 += weeks;
		if (rank <= 10)
			inTop10 += weeks;
		if (rank <= 20)
			inTop20 += weeks;
		if (rank <= 50)
			inTop50 += weeks;
		if (rank <= 100)
			inTop100 += weeks;
		if (rank <= 200)
			inTop200 += weeks;
		if (rank == 2)
			atNo2 += weeks;
		if (rank == 3)
			atNo3 += weeks;
		if (rank == 4)
			atNo4 += weeks;
		if (rank == 5)
			atNo5 += weeks;
		if (rank >= 6 && rank <= 10)
			atNo6_10 += weeks;
		if (rank >= 11 && rank <= 20)
			atNo11_20 += weeks;
		if (rank >= 21 && rank <= 50)
			atNo21_50 += weeks;
		if (rank >= 51 && rank <= 100)
			atNo51_100 += weeks;
		if (rank >= 101 && rank <= 200)
			atNo101_200 += weeks;
	}
}
