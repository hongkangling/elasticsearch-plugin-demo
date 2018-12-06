/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import java.io.Serializable;

public class TermTypeScoreAttributeImpl extends AttributeImpl implements TermTypeScoreAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private int typeScore = 0;

	public void setTypeScore(int sc) {
		this.typeScore = sc;
	}

	@Override
	public void clear() {
		typeScore = 0;
	}

	@Override
	public void reflectWith(AttributeReflector attributeReflector) {

	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof TermTypeScoreAttributeImpl) {
			TermTypeScoreAttributeImpl o = (TermTypeScoreAttributeImpl) other;
			return o.typeScore == typeScore;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return typeScore;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		TermTypeScoreAttribute t = (TermTypeScoreAttribute) target;
		t.setTypeScore(typeScore);
	}

	@Override
	public String toString() {
		return "typeScore=" + typeScore;

	}

	public int getTypeScore() {
		return typeScore;
	}

}
