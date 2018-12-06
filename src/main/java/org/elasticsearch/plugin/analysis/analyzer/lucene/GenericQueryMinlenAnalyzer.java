package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public class GenericQueryMinlenAnalyzer extends Analyzer {

	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new ICTokenizer(false, 2);
		TokenStream result = new NormalizeTokenFilter(source);
		return new TokenStreamComponents(source, result) {
			@Override
			protected void setReader(final Reader reader) {
				super.setReader(reader);
				try {
					sink.reset();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

}
