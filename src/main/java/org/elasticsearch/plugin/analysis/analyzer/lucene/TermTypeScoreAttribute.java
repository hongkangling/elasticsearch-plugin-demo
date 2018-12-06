/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.Attribute;

/**
 * @author li_yao
 *
 */
public interface TermTypeScoreAttribute extends Attribute {

	public void setTypeScore(int score);

	public int getTypeScore();

}
