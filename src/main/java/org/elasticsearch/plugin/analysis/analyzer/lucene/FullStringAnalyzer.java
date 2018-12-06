package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.StringReader;

public class FullStringAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new FullStringTokenizer());

	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		Analyzer analyzer = new FullStringAnalyzer();
		TokenStream ts = analyzer.tokenStream("eeeW", new StringReader("上海人,民广场,sfsgfdsg台灣,中正大學"));
		// 重置TokenStream（重置StringReader）
		ts.reset();
		// 迭代获取分词结果
		while (ts.incrementToken()) {
			System.out.println(ts.toString());
		}
	}
}
