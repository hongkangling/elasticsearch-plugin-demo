package org.elasticsearch.plugin.analysis.analyzer.pinyin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author kmchu
 * 
 */
public class GreedyStragyParser {
	private static final GreedyStragyParser instance = new GreedyStragyParser();
	private final PinYinBags bag;

	private GreedyStragyParser() {
		bag = PinYinBags.instance;
	}

	public static GreedyStragyParser getInstance() {
		return instance;

	}

	public List<String> parse(String str) {
		if ("".equals(str.trim())) {
			return Collections.emptyList();
		}
		PinYinIndex idx = bag.search(str.trim());
		if (!idx.isPinYin() && idx.getIndex() > -1) {
			return Arrays.asList(str.trim() + PinYinMarkEnum.PARTIAL_WORD_MARKER.getToken());
		}

		List<String> result = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			builder.append(str.charAt(i));
			PinYinIndex index = bag.search(builder.toString());
			if (!index.isPinYin()) {
				continue;
			} else {
				List<String> subResults = parse(str.substring(builder.length()));

				for (String subResult : subResults) {
					result.add(builder.toString() + PinYinMarkEnum.BLANK_DELIMITER.getToken() + subResult);
				}
			}
			if (builder.length() == str.length()) {
				result.add(str);
			}
		}

		return result;
	}
}
