package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public class GenericFreqIndexAnalyzer extends Analyzer {

	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source = new ICTokenizer(false, 0);
		Tokenizer freqTokenizer = new FreqTokenizer<TokenStream>(
				new SynonymTokenFilter(new NormalizeTokenFilter(source))) {
			@Override
			public void reuseTokenStream(TokenStream stream) throws IOException {
				source.reset();
			}
		};

		return new TokenStreamComponents(freqTokenizer) {
			@Override
			public void setReader(final Reader reader) {
				super.setReader(reader);
			}
		};
	}

}
