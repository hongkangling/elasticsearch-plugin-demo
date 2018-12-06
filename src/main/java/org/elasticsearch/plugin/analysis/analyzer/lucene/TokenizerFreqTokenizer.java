package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;

public class TokenizerFreqTokenizer extends FreqTokenizer<Tokenizer> {

	public TokenizerFreqTokenizer(Tokenizer stream) {
		super(stream);
	}

	@Override
	public void reuseTokenStream(Tokenizer stream) throws IOException {
		stream.reset();
	}

}
