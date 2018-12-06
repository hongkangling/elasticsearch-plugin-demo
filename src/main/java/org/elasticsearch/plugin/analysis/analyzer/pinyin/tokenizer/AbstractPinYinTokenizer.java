package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenizer;

import org.apache.lucene.analysis.Tokenizer;

public abstract class AbstractPinYinTokenizer extends Tokenizer {

	public abstract void setDefaultPinYin(String defaultPinYin);

}
