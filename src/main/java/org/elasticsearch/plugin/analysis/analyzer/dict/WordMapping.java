package org.elasticsearch.plugin.analysis.analyzer.dict;

public class WordMapping {
	/**
	 * 纠错词映射词
	 * 
	 * @param input
	 * @param typeid
	 * @return
	 */
	public static String spellCheckMapping(String input) {
		String output = "";
		if (Dictionary.getInstance().spellCheckWordMapping.containsKey(input.toLowerCase())) {
			output = Dictionary.getInstance().spellCheckWordMapping.get(input.toLowerCase());
		}
		return null != output ? output.toLowerCase() : output;
	}

	/**
	 * 同义词映射词(多个以|分隔)
	 * 
	 * @param input
	 * @return
	 */
	public static String synonymMapping(String input) {
		String output = "";
		if (Dictionary.getInstance().synonymWordMapping.containsKey(input)) {
			output = Dictionary.getInstance().synonymWordMapping.get(input);
		}
		return null != output ? output.toLowerCase() : output;
	}

	public static String getWordType(int typeid) {
		if (Dictionary.getInstance().wordType == null || Dictionary.getInstance().wordType.isEmpty())
			return "";
		String output = "";
		if (Dictionary.getInstance().wordType.containsKey(typeid)) {
			output = Dictionary.getInstance().wordType.get(typeid);
		}
		return output;
	}

	public static String getWordTypes(String types) {
		StringBuilder typename = new StringBuilder();
		String[] typeids = types.split(",");
		for (String id : typeids) {
			int i = 0;
			try {
				i = Integer.parseInt(id);
			} catch (NumberFormatException e) {
			}
			if (typename.length() > 0) {
				typename.append(",");
			}
			typename.append(getWordType(i));
		}
		return typename.toString();
	}
}
