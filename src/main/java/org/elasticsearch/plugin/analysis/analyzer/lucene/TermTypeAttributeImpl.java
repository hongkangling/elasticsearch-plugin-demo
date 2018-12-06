/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.analyzer.dict.WordMapping;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import java.io.Serializable;

/**
 * @author li_yao
 *
 */
public class TermTypeAttributeImpl extends AttributeImpl implements TermTypeAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private int type = 0;

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	@Override
	public void clear() {
		type = 0;
	}

	@Override
	public void reflectWith(AttributeReflector attributeReflector) {

	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof TermTypeAttributeImpl) {
			TermTypeAttributeImpl o = (TermTypeAttributeImpl) other;
			return o.type == type;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		TermTypeAttribute t = (TermTypeAttribute) target;
		t.setType(type);
	}

	@Override
	public String toString() {
		return "type=" + type + ",name=" + WordMapping.getWordType(type);

	}

}
