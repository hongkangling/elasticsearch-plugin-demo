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
public class TermTypesAttributeImpl extends AttributeImpl implements TermTypesAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private String types = "";

	public void setTypes(String types) {
		this.types = types;
	}

	public String getTypes() {
		return types;
	}

	@Override
	public void clear() {
		types = "";
	}

	@Override
	public void reflectWith(AttributeReflector attributeReflector) {

	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof TermTypesAttributeImpl) {
			TermTypesAttributeImpl o = (TermTypesAttributeImpl) other;
			return o.types == types;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return types.hashCode();
	}

	@Override
	public void copyTo(AttributeImpl target) {
		TermTypesAttribute t = (TermTypesAttribute) target;
		t.setTypes(types);
	}

	@Override
	public String toString() {
		StringBuilder wordTypes = new StringBuilder();
		try {
			String[] typeids = types.split(",");
			for (String id : typeids) {
				if (!id.isEmpty()) {
					int tid = Integer.parseInt(id);
					wordTypes.append(tid);
					wordTypes.append(",");
					wordTypes.append(WordMapping.getWordType(tid));
					wordTypes.append(";");
				}
			}
			if (wordTypes.length() > 0)
				wordTypes.deleteCharAt(wordTypes.length() - 1);
		} catch (Exception e) {

		}
		return wordTypes.toString();

	}

}
