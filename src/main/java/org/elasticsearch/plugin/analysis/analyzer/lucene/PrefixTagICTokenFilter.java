package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;

public class PrefixTagICTokenFilter extends TokenFilter {
	private final CharTermAttribute termAttr;

	private final OffsetAttribute offAttr;
	private char[] TERM_BUFFER = new char[256];
	private char[] firstTerm = null;
	private int startOffsetOfWord = -1;

	public PrefixTagICTokenFilter(TokenStream input) {
		super(input);
		termAttr = input.getAttribute(CharTermAttribute.class);
		offAttr = input.hasAttribute(OffsetAttribute.class) ? input.getAttribute(OffsetAttribute.class) : null;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (firstTerm != null) {
			termAttr.copyBuffer(firstTerm, 0, termAttr.length() + 1);
			firstTerm = null;
			return true;
		}
		if (input.incrementToken()) {
			if (offAttr != null) {
				if (startOffsetOfWord == -1) {
					startOffsetOfWord = offAttr.startOffset();
				}
				if (offAttr.startOffset() == startOffsetOfWord) {
					if (TERM_BUFFER.length < termAttr.length() + 1) {
						TERM_BUFFER = new char[termAttr.length() + 1];
					}
					firstTerm = TERM_BUFFER;
					System.arraycopy(termAttr.buffer(), 0, firstTerm, 1, termAttr.length());
					firstTerm[0] = PinYinMarkEnum.STARTS.getChar();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		firstTerm = null;
		startOffsetOfWord = -1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
