package org.elasticsearch.plugin.gridsum;

import org.apache.lucene.analysis.Analyzer;

/**
 * @author linghongkang
 */
public class GridSumAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String s) {
        return new TokenStreamComponents(new GridSumTokenizer());
    }
}
