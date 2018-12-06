package org.elasticsearch.plugin.analysis.analyzer.pinyin.util;

import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import com.homedo.bigdata.analysis.util.word.PinyinMapper;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PinYinHelper {
	private final static Logger logger = Loggers.getLogger(PinYinHelper.class);

	public static List<String> getWordsPinYin(String keyword, String defaultPinYin) throws IOException {
		List<String> result = InputSplitter.split(keyword);
		List<String> pinyinSequences = PinYinPermutor.permute(result);
		pinyinSequences = PinYinFilter.filter(pinyinSequences, defaultPinYin);
		List<String> composedSegments = PinYinMarker.mark(pinyinSequences, result);
		return composedSegments;
	}

	static class InputSplitter {
		public static List<String> split(String keywords) {
			List<String> result = new ArrayList<String>();
			StringBuilder builder = new StringBuilder();
			for (char c : keywords.toCharArray()) {
				if (CharacterHelper.isCJKCharacter(c, false)) {
					if (builder.length() > 0) {
						result.add(builder.toString());
						builder.delete(0, builder.length());
					}
					result.add(String.valueOf(c));
				}

				else if (Character.isDigit(c)) {
					if (builder.length() > 0 && !Character.isDigit(builder.charAt(0))) {
						result.add(builder.toString());
						builder.delete(0, builder.length());
					}
					builder.append(c);
				} else if (Character.isLetter(c)) {
					if (builder.length() > 0 && !Character.isLetter(builder.charAt(0))) {
						result.add(builder.toString());
						builder.delete(0, builder.length());
					}
					builder.append(c);
				} else {
					if (builder.length() > 0) {
						result.add(builder.toString());
						builder.delete(0, builder.length());
					}
				}
			}
			if (builder.length() > 0) {
				result.add(builder.toString());
			}
			return result;
		}

	}

	static class PinYinPermutor {
		public static List<String> permute(List<String> results) throws IOException {
			List<String> pinyinResult = new ArrayList<String>();

			for (String str : results) {
				List<String> interResult = new ArrayList<String>();
				String[] pinyinArray = PinyinMapper.get().get(str.charAt(0));
				if (pinyinArray != null) {
					if (pinyinResult.isEmpty()) {
						for (String pinyinItem : pinyinArray) {
							interResult.add(pinyinItem);
						}
					} else {
						for (String pinyinItem : pinyinArray) {
							for (String string : pinyinResult) {
								interResult.add(string + " " + pinyinItem);
							}
						}
					}
				} else {
					if (CharacterHelper.isCJKCharacter(str.charAt(0))) {
						logger.debug("there is no pinyin for character[ " + str.charAt(0) + "]");
					}
					if (pinyinResult.isEmpty()) {
						interResult.add(str);
					} else {
						for (String string : pinyinResult) {
							interResult.add(string + " " + str);
						}
					}
				}

				pinyinResult = interResult;
			}

			return pinyinResult;
		}
	}

	static class PinYinFilter {
		public static List<String> filter(List<String> sequences, String pinyin) {
			if (pinyin == null || "".equals(pinyin.trim())) {
				return sequences;
			}
			List<String> filtedSequences = new ArrayList<String>(sequences.size());
			for (String seq : sequences) {
				if (pinyin.equals(seq.replaceAll("\\s+", ""))) {
					filtedSequences.add(seq);
				}
			}

			if (filtedSequences.isEmpty()) {
				return sequences;
			}

			return filtedSequences;
		}
	}

	static class PinYinMarker {
		public static List<String> mark(List<String> pinyinSequences, List<String> splitResult) {

			List<String> result = new ArrayList<String>();
			for (String seq : pinyinSequences) {
				StringBuilder builder = new StringBuilder();

				String[] items = seq.split("\\s");
				if (items.length != splitResult.size()) {
					logger.debug(seq + " is not consistent with " + splitResult);
					if (items.length < splitResult.size()) {
						continue;
					}
				}
				List<String> composed = new ArrayList<String>();
				// filter unmatch case "dunhilton" and seperate ewords
				boolean isFirstCnChar = false;
				for (int i = 0; i < splitResult.size(); i++) {
					String str = splitResult.get(i);
					if (str.charAt(0) >= 'A' && str.charAt(0) <= 'z') {
						composed.add(i == 0
								? PinYinMarkEnum.STARTS.getToken() + PinYinMarkEnum.EWORD_PREFIX.getToken() + items[i]
								: PinYinMarkEnum.EWORD_PREFIX.getToken() + items[i]);
					} else {
						String token = items[i];
						if (!isFirstCnChar) {
							composed.add(PinYinMarkEnum.STARTS.getToken() + token);
							isFirstCnChar = true;
						} else {
							composed.add(token);
						}
					}

				}

				for (int i = 0; i < composed.size(); i++) {
					builder.append(composed.get(i) + " ");
				}
				result.add(builder.toString().trim());
			}

			return result;
		}

	}

	public static void main(String[] args) throws IOException {
		try {
			String[] cases = new String[] { "赛特医院（石狮）", "24K国际连锁酒店", "速8", "君悦(Grand Hyatt)", "JW万豪", "10号支线",
					"费尔蒙(Fairmont)", "上海118广场", "衛生署-控煙辦公室", "Rome", "英雄super's酒吧", "天 top 地" };
			for (String str : cases) {
				System.out.println("------>  " + str);
				List<String> result = InputSplitter.split(str);
				List<String> pinyinSequences = PinYinPermutor.permute(result);
				System.out.println("Split :" + result);
				System.out.println("PINYIN: " + pinyinSequences);
				System.out.println("Composed:" + PinYinMarker.mark(pinyinSequences, result));
			}

		} finally {
			System.exit(0);
		}
	}

}
