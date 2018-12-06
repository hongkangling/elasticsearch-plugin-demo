/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.util.Attribute;

/**
 * @author li_yao
 *
 */
public interface TermScoreAttribute extends Attribute {

	public void setScore(int score);

	public int getScore();

}
