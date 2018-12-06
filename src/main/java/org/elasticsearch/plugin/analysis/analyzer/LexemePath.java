package org.elasticsearch.plugin.analysis.analyzer;

/**
 * Lexeme链（路径）
 */
class LexemePath extends QuickSortSet implements Comparable<LexemePath> {

	// 起始位置
	private int pathBegin;
	// 结束
	private int pathEnd;
	// 词元链的有效字符长度
	private int payloadLength;
	// 是否最大长度切分
	private boolean isMaxLength = false;
	// 是否后向匹配
	private boolean isBackwardMatch = true;
	private int typeScore = 0;
	private int score = 0;

	LexemePath() {
		this.pathBegin = -1;
		this.pathEnd = -1;
		this.payloadLength = 0;
	}

	LexemePath(boolean ismaxlength) {
		this();
		this.isMaxLength = ismaxlength;
	}

	LexemePath(boolean ismaxlength, boolean isBackwardMatch) {
		this(ismaxlength);
		this.isBackwardMatch = isBackwardMatch;
	}

	/**
	 * 向LexemePath追加相交的Lexeme()
	 * 
	 * @param lexeme
	 * @return
	 */
	boolean addCrossLexeme(Lexeme lexeme) {

		if (this.isEmpty()) {
			this.addLexeme(lexeme);
			this.pathBegin = lexeme.getBegin();
			this.pathEnd = lexeme.getBegin() + lexeme.getLength();
			this.payloadLength += lexeme.getLength();
			typeScore = typeScore + lexeme.typeScore;
			score = score + lexeme.score;
			return true;

		} else if (this.checkCross(lexeme)) {
			this.addLexeme(lexeme);
			typeScore = typeScore + lexeme.typeScore;
			score = score + lexeme.score;
			if (lexeme.getBegin() + lexeme.getLength() > this.pathEnd) {
				this.pathEnd = lexeme.getBegin() + lexeme.getLength();
			}
			this.payloadLength = this.pathEnd - this.pathBegin;
			return true;

		} else {
			return false;

		}
	}

	/**
	 * 向LexemePath追加不相交的Lexeme
	 * 
	 * @param lexeme
	 * @return
	 */
	boolean addNotCrossLexeme(Lexeme lexeme) {
		if (this.isEmpty()) {
			this.addLexeme(lexeme);
			typeScore = typeScore + lexeme.typeScore;
			score = score + lexeme.score;
			this.pathBegin = lexeme.getBegin();
			this.pathEnd = lexeme.getBegin() + lexeme.getLength();
			this.payloadLength += lexeme.getLength();
			return true;

		} else if (this.checkCross(lexeme)) {
			return false;

		} else {
			this.addLexeme(lexeme);
			typeScore = typeScore + lexeme.typeScore;
			score = score + lexeme.score;
			this.payloadLength += lexeme.getLength();
			Lexeme head = this.peekFirst();
			this.pathBegin = head.getBegin();
			Lexeme tail = this.peekLast();
			this.pathEnd = tail.getBegin() + tail.getLength();
			return true;

		}
	}

	/**
	 * 移除尾部的Lexeme
	 * 
	 * @return
	 */
	Lexeme removeTail() {
		Lexeme tail = this.pollLast();
		if (this.isEmpty()) {
			this.pathBegin = -1;
			this.pathEnd = -1;
			this.payloadLength = 0;
		} else {
			this.payloadLength -= tail.getLength();
			Lexeme newTail = this.peekLast();
			this.pathEnd = newTail.getBegin() + newTail.getLength();
		}
		return tail;
	}

	/**
	 * 检测词元位置交叉（有歧义的切分）
	 * 
	 * @param lexeme
	 * @return
	 */
	boolean checkCross(Lexeme lexeme) {
		return (lexeme.getBegin() >= this.pathBegin && lexeme.getBegin() < this.pathEnd)
				|| (this.pathBegin >= lexeme.getBegin() && this.pathBegin < lexeme.getBegin() + lexeme.getLength());
	}

