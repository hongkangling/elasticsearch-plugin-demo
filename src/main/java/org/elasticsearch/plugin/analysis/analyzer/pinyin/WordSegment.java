package org.elasticsearch.plugin.analysis.analyzer.pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordSegment {
	private String word;
	private WordSegmentType type;
	private List<String> tones;

	public WordSegment(String word, WordSegmentType type) {
		this.word = word;
		this.type = type;
	}

	/*
	 * public void init() throws IOException{ initPinYin(); }
	 * 
	 * private void initPinYin() throws IOException { if(type ==
	 * WordSegmentType.ChineseChar){ String[] result =
	 * PinyinMapper.get().get(word.charAt(0)); if(result!=null){ tones = new
	 * ArrayList<String>(result.length); for(int i=0;i<result.length;i++){
	 * tones.add(result[i]); } } }else{ tones = new ArrayList<String>(1);
	 * tones.add(word); }
	 * 
	 * }
	 */
	public void setTones(List<String> tones) {
		this.tones = tones;
	}

	public boolean isTonesNull() {
		return tones == null;
	}

	public List<String> getTones() {
		if (tones == null) {
			return Collections.emptyList();
		}
		return tones;
	}

	public WordSegmentType getType() {
		return type;
	}

	public String getWord() {
		return word;
	}

	public List<String> getNormalizedTones() {
		if (tones == null) {
			return Collections.emptyList();
		}

		if (type != WordSegmentType.EnglishWord) {
			return getTones();
		}

		List<String> normalizedTones = new ArrayList<String>(tones.size());
		for (String tone : tones) {
			normalizedTones.add(PinYinMarkEnum.EWORD_PREFIX.getToken() + tone);
		}
		return normalizedTones;
	}

	public void filter(String pinyin) {
		if (tones != null) {
			if (tones.contains(pinyin)) {
				tones.clear();
				tones.add(pinyin);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("word:" + word + " type:" + type.name() + " tones: " + tones);
		return builder.toString();
	}
}
