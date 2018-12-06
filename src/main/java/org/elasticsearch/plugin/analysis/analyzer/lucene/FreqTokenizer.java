package org.elasticsearch.plugin.analysis.analyzer.lucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public abstract class FreqTokenizer<T extends TokenStream> extends Tokenizer {

	final public static Reader EMPTY_READER = new StringReader("");

	public static final char SEPARATOR1 = ',';
	public static final char SEPARATOR2 = '#';

	T stream;
	final List<String> fieldValues;
	final List<Integer> fieldFreqs;
	final List<Integer> fieldStartOffsets;

	CharTermAttribute textAttr;
	PositionIncrementAttribute posIncrAttr;
	OffsetAttribute offsetAttr = null;

	int fieldIndex;
	int termFreq;

	public FreqTokenizer(T stream) {
		this.stream = stream;
		fieldValues = new ArrayList<String>();
		fieldFreqs = new ArrayList<Integer>();
		fieldStartOffsets = new ArrayList<Integer>();

		textAttr = stream.addAttribute(CharTermAttribute.class);
		posIncrAttr = stream.addAttribute(PositionIncrementAttribute.class);

		if (stream.hasAttribute(OffsetAttribute.class)) {
			offsetAttr = stream.getAttribute(OffsetAttribute.class);
		}

		try {
			reset();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract void reuseTokenStream(T stream) throws IOException;

	private void splitCombinedFields(Reader input) throws IOException {
		fieldValues.clear();
		fieldFreqs.clear();
		fieldStartOffsets.clear();
		StringBuilder sb = new StringBuilder(1000);
		int c;
		while ((c = input.read()) != -1) {
			sb.append((char) c);
		}
		// fieldLength,fieldFrequence#fieldValuefieldLength,fieldFrequence#fieldValue...
		int pos0 = 0;// the position of the fieldLength
		int pos1 = -1;// the position of the comma between fieldLength and
						// fieldFrequence
		int lastOffest = 0;
		for (int i = 0; i < sb.length();) {
			char ch = sb.charAt(i);
			if (ch == SEPARATOR1) {
				pos1 = i;
				++i;
			} else if (ch == SEPARATOR2) {
				int len = Integer.parseInt(sb.substring(pos0, pos1));
				int freq = Integer.parseInt(sb.substring(pos1 + 1, i));
				pos0 = i + 1 + len;
				fieldValues.add(sb.substring(i + 1, pos0));
				fieldFreqs.add(freq);
				fieldStartOffsets.add(lastOffest);
				lastOffest += len + 2;// Distinguish two fields
				i = pos0 + 1;
			} else {
				++i;
			}
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		splitCombinedFields(input);
		fieldIndex = -1;
		termFreq = 0;
		while (stream.incrementToken()) {
		} // disable stream for initialize
	}

	@Override
	public boolean incrementToken() throws IOException {
		int posIncr = 0;// default ,repeat one token
		while (termFreq < 1) {// find next term
			int posIncrInterval = 0;// position interval between two fields
			while (!stream.incrementToken()) {// find next available field
				if (++fieldIndex == fieldValues.size()) {// all fields tokenized
					return false;
				}
				// resetField,find next field
				reuseTokenStream(stream);

				// Distinguish two fields
				posIncrInterval = fieldIndex == 0 ? 0 : 2;
			}
			posIncr = posIncrInterval + posIncrAttr.getPositionIncrement();
			if (offsetAttr != null) {
				// Distinguish two fields
				int offestIncr = fieldStartOffsets.get(fieldIndex);
				offsetAttr.setOffset(offsetAttr.startOffset() + offestIncr, offsetAttr.endOffset() + offestIncr);
			}
			termFreq = fieldFreqs.get(fieldIndex).intValue();
		}
		posIncrAttr.setPositionIncrement(posIncr);
		--termFreq;
		return true;
	}

	/**
	 * format:fieldLength,fieldFrequence#fieldValuefieldLength,fieldFrequence#fieldValue...
	 * 
	 * @param texts
	 *            the field values
	 * @param freqs
	 *            the field frequencies,the length of the array must be equal to
	 *            that of texts's
	 * @return a string combined by texts with length and frequency of each
	 *         field
	 */
	public static String append(String[] texts, int[] freqs) {
		StringBuilder sb = new StringBuilder(5000);
		for (int i = 0; i < texts.length; i++) {
			append(sb, texts[i], freqs[i]);
		}
		return sb.toString();
	}

	public static void append(StringBuilder sb, String text, int freq) {
		if (text == null) {
			text = "";
		}
		sb.append(text.length());
		sb.append(SEPARATOR1);
		sb.append(freq);
		sb.append(SEPARATOR2);
		sb.append(text);
	}

}
