package org.elasticsearch.plugin.analysis.analyzer.pinyin.analyzer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.NormalizeTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.CompositedTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.GeneralPinYinTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.MuiltiPinYinTokenizer;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenizer.SimpleJianPinTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CnPinYinJianPinIndexAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer chineseSource = new ICTokenizer(false, 0);
		GeneralPinYinTokenizer tokenizer = new GeneralPinYinTokenizer();
		SimpleJianPinTokenizer jianPinTokenizer = new SimpleJianPinTokenizer();

		MuiltiPinYinTokenizer muiltiPinYinTokenizer = new MuiltiPinYinTokenizer(tokenizer, jianPinTokenizer);
		CompositedTokenizer source = new CompositedTokenizer(chineseSource, muiltiPinYinTokenizer);
		TokenStream result = new NormalizeTokenFilter(source);
		return new TokenStreamComponents(source, result) {
			@Override
			protected void setReader(final Reader reader) {
				try {
					super.setReader(reader);
					((CompositedTokenizer) source).muiltiPinYinTokenizerSetReader(reader);
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

		String[] items = new String[] { "美电贝尔BL-NVR200/16", "中建宏发镀锌钢导线管 黄色 3.9米/根 黄色 25*1.0" };

		Analyzer analyzer = new CnPinYinJianPinIndexAnalyzer();
		for (String item : items) {
			//
			Reader reader = new StringReader(item);
			System.out.println("Item: " + item + " length:" + item.length());
			TokenStream ts = analyzer.tokenStream("", reader);
			ts.reset();
			while (ts.incrementToken()) {
				CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
				System.out.println(term);
			}
			ts.close();
			System.out.println();
		}
		analyzer.close();
	}

}
