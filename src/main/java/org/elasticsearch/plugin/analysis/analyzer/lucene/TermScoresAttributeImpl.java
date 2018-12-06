/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import java.io.Serializable;

/**
 * @author li_yao
 *
 */
public class TermScoresAttributeImpl extends AttributeImpl implements TermScoresAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private String scores = "";

	@Override public void setScores(String scores) {
		this.scores = scores;
	}

	@Override public String getScores() {
		return scores;
	}

	@Override
	public void clear(){
		scores = "";
	}

	@Override
	public void reflectWith(AttributeReflector attributeReflector) {

	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof TermScoresAttributeImpl) {
			TermScoresAttributeImpl o = (TermScoresAttributeImpl) other;
			return o.scores == scores;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return scores.hashCode();
	}

	@Override
	public void copyTo(AttributeImpl target) {
	    TermScoresAttribute t = (TermScoresAttribute) target;
		t.setScores(scores);
	}

	@Override
	public String toString() {
		return scores;
	}

}
