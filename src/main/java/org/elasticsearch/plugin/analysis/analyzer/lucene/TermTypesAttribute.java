/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.Attribute;

/**
 * 
 * @author zzfu
 *
 */
public interface TermTypesAttribute extends Attribute {

	public void setTypes(String types);

	public String getTypes();

}
