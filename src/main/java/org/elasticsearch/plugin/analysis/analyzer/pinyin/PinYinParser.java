package org.elasticsearch.plugin.analysis.analyzer.pinyin;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kmchu
 * 
 * 
 */
public class PinYinParser {
	/**
	 * return any number immediately since it's not pinyin. There is nothing
	 * return if it's not pinyin.
	 */
	public static List<String> parse(String str) {
		if (str.length() > 0 && CharacterHelper.isCnDigit(str.charAt(0))) {
			List<String> results = new ArrayList<String>(1);
			results.add(str);
			return results;
		}
		List<String> longResult = GreedyStragyParser.getInstance().parse(str);
		return longResult;
	}

	public static void main(String[] args) {
		String[] inputs = new String[] {
				// "rujiajiudian","xiandayanta","xierdun","hilton","london","99liansuo","rjkj","xier","jiudian",
				"dian", "xian", "zhon", "xi", "xa", "x", "118" };
		for (String input : inputs) {
			System.out.println("-------input: " + input + System.currentTimeMillis());
			System.out.println("parsed result: " + PinYinParser.parse(input));
			System.out.println("-------Finish: " + System.currentTimeMillis());
		}

		// String[] segments = new String[]{"七t","九九liansuo","rujia"};
		// for(String seg:segments){
		// System.out.println(" ------------ ----------------------" + seg);
		// System.out.println("" + parser.split(seg));
		// }
	}
}
