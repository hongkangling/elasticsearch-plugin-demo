package org.elasticsearch.plugin.analysis.elasticsearch.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.pinyin.analyzer.CnPinYinJianPinIndexAnalyzer;
import com.homedo.bigdata.analysis.config.EnvInfo;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class CnPinYinJianPinIndexAnalyzerProvider extends AbstractIndexAnalyzerProvider<CnPinYinJianPinIndexAnalyzer> {
    private final CnPinYinJianPinIndexAnalyzer cnPinYinJianPinIndexAnalyzer;

    public CnPinYinJianPinIndexAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        new EnvInfo(environment, settings);
        this.cnPinYinJianPinIndexAnalyzer = new CnPinYinJianPinIndexAnalyzer();
    }

    @Override
    public CnPinYinJianPinIndexAnalyzer get() {
        return this.cnPinYinJianPinIndexAnalyzer;
    }
}
