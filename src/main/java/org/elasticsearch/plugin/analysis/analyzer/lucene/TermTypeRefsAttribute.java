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
public interface TermTypeRefsAttribute extends Attribute {

	public void setTypeRefs(String typeRefs);

	public String getTypeRefs();

}
