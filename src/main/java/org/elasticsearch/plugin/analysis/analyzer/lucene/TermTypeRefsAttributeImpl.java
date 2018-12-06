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
public class TermTypeRefsAttributeImpl extends AttributeImpl implements TermTypeRefsAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 11L;

	private String typeRefs = "";

	public void setTypeRefs(String typeRefs) {
		this.typeRefs = typeRefs;
	}

	public String getTypeRefs() {
		return typeRefs;
	}

	@Override
	public void clear() {
		typeRefs = "";
	}

	@Override
	public void reflectWith(AttributeReflector attributeReflector) {

	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof TermTypeRefsAttributeImpl) {
			TermTypeRefsAttributeImpl o = (TermTypeRefsAttributeImpl) other;
			return o.typeRefs == typeRefs;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return typeRefs.hashCode();
	}

	@Override
	public void copyTo(AttributeImpl target) {
		TermTypesAttribute t = (TermTypesAttribute) target;
		t.setTypes(typeRefs);
	}

	@Override
	public String toString() {
		StringBuilder wordTypes = new StringBuilder();
		try {
			String[] typeRefIds = typeRefs.split(",");
			for (String id : typeRefIds) {
				if (!id.isEmpty()) {
					int tid = Integer.parseInt(id);
					wordTypes.append(",");
					wordTypes.append(tid);
				}
			}
			if (wordTypes.length() > 0)
				wordTypes.deleteCharAt(0);
		} catch (Exception e) {

		}
		return wordTypes.toString();

	}

}
