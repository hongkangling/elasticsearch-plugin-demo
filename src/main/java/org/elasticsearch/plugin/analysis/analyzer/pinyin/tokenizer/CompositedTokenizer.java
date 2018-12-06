package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.SynonymTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenfilter.MarkICTokenFilter;
import com.homedo.bigdata.analysis.analyzer.pinyin.tokenfilter.PinYinTokenFilter;
import com.homedo.bigdata.analysis.util.StringHelper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CompositedTokenizer extends Tokenizer {
	private TokenStream chineseWordStream;
	private TokenStream pinyinWordStream;
	private final Tokenizer icTokenizer;
	private final Tokenizer pyTokenizer;
	private final CharTermAttribute term;

	public CompositedTokenizer(Tokenizer chineseTokenizer, AbstractPinYinTokenizer pyTokenizer) {
		this.pyTokenizer = pyTokenizer;
		this.icTokenizer = chineseTokenizer;
		chineseWordStream = new MarkICTokenFilter(new SynonymTokenFilter(icTokenizer));
		pinyinWordStream = new PinYinTokenFilter(pyTokenizer);
		term = addAttribute(CharTermAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (chineseWordStream.incrementToken()) {
			CharTermAttribute attr = chineseWordStream.getAttribute(CharTermAttribute.class);
			term.copyBuffer(attr.buffer(), 0, attr.length());
			return true;
		}

		if (pinyinWordStream.incrementToken()) {
			CharTermAttribute attr = pinyinWordStream.getAttribute(CharTermAttribute.class);
			term.copyBuffer(attr.buffer(), 0, attr.length());
			return true;
		}

		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		icTokenizer.reset();
		pyTokenizer.reset();
	}

	public void tokenizerSetReader(Reader reader) throws IOException {
		String content = StringHelper.readerToString(reader);
		icTokenizer.setReader(new StringReader(content));
		pyTokenizer.setReader(new StringReader(content));
	}

	public void muiltiPinYinTokenizerSetReader(Reader reader) throws IOException {
		String content = StringHelper.readerToString(reader);
		icTokenizer.setReader(new StringReader(content));
		((MuiltiPinYinTokenizer) pyTokenizer).tokenizerSetReader(new StringReader(content));
	}

	@Override
	public void close() throws IOException {
		super.close();
		pyTokenizer.close();
		icTokenizer.close();
	}
}
