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
public interface TermScoresAttribute extends Attribute {

	public void setScores(String scores);

	public String getScores();

}
