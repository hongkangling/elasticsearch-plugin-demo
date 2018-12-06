package org.elasticsearch.plugin.gridsum;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

/**
 * @author linghongkang
 */
public class GridSumAnalyzerProvider extends AbstractIndexAnalyzerProvider {
    private  final GridSumAnalyzer gridSumAnalyzer;

    public GridSumAnalyzerProvider(IndexSettings indexSettings,Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        gridSumAnalyzer=new GridSumAnalyzer();
    }


    @Override
    public GridSumAnalyzer get() {
        return gridSumAnalyzer;
    }
}
