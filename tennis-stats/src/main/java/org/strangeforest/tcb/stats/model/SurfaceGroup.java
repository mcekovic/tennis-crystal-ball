package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.util.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.Surface.*;

public enum SurfaceGroup {

	FAST(EnumSet.of(HARD, GRASS, CARPET), "Fast (H, G, Cp)"),
	SLOW(EnumSet.of(CLAY), "Slow (Cl)"),
	FIRM(EnumSet.of(HARD, CARPET), "Firm (H, Cp)"),
	SOFT(EnumSet.of(CLAY, GRASS), "Soft (Cl, G)");

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
