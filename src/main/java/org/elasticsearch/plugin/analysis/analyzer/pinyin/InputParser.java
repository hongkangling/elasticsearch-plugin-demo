package org.elasticsearch.plugin.analysis.analyzer.pinyin;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kmchu
 */
public class InputParser {

	public static List<String> parse(String queryStr) {
		List<String> segments = InputSplitter.split(queryStr);

		List<String> result = PinYinParser.parse(segments.get(0));
		for (int i = 1; i < segments.size(); i++) {
			result = ResultMerger.merge(result, PinYinParser.parse(segments.get(i)));
		}
		// result = ResultCompositor.composite(result);
		result = PinYinMarker.mark(result);
		return result;
	}

	static class InputSplitter {

		public static List<String> split(String segment) {
			StringBuilder builder = new StringBuilder();
			boolean isChar = true;
			for (int index = 0; index < segment.length(); index++) {
				char c = segment.charAt(index);
				if ((isChar && c >= 'a' && c <= 'z') || (!isChar && CharacterHelper.isCnDigit(c))) {
					builder.append(c);
				} else {
					builder.append(PinYinMarkEnum.BLANK_DELIMITER.getToken() + c);
					isChar = !isChar;
				}
			}
			return Arrays.asList(builder.toString().trim().split("\\s"));
		}
	}

	static class ResultMerger {

		public static List<String> merge(List<String> preList, List<String> siblingList) {
			List<String> result = new ArrayList<String>(preList.size() * siblingList.size());
			for (String preStr : preList) {
				for (String siblingStr : siblingList) {
					result.add(preStr + PinYinMarkEnum.BLANK_DELIMITER.getToken() + siblingStr);
				}
			}

			return result;
		}
	}

	static class ResultCompositor {
		public static List<String> composite(List<String> result) {
			List<String> compositedResults = new ArrayList<String>(result.size());
			for (String str : result) {
				if (!str.contains(PinYinMarkEnum.BLANK_DELIMITER.getToken())) {
					compositedResults.add(str);
					continue;
				}

				StringBuilder builder = new StringBuilder();
				String[] arrays = str.split("\\s");
				for (int i = 0; i < arrays.length - 1; i++) {
					builder.append(arrays[i] + arrays[i + 1] + PinYinMarkEnum.BLANK_DELIMITER.getToken());
				}
				// builder.append(arrays[arrays.length-1]);

				compositedResults.add(builder.toString().trim());
			}
			return compositedResults;
		}

	}

	static class PinYinMarker {
		public static List<String> mark(List<String> result) {
			List<String> results = new ArrayList<String>(2 * result.size());
			for (String str : result) {
				results.add(str);
				if (str.trim().endsWith(PinYinMarkEnum.PARTIAL_WORD_MARKER.getToken())) {
					continue;
				}
				String lastSegment = str.trim();
				if (lastSegment.contains(" ")) {
					lastSegment = lastSegment.substring(lastSegment.lastIndexOf(' ') + 1);
				}
				boolean flag = PinYinBags.instance.isPotentialPrefix(lastSegment);
				if (flag) {
					results.add(str.trim() + PinYinMarkEnum.PARTIAL_WORD_MARKER.getToken());
				}
			}
			return results;
		}

	}

	public static void main(String[] args) {
		String[] segments = new String[] {
				// "七t",
				"九九liansuo", "rujia", "rujiajiudian", "xiandayanta", "xierdun", "hilton", "london",
				// "99liansuo",
				"rjkj", "xier", "jiudian", "dian", "xian", "zhon", "huashe", "shang" };
		for (String seg : segments) {
			System.out.println("------------  ----------------------" + seg + System.currentTimeMillis());
			System.out.println(InputParser.parse(seg));
			System.out.println("++++++++++++++++++++++++++++++++++++" + System.currentTimeMillis());
		}
	}
}
