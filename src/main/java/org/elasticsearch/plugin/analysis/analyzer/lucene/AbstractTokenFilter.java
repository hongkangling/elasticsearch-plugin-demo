package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public abstract class AbstractTokenFilter extends TokenFilter {
	protected boolean first;
	protected final CharTermAttribute textAttr;
	protected PositionIncrementAttribute posIncrAttr;
	protected boolean sourceHasPosIncrAttr;
	protected int lastPosIncr;

	protected AbstractTokenFilter(TokenStream input) {
		super(input);
		textAttr = input.getAttribute(CharTermAttribute.class);
		try {
			posIncrAttr = input.getAttribute(PositionIncrementAttribute.class);
			sourceHasPosIncrAttr = true;
		} catch (IllegalArgumentException e) {
			posIncrAttr = input.addAttribute(PositionIncrementAttribute.class);
			sourceHasPosIncrAttr = false;
		}
	}

	@Override
	public void end() throws IOException {
		super.end();
	}

	public void reinit() {
		first = true;
		lastPosIncr = 0;
	}

}