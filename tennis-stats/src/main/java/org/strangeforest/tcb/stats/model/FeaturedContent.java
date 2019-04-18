package org.strangeforest.tcb.stats.model;

public class FeaturedContent {

	public enum Type {

		PLAYER, RECORD, BLOG, PAGE;

		private final FeaturedContent empty = new FeaturedContent(this, null, null);

		public FeaturedContent empty() {
			return empty;
		}
	}

	private final Type type;
	private final String value;
	private final String content;

	public FeaturedContent(Type type, String value, String content) {
		this.type = type;
		this.value = value;
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getContent() {
		return content;
	}

	public boolean isOfType(Type type) {
		return this.type == type;
	}
}
