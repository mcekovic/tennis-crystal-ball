package org.strangeforest.tcb.stats.model.records;

public class RecordColumn {

	private final String name;
	private final String type;
	private final String formatter;
	private final String width;
	private final String align;
	private final String caption;

	public RecordColumn(String name, String type, String formatter, String width, String align, String caption) {
		this.name = name;
		this.type = type;
		this.formatter = formatter;
		this.width = width;
		this.align = align;
		this.caption = caption;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getFormatter() {
		return formatter;
	}

	public String getWidth() {
		return width;
	}

	public String getAlign() {
		return align;
	}

	public String getCaption() {
		return caption;
	}
}
