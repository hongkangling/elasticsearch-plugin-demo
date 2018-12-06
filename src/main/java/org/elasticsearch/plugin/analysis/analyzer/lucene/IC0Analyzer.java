package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.Reader;

public abstract class IC0Analyzer extends Analyzer {

	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new ICTokenizer(false, 0);
		final AbstractTokenFilter result = createResult(source);
		return new TokenStreamComponents(source, result) {
			@Override
			protected void setReader(final Reader reader) {
				super.setReader(reader);
				result.reinit();
			}
		};

	}

	protected abstract AbstractTokenFilter createResult(TokenStream source);

}
