/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.search.similarities.ClassicSimilarity;

public class ICSimilarity extends ClassicSimilarity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.lucene.search.Similarity#coord(int, int)
	 */
	public float coord(int overlap, int maxOverlap) {
		float overlap2 = (float) Math.pow(2, overlap);
		float maxOverlap2 = (float) Math.pow(2, maxOverlap);
		return (overlap2 / maxOverlap2);
	}
}
