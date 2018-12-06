package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

public class StringSeparatorTokenizer extends AbstractCharTokenizer {

	int[] separator;
	int addedSeparatorCharLen;
	int lastSeparatorIndex;

	protected int length = 0;
	int startOffset = 0;
	int currSeparatorIndex = 0;

	public StringSeparatorTokenizer(String separator) {
		this.separator = toCodePoints(separator);
	}

	public StringSeparatorTokenizer(AttributeFactory factory, String separator) {
		super(factory);
		this.separator = toCodePoints(separator);
	}

	int[] toCodePoints(String separator) {
		int[] cps = CharacterHelper.toCodePoints(separator);
		addedSeparatorCharLen = separator.length() - Character.charCount(cps[cps.length - 1]);
		lastSeparatorIndex = cps.length - 1;
		return cps;
	}

	@Override
	protected boolean noMoreChar() {
		termAtt.setLength(length);
		offsetAtt.setOffset(correctOffset(startOffset), finalOffset);
		startOffset = 0;
		length = 0;
		currSeparatorIndex = 0;
		return termAtt.length() > 0;
	}

	void addCodePoint(int c) {
		if (length >= termAtt.buffer().length - 1) {
			termAtt.resizeBuffer(2 + length);
		}
		length += Character.toChars(c, termAtt.buffer(), length);
	}

	@Override
	final protected int processChar(int c) {
		c = normalize(c);
		if (c == separator[currSeparatorIndex]) {
			if (currSeparatorIndex == lastSeparatorIndex) {
				termAtt.setLength(length - addedSeparatorCharLen);
				offsetAtt.setOffset(correctOffset(startOffset),
						correctOffset(bufferOffset + bufferIndex - addedSeparatorCharLen - 1));
				startOffset = bufferOffset + bufferIndex;
				length = 0;
				currSeparatorIndex = 0;
				return termAtt.length() == 0 ? 2 : 1;
			} else {
				++currSeparatorIndex;
				addCodePoint(c);
				return 2;
			}
		} else {
			currSeparatorIndex = c == separator[0] ? 1 : 0;
			addCodePoint(c);
			return 2;
		}
	}

	protected int normalize(int c) {
		// return CharacterHelper.normalize(c);
		return c;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		startOffset = 0;
		length = 0;
		currSeparatorIndex = 0;
	}

}
