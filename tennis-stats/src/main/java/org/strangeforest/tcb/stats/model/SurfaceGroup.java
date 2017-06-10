package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.Surface.*;

public enum SurfaceGroup {

	FASTEST(EnumSet.of(GRASS, CARPET), "Fastest (G, Cp)"),
	FAST(EnumSet.of(HARD, GRASS), "Fast (H, G)"),
	FAST_2(EnumSet.of(HARD, GRASS, CARPET), "Fast (H, G, Cp)"),
	MEDIUM(EnumSet.of(HARD), "Medium (H)"),
	SLOW(EnumSet.of(HARD, CLAY), "Slow (H, Cl)"),
	SLOWEST(EnumSet.of(CLAY), "Slowest (Cl)"),
	FIRM(EnumSet.of(HARD, CARPET), "Firm (H, Cp)"),
	SOFT(EnumSet.of(CLAY, GRASS), "Soft (Cl, G)"),
	NON_HARD(EnumSet.of(CLAY, GRASS, CARPET), "Non-Hard"),
	NON_CLAY(EnumSet.of(HARD, GRASS, CARPET), "Non-Clay"),
	NON_GRASS(EnumSet.of(HARD, CLAY, CARPET), "Non-Grass"),
	NON_CARPET(EnumSet.of(HARD, CLAY, GRASS), "Non-Carpet");

	private final EnumSet<Surface> surfaces;
	private final String codes;
	private final String text;

	SurfaceGroup(EnumSet<Surface> surfaces, String text) {
		this.surfaces = surfaces;
		codes = surfaces.stream().map(Surface::getCode).collect(joining());
		this.text = text;
	}

	public EnumSet<Surface> getSurfaces() {
		return surfaces;
	}

	public String getCodes() {
		return codes;
	}

	public String getText() {
		return text;
	}

	public String getSurfacesText() {
		return CodedEnum.joinTexts(Surface.class, codes);
	}
}
