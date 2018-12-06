package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.analyzer.ICSegmentation;
import com.homedo.bigdata.analysis.analyzer.Lexeme;
import com.homedo.bigdata.analysis.analyzer.dict.DictLandmark;
import com.homedo.bigdata.analysis.analyzer.dict.Word;
import com.homedo.bigdata.analysis.analyzer.dict.WordMapping;
import com.homedo.bigdata.analysis.config.Config;
import com.homedo.bigdata.analysis.util.word.ZHConverter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ICTokenizer extends Tokenizer {
	// 分词类型，0 建索引时分词; 1 搜索时分词
	private int segment_type = 1;
	// 分词器实现
	private ICSegmentation _IKImplement;
	// 词元文本属性
	private CharTermAttribute termAtt;
	// 词元位移属性
	private OffsetAttribute offsetAtt;
	// 词元类型属性

	private TermTypeAttribute typeAtt;
	// 词元类型属性
	private TermTypesAttribute typesAtt;
	private TermScoreAttribute scoreAtt;
	private TermTypeScoreAttribute typeScoreAtt;

	private TermScoresAttribute scoresAttr;

	private TermTypeRefsAttribute typeRefsAttr; //

	// 记录最后一个词元的结束位置
	private int finalOffset;

	/**
	 * Lucene Tokenizer适配器类构造函数
	 * 
	 * @param ismax
	 *            当为true时，分词器进行最大词长切分；当为false是，采用最细粒度切分
	 */
	public ICTokenizer(boolean ismax, int type) {
		init(type);
		_IKImplement = new ICSegmentation(ismax, type);
	}

	public ICTokenizer(boolean ismax, int type, boolean isBackwardMatch) {
		init(type);
		_IKImplement = new ICSegmentation(ismax, type, isBackwardMatch);
	}

	private void init(int type) {
		offsetAtt = addAttribute(OffsetAttribute.class);
		termAtt = addAttribute(CharTermAttribute.class);
		typesAtt = addAttribute(TermTypesAttribute.class);
		typeAtt = addAttribute(TermTypeAttribute.class);
		scoreAtt = addAttribute(TermScoreAttribute.class);
		typeScoreAtt = addAttribute(TermTypeScoreAttribute.class);
		typeRefsAttr = addAttribute(TermTypeRefsAttribute.class);
		scoresAttr = addAttribute(TermScoresAttribute.class);
		segment_type = type;
	}

	public ArrayList<Word> GetWords(int CityID) {
		ArrayList<Word> wordList = new ArrayList<Word>();
		try {
			for (Lexeme lexeme = _IKImplement.next(); lexeme != null; lexeme = _IKImplement.next()) {
				Word wd = new Word(lexeme.getLexemeType(), lexeme.getLexemeText().trim());
				wd.setTypes(lexeme.getLexemeTypes());
				if (wd.getType() == 8 && CityID != 0) {
					Word landmark = DictLandmark.getLandmarkInfo(CityID, wd.getWordText());
				}
				wordList.add(wd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wordList;
	}

	private List<Word> synoWords = new ArrayList<Word>(2);

	@Override
	public final boolean incrementToken() throws IOException {
		// 清除所有的词元属性
		clearAttributes();
		// 处理同义词列表
		if (segment_type == 0 && synoWords.size() > 0) {
			Word wd = synoWords.get(0);
			// 将Lexeme转成Attributes
			// 设置词元文本
			termAtt.append(wd.getWordText());
			// 设置词元类型
			typeAtt.setType(wd.getType());
			typesAtt.setTypes(wd.getTypes());
			scoreAtt.setScore(wd.getScore());
			typeScoreAtt.setTypeScore(wd.getTypeScore());
			typeRefsAttr.setTypeRefs(wd.getAttributeids());
			scoresAttr.setScores(wd.getScores());
			synoWords.remove(0);
			return true;
		}

		Lexeme nextLexeme = _IKImplement.next();
		if (nextLexeme != null) {
			String word = nextLexeme.getLexemeText();
			int type = nextLexeme.getLexemeType();
			String types = nextLexeme.getLexemeTypes();
			// 设置同义词列表
			if (segment_type == 0 && Config.getInstance().getBoolean("synonyms", false)) {
				String synoWord = WordMapping.synonymMapping(word);
				if (!synoWord.equals("")) {
					String[] words = synoWord.split("\\|");
					for (String wd : words) {
						if (!wd.equals("")) {
							synoWords.add(new Word(type, wd, types));
						}
					}
				}
			}

			// 将Lexeme转成Attributes
			// 设置词元文本
			termAtt.append(word);
			// 设置词元类型
			typeAtt.setType(type);
			typesAtt.setTypes(types);
			// 设置词元长度
			termAtt.setLength(nextLexeme.getLength());
			scoreAtt.setScore(nextLexeme.score);
			typeScoreAtt.setTypeScore(nextLexeme.typeScore);
			typeRefsAttr.setTypeRefs(nextLexeme.getLexemeTypeAttrIds());

			//String lScores = nextLexeme.getScores();
			//scoresAttr.setScores(lScores);
			scoresAttr.setScores(nextLexeme.getScores());
			// 设置词元位移
			offsetAtt.setOffset(nextLexeme.getBeginPosition(), nextLexeme.getEndPosition());
			// 记录分词的最后位置
			finalOffset = nextLexeme.getEndPosition();
			// 返会true告知还有下个词元
			return true;
		}
		// 返会false告知词元输出完毕
		return false;
	}

	public void reset() throws IOException {
		super.reset();
		_IKImplement.reset(trad2simp(input));
	}

	@Override
	public final void end() {
		// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	public Reader trad2simp(Reader reader) {
		StringBuilder builder = new StringBuilder();
		int charsRead = -1;
		char[] chars = new char[100];
		do {
			try {
				charsRead = reader.read(chars, 0, chars.length);
			} catch (IOException e) {

				e.printStackTrace();
			}
			if (charsRead > 0)
				builder.append(chars, 0, charsRead);
		} while (charsRead > 0);
		reader = new StringReader(ZHConverter.trad2simp(builder.toString()));
		return reader;
	}
}
