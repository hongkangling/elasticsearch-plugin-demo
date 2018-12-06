package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class GenericIndexAnalyzer extends Analyzer {

	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new ICTokenizer(false, 0);
		TokenStream result = new SynonymTokenFilter(new NormalizeTokenFilter(source));
		return new TokenStreamComponents(source, result);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] items = new String[] { "华为（HUAWEI）S1700-8G-AC 8口 千兆交换机" };
		GenericIndexAnalyzer analyzer = new GenericIndexAnalyzer();
		for (String item : items) {
			//
			Reader reader = new StringReader(item);
			System.out.println("Item: " + item + " length:" + item.length());
			TokenStream ts = analyzer.tokenStream("segment", reader);
			ts.reset();
			// 保存相应词汇
			CharTermAttribute cta = ts.addAttribute(CharTermAttribute.class);
			while (ts.incrementToken()) {
				System.out.println(cta);
			}
			ts.close();
			System.out.println();
		}
		analyzer.close();
	}
}
