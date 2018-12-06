package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import java.io.Reader;

public class IC0FreqTokenizerAnalyzer extends Analyzer {

	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new TokenizerFreqTokenizer(new ICTokenizer(false, 0));
		return new TokenStreamComponents(source) {

			@Override
			protected void setReader(final Reader reader) {
				super.setReader(reader);
			}
		};

	}

}
