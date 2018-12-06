package org.elasticsearch.plugin.analysis.analyzer.pinyin.util;

import com.homedo.bigdata.analysis.analyzer.dict.Hit;
import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import com.homedo.bigdata.analysis.analyzer.pinyin.WordSegment;
import com.homedo.bigdata.analysis.analyzer.pinyin.WordSegmentFactory;
import com.homedo.bigdata.analysis.analyzer.pinyin.WordSegmentType;
import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import com.homedo.bigdata.analysis.util.word.PinyinMapper;
import com.homedo.bigdata.analysis.analyzer.dict.Dictionary;

import java.io.IOException;
import java.util.*;

/**
 * @author kmchu
 * @create 2014/02/18
 * 
 **/
public class GeneralPinYinHelper {

	private static List<String> getWordsPinYin(String keyword, String defaultPinYin, String[] correctPinyin)
			throws IOException {
		List<WordSegment> result = InputSplitter.split(keyword, correctPinyin); // 一个中文生成一个WordSegment，连续的英文生成一个WordSegment，连续的数字生成一个WordSegment
		calculatePinYin(result); // 如果是中文就获取中文的拼音列表，否则返回一个长度为1的列表，其中元素为英文或数字本身
		result = PinYinFilter.filter(result, defaultPinYin);
		return PinYinCompositor.compose(result);
	}

	// correctPinyin: 从拼音表里拿到多音字的拼音
	public static List<String> getWordsPinYins(String keyword, String defaultPinYin, String[] correctPinyin)
			throws IOException {
		List<String> result = getWordsPinYin(keyword, defaultPinYin, correctPinyin);
		String normalizedKeyword = NormalizeTokenFilter.nomalize(keyword);
		if (!keyword.equals(normalizedKeyword)) {
			result.addAll(getWordsPinYin(normalizedKeyword, defaultPinYin, correctPinyin));
		}
		Set<String> set = new HashSet<String>(result);
		result = new ArrayList<String>(set);
		return result;
	}

	// 获取多音字的拼音
	// 这里从拼音词表获取的时候暂时不考虑相交的情况
	public static String[] getPinyinFromDict(String content, String[] pinyin, List<Hit> tmpHits) {
		char[] charArray = content.toCharArray();
		if (pinyin.length < charArray.length) {
			pinyin = new String[pinyin.length * 2];
		}

		for (int i = 0; i < charArray.length; ++i) {
			pinyin[i] = null; // 初始化为null

			if (!CharacterHelper.isCJKCharacter(charArray[i], false)) {
				tmpHits.clear();
				continue;
			}

			// 优先处理tmpHits中的hit
			if (!tmpHits.isEmpty()) {
				// 处理词段队列
				Hit[] tmpArray = tmpHits.toArray(new Hit[tmpHits.size()]);
				for (Hit hit : tmpArray) {
					hit = Dictionary.matchInPinyinDictWithHit(charArray, i, hit);
					if (hit.isMatch()) {
						setPinyin(pinyin, hit);

						if (!hit.isPrefix()) { // 不是词前缀，hit不需要继续匹配，移除
							tmpHits.remove(hit);
						}
					} else if (hit.isUnmatch()) {
						// 未匹配，移除
						tmpHits.remove(hit);
					}
				}
			}

			// 再对当前指针位置的字符进行单字匹配
			Hit singleCharHit = Dictionary.matchInPinyinDict(charArray, i, 1);
			if (singleCharHit.isMatch()) { // 首字成词
				setPinyin(pinyin, singleCharHit);
			}

			// 前缀匹配则放入hit列表
			if (singleCharHit.isPrefix()) {
				tmpHits.add(singleCharHit);
			}
		}

		return pinyin;
	}

	private static void setPinyin(String[] pinyin, Hit hit) {
		for (int i = hit.getBegin(); i <= hit.getEnd(); ++i) {
			pinyin[i] = hit.getPinyin()[i - hit.getBegin()];
		}
	}

	private static void calculatePinYin(List<WordSegment> result) throws IOException {
		for (WordSegment word : result) {
			if (!word.isTonesNull()) { // 说明在之前的步骤中，已经从多音字拼音表里拿到了具体的拼音
				continue;
			}

			List<String> tones = getWordTones(word.getWord(), word.getType());
			word.setTones(tones);
		}

	}

	private static List<String> getWordTones(String word, WordSegmentType type) throws IOException {
		List<String> tones = null;
		if (type == WordSegmentType.ChineseChar) {
			String[] result = PinyinMapper.get().get(word.charAt(0));
			if (result != null) {
				tones = new ArrayList<String>(result.length);
				for (int i = 0; i < result.length; i++) {
					tones.add(result[i]);
				}
			}
		} else {
			tones = new ArrayList<String>(1);
			tones.add(word);
		}
		return tones;
	}

