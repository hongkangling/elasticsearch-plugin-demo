package org.elasticsearch.plugin;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.plugin.gridsum.GridSumAnalyzer;
import org.junit.Test;

import java.io.IOException;

public class GridSumTest {

    @Test
    public void testAnalyzer() throws IOException {
        GridSumAnalyzer gridSumAnalyzer=new GridSumAnalyzer();
        TokenStream tokenStream = gridSumAnalyzer.tokenStream("text", "beers  is  good");
        CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()){
            System.out.println(termAttribute.toString());
        }
        tokenStream.end();
        tokenStream.close();

    }

}
