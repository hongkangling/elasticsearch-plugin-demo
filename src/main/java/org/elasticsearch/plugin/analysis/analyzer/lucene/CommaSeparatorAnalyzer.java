package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.analyzer.lucene.tokenizer.ToLowerCaseTokenFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CommaSeparatorAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {

		Tokenizer source = new SeparatorTokenizer(',');
		TokenStream result = new ToLowerCaseTokenFilter(source);

		return new TokenStreamComponents(source, result) {
			@Override protected void setReader(Reader reader) {
				super.setReader(reader);
			}
		};
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		Analyzer analyzer = new CommaSeparatorAnalyzer();
		TokenStream ts = analyzer.tokenStream("", new StringReader("10662|PVC,10663|直径20,10664|JG3050-1998,20115|50±1Ω"));
		// 重置TokenStream（重置StringReader）
		ts.reset();
		// 迭代获取分词结果
		while (ts.incrementToken()) {
			System.out.println(ts.toString());
		}
	}
}
