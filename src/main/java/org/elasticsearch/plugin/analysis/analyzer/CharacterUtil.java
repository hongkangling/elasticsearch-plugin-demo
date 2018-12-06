package org.elasticsearch.plugin.analysis.analyzer;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;

/**
 * 字符集识别工具类
 */
public class CharacterUtil {
    /**
     * 未知
     */
    public static final int CHAR_USELESS = 0;
    /**
     * 阿拉伯数字
     */
    public static final int CHAR_ARABIC = 100;
    /**
     * 英文字母
     */
    public static final int CHAR_ENGLISH = 20;
    /**
     * 中文字符
     */
    public static final int CHAR_CHINESE = 101;
    /**
     * 日韩字符
     */
    public static final int CHAR_OTHER_CJK = 102;

    /**
     * 自定义可用符号
     */
    public static final int CHAR_SYMBOL = 103;

    /**
     * 识别字符类型
     *
     * @return int CharacterUtil定义的字符类型常量
     */
    public static int identifyCharType(char input) {
        if (input >= '0' && input <= '9') {
            return CHAR_ARABIC;
        } else if ((input >= 'a' && input <= 'z') || (input >= 'A' && input <= 'Z')) {
            return CHAR_ENGLISH;
        } else if (input == '?' || input == '&' || input == '-') {
            //自定义可用符号
            return CHAR_SYMBOL;
        } else if (input == '，'
            || input == '('
            || input == ')'
            || input == '）'
            || input == '（'//中文逗号
            ) {
            //自定义无效字符
            return CHAR_USELESS;
        } else {
            Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);

            if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
                // 目前已知的中文字符UTF-8集合
                return CHAR_CHINESE;
            } else if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                // 全角数字字符和日韩字符
                // 韩文字符集
                || ub == Character.UnicodeBlock.HANGUL_SYLLABLES
                || ub == Character.UnicodeBlock.HANGUL_JAMO
                || ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                // 日文字符集
                || ub == Character.UnicodeBlock.HIRAGANA
                // 平假名
                || ub == Character.UnicodeBlock.KATAKANA
                // 片假名
                || ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS) {
                return CHAR_OTHER_CJK;
            }
        }
        // 其他的不做处理的字符
        return CHAR_USELESS;
    }

    /**
     * 进行字符规格化（全角转半角，大写转小写处理）
     *
     * @return char
     */
    static char regularize(char input) {
        if (input == 12288) {
            input = (char) 32;
        } else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);
        } else if (input >= 'A' && input <= 'Z') {
            input += 32;
        }

        return input;
    }

    public static String processNumber(String word) {
        char[] wd = word.toCharArray();
        boolean hasNumber = false;
        for (int i = 0; i < wd.length; i++) {
            if (CharacterHelper.isArabicNumber(wd[i])) {
                hasNumber = true;
                int ind = wd[i] - 48;
                wd[i] = CharacterHelper.CnNum[ind];
            } else {
                int cnNum = CharacterHelper.isCnNumber(wd[i]);
                if (cnNum > -1) {
                    hasNumber = true;
                    wd[i] = CharacterHelper.ArabicNum[cnNum];
                }
            }
        }
        if (hasNumber) {
            return String.valueOf(wd);
        }
        return "";
    }
}