	static class InputSplitter {
		public static List<WordSegment> split(String keywords, String[] correctPinyin) {
			List<WordSegment> result = new ArrayList<WordSegment>();
			StringBuilder builder = new StringBuilder();
			int offset = 0;
			for (char c : keywords.toCharArray()) {
				if (CharacterHelper.isCJKCharacter(c, false)) {
					if (builder.length() > 0) {
						result.add(WordSegmentFactory.createInstance(builder.toString()));
						builder.delete(0, builder.length());
					}
					result.add(new WordSegment(String.valueOf(c), WordSegmentType.ChineseChar));
					if (correctPinyin != null && correctPinyin[offset] != null) { // 已在多音字拼音表中找到了具体的拼音，直接设置
						List<String> tone = new ArrayList<String>(1);
						tone.add(correctPinyin[offset]);
						result.get(result.size() - 1).setTones(tone);
					}
				}

				else if (Character.isDigit(c)) {
					if (builder.length() > 0 && !Character.isDigit(builder.charAt(0))) {
						result.add(WordSegmentFactory.createInstance(builder.toString()));
						builder.delete(0, builder.length());
					}
					builder.append(c);
				} else if (Character.isLetter(c)) {
					if (builder.length() > 0 && !Character.isLetter(builder.charAt(0))) {
						result.add(WordSegmentFactory.createInstance(builder.toString()));
						builder.delete(0, builder.length());
					}
					builder.append(c);
				} else {
					if (builder.length() > 0) {
						result.add(WordSegmentFactory.createInstance(builder.toString()));
						builder.delete(0, builder.length());
					}
				}

				++offset;
			}
			if (builder.length() > 0) {
				result.add(WordSegmentFactory.createInstance(builder.toString()));
			}
			return result;
		}

	}

	static class PinYinFilter {
		public static List<WordSegment> filter(List<WordSegment> sequences, String pinyin) {
			if (pinyin == null || "".equals(pinyin.trim())) {
				return sequences;
			}
			String[] pinyinArray = pinyin.split("\\s+");
			if (pinyinArray.length != sequences.size()) {
				return sequences;
			}
			for (int i = 0; i < sequences.size(); i++) {
				WordSegment seq = sequences.get(i);
				seq.filter(pinyinArray[i]);
			}

			return sequences;
		}
	}

	static class PinYinCompositor {
		public static List<String> compose(List<WordSegment> segments) {
			List<String> result = new ArrayList<String>();
			if (segments.isEmpty()) {
				return Collections.emptyList();
			}

			for (int i = 0; i < segments.size(); i++) {
				WordSegment seg = segments.get(i);
				int length = getTermLength(seg.getType());
				if (seg.getType() == WordSegmentType.EnglishWord) {
					for (String tone : seg.getNormalizedTones()) { // 加上英文前缀
						result.add(tone + PinYinMarkEnum.WORD_SUFFIX.getToken()
								+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken() + length);
						if (i == 0) { // 文本开头的词，再加上一个标识
							result.add(PinYinMarkEnum.STARTS.getToken() + tone + PinYinMarkEnum.WORD_SUFFIX.getToken()
									+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken() + (length + 1));
						}
					}
				} else {
					if (i < segments.size() - 1) {
						WordSegment next = segments.get(i + 1);
						if (next.getType() != WordSegmentType.EnglishWord) { // 两个都不是英文
							for (String curTone : seg.getNormalizedTones()) {
								for (String nextTone : next.getNormalizedTones()) {
									result.add(curTone + nextTone + PinYinMarkEnum.WORD_SUFFIX.getToken()
											+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken()
											+ (curTone.length() + seg.getType().getLength()));
									if (i == 0) {
										result.add(PinYinMarkEnum.STARTS.getToken() + curTone + nextTone
												+ PinYinMarkEnum.WORD_SUFFIX.getToken()
												+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken()
												+ (curTone.length() + seg.getType().getLength() + 1));
									}
								}
							}
						}
					}
					if (i == 0) { // 如果是中文，则只有在开头时才会有单独的拼音
						for (String curTone : seg.getNormalizedTones()) {
							result.add(
									PinYinMarkEnum.STARTS.getToken() + curTone + PinYinMarkEnum.WORD_SUFFIX.getToken()
											+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken() + (length + 1));
							result.add(curTone + PinYinMarkEnum.WORD_SUFFIX.getToken()
									+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken() + length);
						}
					}

				}
			}
			return result;
		}

		private static int getTermLength(WordSegmentType type) {
			return type.getLength();
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println(NormalizeTokenFilter.nomalize("10号线"));
		System.out.println(NormalizeTokenFilter.nomalize("速8快捷"));
	}

}
