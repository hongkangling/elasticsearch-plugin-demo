package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenizer;

import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;

public class PinYinIndexTokenizer extends Tokenizer {
	private final CharTermAttribute term;

	private String preToken;
	private int bufferIndex;
	private char[] buffer;

	public PinYinIndexTokenizer(Reader reader) {
		this.input = reader;
		term = addAttribute(CharTermAttribute.class);
		bufferIndex = -1;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (bufferIndex == -1) {
			String token = popToken();
			if (token == null) {
				return false;
			}
			if (preToken != null && isPinYinToken(preToken) && isPinYinToken(token)) {
				if (!token.startsWith(PinYinMarkEnum.STARTS.getToken())) {
					String str = preToken + token;
					buffer = new char[str.length() + 1];
					System.arraycopy(str.toCharArray(), 0, buffer, 0, str.length());
					bufferIndex = preToken.length() + 1;
				}
			} else {
				// 连续的英文及其他
				buffer = new char[token.length() + 1];
				System.arraycopy(token.toCharArray(), 0, buffer, 0, token.length());
				bufferIndex = 1 + firstCharIndex(token);
			}
			preToken = token;
			return incrementToken();
		} else {
			term.copyBuffer(buffer, 0, bufferIndex);
			bufferIndex++;
			if (bufferIndex == buffer.length) {
				buffer[buffer.length - 1] = PinYinMarkEnum.WORD_SUFFIX.getChar();
			}
			if (bufferIndex > buffer.length) {
				bufferIndex = -1;
			}
		}

		return true;
	}

	private String popToken() throws IOException {
		StringBuilder builder = new StringBuilder();
		int i;
		while (true) {
			i = input.read();
			if (i == -1) {
				break;
			}
			char c = (char) i;
			if (c != ' ') {
				builder.append(c);
			} else {
				break;
			}
		}
		if (i == -1 && builder.length() == 0) {
			return null;
		}

		return builder.toString();
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		preToken = null;
	}

	private static boolean isPinYinToken(String token) {
		int index = 0;
		if (token.startsWith(PinYinMarkEnum.STARTS.getToken())) {
			index = 1;
		}
		if (token.length() < index + 1) {
			return false;
		}
		char c = token.charAt(index);
		if (c == PinYinMarkEnum.ABBR_PREFIX.getChar() || c == PinYinMarkEnum.EWORD_PREFIX.getChar()) {
			return false;
		}
		return true;
	}

	private static int firstCharIndex(String token) {
		if (isPinYinToken(token)) {
			if (token.charAt(0) == PinYinMarkEnum.STARTS.getChar()) {
				return 1;
			}
			return 0;
		}
		for (int i = 0; i < token.length(); i++) {
			if (token.charAt(i) == PinYinMarkEnum.EWORD_PREFIX.getChar()
					|| token.charAt(i) == PinYinMarkEnum.ABBR_PREFIX.getChar()) {
				return i + 1;
			}
		}
		return 0;
	}
}
