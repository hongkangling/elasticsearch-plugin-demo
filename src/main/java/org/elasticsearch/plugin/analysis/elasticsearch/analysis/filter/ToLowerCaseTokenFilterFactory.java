package org.elasticsearch.plugin.analysis.elasticsearch.analysis.filter;

import com.homedo.bigdata.analysis.analyzer.lucene.tokenizer.ToLowerCaseTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

public class ToLowerCaseTokenFilterFactory extends AbstractTokenFilterFactory {
    public ToLowerCaseTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return  new ToLowerCaseTokenFilter(tokenStream);
    }
}
