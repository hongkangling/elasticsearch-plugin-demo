package org.elasticsearch.plugin.analysis.analyzer.pinyin;

/**
 * 
 * 
 **/
public enum WordSegmentType {
	ChineseChar(1), EnglishWord(2), Number(1);
	private int length;

	private WordSegmentType(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}
}
