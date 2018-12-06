package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.util.word.CharacterHelper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class NormalizeTokenFilter extends AbstractTokenFilter {
	private final CharTermAttribute termAtt;
	private boolean arabicNum2CnNum = true;

	public NormalizeTokenFilter(TokenStream in) {
		super(in);
		// charUtils = CharacterUtils.getInstance(matchVersion);
		termAtt = addAttribute(CharTermAttribute.class);
	}

	public void setArabicNum2CnNum(boolean arabicNum2CnNum) {
		this.arabicNum2CnNum = arabicNum2CnNum;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			normalizeI(termAtt.buffer());
			return true;
		} else {
			return false;
		}
	}

	private void normalizeI(char[] buffer) {
		if (buffer == null) {
			return;
		}
		for (int i = 0; i < buffer.length;) {
			i += Character.toChars(CharacterHelper.normalize(Character.codePointAt(buffer, i), arabicNum2CnNum), buffer, i);
		}
	}


	public static void normalize(char[] buffer) {
		if (buffer == null) {
			return;
		}
		for (int i = 0; i < buffer.length;) {
			i += Character.toChars(CharacterHelper.normalize(Character.codePointAt(buffer, i)), buffer, i);
		}
	}

	@Override
	public void reset() throws IOException {
		super.clearAttributes();
		super.reset();
	}

	public static String nomalize(String word) {
		if (word == null) {
			return null;
		}
		char[] buffer = word.toCharArray();
		NormalizeTokenFilter.normalize(buffer);
		return new String(buffer);
	}

}
