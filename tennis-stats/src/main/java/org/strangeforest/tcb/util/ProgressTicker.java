package org.strangeforest.tcb.util;

public class ProgressTicker {

	private static final int TICKS_PER_LINE = 100;

	public static ProgressTicker newLineTicker() {
		return new ProgressTicker('\n', TICKS_PER_LINE);
	}

	private final char tick;
	private final long each;
	private Runnable preAction;
	private Runnable postAction;
	private long ticks;

	public ProgressTicker(char tick, int each) {
		this.tick = tick;
		this.each = each;
	}

	public synchronized ProgressTicker withPreAction(Runnable action) {
		preAction = action;
		return this;
	}

	public synchronized ProgressTicker withPostAction(Runnable action) {
		postAction = action;
		return this;
	}

	public synchronized ProgressTicker withDownstreamTicker(ProgressTicker downstreamTicker) {
		postAction = downstreamTicker::tick;
		return this;
	}

	public synchronized long getTicks() {
		return ticks;
	}

	public synchronized void tick() {
		if (++ticks % each == 0L) {
			if (preAction != null)
				preAction.run();
			System.out.print(tick);
			if (postAction != null)
				postAction.run();
		}
	}
}
