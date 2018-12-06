package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenfilter;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class JianPinTokenFilter extends TokenFilter {
	private final int maxBackedLen;
	private final int minBackedLen;
	private final boolean isQuery;
	private final CharTermAttribute termAtt;
	private int bufferIndex;
	private char[] termBuffer;
	private int step;
	private int length;

	public JianPinTokenFilter(TokenStream input, int maxLength, int minLength, boolean isQuery) {
		super(input);
		maxBackedLen = maxLength;
		bufferIndex = -1;
		this.isQuery = isQuery;
		termAtt = input.getAttribute(CharTermAttribute.class);
		minBackedLen = minLength;
		step = minBackedLen;
		if (minBackedLen > maxBackedLen) {
			throw new IllegalArgumentException(
					"minBackedLen:[" + minBackedLen + "] should less than maxBackedLen:[" + maxBackedLen + "]");
		}
		termBuffer = new char[32];
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (bufferIndex == -1) {
			if (!input.incrementToken()) {
				return false;
			}
			length = termAtt.length();

			char[] buffer = termAtt.buffer();
			for (int i = 0; i < length; i++) {// check need backed
				char ch = buffer[i];
				if (CharacterHelper.isCnDigit(ch) || ch >= 'a' && ch <= 'z') {
					bufferIndex = 0;
					break;
				}
			}
			if (bufferIndex == 0) {
				if (length > termBuffer.length) {
					termBuffer = new char[length * 2];
				}
				for (int i = 0; i < length; i++) {// copy term buffer
					termBuffer[i] = buffer[i];
				}
				return incrementToken();
			}

		} else {
			if (isQuery) {
				if (length - bufferIndex <= maxBackedLen) {
					termAtt.copyBuffer(termBuffer, bufferIndex, length - bufferIndex);
					bufferIndex = -1;
				} else {
					termAtt.copyBuffer(termBuffer, bufferIndex, maxBackedLen);
					bufferIndex++;
				}
			} else {// for index
				if (length >= maxBackedLen) {
					termAtt.copyBuffer(termBuffer, bufferIndex, step);
					step++;
					if (step > maxBackedLen) {
						step = minBackedLen;
						bufferIndex++;
					}
					if (bufferIndex + maxBackedLen > length) {
						bufferIndex = -1;
						step = minBackedLen;
					}
				} else {
					bufferIndex = -1;
					step = minBackedLen;
				}
			}
		}
		return true;

	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = -1;
		step = minBackedLen;
	}
}
