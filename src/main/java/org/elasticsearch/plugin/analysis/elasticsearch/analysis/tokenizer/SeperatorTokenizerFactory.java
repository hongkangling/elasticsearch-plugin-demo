package org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.SeparatorTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class SeperatorTokenizerFactory extends AbstractTokenizerFactory {
    private char seperator;
    public SeperatorTokenizerFactory(IndexSettings indexSettings, Environment env, String ignored, Settings settings, char seperator) {
        super(indexSettings, ignored, settings);
        this.seperator = seperator;
    }

    public static SeperatorTokenizerFactory commaSeperatorFactory(IndexSettings indexSettings, Environment env, String ignored, Settings settings) {
        return new SeperatorTokenizerFactory(indexSettings, env, ignored, settings, ',');
    }

    @Override
    public Tokenizer create() {
        return new SeparatorTokenizer(this.seperator);
    }
}
