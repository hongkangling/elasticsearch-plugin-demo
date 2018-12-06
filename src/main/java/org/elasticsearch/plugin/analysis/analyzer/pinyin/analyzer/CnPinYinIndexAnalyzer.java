package org.elasticsearch.plugin.analysis.analyzer.pinyin.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.CompositedTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.GeneralPinYinTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CnPinYinIndexAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer chineseSource = new ICTokenizer(false, 0);
		GeneralPinYinTokenizer tokenizer = new GeneralPinYinTokenizer();
		Tokenizer source = new CompositedTokenizer(chineseSource, tokenizer);
		TokenStream result = new NormalizeTokenFilter(source);
		return new TokenStreamComponents(source, result) {
			@Override
			protected void setReader(final Reader reader) {
				try {
					super.setReader(reader);
					((CompositedTokenizer) source).tokenizerSetReader(reader);
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
		CnPinYinIndexAnalyzer analyzer = new CnPinYinIndexAnalyzer();
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
		}
		analyzer.close();
	}

}
