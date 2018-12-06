package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenfilter;

import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * Require tokenizer which output "word:3" which represents "word" ":" the start
 * index.
 * 
 **/
public class PinYinTokenFilter extends TokenFilter {
	private final CharTermAttribute termAtt;
	private int bufferIndex;
	private char[] termBuffer;
	private int termBufferLength;
	private int length;

	public PinYinTokenFilter(TokenStream input) {
		super(input);
		bufferIndex = -1;
		termAtt = input.getAttribute(CharTermAttribute.class);
		termBuffer = new char[32];
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (bufferIndex == -1) {
			if (!input.incrementToken()) {
				return false;
			}
			termBufferLength = termAtt.length();

			char[] buffer = termAtt.buffer();

			int lastIndex = findSeperator(buffer, termBufferLength);
			if (lastIndex < 0 || lastIndex > termBufferLength - 1) {
				// check illgalargument format. Improve error-capacity.
				return true;
			}
			length = lastIndex;
			extractBufferIndex(lastIndex);

			copyBufferAndExtendBufferIfNecessary(buffer);

			return incrementToken();
		} else {
			termAtt.copyBuffer(termBuffer, 0, bufferIndex++);
			if (bufferIndex > length) {
				bufferIndex = -1;
			}
		}
		return true;

	}

	private void extractBufferIndex(int lastIndex) {
		int startIndex = -1;
		try {
			startIndex = Integer.valueOf(String.valueOf(termAtt.subSequence(lastIndex + 1, termBufferLength)));
		} catch (NumberFormatException e) {
			startIndex = -1;
		}
		if (startIndex < 0 || startIndex > lastIndex) {
			bufferIndex = lastIndex;
		} else {
			bufferIndex = startIndex;
		}
	}

	private void copyBufferAndExtendBufferIfNecessary(char[] buffer) {
		if (length > termBuffer.length) {
			termBuffer = new char[length * 2];
		}
		for (int i = 0; i < length; i++) {
			termBuffer[i] = buffer[i];
		}

	}

	private int findSeperator(char[] buffer, int length) {
		for (int i = length - 1; i > -1; i--) {
			if (buffer[i] == PinYinMarkEnum.TERM_LENGTH_DELIMITER.getChar()) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = -1;
	}
}
