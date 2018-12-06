package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class GenericQueryAnalyzer extends Analyzer {

	@Override
	final protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new ICTokenizer(true, 2);
		TokenStream result = new NormalizeTokenFilter(source);
		return new TokenStreamComponents(source, result) {
			@Override
			protected void setReader(final Reader reader) {
				super.setReader(reader);
			}
		};
	}

	public static void main(String[] args) throws IOException {
		String[] items = new String[] { "西门子开关插座好用吗", "南天 YJV 1x1.5 单芯电力电缆"};
		GenericQueryAnalyzer analyzer = new GenericQueryAnalyzer();
		for (String item : items) {
			//
			Reader reader = new StringReader(item);
			System.out.println("Item: " + item + " length:" + item.length());
			TokenStream ts = analyzer.tokenStream("segment", reader);
			ts.reset();
			// 保存相应词汇
			CharTermAttribute cta = ts.addAttribute(CharTermAttribute.class);
			TermTypesAttribute termTypes = ts.addAttribute(TermTypesAttribute.class);
			StringBuffer sb = new StringBuffer();
			while (ts.incrementToken()) {
				sb.append(cta + ";" + termTypes.getTypes() + "\n");
			}
			ts.close();
			System.out.println(sb.toString());
		}
		analyzer.close();
	}
}
