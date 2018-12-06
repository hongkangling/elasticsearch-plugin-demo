package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.PrefixTagICTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;

public class PinYinCompositedTokenizer extends Tokenizer {
	private final TokenStream wordStream;
	private final Tokenizer icTokenizer;
	private final PinYinIndexTokenizer pyTokenizer;
	private final CharTermAttribute term;

	public PinYinCompositedTokenizer(Reader reader) {
		pyTokenizer = new PinYinIndexTokenizer(reader);
		icTokenizer = new ICTokenizer(false, 0);
		wordStream = new PrefixTagICTokenFilter(icTokenizer);
		term = addAttribute(CharTermAttribute.class);

		try {
			reset();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (wordStream.incrementToken()) {
			CharTermAttribute attr = wordStream.getAttribute(CharTermAttribute.class);
			term.copyBuffer(attr.buffer(), 0, attr.length());
			return true;
		}

		if (pyTokenizer.incrementToken()) {
			CharTermAttribute attr = pyTokenizer.getAttribute(CharTermAttribute.class);
			term.copyBuffer(attr.buffer(), 0, attr.length());
			return true;
		}

		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		wordStream.reset();
		icTokenizer.reset();
		pyTokenizer.reset();
	}
}
