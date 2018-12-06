package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.analyzer.dict.WordMapping;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SynonymTokenFilter extends TokenFilter {

	protected CharTermAttribute termAttr;

	protected PositionIncrementAttribute posAttr = null;

	protected List<String> synonyms = null;

	protected int point = -1;

	public SynonymTokenFilter(TokenStream stream) {
		super(stream);
		termAttr = getAttribute(CharTermAttribute.class);
		if (stream.hasAttribute(PositionIncrementAttribute.class)) {
			posAttr = getAttribute(PositionIncrementAttribute.class);
		}
	}

	@Override
	public boolean incrementToken() throws IOException {
		boolean hasToken = false;
		if (point == -1) {
			hasToken = input.incrementToken();
			if (hasToken) {
				String term = termAttr.toString();
				String synonymsStr = WordMapping.synonymMapping(term);

				List<String> synonymsTmp = new ArrayList<String>();
				if (!StringUtils.isEmpty(synonymsStr)) {
					String[] synonymsArr = synonymsStr.split("\\|");
					for (String ws : synonymsArr) {
						synonymsTmp.add(ws);
					}
				}
				synonyms = synonymsTmp;
				if (synonyms != null && synonyms.size() > 0) {
					point = 0;
				}
			}
		} else {
			if (point < synonyms.size()) {
				hasToken = true;
				char[] buffer = synonyms.get(point).toCharArray();
				termAttr.copyBuffer(buffer, 0, buffer.length);
				if (posAttr != null) {
					posAttr.setPositionIncrement(0);
				}
				++point;
			} else {
				synonyms = null;
				point = -1;
				return incrementToken();
			}
		}
		return hasToken;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		synonyms = null;
		point = -1;
	}
}
