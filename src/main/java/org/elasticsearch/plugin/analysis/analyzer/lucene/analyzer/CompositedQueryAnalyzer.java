package org.elasticsearch.plugin.analysis.analyzer.lucene.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.lucene.tokenizer.CompositedICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.tokenizer.CompositedICTokenizer.ICTokenizerAndWeight;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CompositedQueryAnalyzer extends Analyzer {


	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new CompositedICTokenizer(new ICTokenizerAndWeight(new ICTokenizer(false, 2, false), 1), // 非最长匹配
																													// +
																													// 前向匹配
				new ICTokenizerAndWeight(new ICTokenizer(false, 2), 2), // 非最长匹配
																		// +
																		// 后向匹配
				new ICTokenizerAndWeight(new ICTokenizer(true, 2, false), 3), // 最长匹配
																				// +
																				// 前向匹配
				new ICTokenizerAndWeight(new ICTokenizer(true, 2), 4)); // 最长匹配
																		// +
																		// 后向匹配
		TokenStream result = new NormalizeTokenFilter(source);
		return new TokenStreamComponents(source, result) {
			@Override
			protected void setReader(final Reader reader) {
				try {
					super.setReader(reader);
					((CompositedICTokenizer) source).tokenizerSetReader(reader);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] items = new String[] { "w们RS-1660-24TC-A" };
		@SuppressWarnings("resource")
		CompositedQueryAnalyzer analyzer = new CompositedQueryAnalyzer();
		for (String item : items) {
			Reader reader = new StringReader(item);
			System.out.println("Item: " + item + " length:" + item.length());
			TokenStream ts = analyzer.tokenStream("segment", reader);
			ts.reset();
			if (ts instanceof NormalizeTokenFilter) {
				((NormalizeTokenFilter) ts).setArabicNum2CnNum(false);
			}
			while (ts.incrementToken()) {
				CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
				System.out.println(term + "\t");
			}
			ts.close();
			System.out.println();
		}
		System.exit(0);
	}
}
