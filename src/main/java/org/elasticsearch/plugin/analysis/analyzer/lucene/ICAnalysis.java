/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.StringReader;

public class ICAnalysis extends Analyzer {

	private boolean ismax = false;
	private int type = 1;

	/**
	 * 分词器Lucene Analyzer接口实现类 默认最细粒度切分算法
	 */
	public ICAnalysis() {
		this(false);
	}

	/**
	 * 
	 * @param type分词类型
	 */
	public ICAnalysis(int segmenter_Type) {
		this(false);
		this.type = segmenter_Type;
	}

	/**
	 * 分词器Lucene Analyzer接口实现类
	 * 
	 * @param ismax
	 *            当为true时，分词器进行最大词长切分
	 */
	public ICAnalysis(boolean ismax) {
		super();
		this.ismax = ismax;
	}

	/**
	 * 
	 * @param type
	 *            0 index 1 search
	 * @param maxlen
	 */
	public ICAnalysis(int type, boolean ismax) {
		this.ismax = ismax;
		this.type = type;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		ICTokenizer tokenizer = new ICTokenizer(ismax, type);
		return new TokenStreamComponents(tokenizer);
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		Analyzer analyzer = new ICAnalysis(2, true);
		TokenStream ts = analyzer.tokenStream("", new StringReader("上海人民广场台灣中正大學"));
		// 重置TokenStream（重置StringReader）
		ts.reset();
		// 迭代获取分词结果
		while (ts.incrementToken()) {
			System.out.println(ts.toString());
		}
	}
}
