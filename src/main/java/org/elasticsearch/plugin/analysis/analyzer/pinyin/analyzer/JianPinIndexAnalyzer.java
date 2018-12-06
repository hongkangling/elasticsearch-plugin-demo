package org.elasticsearch.plugin.analysis.analyzer.pinyin.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenfilter.PinYinTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.SimpleJianPinTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JianPinIndexAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		SimpleJianPinTokenizer tokenizer = new SimpleJianPinTokenizer();
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
	 */
	public static void main(String[] args) throws IOException {
		String[] items = new String[] { "中文英文", "motel168", "7天", "上外" };
		JianPinIndexAnalyzer analyzer = new JianPinIndexAnalyzer();
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
			System.out.println();
			ts.close();
		}
		analyzer.close();

	}

}
