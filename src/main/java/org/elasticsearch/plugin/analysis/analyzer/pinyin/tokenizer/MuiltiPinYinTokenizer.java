package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenizer;

import com.homedo.bigdata.analysis.util.StringHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class MuiltiPinYinTokenizer extends AbstractPinYinTokenizer {
	private final AbstractPinYinTokenizer[] pyTokenizers;
	private final CharTermAttribute term;
	private int index;

	public MuiltiPinYinTokenizer(AbstractPinYinTokenizer... tokenizers) {
		pyTokenizers = tokenizers;
		term = addAttribute(CharTermAttribute.class);
		index = 0;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (index >= pyTokenizers.length) {
			return false;
		}

		if (pyTokenizers[index].incrementToken()) {
			CharTermAttribute attr = pyTokenizers[index].getAttribute(CharTermAttribute.class);
			term.copyBuffer(attr.buffer(), 0, attr.length());
			return true;
		} else {
			index++;
			return incrementToken();
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		for (AbstractPinYinTokenizer tokenizer : pyTokenizers) {
			tokenizer.reset();
		}
		index = 0;
	}

	public void tokenizerSetReader(Reader reader) throws IOException {
		String content = StringHelper.readerToString(reader);
		for (AbstractPinYinTokenizer tokenizer : pyTokenizers) {
			tokenizer.setReader(new StringReader(content));
		}
		index = 0;
	}

	@Override
	public void setDefaultPinYin(String defaultPinYin) {
	}

	@Override
	public void close() throws IOException {
		super.close();
		for (AbstractPinYinTokenizer tokenizer : pyTokenizers) {
			tokenizer.close();
		}
		index = 0;
	}
}
