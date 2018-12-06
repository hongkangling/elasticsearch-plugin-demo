package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;

public class SingleCharTokenizer extends AbstractCharTokenizer {

	public SingleCharTokenizer() {
		super();
	}

	public SingleCharTokenizer(AttributeFactory factory) {
		super(factory);
	}

	@Override
	final protected boolean noMoreChar() {
		return false;
	}

	@Override
	final protected int processChar(int c) {
		c = normalize(c);
		if (isTokenChar(c)) {// if it's a token char
			int len = Character.toChars(c, termAtt.buffer(), 0);
			termAtt.setLength(len);
			offsetAtt.setOffset(correctOffset(bufferOffset + bufferIndex - len),
					finalOffset = correctOffset(bufferOffset + bufferIndex));
			return 1;
		}
		return 2;
	}

	/**
	 * Returns true iff a codepoint should be included in a token. This
	 * tokenizer generates as tokens adjacent sequences of codepoints which
	 * satisfy this predicate. Codepoints for which this is false are used to
	 * define token boundaries and are not included in tokens.
	 * <p>
	 * As of Lucene 3.1 the char based API ({@link #isTokenChar(char)} and
	 * {@link #normalize(char)}) has been depreciated in favor of a Unicode 4.0
	 * compatible int based API to support codepoints instead of UTF-16 code
	 * units. Subclasses of {@link SingleCharTokenizer} must not override the
	 * char based methods if a {@link Version} >= 3.1 is passed to the
	 * constructor.
	 * <p>
	 * <p>
	 * NOTE: This method will be marked <i>abstract</i> in Lucene 4.0.
	 * </p>
	 */
	protected boolean isTokenChar(int c) {
		return true;
	}

	/**
	 * Called on each token character to normalize it before it is added to the
	 * token. The default implementation does nothing. Subclasses may use this
	 * to, e.g., lowercase tokens.
	 * <p>
	 * As of Lucene 3.1 the char based API ({@link #isTokenChar(char)} and
	 * {@link #normalize(char)}) has been depreciated in favor of a Unicode 4.0
	 * compatible int based API to support codepoints instead of UTF-16 code
	 * units. Subclasses of {@link SingleCharTokenizer} must not override the
	 * char based methods if a {@link Version} >= 3.1 is passed to the
	 * constructor.
	 * <p>
	 * <p>
	 * NOTE: This method will be marked <i>abstract</i> in Lucene 4.0.
	 * </p>
	 */
	protected int normalize(int c) {
		return c;
	}

}
