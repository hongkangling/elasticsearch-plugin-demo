/**
 * 
 */
package org.elasticsearch.plugin.analysis.analyzer.dict;

public class Hit {
	// Hit不匹配
	private static final int UNMATCH = 0x00000000;
	// Hit完全匹配
	private static final int MATCH = 0x00000001;
	// Hit前缀匹配
	private static final int PREFIX = 0x00000010;
	// 该HIT当前状态，默认未匹配
	private int hitState = UNMATCH;
	// 记录词典匹配过程中，当前匹配到的词典分支节点
	private DictSegment matchedDictSegment;
	/*
	 * 词段开始位置
	 */
	private int begin;
	/*
	 * 词段的结束位置
	 */
	private int end;
	/**
	 * 词性
	 */
	private int wordType;
	/**
	 * 
	 */
	private String wordTypes = "";

	private int attributeId = -1;
	private String attributeIds = "";

	private int score = 0;
	private String scores = "";

	private int typeScore = 0;

	// 用于人工干预词表，存储切分规则
	// 数组的长度表示切成几个词，值表示每个词的长度
	private int[] userRule = null;

	// 当前词的拼音
	private String[] pinyin;

	/**
	 * 判断是否完全匹配
	 */
	public boolean isMatch() {
		return (this.hitState & MATCH) > 0;
	}

	/**
	 * 
	 */
	public void setMatch() {
		this.hitState = this.hitState | MATCH;
	}

	/**
	 * 判断是否是词的前缀
	 */
	public boolean isPrefix() {
		return (this.hitState & PREFIX) > 0;
	}

	/**
	 * 
	 */
	public void setPrefix() {
		this.hitState = this.hitState | PREFIX;
	}

	/**
	 * 判断是否是不匹配
	 */
	public boolean isUnmatch() {
		return this.hitState == UNMATCH;
	}

	/**
	 * 
	 */
	public void setUnmatch() {
		this.hitState = UNMATCH;
	}

	public DictSegment getMatchedDictSegment() {
		return matchedDictSegment;
	}

	public void setMatchedDictSegment(DictSegment matchedDictSegment) {
		this.matchedDictSegment = matchedDictSegment;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * 获取词性
	 * 
	 * @return
	 */
	public int getWordType() {
		return wordType;
	}

	/**
	 * 设置词性
	 * 
	 * @param type
	 */
	public void setWordType(int wordtype) {
		this.wordType = wordtype;
	}

	/**
	 * 获取词性
	 * 
	 * @return
	 */
	public String getWordTypes() {
		return wordTypes;
	}

	/**
	 * 设置词性
	 * 
	 * @param type
	 */
	public void setWordTypes(String wordtypes) {
		this.wordTypes = wordtypes;
	}

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

	public int[] getUserRule() {
		return userRule;
	}

	public void setUserRule(int[] userRule) {
		this.userRule = userRule;
	}

	public String[] getPinyin() {
		return pinyin;
	}

	public void setPinyin(String[] pinyin) {
		this.pinyin = pinyin;
	}

	public int getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}

	public String getAttributeIds() {
		return attributeIds;
	}

	public void setAttributeIds(String attributeIds) {
		this.attributeIds = attributeIds;
	}

	public String getScores() {
		return scores;
	}

	public void setScores(String scores) {
		this.scores = scores;
	}
}
