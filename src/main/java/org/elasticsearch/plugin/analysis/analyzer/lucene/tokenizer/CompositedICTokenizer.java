package org.elasticsearch.plugin.analysis.analyzer.lucene.tokenizer;

import com.homedo.bigdata.analysis.analyzer.lucene.ICTokenizer;
import com.homedo.bigdata.analysis.analyzer.lucene.TermTypeAttribute;
import com.homedo.bigdata.analysis.util.StringHelper;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CompositedICTokenizer extends Tokenizer {
	private ICTokenizerAndWeight[] tokenizerAndWeights;
	private int index;
	private boolean isReset = false;
	private CharTermAttribute term;
	private OffsetAttribute offsetAtt;
	private TermTypeAttribute typeAtt;
	private FlagsAttribute flagsAttr;
	private int flag;

	public CompositedICTokenizer(ICTokenizerAndWeight... tokenizerAndWeights) {

		if (tokenizerAndWeights == null) {
			throw new IllegalArgumentException("at least one tokenizer required");
		}

		for (ICTokenizerAndWeight tokenizerAndWeight : tokenizerAndWeights) {
			if (tokenizerAndWeight.weight == FlagEnum.TERM.getVal()) {
				throw new IllegalArgumentException("weight cannot be the same as FlagEnum.TERM");
			}
		}

		this.tokenizerAndWeights = tokenizerAndWeights;
		index = 0;
		term = addAttribute(CharTermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		typeAtt = addAttribute(TermTypeAttribute.class);
		flagsAttr = addAttribute(FlagsAttribute.class);
		flag = this.tokenizerAndWeights[0].weight;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (!isReset) {
			reset();
		}

		if (isReset && index < tokenizerAndWeights.length) {
			if (tokenizerAndWeights[index].tokenizer.incrementToken()) {
				clearAttributes();
				flagsAttr.setFlags(flag);

				CharTermAttribute termAttr = tokenizerAndWeights[index].tokenizer.getAttribute(CharTermAttribute.class);
				term.copyBuffer(termAttr.buffer(), 0, termAttr.length());

				if (tokenizerAndWeights[index].tokenizer.hasAttribute(OffsetAttribute.class)) {
					OffsetAttribute attr = tokenizerAndWeights[index].tokenizer.getAttribute(OffsetAttribute.class);
					offsetAtt.setOffset(attr.startOffset(), attr.endOffset());
				}

				if (tokenizerAndWeights[index].tokenizer.hasAttribute(TermTypeAttribute.class)) {
					TermTypeAttribute attr = tokenizerAndWeights[index].tokenizer.getAttribute(TermTypeAttribute.class);
					typeAtt.setType(attr.getType());
				}
				flag = FlagEnum.TERM.getVal();
				return true;
			}
			index++;
			if (index < tokenizerAndWeights.length) {
				flag = this.tokenizerAndWeights[index].weight;
			}
			return incrementToken();
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		for (ICTokenizerAndWeight ict : tokenizerAndWeights) {
			ict.tokenizer.reset();
		}
		index = 0;
		flag = this.tokenizerAndWeights[0].weight;
		isReset = true;
	}

	public void tokenizerSetReader(Reader reader) throws IOException {
		String content = StringHelper.readerToString(reader);
		for (ICTokenizerAndWeight ict : tokenizerAndWeights) {
			ict.tokenizer.setReader(new StringReader(content));
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		for (ICTokenizerAndWeight ict : tokenizerAndWeights) {
			ict.tokenizer.close();
		}
	}

	public static enum FlagEnum {
		TERM(0), SPLITTER(1);
		private int i;

		private FlagEnum(int i) {
			this.i = i;
		}

		public int getVal() {
			return i;
		}
	}

	public static class ICTokenizerAndWeight {
		public ICTokenizerAndWeight(ICTokenizer tokenizer, int weight) {
			this.tokenizer = tokenizer;
			this.weight = weight;
		}

		private ICTokenizer tokenizer;
		private int weight;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] items = new String[] { "标准机柜" };
		CompositedICTokenizer ts = new CompositedICTokenizer(
				new ICTokenizerAndWeight(new ICTokenizer(false, 2, false), 1));
		for (String item : items) {
			System.out.println("Item: " + item + " length:" + item.length());
			ts.tokenizerSetReader(new StringReader(item));
			ts.setReader(new StringReader(item));
			ts.reset();
			while (ts.incrementToken()) {
				CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
				FlagsAttribute fa = ts.getAttribute(FlagsAttribute.class);
				OffsetAttribute offset = ts.getAttribute(OffsetAttribute.class);
				System.out.println(term + "\t :flag " + fa.getFlags() + "  " + offset + " ");
			}
			ts.close();
			System.out.println();
		}

	}

}
