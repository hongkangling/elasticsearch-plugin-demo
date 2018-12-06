package org.elasticsearch.plugin.analysis.analyzer.pinyin.tokenizer;

import com.homedo.bigdata.analysis.analyzer.dict.Hit;
import com.homedo.bigdata.analysis.analyzer.pinyin.PinYinMarkEnum;
import com.homedo.bigdata.analysis.analyzer.pinyin.util.GeneralPinYinHelper;
import com.homedo.bigdata.analysis.analyzer.pinyin.util.JianPinHelper;
import com.homedo.bigdata.analysis.config.Config;
import com.homedo.bigdata.analysis.util.StringHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SimpleJianPinTokenizer extends AbstractPinYinTokenizer {
	private final CharTermAttribute term;
	private static final String PREFIX = PinYinMarkEnum.STARTS.getToken() + PinYinMarkEnum.ABBR_PREFIX.getToken();
	private char[] buffer;
	private List<String> wordSegments;
	private int index;
	private boolean initialized;

	// 是否启用多音字拼音表
	private boolean enableMultiPinyinConfig = Config.getInstance().getBoolean("analysis.multi_pinyin_config", false);
	private List<Hit> tmpHits = new LinkedList<>();
	private String[] pinyin = new String[512]; // 存储输入串中中文的拼音，下标为某个中文在原始输入串中的偏移

	public SimpleJianPinTokenizer() {
		term = addAttribute(CharTermAttribute.class);
		buffer = new char[32];
		initialized = false;

	}

	private void init() throws IOException {
		String content = StringHelper.readerToString(input).toLowerCase();
		getPinyinFromDict(content);
		wordSegments = JianPinHelper.getWordsShouPins(content, pinyin);
		index = 0;
		tmpHits.clear();
		initialized = true;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (!initialized) {
			init();
		}
		if (index >= wordSegments.size()) {
			return false;
		}
		String composedToken = composeToken(wordSegments.get(index));
		index++;
		copyAndExtendBufferIfNecessary(composedToken);
		term.copyBuffer(buffer, 0, composedToken.length());
		return true;
	}

	private void copyAndExtendBufferIfNecessary(String composedToken) {
		if (buffer.length < composedToken.length()) {
			buffer = new char[2 * composedToken.length()];
		}
		for (int i = 0; i < composedToken.length(); i++) {
			buffer[i] = composedToken.charAt(i);
		}
	}

	private String composeToken(String token) {
		if (index == 0) {
			return PREFIX + token + PinYinMarkEnum.WORD_SUFFIX.getToken()
					+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken() + (PREFIX.length() + 1);
		} else {
			int commonStrIndex = getCommonStrIndex(wordSegments.get(index - 1), token);
			return PREFIX + token + PinYinMarkEnum.WORD_SUFFIX.getToken()
					+ PinYinMarkEnum.TERM_LENGTH_DELIMITER.getToken() + (PREFIX.length() + 1 + commonStrIndex);
		}
	}

	private int getCommonStrIndex(String string, String token) {
		for (int i = 0; i < string.toCharArray().length && i < token.length(); i++) {
			if (string.charAt(i) != token.charAt(i)) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		initialized = false;
	}

	@Override
	public void setDefaultPinYin(String defaultPinYin) {

	}

	// 获取多音字的拼音
	// 这里从拼音词表获取的时候暂时不考虑相交的情况
	private void getPinyinFromDict(String content) {
		if (!enableMultiPinyinConfig) {
			return;
		}

		this.pinyin = GeneralPinYinHelper.getPinyinFromDict(content, pinyin, tmpHits);
	}
}
