package org.elasticsearch.plugin.analysis.analyzer.pinyin;

public enum PinYinMarkEnum {
	STARTS("^"), //
	WORD_SUFFIX("$"), //
	EWORD_PREFIX("_"), //
	ABBR_PREFIX("#"), //
	BLANK_DELIMITER(" "), PARTIAL_WORD_MARKER("-"), TERM_LENGTH_DELIMITER(":");

	private String token;
	private char c;

	private PinYinMarkEnum(String token, char c) {
		this.token = token;
		this.c = c;
	}

	private PinYinMarkEnum(String token) {
		this.token = token;
		if (token != null && token.length() > 0) {
			this.c = token.charAt(0);
		}
	}

	public String getToken() {
		return token;
	}

	public char getChar() {
		return c;
	}
}
