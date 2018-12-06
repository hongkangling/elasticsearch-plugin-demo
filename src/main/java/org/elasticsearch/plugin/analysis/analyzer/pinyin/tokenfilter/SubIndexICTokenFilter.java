package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenfilter;

import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class SubIndexICTokenFilter extends TokenFilter {
	private final CharTermAttribute termAttr;

	public SubIndexICTokenFilter(TokenStream input) {
		super(input);
		termAttr = input.getAttribute(CharTermAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			int termLength = termAttr.length();
			if (termLength < 1) {
				return incrementToken();
			}
			termAttr.append(PinYinMarkEnum.TERM_LENGTH_DELIMITER.getChar());
			if (termAttr.charAt(0) == PinYinMarkEnum.EWORD_PREFIX.getChar()) {
				termAttr.append("2");
			} else if (termAttr.charAt(0) == PinYinMarkEnum.STARTS.getChar()) {
				if (termLength > 1) {
					if (termAttr.charAt(1) == PinYinMarkEnum.EWORD_PREFIX.getChar()) {
						termAttr.append("3");
					}
				}
				termAttr.append(String.valueOf(termLength));
			} else {
				termAttr.append(String.valueOf(termLength));
			}

			return true;
		}
		return false;
	}
}
