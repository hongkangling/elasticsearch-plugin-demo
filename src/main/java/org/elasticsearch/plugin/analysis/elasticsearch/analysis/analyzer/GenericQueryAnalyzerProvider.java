package org.elasticsearch.plugin.analysis.elasticsearch.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.GenericQueryAnalyzer;
import com.homedo.bigdata.analysis.config.EnvInfo;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class GenericQueryAnalyzerProvider extends AbstractIndexAnalyzerProvider<GenericQueryAnalyzer> {
    private final GenericQueryAnalyzer genericQueryAnalyzer;

    public GenericQueryAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        new EnvInfo(environment, settings);
        this.genericQueryAnalyzer = new GenericQueryAnalyzer();
    }

    @Override
    public GenericQueryAnalyzer get() {
        return this.genericQueryAnalyzer;
    }
}
