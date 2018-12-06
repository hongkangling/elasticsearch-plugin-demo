package org.elasticsearch.plugin.analysis.util.word;

import java.util.Arrays;

public class CharacterHelper {

	public static boolean isSpaceLetter(char input) {
		return input == 8 || input == 9 || input == 10 || input == 13 || input == 32 || input == 160;
	}

	public static boolean isEnglishLetter(char input) {
		return (input >= 'a' && input <= 'z') || (input >= 'A' && input <= 'Z');
	}

	public static boolean isArabicNumber(char input) {
		return input >= '0' && input <= '9';
	}

	public static final char[] CnNum = { '零', '一', '二', '三', '四', '五', '六', '七', '八', '九' };// CnNum
	public static final char[] ArabicNum = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static boolean[] CnNumMap;
	static {
		char maxCh = 0;
		for (char ch : CnNum) {
			if (ch > maxCh) {
				maxCh = ch;
			}
		}
		CnNumMap = new boolean[maxCh + 1];
		for (char ch : CnNum) {
			CnNumMap[ch] = true;
		}
	}

	public static boolean isCnDigit(char ch) {
		return ch < CnNumMap.length && CnNumMap[ch];
	}

	public static int isCnNumber(char input) {
		for (int i = 0; i < CnNum.length; i++) {
			if (input == CnNum[i])
				return i;
		}
		return -1;
	}

	public static boolean isCJKCharacter(char input) {
		return isCJKCharacter(input, true);
	}

	public static boolean isCJKLN(char input) {
		return isCJKCharacter(input, false) || isEnglishLetter(input) || isArabicNumber(input);
	}

	public static boolean isCJKCharacter(Character.UnicodeBlock ub) {
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
		// //全角数字字符和日韩字符
		// || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
		// //韩文字符集
		// || ub == Character.UnicodeBlock.HANGUL_SYLLABLES
		// || ub == Character.UnicodeBlock.HANGUL_JAMO
		// || ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
		// //日文字符集
		// || ub == Character.UnicodeBlock.HIRAGANA //平假名
		// || ub == Character.UnicodeBlock.KATAKANA //片假名
		// || ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
		) {
			return true;
		} else {
			return false;
		}
		// 其他的CJK标点符号，可以不做处理
		// || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
		// || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
	}

	public static boolean isCJKCharacter(int codePoint, boolean includeSymbol) {
		if (!includeSymbol && isCJKSymbol(codePoint)) {
			return false;
		}
		return isCJKCharacter(Character.UnicodeBlock.of(codePoint));
	}

	public static boolean isCJKCharacter(char input, boolean includeSymbol) {
		if (!includeSymbol && isCJKSymbol(input)) {
			return false;
		}
		return isCJKCharacter(Character.UnicodeBlock.of(input));
	}

	public static boolean isCJKSymbol(int input) {
		if (input == 12288 || input > 65280 && input < 65375) {
			return true;
		}
		return false;
	}

	/**
	 * 进行字符规格化（全角转半角，大写转小写处理）
	 * 
	 * @param input
	 * @return char
	 */
	public static char regularize(char input) {
		if (input == 12288) {
			input = (char) 32;

		} else if (input > 65280 && input < 65375) {
			input = (char) (input - 65248);
		}
		return toLower(input);
	}

	/**
	 * 大写转小写
	 * 
	 * @param input
	 * @return
	 */
	public static char toLower(char input) {
		if (input >= 'A' && input <= 'Z') {
			input += 32;
		}
		return input;
	}

	public static String regularize(String input) {
		return null;
	}

	public static int normalize(int c) {
		return normalize(c, true);
	}

	public static int normalize(int c, boolean arabicNum2CnNum) {
		if (c == 12288) {
			c = 32;
		} else if (c > 65280 && c < 65375) {
			c -= 65248;
		}
		if (c >= 'A' && c <= 'Z') {
			c += 32;
		} else if (c >= '0' && c <= '9') {
			c = arabicNum2CnNum ? CnNum[c - '0'] : c;
		}
		return c;
	}


	public static int[] toCodePoints(String str) {
		int[] codePoints = new int[str.length()];
		int j = 0;
		for (int i = 0; i < str.length();) {
			int c = str.codePointAt(i);
			codePoints[j++] = c;
			i += Character.charCount(c);
		}
		if (j < str.length()) {
			codePoints = Arrays.copyOf(codePoints, j);
		}
		return codePoints;
	}
}
