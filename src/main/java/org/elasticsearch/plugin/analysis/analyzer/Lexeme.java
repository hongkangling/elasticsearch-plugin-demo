package org.elasticsearch.plugin.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.dict.Hit;

public class Lexeme implements Comparable<Lexeme> {
	// lexemeType常量
	// 未知
	public static final int TYPE_UNKNOWN = 0;
	// 英文
	public static final int TYPE_ENGLISH = 20;
	// 数字
	public static final int TYPE_ARABIC = 100;
	// 中文单字
	public static final int TYPE_CNCHAR = 101;
	// 日韩文字
	public static final int TYPE_OTHER_CJK = 102;
	// 中文数词
	public static final int TYPE_CNUM = 103;
	// 中文量词
	public static final int TYPE_COUNT = 104;
	// 中文数量词
	public static final int TYPE_CQUAN = 105;
	// 中文词元
	public static final int TYPE_CNWORD = 106;
	// 英文数字混合
	public static final int TYPE_LETTER = 107;

	// 词元的起始位移
	private int offset;
	// 词元的相对起始位置
	private int begin;
	// 词元的长度
	private int length;
	// 词元文本
	private String lexemeText;
	// 词元类型
	private int lexemeType;
	private String lexemeTypes = "";
	public int score = 0;
	public int typeScore = 0;

	public String scores = "";

	// 词元数据源ID
	private int lexemeTypeAttrId;
	private String lexemeTypeAttrIds = "";

	// 是否是未登录词
	private boolean isUnlistedWord = false;

	public Lexeme(int offset, int begin, int length, Hit hit) {
		this(offset, begin, length, hit.getWordType());
		this.lexemeTypes = hit.getWordTypes();
		this.score = hit.getScore();
		this.typeScore = hit.getTypeScore();
		this.lexemeTypeAttrId = hit.getAttributeId();
		this.lexemeTypeAttrIds = hit.getAttributeIds();
		this.scores = hit.getScores();

	}

	public Lexeme(int offset, int begin, int length, int lexemeType) {
		this.offset = offset;
		this.begin = begin;
		if (length < 0) {
			throw new IllegalArgumentException("length < 0");
		}
		this.length = length;
		this.lexemeType = lexemeType;
	}

	public Lexeme(int offset, int begin, int length, int lexemeType, boolean isUnlistedWord) {
		this(offset, begin, length, lexemeType);
		this.setUnlistedWord(isUnlistedWord);
	}

	public boolean isUnlistedWord() {
		return isUnlistedWord;
	}

	public void setUnlistedWord(boolean isUnlistedWord) {
		this.isUnlistedWord = isUnlistedWord;
	}

	/*
	 * 判断词元相等算法 起始位置偏移、起始位置、终止位置相同
	 * 
	 * @see java.lang.Object#equals(Object o)
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (this == o) {
			return true;
		}

		if (o instanceof Lexeme) {
			Lexeme other = (Lexeme) o;
			if (this.offset == other.getOffset() && this.begin == other.getBegin()
					&& this.length == other.getLength()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/*
	 * 词元哈希编码算法
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int absBegin = getBeginPosition();
		int absEnd = getEndPosition();
		return (absBegin * 37) + (absEnd * 31) + ((absBegin * absEnd) % getLength()) * 11;
	}

	/*
	 * 词元在排序集合中的比较算法
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Lexeme other) {
		// 起始位置优先
		if (this.begin < other.getBegin()) {
			return -1;
		} else if (this.begin == other.getBegin()) {
			// 词元长度优先
			if (this.length > other.getLength()) {
				return -1;
			} else if (this.length == other.getLength()) {
				return 0;
			} else {// this.length < other.getLength()
				return 1;
			}

		} else {// this.begin > other.getBegin()
			return 1;
		}
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getBegin() {
		return begin;
	}

	/**
	 * 获取词元在文本中的起始位置
	 * 
	 * @return int
	 */
	public int getBeginPosition() {
		return offset + begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	/**
	 * 获取词元在文本中的结束位置
	 * 
	 * @return int
	 */
	public int getEndPosition() {
		return offset + begin + length;
	}

	/**
	 * 获取词元的字符长度
	 * 
	 * @return int
	 */
	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		if (this.length < 0) {
			throw new IllegalArgumentException("length < 0");
		}
		this.length = length;
	}

	/**
	 * 获取词元的文本内容
	 * 
	 * @return String
	 */
	public String getLexemeText() {
		if (lexemeText == null) {
			return "";
		}
		return lexemeText;
	}

	public void setLexemeText(String lexemeText) {
		if (lexemeText == null) {
			this.lexemeText = "";
			this.length = 0;
		} else {
			this.lexemeText = lexemeText;
			this.length = lexemeText.length();
		}
	}

	/**
	 * 获取词元类型
	 * 
	 * @return int
	 */
	public int getLexemeType() {
		return lexemeType;
	}

	public void setLexemeType(int lexemeType) {
		this.lexemeType = lexemeType;
	}

	/**
	 * 获取词元类型
	 * 
	 * @return int
	 */
	public String getLexemeTypes() {
		return lexemeTypes;
	}

	public void setLexemeTypes(String lexemeTypes) {
		this.lexemeTypes = lexemeTypes;
	}

	public int getLexemeTypeAttrId() {
		return lexemeTypeAttrId;
	}

	public void setLexemeTypeAttrId(int lexemeTypeAttrId) {
		this.lexemeTypeAttrId = lexemeTypeAttrId;
	}

	public String getLexemeTypeAttrIds() {
		return lexemeTypeAttrIds;
	}

	public void setLexemeTypeAttrIds(String lexemeTypeAttrIds) {
		this.lexemeTypeAttrIds = lexemeTypeAttrIds;
	}

	public String getScores() {
		return scores;
	}

	public void setScores(String scores) {
		this.scores = scores;
	}

	/**
	 * 合并两个相邻的词元
	 * 
	 * @param l
	 * @param lexemeType
	 * @return boolean 词元是否成功合并
	 */
	public boolean append(Lexeme l, int lexemeType) {
		if (l != null && this.getEndPosition() == l.getBeginPosition()) {
			this.length += l.getLength();
			this.lexemeType = lexemeType;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	public String toString() {
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(this.getBeginPosition()).append("-").append(this.getEndPosition());
		strbuf.append(" : ").append(this.lexemeText).append(" : \t");
		switch (lexemeType) {
		case TYPE_UNKNOWN:
			strbuf.append("UNKONW");
			break;
		case TYPE_ENGLISH:
			strbuf.append("ENGLISH");
			break;
		case TYPE_ARABIC:
			strbuf.append("ARABIC");
			break;
		case TYPE_LETTER:
			strbuf.append("LETTER");
			break;
		case TYPE_CNWORD:
			strbuf.append("CN_WORD");
			break;
		case TYPE_CNCHAR:
			strbuf.append("CN_CHAR");
			break;
		case TYPE_OTHER_CJK:
			strbuf.append("OTHER_CJK");
			break;
		case TYPE_COUNT:
			strbuf.append("COUNT");
			break;
		case TYPE_CNUM:
			strbuf.append("CN_NUM");
			break;
		case TYPE_CQUAN:
			strbuf.append("CN_QUAN");
			break;

		}
		return strbuf.toString();
	}
}