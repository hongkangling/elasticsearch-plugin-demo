package org.elasticsearch.plugin.analysis.analyzer.pinyin;

/**
 * PinYinIndex to represent one result.
 * 
 */
public class PinYinIndex {
	private final int index;
	private final boolean isPinYin;

	public PinYinIndex(int index, boolean isPrefix) {
		this.index = index;
		this.isPinYin = isPrefix;
	}

	public int getIndex() {
		return index;
	}

	public boolean isPinYin() {
		return isPinYin;
	}

	public static PinYinIndex NOT_PINYIN = new PinYinIndex(-3, false);
	public static PinYinIndex NOT_FOUND = new PinYinIndex(-1, false);
}
