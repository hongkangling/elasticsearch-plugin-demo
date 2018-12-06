package org.elasticsearch.plugin.analysis.elasticsearch.plugin;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.analyzer.*;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.filter.GenericTokenFilterFactory;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.filter.NormalizeTokenFilterFactory;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.filter.ToLowerCaseTokenFilterFactory;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer.CompositedICTokenizerFactory;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer.GenericTokenizerFactory;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer.PYCompositedTokenizerFactory;
import org.elasticsearch.plugin.analysis.elasticsearch.analysis.tokenizer.SeperatorTokenizerFactory;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Map;

public class HmAnalysisPlugin extends Plugin implements AnalysisPlugin {
    private final static Logger LOGGER = ESLoggerFactory.getLogger(HmAnalysisPlugin.class);
    public final static String PLUGIN_NAME = "analysis-hm";

    private final static String HM_CN_PY_JP = "hm_cn_py_jp";
    private final static String HM_COMMA = "hm_comma";
    private final static String HM_COMPOSITE = "hm_composite";
    private final static String HM_GENERAL_INDEX = "hm_general_index";
    private final static String HM_GENERAL_QUERY = "hm_general_query";

    public HmAnalysisPlugin() {
        super();
        LOGGER.info("{} plugin is installed", PLUGIN_NAME);
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> map = Maps.newHashMap();

        map.put(HM_CN_PY_JP, PYCompositedTokenizerFactory::new);
        map.put(HM_COMMA, SeperatorTokenizerFactory::commaSeperatorFactory);
        map.put(HM_COMPOSITE, CompositedICTokenizerFactory::new);
        map.put(HM_GENERAL_INDEX, GenericTokenizerFactory::indexTokenizerFactory);
        map.put(HM_GENERAL_QUERY, GenericTokenizerFactory::queryTokenizerFactory);

        return map;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> map = Maps.newHashMap();
        map.put(HM_CN_PY_JP, NormalizeTokenFilterFactory::new);
        map.put(HM_COMMA, ToLowerCaseTokenFilterFactory::new);
        map.put(HM_COMPOSITE, NormalizeTokenFilterFactory::new);
        map.put(HM_GENERAL_INDEX, GenericTokenFilterFactory::new);
        map.put(HM_GENERAL_QUERY, NormalizeTokenFilterFactory::new);
        return map;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> map = Maps.newHashMap();

        map.put(HM_CN_PY_JP, CnPinYinJianPinIndexAnalyzerProvider::new);
        map.put(HM_COMMA, CommaSeparatorAnalyzerProvider::new);
        map.put(HM_COMPOSITE, CompositedQueryAnalyzerProvider::new);
        map.put(HM_GENERAL_INDEX, GenericIndexAnalyzerProvider::new);
        map.put(HM_GENERAL_QUERY, GenericQueryAnalyzerProvider::new);

        return map;
    }
}
