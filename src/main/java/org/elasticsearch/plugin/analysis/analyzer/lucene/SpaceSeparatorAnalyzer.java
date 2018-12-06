package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;

public class SpaceSeparatorAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new SeparatorTokenizer(' '));
	}

}
