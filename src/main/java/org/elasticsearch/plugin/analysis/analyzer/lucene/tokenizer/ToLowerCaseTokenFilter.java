package org.elasticsearch.plugin.analysis.analyzer.lucene.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.AbstractTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class ToLowerCaseTokenFilter extends AbstractTokenFilter {
	private final CharTermAttribute termAtt;

	public ToLowerCaseTokenFilter(TokenStream in) {
		super(in);
		termAtt = addAttribute(CharTermAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			normalizeI(termAtt.buffer());
			return true;
		} else {
			return false;
		}
	}

	private void normalizeI(char[] buffer) {
		if (buffer == null) {
			return;
		}
		for (int i = 0; i < buffer.length;) {
			i += Character.toChars(Character.toLowerCase(buffer[i]), buffer, i);
		}
	}

	@Override
	public void reset() throws IOException {
		super.clearAttributes();
		super.reset();
	}

}
