/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.Attribute;

/**
 * @author li_yao
 *
 */
public interface TermTypeAttribute extends Attribute {

	public void setType(int type);

	public int getType();

}
