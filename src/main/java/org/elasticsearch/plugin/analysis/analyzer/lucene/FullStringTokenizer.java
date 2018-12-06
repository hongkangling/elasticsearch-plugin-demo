package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

public class FullStringTokenizer extends AbstractCharTokenizer {

	protected int length = 0;

	public FullStringTokenizer() {
	}

	public FullStringTokenizer(AttributeFactory factory) {
		super(factory);
	}

	@Override
	protected boolean noMoreChar() {
		if (length > 0) {
			termAtt.setLength(length);
			offsetAtt.setOffset(correctOffset(0), finalOffset = correctOffset(length));
			length = 0;
			return true;
		}
		return false;
	}

	@Override
	final protected int processChar(int c) {
		c = normalize(c);
		if (isTokenChar(c)) {// if it's a token char
			if (length >= termAtt.buffer().length - 1) {
				termAtt.resizeBuffer(2 + length);
			}
			length += Character.toChars(c, termAtt.buffer(), length);
		}
		return 2;
	}

	protected boolean isTokenChar(int c) {
		return CharacterHelper.isCJKCharacter(c, false) || Character.isLetterOrDigit(c);
	}

	protected int normalize(int c) {
		return CharacterHelper.normalize(c);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		length = 0;
	}

}
