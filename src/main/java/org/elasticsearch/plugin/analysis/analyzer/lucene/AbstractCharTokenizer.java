package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.CharacterUtils.CharacterBuffer;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

public abstract class AbstractCharTokenizer extends Tokenizer {

	protected int bufferOffset = 0;
	protected int bufferIndex = 0;
	protected int bufferLen = 0;
	protected int finalOffset = 0;

	protected static final int IO_BUFFER_SIZE = 4096;

	protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	private final CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);

	public AbstractCharTokenizer() {

	}

	public AbstractCharTokenizer(AttributeFactory factory) {
		super(factory);
	}

	abstract protected boolean noMoreChar();

	/**
	 * 0 return false, 1 return true, other:continue
	 * 
	 * @return the code
	 */
	abstract protected int processChar(int c);

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();

		while (true) {
			if (bufferIndex >= bufferLen) {
				bufferOffset += bufferLen;
				// read supplementary char aware with CharacterUtils
				if (!CharacterUtils.fill(ioBuffer, input)) {
					bufferLen = 0;
					bufferIndex = 0;
					finalOffset = correctOffset(bufferOffset);
					return noMoreChar();
				}
				bufferLen = ioBuffer.getLength();
				bufferIndex = 0;
			}
			int c = Character.codePointAt(ioBuffer.getBuffer(), bufferIndex);

			bufferIndex += Character.charCount(c);

			int code = processChar(c);
			if (code == 0) {
				return false;
			}
			if (code == 1) {
				return true;
			}
		}

	}

	@Override
	public final void end() {// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = 0;
		bufferOffset = 0;
		bufferLen = 0;
		finalOffset = 0;
		ioBuffer.reset(); // make sure to reset the IO buffer!!
	}
}
