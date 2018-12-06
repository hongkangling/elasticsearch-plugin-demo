package org.elasticsearch.plugin.analysis.analyzer.pinyin.util;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;

import java.util.ArrayList;
import java.util.List;

public class QuerySplitter {
	public static List<String> tokenizeQueryParams(String keywords) {
		List<String> result = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		for (char c : keywords.toCharArray()) {
			if (CharacterHelper.isCJKCharacter(c, false)) {
				if (builder.length() > 0) {
					result.add(builder.toString());
					builder.delete(0, builder.length());
				}
				// result.add(String.valueOf(c));
			}

			else if (Character.isLetterOrDigit(c)) {
				if (builder.length() > 0 && !Character.isLetterOrDigit(c)) {
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
