package org.elasticsearch.plugin.analysis.elasticsearch.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.GenericIndexAnalyzer;
import com.homedo.bigdata.analysis.config.EnvInfo;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class GenericIndexAnalyzerProvider extends AbstractIndexAnalyzerProvider<GenericIndexAnalyzer> {
    private final GenericIndexAnalyzer genericIndexAnalyzer;

    public GenericIndexAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        new EnvInfo(environment, settings);
        this.genericIndexAnalyzer = new GenericIndexAnalyzer();
    }

    @Override
    public GenericIndexAnalyzer get() {
        return this.genericIndexAnalyzer;
    }
}
