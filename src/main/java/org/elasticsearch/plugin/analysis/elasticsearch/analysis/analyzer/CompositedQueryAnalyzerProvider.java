package org.elasticsearch.plugin.analysis.elasticsearch.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.analyzer.CompositedQueryAnalyzer;
import com.homedo.bigdata.analysis.config.EnvInfo;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class CompositedQueryAnalyzerProvider extends AbstractIndexAnalyzerProvider<CompositedQueryAnalyzer> {
    private final CompositedQueryAnalyzer compositedQueryAnalyzer;

    public CompositedQueryAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        new EnvInfo(environment, settings);
        this.compositedQueryAnalyzer = new CompositedQueryAnalyzer();
    }

    @Override
    public CompositedQueryAnalyzer get() {
        return this.compositedQueryAnalyzer;
    }
}
