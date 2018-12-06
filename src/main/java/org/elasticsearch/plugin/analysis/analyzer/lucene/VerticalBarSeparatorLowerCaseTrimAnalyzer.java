package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;

public class VerticalBarSeparatorLowerCaseTrimAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String s) {
		Tokenizer source = new SeparatorTokenizer('|');
		// TokenStream result = new TrimTokenFilter(new
		// LowerCaseFilter(source));
		TokenStream result = new LowerCaseFilter(source);
		return new TokenStreamComponents(source, result);
	}

	// trimTokenFilter是5.X废弃掉的功能，暂时注释掉，后续再思考
	// public static void main(String[] args) throws IOException {
	// VerticalBarSeparatorLowerCaseTrimAnalyzer analyzer = new
	// VerticalBarSeparatorLowerCaseTrimAnalyzer();
	// TokenStream ts = analyzer.tokenStream("text", new StringReader("黄继刚|炸碉堡 |
	// \t如果前面没空格就对了|\n如果上面下面没空行就对了\n|中间 空格\t要
	// 保留||||||上面空字符串扔了|中间换\n行不用管|ABCD都是小写|测试数字1234数字不会处理|舒适"));
	// CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
	// ts.reset();
	// while (ts.incrementToken()) {
	// System.out.println(term.toString());
	// }
	// ts.end();
	// ts.close();
	// analyzer.close();
	// }
}
