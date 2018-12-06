package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenfilter;

import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;

/**
 * Mark
 * 
 **/
public class MarkICTokenFilter extends TokenFilter {
	private final CharTermAttribute termAttr;

	private final OffsetAttribute offAttr;
	private char[] TERM_BUFFER = new char[256];
	private char[] firstTerm = null;
	private int startOffsetOfWord = -1;
	private int length;

	public MarkICTokenFilter(TokenStream input) {
		super(input);
		termAttr = input.getAttribute(CharTermAttribute.class);
		offAttr = input.hasAttribute(OffsetAttribute.class) ? input.getAttribute(OffsetAttribute.class) : null;
		length = -1;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (firstTerm != null) {
			termAttr.copyBuffer(firstTerm, 0, length);
			firstTerm = null;
			return true;
		}
		if (input.incrementToken()) {
			if (offAttr != null) {
				if (startOffsetOfWord == -1) {
					startOffsetOfWord = offAttr.startOffset();
				}
				int termLength = termAttr.length();
				if (TERM_BUFFER.length < termAttr.length() + 1) {
					TERM_BUFFER = new char[termAttr.length() * 2];
				}
				firstTerm = TERM_BUFFER;
				if (offAttr.startOffset() == startOffsetOfWord) {

					if (isEnglishWord(termAttr.buffer(), termAttr.length())) {
						System.arraycopy(termAttr.buffer(), 0, firstTerm, 2, termLength);
						firstTerm[0] = PinYinMarkEnum.STARTS.getChar();
						firstTerm[1] = PinYinMarkEnum.EWORD_PREFIX.getChar();
						firstTerm[termLength + 2] = PinYinMarkEnum.WORD_SUFFIX.getChar();
						length = termLength + 3;
						termAttr.copyBuffer(firstTerm, 1, length - 1);
					} else {
						System.arraycopy(termAttr.buffer(), 0, firstTerm, 1, termLength);
						firstTerm[0] = PinYinMarkEnum.STARTS.getChar();
						length = termLength + 1;
						termAttr.copyBuffer(firstTerm, 1, length - 1);
					}
				} else {
					if (isEnglishWord(termAttr.buffer(), termAttr.length())) {
						System.arraycopy(termAttr.buffer(), 0, firstTerm, 1, termLength);
						firstTerm[0] = PinYinMarkEnum.EWORD_PREFIX.getChar();
						firstTerm[termLength + 1] = PinYinMarkEnum.WORD_SUFFIX.getChar();
						length = termLength + 2;
						return incrementToken();
					} else {
						firstTerm = null;
					}
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
		length = -1;
	}

	private boolean isEnglishWord(char[] array, int arraySize) {
		for (int i = 0; i < arraySize; i++) {
			if (!Character.isUpperCase(array[i]) && !Character.isLowerCase(array[i])) {
				return false;
			}
		}
		return true;
	}
}
