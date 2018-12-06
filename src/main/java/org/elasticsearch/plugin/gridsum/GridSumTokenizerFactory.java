package org.elasticsearch.plugin.gridsum;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

/**
 * @author linghongkang
 */
public class GridSumTokenizerFactory extends AbstractTokenizerFactory {


    public GridSumTokenizerFactory(IndexSettings indexSettings, Environment environment, String ignored, Settings settings) {
        super(indexSettings, ignored, settings);
    }


    @Override
    public Tokenizer create() {
        return new GridSumTokenizer();
    }
}
