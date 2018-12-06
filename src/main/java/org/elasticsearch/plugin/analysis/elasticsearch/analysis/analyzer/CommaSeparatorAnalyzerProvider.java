package org.elasticsearch.plugin.analysis.elasticsearch.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.CommaSeparatorAnalyzer;
import com.homedo.bigdata.analysis.config.EnvInfo;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class CommaSeparatorAnalyzerProvider extends AbstractIndexAnalyzerProvider<CommaSeparatorAnalyzer> {
    private final CommaSeparatorAnalyzer commaSeparatorAnalyzer;

    public CommaSeparatorAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        new EnvInfo(environment, settings);
        this.commaSeparatorAnalyzer = new CommaSeparatorAnalyzer();
    }

    @Override
    public CommaSeparatorAnalyzer get() {
        return this.commaSeparatorAnalyzer;
    }
}
