package org.elasticsearch.plugin.analysis.analyzer.pinyin.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenfilter.PinYinTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.GeneralPinYinTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.MuiltiPinYinTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.SimpleJianPinTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class PinYinJianPinIndexAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		GeneralPinYinTokenizer tokenizer = new GeneralPinYinTokenizer();
		SimpleJianPinTokenizer jianPinTokenizer = new SimpleJianPinTokenizer();
		MuiltiPinYinTokenizer muiltiPinYinTokenizer = new MuiltiPinYinTokenizer(tokenizer, jianPinTokenizer);
		TokenStream result = new NormalizeTokenFilter(new PinYinTokenFilter(muiltiPinYinTokenizer));
		return new TokenStreamComponents(muiltiPinYinTokenizer, result) {
			@Override
			protected void setReader(final Reader reader) {
				try {
					((MuiltiPinYinTokenizer) source).tokenizerSetReader(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		PinYinJianPinIndexAnalyzer analyzer = new PinYinJianPinIndexAnalyzer();
		for (String item : items) {
			//
			Reader reader = new StringReader(item);
			System.out.println("Item: " + item + " length:" + item.length());
			TokenStream ts = analyzer.tokenStream("segment", reader);

			while (ts.incrementToken()) {
				CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
				System.out.println(term + "\t ");
			}
			System.out.println();
		}
		analyzer.close();
	}

}
