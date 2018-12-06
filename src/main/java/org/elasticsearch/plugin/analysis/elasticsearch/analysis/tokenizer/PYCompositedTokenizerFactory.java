package org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.CompositedTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.GeneralPinYinTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.MuiltiPinYinTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.SimpleJianPinTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class PYCompositedTokenizerFactory extends AbstractTokenizerFactory {
    public PYCompositedTokenizerFactory(IndexSettings indexSettings, Environment environment, String ignored, Settings settings) {
        super(indexSettings, ignored, settings);
    }
    @Override
    public Tokenizer create() {
        Tokenizer chineseSource = new ICTokenizer(false, 0);
        GeneralPinYinTokenizer tokenizer = new GeneralPinYinTokenizer();
        SimpleJianPinTokenizer jianPinTokenizer = new SimpleJianPinTokenizer();
        MuiltiPinYinTokenizer muiltiPinYinTokenizer = new MuiltiPinYinTokenizer(tokenizer, jianPinTokenizer);

        return new CompositedTokenizer(chineseSource, muiltiPinYinTokenizer);
    }
}