	int getPathBegin() {
		return pathBegin;
	}

	int getPathEnd() {
		return pathEnd;
	}

	/**
	 * 获取Path的有效词长
	 * 
	 * @return
	 */
	int getPayloadLength() {
		return this.payloadLength;
	}

	/**
	 * 获取LexemePath的路径长度
	 * 
	 * @return
	 */
	int getPathLength() {
		return this.pathEnd - this.pathBegin;
	}

	public void setIsMaxLength(boolean ismax) {
		isMaxLength = ismax;
	}

	/**
	 * X权重（词元长度积）
	 * 
	 * @return
	 */
	int getXWeight() {
		int product = 1;
		Cell c = this.getHead();
		while (c != null && c.getLexeme() != null) {
			product *= c.getLexeme().getLength();
			c = c.getNext();
		}
		return product;
	}

	/**
	 * 词元位置权重
	 * 
	 * @return
	 */
	int getPWeight() {
		int pWeight = 0;
		int p = 0;
		Cell c = this.getHead();
		while (c != null && c.getLexeme() != null) {
			p++;
			pWeight += p * c.getLexeme().getLength();
			c = c.getNext();
		}
		return pWeight;
	}

	LexemePath copy() {
		LexemePath theCopy = new LexemePath(isMaxLength, isBackwardMatch);
		theCopy.pathBegin = this.pathBegin;
		theCopy.pathEnd = this.pathEnd;
		theCopy.payloadLength = this.payloadLength;
		Cell c = this.getHead();
		while (c != null && c.getLexeme() != null) {
			theCopy.addLexeme(c.getLexeme());
			theCopy.typeScore = theCopy.typeScore + c.getLexeme().typeScore;
			theCopy.score = theCopy.score + c.getLexeme().score;
			c = c.getNext();
		}
		return theCopy;
	}

	public int compareTo(LexemePath o) {
		if (isMaxLength) {
			if (this.payloadLength > o.payloadLength) {
				return -1;
			} else if (this.payloadLength < o.payloadLength) {
				return 1;
			} else {
				if (this.size() < o.size()) {
					return -1;
				} else if (this.size() > o.size()) {
					return 1;
				} else {
					if (this.getPathLength() > o.getPathLength()) {
						return -1;
					} else if (this.getPathLength() < o.getPathLength()) {
						return 1;
					}
				}
			}
		}
		if (!isMaxLength) {
			if (this.size() < o.size()) {
				return -1;
			} else if (this.size() > o.size()) {
				return 1;
			}
		}
		if (this.typeScore > o.typeScore)
			return -1;
		else if (this.typeScore < o.typeScore)
			return 1;

		else if (this.typeScore == o.typeScore) {
			if (this.score > o.score)
				return -1;
			else if (this.score < o.score)
				return 1;
		}
		if (this.pathEnd > o.pathEnd) {
			return this.isBackwardMatch ? -1 : 1;
		} else if (pathEnd < o.pathEnd) {
			return this.isBackwardMatch ? 1 : -1;
		} else {
			if (this.getXWeight() > o.getXWeight()) {
				return -1;
			} else if (this.getXWeight() < o.getXWeight()) {
				return 1;
			} else {
				if (this.getPWeight() > o.getPWeight()) {
					return -1;
				} else if (this.getPWeight() < o.getPWeight()) {
					return 1;
				}

			}
		}

		return 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("pathBegin  : ").append(pathBegin).append("\r\n");
		sb.append("pathEnd  : ").append(pathEnd).append("\r\n");
		sb.append("payloadLength  : ").append(payloadLength).append("\r\n");
		Cell head = this.getHead();
		while (head != null) {
			sb.append("lexeme : ").append(head.getLexeme()).append("\r\n");
			head = head.getNext();
		}
		return sb.toString();
	}

}
