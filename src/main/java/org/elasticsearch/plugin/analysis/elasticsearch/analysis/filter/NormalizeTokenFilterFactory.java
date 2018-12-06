package org.elasticsearch.plugin.analysis.elasticsearch.analysis.filter;

import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

public class NormalizeTokenFilterFactory extends AbstractTokenFilterFactory {
    public NormalizeTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new NormalizeTokenFilter(tokenStream);
    }
}
