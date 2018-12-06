package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.TokenStream;

public class IC0FreqTokeFilterAnalyzer extends IC0Analyzer {

	@Override
	protected AbstractTokenFilter createResult(TokenStream source) {
		return new FreqTokenFilter(source);
	}

}
