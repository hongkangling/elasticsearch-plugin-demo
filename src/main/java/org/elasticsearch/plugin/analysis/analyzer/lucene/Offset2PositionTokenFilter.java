package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public class Offset2PositionTokenFilter extends TokenFilter {

	protected OffsetAttribute offsetAttr;

	protected int prevStartOffset = 0;

	protected UnlimitedPositionIncrementAttributeImpl posAttr = new UnlimitedPositionIncrementAttributeImpl();

	public Offset2PositionTokenFilter(TokenStream stream) {
		super(stream);
		offsetAttr = getAttribute(OffsetAttribute.class);
		if (stream.hasAttribute(PositionIncrementAttribute.class)) {
			throw new IllegalArgumentException(PositionIncrementAttribute.class + " aleady exists");
		}
		stream.addAttributeImpl(posAttr);
	}

	@Override
	public boolean incrementToken() throws IOException {
		boolean hasToken = input.incrementToken();
		if (hasToken) {
			posAttr.setPositionIncrement(offsetAttr.startOffset() - prevStartOffset);
			prevStartOffset = offsetAttr.startOffset();
		}
		return hasToken;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		prevStartOffset = 0;
	}
}
