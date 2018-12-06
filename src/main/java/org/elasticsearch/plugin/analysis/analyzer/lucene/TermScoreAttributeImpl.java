package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import java.io.Serializable;

public class TermScoreAttributeImpl extends AttributeImpl implements TermScoreAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private int score = 0;

	public void setScore(int sc) {
		this.score = sc;
	}

	@Override
	public void clear() {
		score = 0;
	}

	@Override
	public void reflectWith(AttributeReflector attributeReflector) {

	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof TermScoreAttributeImpl) {
			TermScoreAttributeImpl o = (TermScoreAttributeImpl) other;
			return o.score == score;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return score;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		TermScoreAttribute t = (TermScoreAttribute) target;
		t.setScore(score);
	}

	@Override
	public String toString() {
		return "score=" + score;

	}

	public int getScore() {
		return score;
	}

}
