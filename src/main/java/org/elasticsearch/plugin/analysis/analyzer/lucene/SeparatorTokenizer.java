package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

public class SeparatorTokenizer extends CharTokenizer {

	final int separator;

	public SeparatorTokenizer(int separator) {
		this.separator = separator;
	}

	public SeparatorTokenizer(AttributeFactory factory, int separator) {
		super(factory);
		this.separator = separator;
	}

	protected boolean isTokenChar(int c) {
		return c != separator;
	}

}
