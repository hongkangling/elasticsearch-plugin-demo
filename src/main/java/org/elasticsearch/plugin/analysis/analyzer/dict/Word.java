package org.elasticsearch.plugin.analysis.analyzer.dict;

public class Word implements Comparable<Word> {
	/**
	 * 副词/介词=1
	 */
	public static final int type_PrepDict = 1;
	/**
	 * 姓氏词=2
	 */
	public static final int type_SurnameDict = 2;
	/**
	 * 量词=3
	 */
	public static final int type_QuantifierDict = 3;
	/**
	 * 停止词=4
	 */
	public static final int type_StopWords = 4;
	/**
	 * 后缀词=5
	 */
	public static final int type_SuffixDict = 5;
	/**
	 * 通用词=6
	 */
	public static final int type_MainDict = 6;
	/**
	 * 建材分类: 一级分类=1111
	 */
	public static final int type_Category1 = 1111;
	/**
	 * 建材分类: 二级分类=1113
	 */
	public static final int type_Category2 = 1113;
	/**
	 * 建材分类: 三级分类=1113
	 */
	public static final int type_Category3 = 1114;
	/**
	 * 建材品牌=1112
	 */
	public static final int type_Brand = 1112;
	/**
	 * 词性
	 */
	private int type = -1;
	private String types = "";
	/**
	 * 词文本
	 */
	private String wordtext;
	/**
	 * 属性ID
	 */
	private int attributeid = -1;
	private String attributeids = "";

	private int score = 0;
	private String scores = "";

	private int typeScore = 0;

	public int getScore() {
		return score;
	}

	public void setScore(int sc) {
		score = sc;
	}

	public int getTypeScore() {
		return typeScore;
	}

	public void setTypeScore(int tsc) {
		typeScore = tsc;
	}

	/**
	 * 获取词性
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 设置词性
	 * 
	 * @param type
	 */
	public void setType(int Type) {
		this.type = Type;
	}

	/**
	 * 获取词性
	 * 
	 * @return
	 */
	public String getTypes() {
		return types;
	}

	/**
	 * 设置词性
	 * 
	 * @param type
	 */
	public void setTypes(String Types) {
		this.types = Types;
	}

	/**
	 * 获取词文本
	 * 
	 * @return
	 */
	public String getWordText() {
		return wordtext;
	}

	/**
	 * 设置词文本
	 * 
	 * @param type
	 */
	public void setWordText(String WordText) {
		this.wordtext = WordText;
	}

	public Word(int Type, int attributeid, String Word) {
		this(Type, Word);
		this.attributeid = attributeid;
	}

	public Word(int Type, String Word) {
		this.type = Type;
		this.wordtext = Word;
	}

	public Word(int Type, String Word, String types) {
		this.type = Type;
		this.wordtext = Word;
		this.types = types;
	}

	public Word() {

	}

	public int compareTo(Word o) {
		int len = wordtext == null ? 0 : wordtext.length();
		int len2 = o.wordtext == null ? 0 : o.wordtext.length();
		return len - len2;
	}

	public int getAttributeid() {
		return attributeid;
	}

	public void setAttributeid(int attributeid) {
		this.attributeid = attributeid;
	}

	public String getAttributeids() {
		return attributeids;
	}

	public void setAttributeids(String attributeids) {
		this.attributeids = attributeids;
	}

	public String getScores() {
		return scores;
	}

	public void setScores(String scores) {
		this.scores = scores;
	}
}
