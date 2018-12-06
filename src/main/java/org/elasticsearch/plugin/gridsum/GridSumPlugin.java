package org.elasticsearch.plugin.gridsum;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Collections;
import java.util.Map;


/**
 * @author linghongkang
 */
public class GridSumPlugin extends Plugin implements AnalysisPlugin {
    public static String PLUGIN_NAME="analysis-gridsum";
    private final  static Logger LOGGER= LogManager.getLogger(GridSumPlugin.class);

    public GridSumPlugin(){
        super();
        LOGGER.info("{} installed   into elasticsearch",PLUGIN_NAME);
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {

        return Collections.singletonMap("gridsum-word",GridSumTokenizerFactory::new);
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return Collections.singletonMap("gridsum",GridSumAnalyzerProvider::new);
    }
}
