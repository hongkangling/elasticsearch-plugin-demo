package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;

public class LNBackedTokenFilter extends TokenFilter {
	public static int BUFFER_LENGTH = 32;
	private final int maxBackedLen;
	private final int queryStep;
	private final int initialLen;

	private final CharTermAttribute termAtt;
	private final OffsetAttribute offAtt;
	private final TermTypeAttribute typeAtt;
	private int bufferIndex = -1;
	private char[] termBuffer;
	private int termLength;
	private int startOffset;
	private int backedLen;

	/**
	 * 
	 * @param input
	 * @param maxBackedLen
	 * @param queryStep
	 *            if > 0 for query else for step
	 */
	public LNBackedTokenFilter(TokenStream input, int maxBackedLen, int queryStep) {
		super(input);
		if (maxBackedLen < 1) {
			throw new IllegalArgumentException("maxBackedLen:" + maxBackedLen + " must greater than 0");
		}
		this.maxBackedLen = maxBackedLen;
		this.queryStep = queryStep;
		initialLen = queryStep > 0 ? maxBackedLen : 1;

		termAtt = input.getAttribute(CharTermAttribute.class);
		offAtt = input.hasAttribute(OffsetAttribute.class) ? input.getAttribute(OffsetAttribute.class) : null;
		typeAtt = input.hasAttribute(TermTypeAttribute.class) ? input.getAttribute(TermTypeAttribute.class) : null;
		termBuffer = new char[BUFFER_LENGTH];
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (bufferIndex == -1) {
			if (!input.incrementToken()) {
				return false;
			}
			termLength = termAtt.length();
			if (termLength > initialLen) {
				char[] buffer = termAtt.buffer();
				bufferIndex = -1;
				for (int i = 0; i < termLength; i++) {// check need backed
					char ch = buffer[i];
					if (CharacterHelper.isCnDigit(ch) || ch >= 'a' && ch <= 'z') {
						bufferIndex = 0;
						break;
					}
				}
				if (bufferIndex == 0) {
					if (termLength > termBuffer.length) {
						termBuffer = new char[termLength * 2];
					}
					for (int i = 0; i < termLength; i++) {// copy term buffer
						termBuffer[i] = buffer[i];
					}

					if (offAtt != null) {
						startOffset = offAtt.startOffset();
					}
					backedLen = initialLen;
					if (queryStep > 0) {// for query
						return incrementToken();
					}
				}
			}
		} else {
			termAtt.copyBuffer(termBuffer, bufferIndex, backedLen);
			if (offAtt != null) {
				offAtt.setOffset(startOffset + bufferIndex, startOffset + bufferIndex + backedLen);
			}
			if (typeAtt != null) {
				typeAtt.setType(21);
			}
			if (queryStep > 0) {// for query
				int step = queryStep;
				for (; step > 0; step--) {
					if (bufferIndex + step + backedLen <= termLength) {
						break;
					}
				}
				if (step > 0) {
					bufferIndex += step;
				} else {
					bufferIndex = -1;
				}
			} else {// for index
				if (++backedLen > maxBackedLen || bufferIndex + backedLen > termLength) {
					backedLen = 1;
					if (++bufferIndex == termLength) {
						bufferIndex = -1;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = -1;
	}

}
