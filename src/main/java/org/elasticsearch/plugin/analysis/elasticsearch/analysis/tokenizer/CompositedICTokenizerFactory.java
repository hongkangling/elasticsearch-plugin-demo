package org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.tokenizer.CompositedICTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class CompositedICTokenizerFactory extends AbstractTokenizerFactory {
    public CompositedICTokenizerFactory(IndexSettings indexSettings, Environment environment, String ignored, Settings settings) {
        super(indexSettings, ignored, settings);
    }

    @Override
    public Tokenizer create() {
        return new CompositedICTokenizer(new CompositedICTokenizer.ICTokenizerAndWeight(new ICTokenizer(false, 2, false), 1), // 非最长匹配
                // +
                // 前向匹配
                new CompositedICTokenizer.ICTokenizerAndWeight(new ICTokenizer(false, 2), 2), // 非最长匹配
                // +
                // 后向匹配
                new CompositedICTokenizer.ICTokenizerAndWeight(new ICTokenizer(true, 2, false), 3), // 最长匹配
                // +
                // 前向匹配
                new CompositedICTokenizer.ICTokenizerAndWeight(new ICTokenizer(true, 2), 4)); // 最长匹配
        // +
        // 后向匹配;
    }
}
