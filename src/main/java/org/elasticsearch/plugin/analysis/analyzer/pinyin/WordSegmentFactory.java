package org.elasticsearch.plugin.analysis.analyzer.pinyin;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;

public class WordSegmentFactory {
	public static WordSegment createInstance(String word) {
		if (word == null || word.length() == 0) {
			throw new IllegalArgumentException("Expect at least one char");
		}
		if (CharacterHelper.isCJKCharacter(word.charAt(0), false)) {
			return new WordSegment(word, WordSegmentType.ChineseChar);
		} else if (Character.isDigit(word.charAt(0))) {
			return new WordSegment(word, WordSegmentType.Number);
		} else {
			return new WordSegment(word, WordSegmentType.EnglishWord);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
