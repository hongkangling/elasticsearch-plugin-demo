package org.elasticsearch.plugin.analysis.analyzer.pinyin.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenfilter.PinYinTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.GeneralPinYinTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class PinYinIndexAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		GeneralPinYinTokenizer tokenizer = new GeneralPinYinTokenizer();
		TokenStream result = new NormalizeTokenFilter(new PinYinTokenFilter(tokenizer));
		return new TokenStreamComponents(tokenizer, result) {
			@Override
			protected void setReader(final Reader reader) {
				source.setReader(reader);
				super.setReader(reader);
			}
		};
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] items = new String[] { "上海火车南站地区", "上海南站站1号线" };
		PinYinIndexAnalyzer analyzer = new PinYinIndexAnalyzer();
		for (String item : items) {
			//
			Reader reader = new StringReader(item);
			System.out.println("Item: " + item + " length:" + item.length());
			TokenStream ts = analyzer.tokenStream("segment", reader);
			ts.reset();
			while (ts.incrementToken()) {
				CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
				System.out.println(term + "\t ");
			}
			ts.close();
			System.out.println();
		}
		analyzer.close();
	}

}
