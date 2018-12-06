package org.elasticsearch.plugin.analysis.analyzer.pinyin.util;

import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.config.Config;
import com.homedo.bigdata.analysis.util.word.PinyinMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JianPinHelper {
	private static Set<Character> shoupinSet = new HashSet<Character>();
	private static final String shoupinStr = "bpmfdtnlgkhjqxrzcsywae";
	static {
		for (char c : shoupinStr.toCharArray()) {
			shoupinSet.add(Character.valueOf(c));
		}
	}

	public static List<String> getWordsShouPin(String keyword) throws IOException {
		return getWordsShouPin(keyword, null);
	}

	private static List<String> getWordsShouPin(String keyword, String[] correctPinyin) throws IOException {
		List<String> pinyinResult = new ArrayList<String>();
		int pinyinCount = 0;
		int offset = 0;
		for (char c : keyword.toCharArray()) {
			List<String> interResult = new ArrayList<String>();

			String[] pinyinArray = null;
			if (correctPinyin == null || correctPinyin[offset] == null) {
				pinyinArray = PinyinMapper.get().get(c);
			} else {
				pinyinArray = new String[1];
				pinyinArray[0] = correctPinyin[offset];
			}
			++offset;

			if (pinyinArray != null) {
				// uniqPinYinArray
				List<String> uniqShouPinArray = uniq(pinyinArray);
				if (pinyinCount <= 10) {
					pinyinCount = pinyinCount + uniqShouPinArray.size();
				} else {
					uniqShouPinArray = uniqShouPinArray.subList(0, 1);
				}
				if (pinyinResult.isEmpty()) {
					for (String pinyinItem : uniqShouPinArray) {
						interResult.add(pinyinItem);
					}
				} else {
					for (String pinyinItem : uniqShouPinArray) {
						for (String string : pinyinResult) {
							interResult.add(string + pinyinItem);
						}
					}
				}
			} else {
				if (pinyinResult.isEmpty()) {
					interResult.add(String.valueOf(c));
				} else {
					for (String string : pinyinResult) {
						interResult.add(string + String.valueOf(c));
					}
				}
			}

			pinyinResult = interResult;
		}

		return pinyinResult;
	}

	public static List<String> getWordsShouPins(String keyword) throws IOException {
		return getWordsShouPins(keyword, null);
	}

	public static List<String> getWordsShouPins(String keyword, String[] correctPinyin) throws IOException {
		List<String> result = null;
		boolean isJianPinIndexSimplify = Config.getInstance().getBoolean("jianpin.index.simplify", false);
		if (!isJianPinIndexSimplify) {
			result = getWordsShouPin(keyword, correctPinyin);
			String normalizedKeyword = NormalizeTokenFilter.nomalize(keyword);
			if (!keyword.equals(normalizedKeyword)) {
				result.addAll(getWordsShouPin(normalizedKeyword, correctPinyin));
			}
		} else {
			// For multilanguage
			result = getWordsShouPin_simplify(keyword, correctPinyin);
			String normalizedKeyword = NormalizeTokenFilter.nomalize(keyword);
			if (!keyword.equals(normalizedKeyword)) {
				result.addAll(getWordsShouPin_simplify(normalizedKeyword, correctPinyin));
			}
		}
		return result;
	}

	private static List<String> uniq(String[] pinyinArray) {
		if (pinyinArray.length == 1) {
			List<String> result = new ArrayList<String>(1);
			if (pinyinArray[0] != null && pinyinArray[0].length() > 0) {
				result.add(pinyinArray[0].substring(0, 1));
				return result;
			}
		}
		Set<String> hashSet = new HashSet<String>();
		for (int i = 0; i < pinyinArray.length; i++) {
			if (pinyinArray[i] != null && pinyinArray[i].length() > 0) {
				String item = pinyinArray[i].substring(0, 1);
				hashSet.add(item);
			}
		}
		List<String> result = new ArrayList<String>(hashSet);
		return result;
	}

	public static boolean isJianPinSequences(String str) {
		if (str == null) {
			return false;
		}
		for (char c : str.toCharArray()) {
			if (!shoupinSet.contains(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ShouPin Simplify method Only keep first pinyin character for first part
	 * splitted stop symbol
	 * 
	 * @param keyword
	 * @return
	 * @throws IOException
	 */
	public static List<String> getWordsShouPin_simplify(String keyword) throws IOException {
		return getWordsShouPin_simplify(keyword, null);
	}

	private static List<String> getWordsShouPin_simplify(String keyword, String[] correctPinyin) throws IOException {
		List<String> pinyinResult = new ArrayList<String>();
		int pinyinCount = 0;
		int offset = 0;
		if (keyword != null) {
			for (char c : keyword.toCharArray()) {
				List<String> interResult = new ArrayList<String>();

				String[] pinyinArray = null;
				if (correctPinyin == null || correctPinyin[offset] == null) {
					pinyinArray = PinyinMapper.get().get(c);
				} else {
					pinyinArray = new String[1];
					pinyinArray[0] = correctPinyin[offset];
				}
				++offset;

				if (pinyinArray != null) {
					// uniqPinYinArray
					List<String> uniqShouPinArray = uniq(pinyinArray);
					if (pinyinCount <= 10) {
						pinyinCount = pinyinCount + uniqShouPinArray.size();
					} else {
						uniqShouPinArray = uniqShouPinArray.subList(0, 1);
					}
					if (pinyinResult.isEmpty()) {
						for (String pinyinItem : uniqShouPinArray) {
							interResult.add(pinyinItem);
						}
					} else {
						for (String pinyinItem : uniqShouPinArray) {
							for (String string : pinyinResult) {
								interResult.add(string + pinyinItem);
							}
						}
					}
				} else {
					// If meet stop symbol, ignore left
					if (checkStopSymbol(String.valueOf(c))) {
						break;
					} else {
						// Other, combine into it
						if (pinyinResult.isEmpty()) {
							interResult.add(String.valueOf(c));
						} else {
							for (String string : pinyinResult) {
								interResult.add(string + String.valueOf(c));
							}
						}
					}
				}
				// If not empty, set into outside list
				if (!interResult.isEmpty()) {
					pinyinResult = interResult;
				}
			} // End for
		} // End if<keyword>
		return pinyinResult;
	}

	// Check whether contain stop symbol
	private static boolean checkStopSymbol(String str) throws PatternSyntaxException {
		String regEx = "[`\\\\~!@#$%^&*()+=|{}':;\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？, ]";
		String checkRegEx = regEx;
		Pattern p = Pattern.compile(checkRegEx);
		Matcher m = p.matcher(str);
		return m.find();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String[] cases = new String[] { "阿布戴尔 Aberdare abde Abudaier", "24K国际连锁酒店", "速8", "君悦(Grand Hyatt)", "JW万豪",
				"10号支线", "费尔蒙(Fairmont)", "上海118广场", "衛生署-控煙辦公室", "Rome", "长沙" };

		for (String str : cases) {
			System.out.println("------>  " + str);
			List<String> pinyinSequences = getWordsShouPin(str, null);
			System.out.println("PINYIN: " + pinyinSequences);
		}

	}

}
