package org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class GenericTokenizerFactory extends AbstractTokenizerFactory {
    /**
     * 标记 创建索引|检索索引
     * 0 - 创建索引
     * 1 - 检索索引
     */
    private int type;
    /**
     * 当为true时，分词器进行最大词长切分；
     * 当为false是，采用最细粒度切分
     */
    private boolean isMax;
    public GenericTokenizerFactory(IndexSettings indexSettings, Environment environment, String ignored, Settings settings, int type, boolean isMax) {
        super(indexSettings, ignored, settings);
        this.type = type;
        this.isMax = isMax;
    }

    public static GenericTokenizerFactory indexTokenizerFactory(IndexSettings indexSettings, Environment environment, String ignored, Settings settings) {
        return new GenericTokenizerFactory(indexSettings, environment, ignored, settings, 0, false);
    }

    public static GenericTokenizerFactory queryTokenizerFactory(IndexSettings indexSettings, Environment environment, String ignored, Settings settings) {
        return new GenericTokenizerFactory(indexSettings, environment, ignored, settings, 1, true);
    }

    @Override
    public Tokenizer create() {
        return new ICTokenizer(this.isMax, this.type);
    }
}
