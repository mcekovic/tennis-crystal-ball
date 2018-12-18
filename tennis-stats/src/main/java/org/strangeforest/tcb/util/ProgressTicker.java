package org.strangeforest.tcb.util;

public class ProgressTicker {

	private final char tick;
	private final long each;
	private final ProgressTicker downstreamTicker;
	private long ticks;

	public ProgressTicker(char tick, int each) {
		this(tick, each, null);
	}

	public ProgressTicker(char tick, int each, ProgressTicker downstreamTicker) {
		this.tick = tick;
		this.each = each;
		this.downstreamTicker = downstreamTicker;
	}

	public synchronized long getTicks() {
		return ticks;
	}

	public synchronized void tick() {
		if (++ticks % each == 0L) {
			System.out.print(tick);
			if (downstreamTicker != null)
				downstreamTicker.tick();
		}
	}
}
