package org.elasticsearch.plugin.analysis.analyzer;

import com.homedo.bigdata.analysis.analyzer.dict.Hit;
import com.homedo.bigdata.analysis.config.Config;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import com.homedo.bigdata.analysis.analyzer.dict.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Context {

	// 分词类型 1搜索时分词; 0=建索引时分词
	private int segmentType = 1;

	// 默认缓冲区大小
	private static final int BUFF_SIZE = 4096;
	// 缓冲区耗尽的临界值
	private static final int BUFF_EXHAUST_CRITICAL = 100;

	// 字符窜读取缓冲
	private char[] segmentBuff;
	// 字符类型数组
	private int[] charTypes;

	// 记录Reader内已分析的字串总长度
	// 在分多段分析词元时，该变量累计当前的segmentBuff相对于reader起始位置的位移
	private int buffOffset;
	// 当前缓冲区位置指针
	private int cursor;
	// 最近一次读入的,可处理的字串长度
	private int available;

	// 子分词器锁
	// 该集合非空，说明有子分词器在占用segmentBuff
	private Set<String> buffLocker;

	// 原始分词结果集合，未经歧义处理
	private QuickSortSet orgLexemes;
	// LexemePath位置索引表
	private Map<Integer, LexemePath> pathMap;
	// 最终分词结果集
	private LinkedList<Lexeme> results;
	// 存放经过组合的词
	private LinkedList<Lexeme> combineResults;

	private UserRuleProcessor userRuleProcessor;

	private LetterProcessor letterProcessor;

	static private Logger log = Loggers.getLogger(Context.class);
	private boolean analysisLog = Config.getInstance().getBoolean("analysis.log", false); // 是否打印分词日志
	private boolean analysisSort = Config.getInstance().getBoolean("analysis.sort", false); // 是否排序分词

	public Context(int segmenttype) {
		this.segmentType = segmenttype;
		this.segmentBuff = new char[BUFF_SIZE];
		this.charTypes = new int[BUFF_SIZE];
		this.buffLocker = new HashSet<String>();
		this.orgLexemes = new QuickSortSet();
		this.pathMap = new HashMap<Integer, LexemePath>();
		this.results = new LinkedList<Lexeme>();
		this.combineResults = new LinkedList<Lexeme>();
		this.userRuleProcessor = new UserRuleProcessor();
		this.letterProcessor = new LetterProcessor();
	}

	public int getCursor() {
		return this.cursor;
	}
	//
	// void setCursor(int cursor){
	// this.cursor = cursor;
	// }

	public char[] getSegmentBuff() {
		return this.segmentBuff;
	}

	public char getCurrentChar() {
		return this.segmentBuff[this.cursor];
	}

	public int getCurrentCharType() {
		return this.charTypes[this.cursor];
	}

	public int getNextCharType() {
		if (this.cursor < this.available - 1) {
			return CharacterUtil.identifyCharType(this.segmentBuff[this.cursor + 1]);
		} else {
			return CharacterUtil.CHAR_USELESS;
		}
	}

	public int getBufferOffset() {
		return this.buffOffset;
	}

	/**
	 * 根据context的上下文情况，填充segmentBuff
	 * 
	 * @param reader
	 * @return 返回待分析的（有效的）字串长度
	 * @throws IOException
	 */
	int fillBuffer(Reader reader) throws IOException {
		int readCount = 0;
		if (this.buffOffset == 0) {
			// 首次读取reader
			readCount = reader.read(segmentBuff);
		} else {
			int offset = this.available - this.cursor;
			if (offset > 0) {
				// 最近一次读取的>最近一次处理的，将未处理的字串拷贝到segmentBuff头部
				System.arraycopy(this.segmentBuff, this.cursor, this.segmentBuff, 0, offset);
				readCount = offset;
			}
			// 继续读取reader ，以onceReadIn - onceAnalyzed为起始位置，继续填充segmentBuff剩余的部分
			readCount += reader.read(this.segmentBuff, offset, BUFF_SIZE - offset);
		}
		// 记录最后一次从Reader中读入的可用字符长度
		this.available = readCount;
		// 重置当前指针
		this.cursor = 0;
		return readCount;
	}

	/**
	 * 初始化buff指针，处理第一个字符
	 */
	void initCursor() {
		this.cursor = 0;
		this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
		this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
	}

	/**
	 * 指针+1 成功返回 true； 指针已经到了buff尾部，不能前进，返回false 并处理当前字符
	 */
	boolean moveCursor() {
		if (this.cursor < this.available - 1) {
			this.cursor++;
			this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
			this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 设置当前segmentBuff为锁定状态 加入占用segmentBuff的子分词器名称，表示占用segmentBuff
	 * 
	 * @param segmenterName
	 */
	public void lockBuffer(String segmenterName) {
		this.buffLocker.add(segmenterName);
	}

	/**
	 * 移除指定的子分词器名，释放对segmentBuff的占用
	 * 
	 * @param segmenterName
	 */
	public void unlockBuffer(String segmenterName) {
		this.buffLocker.remove(segmenterName);
	}

	/**
	 * 只要buffLocker中存在segmenterName 则buffer被锁定
	 * 
	 * @return boolean 缓冲去是否被锁定
	 */
	boolean isBufferLocked() {
		return this.buffLocker.size() > 0;
	}

	/**
	 * 判断当前segmentBuff是否已经用完 当前执针cursor移至segmentBuff末端this.available - 1
	 * 
	 * @return
	 */
	public boolean isBufferConsumed() {
		return this.cursor == this.available - 1;
	}

	/**
	 * 判断segmentBuff是否需要读取新数据
	 * 
	 * 满足一下条件时， 1.available == BUFF_SIZE 表示buffer满载 2.buffIndex < available - 1
	 * && buffIndex > available - BUFF_EXHAUST_CRITICAL表示当前指针处于临界区内
	 * 3.!context.isBufferLocked()表示没有segmenter在占用buffer
	 * 要中断当前循环（buffer要进行移位，并再读取数据的操作）
	 * 
	 * @return
	 */
	boolean needRefillBuffer() {
		return this.available == BUFF_SIZE && this.cursor < this.available - 1
				&& this.cursor > this.available - BUFF_EXHAUST_CRITICAL && !this.isBufferLocked();
	}

	/**
	 * 累计当前的segmentBuff相对于reader起始位置的位移
	 */
	void markBufferOffset() {
		this.buffOffset += this.cursor;
	}

	/**
	 * 向分词结果集添加词元
	 * 
	 * @param lexeme
	 */
	public void addLexeme(Lexeme lexeme) {
		this.orgLexemes.addLexeme(lexeme);
	}

	/**
	 * 添加分词结果路径 路径起始位置 ---> 路径 映射表
	 * 
	 * @param path
	 */
	public void addLexemePath(LexemePath path) {
		if (path != null) {
			this.pathMap.put(path.getPathBegin(), path);
		}
	}

	/**
	 * 返回原始分词结果
	 * 
	 * @return
	 */
	public QuickSortSet getOrgLexemes() {
		return this.orgLexemes;
	}

	/**
	 * 输出分词结果到结果集合
	 */
	void outputToResult() {
		int index = 0;
		int preUnknownIndex = -1;
		for (; index <= this.cursor;) {
			// 跳过标点符号等字符
			if (CharacterUtil.CHAR_USELESS == this.charTypes[index]) {
				// 前面一段未匹配
				preUnknownIndex = processUnknown(preUnknownIndex, index - preUnknownIndex);
				index++;
				continue;
			}
			// 从pathMap找出对应index位置的LexemePath
			LexemePath path = this.pathMap.get(index);
			if (path != null) {
				// 前面一段未匹配
				preUnknownIndex = processUnknown(preUnknownIndex, index - preUnknownIndex);
				// 输出LexemePath中的lexeme到results集合
				Lexeme l = path.pollFirst();
				while (l != null) {
					this.results.add(l);
					// 将index移至lexeme后
					index = l.getBegin() + l.getLength();
					l = path.pollFirst();
					if (l != null) {
						// 输出path内部，词元间遗漏的单字
						for (; index < l.getBegin(); index++) {
							this.outputSingleCJK(index);
						}
					}
				}
			} else {// pathMap中找不到index对应的LexemePath
					// 单字输出
				if (preUnknownIndex == -1)
					preUnknownIndex = index;
				// 最后一个字
				if (index == this.cursor) {
					preUnknownIndex = processUnknown(preUnknownIndex, index - preUnknownIndex + 1);
				}
				index++;
			}
		}
		// segmentType==2是返回未识别词连续的整段

		// 清空当前的Map
		this.pathMap.clear();

		userRuleProcessor.process();
		letterProcessor.process();
		postProcess();
	}

	// 对结果集进行后续处理
	private void postProcess() {
		// 这里适配原有逻辑，当搜索分词时，仅保留二元组合词，去掉相应的单字结果
		if (segmentType > 0) {
			for (Lexeme lex : combineResults) {
				if (inUserRuleRanges(lex)) {
					continue;
				}

				for (int i = 0; i < results.size();) {
					if (results.get(i).getBegin() > lex.getBegin() + lex.getLength()) {
						break;
					}

					if (results.get(i).isUnlistedWord() && results.get(i).getBegin() >= lex.getBegin()
							&& results.get(i).getBegin() + results.get(i).getLength() <= lex.getBegin()
									+ lex.getLength()) {
						results.remove(i);
					} else {
						++i;
					}
				}
			}
		} else {
			logCrossLexeme();

			// 索引分词时，启用一元、二元分词
			for (int index = 0; index <= this.cursor; ++index) {
				// 跳过非中文字符
				if (CharacterUtil.CHAR_CHINESE != this.charTypes[index]) {
					continue;
				}

				// 一元
				Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, Lexeme.TYPE_CNCHAR);
				if (!exists(singleCharLexeme))
					this.results.add(singleCharLexeme);

				// 二元
				if (index < cursor && CharacterUtil.CHAR_CHINESE == this.charTypes[index + 1]) {
					Lexeme doubleLexeme = new Lexeme(this.buffOffset, index, 2, Lexeme.TYPE_CNWORD);
					if (!inUserRuleRanges(doubleLexeme) && !exists(doubleLexeme))
						this.results.add(doubleLexeme);
				}
			}
		}

		// 将组合词放进结果集
		for (Lexeme lex : combineResults) {
			if (!inUserRuleRanges(lex) && !exists(lex)) {
				results.add(lex);
			}
		}
		combineResults.clear();

		if (analysisSort) {
			Collections.sort(results);
		}
	}

	private int processUnknown(int preUnknownIndex, int len) {
		if (preUnknownIndex != -1) {
			for (int i = preUnknownIndex; i < preUnknownIndex + len; ++i) {
				outputSingleCJK(i);
			}

			if (len > 1) {
				if (segmentType > 0) {// search index将未识别词段进行二元迭代
					for (int i = preUnknownIndex; i < preUnknownIndex + len - 1; i++) {
						// 二元
						if (i < cursor && CharacterUtil.CHAR_CHINESE == this.charTypes[i]
								&& CharacterUtil.CHAR_CHINESE == this.charTypes[i + 1]) {
							Lexeme doubleLexeme = new Lexeme(this.buffOffset, i, 2, Lexeme.TYPE_CNWORD);
							if (!exists(doubleLexeme))
								this.combineResults.add(doubleLexeme);
						}
					}
				} else {
					Lexeme unknownLexeme = new Lexeme(this.buffOffset, preUnknownIndex, len, -2);
					if (!exists(unknownLexeme))
						this.combineResults.add(unknownLexeme);
				}
			}
			preUnknownIndex = -1;
		}
		return preUnknownIndex;
	}

	private boolean exists(Lexeme l) {
		for (Lexeme lex : this.results) {
			if (lex.getBegin() == l.getBegin() && lex.getLength() == l.getLength())
				return true;
		}
		return false;
	}

	// 判断组合词是否在人工配置词的起始范围内
	private boolean inUserRuleRanges(Lexeme lex) {
		for (int i = 0; i < userRuleProcessor.userRuleRanges.size(); i += 2) {
			if ((lex.getBegin() >= userRuleProcessor.userRuleRanges.get(i)
					&& lex.getBegin() < userRuleProcessor.userRuleRanges.get(i + 1))
					|| (lex.getBegin() + lex.getLength() > userRuleProcessor.userRuleRanges.get(i)
							&& lex.getBegin() + lex.getLength() <= userRuleProcessor.userRuleRanges.get(i + 1))) {
				return true;
			}
		}

		return false;
	}

	// 日志记录交叉歧义的词
	private void logCrossLexeme() {
		if (!analysisLog) {
			return;
		}

		for (int i = 0; i < results.size(); ++i) {
			for (int j = 0; j < results.size(); ++j) {
				if (results.get(j).getBegin() > results.get(i).getBegin()
						&& results.get(j).getBegin() < results.get(i).getBegin() + results.get(i).getLength()
						&& results.get(j).getBegin() + results.get(j).getLength() > results.get(i).getBegin()
								+ results.get(i).getLength()) {
					log.warn("find cross lexemes: {} {}, input: {}",
							String.valueOf(segmentBuff, results.get(i).getBegin(), results.get(i).getLength()),
							String.valueOf(segmentBuff, results.get(j).getBegin(), results.get(j).getLength()),
							String.valueOf(segmentBuff, 0, available));
				}
			}
		}
	}

	/**
	 * 对CJK字符进行单字输出
	 * 
	 * @param index
	 */
	private void outputSingleCJK(int index) {
		if (CharacterUtil.CHAR_CHINESE == this.charTypes[index]) {
			Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, Lexeme.TYPE_CNCHAR, true);
			this.results.add(singleCharLexeme);
		} else if (CharacterUtil.CHAR_OTHER_CJK == this.charTypes[index]) {
			Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, Lexeme.TYPE_OTHER_CJK, true);
			this.results.add(singleCharLexeme);
		}
	}

	/**
	 * 返回lexeme
	 * 
	 * 同时处理合并
	 * 
	 * @return
	 */
	Lexeme getNextLexeme() {
		// 从结果集取出，并移除第一个Lexme
		Lexeme result = this.results.pollFirst();
		while (result != null) {
			// 数量词合并
			this.compound(result);
			if (Dictionary.isStopWord(this.segmentBuff, result.getBegin(), result.getLength())) {
				// 是停止词继续取列表的下一个
				result = this.results.pollFirst();
			} else {
				// 不是停止词, 生成lexeme的词元文本,输出
				result.setLexemeText(String.valueOf(segmentBuff, result.getBegin(), result.getLength()));
				break;
			}
		}
		return result;
	}

	/**
	 * 重置分词上下文状态
	 */
	void reset() {
		this.buffLocker.clear();
		this.orgLexemes = new QuickSortSet();
		this.available = 0;
		this.buffOffset = 0;
		this.charTypes = new int[BUFF_SIZE];
		this.cursor = 0;
		this.results.clear();
		this.combineResults.clear();
		this.segmentBuff = new char[BUFF_SIZE];
		this.pathMap.clear();
		this.userRuleProcessor.reset();
	}

	/**
	 * 组合词元
	 */
	private void compound(Lexeme result) {
		if (segmentType == 0) {
			return;
		}
		// 数量词合并处理
		if (!this.results.isEmpty()) {

			if (Lexeme.TYPE_ARABIC == result.getLexemeType()) {
				Lexeme nextLexeme = this.results.peekFirst();
				boolean appendOk = false;
				if (Lexeme.TYPE_CNUM == nextLexeme.getLexemeType()) {
					// 合并英文数词+中文数词
					appendOk = result.append(nextLexeme, Lexeme.TYPE_CNUM);
				} else if (Lexeme.TYPE_COUNT == nextLexeme.getLexemeType()) {
					// 合并英文数词+中文量词
					appendOk = result.append(nextLexeme, Lexeme.TYPE_CQUAN);
				}
				if (appendOk) {
					// 弹出
					this.results.pollFirst();
				}
			}

			// 可能存在第二轮合并
			if (Lexeme.TYPE_CNUM == result.getLexemeType() && !this.results.isEmpty()) {
				Lexeme nextLexeme = this.results.peekFirst();
				boolean appendOk = false;
				if (Lexeme.TYPE_COUNT == nextLexeme.getLexemeType()) {
					// 合并中文数词+中文量词
					appendOk = result.append(nextLexeme, Lexeme.TYPE_CQUAN);
				}
				if (appendOk) {
					// 弹出
					this.results.pollFirst();
				}
			}

		}
	}

	// 匹配待分词字符串得到的一个人工干预规则
	private class UserRulePattern {
		UserRulePattern(int begin, int length, int[] rule) {
			this.begin = begin;
			this.length = length;
			this.rule = rule;
			this.matchBegin = -1;
			this.matchEnd = -1;
		}

		private int begin; // 匹配到的字符串在segmentBuff中的起始位置
		private int length; // 匹配到的词长
		private int[] rule; // 具体的切分规则

		// 查询人工干预词表得到干预项之后，需要确认该字符串是否是有效划分
		// matchBegin和matchEnd分别表示干预项对应在results里面的下标索引
		private int matchBegin;
		private int matchEnd;
	}

	// 在这个类里进行人工干预的处理
	private class UserRuleProcessor {
		UserRuleProcessor() {
			this.tmpHits = new LinkedList<Hit>();
			this.rulePatterns = new ArrayList<UserRulePattern>();
			this.userRuleRanges = new ArrayList<Integer>();
			this.enable = Config.getInstance().getBoolean("analysis.human_intervention", false); // TODO
		}

		public void process() {
			if (!enable) {
				return;
			}

			matchUserRule();
			removeInvalidRulePattern();
			splitAsRule();
		}

		// 在人工干预词表查询是否有人工匹配项
		private void matchUserRule() {
			for (int i = 0; i <= cursor; ++i) {
				if (CharacterUtil.CHAR_USELESS == charTypes[i]) {
					tmpHits.clear();
					continue;
				}
				// 优先处理tmpHits中的hit
				if (!tmpHits.isEmpty()) {
					// 处理词段队列
					Hit[] tmpArray = tmpHits.toArray(new Hit[tmpHits.size()]);
					for (Hit hit : tmpArray) {
						hit = Dictionary.matchInUserRuleDictWithHit(segmentBuff, i, hit);
						if (hit.isMatch()) {
							rulePatterns.add(
									new UserRulePattern(hit.getBegin(), i - hit.getBegin() + 1, hit.getUserRule()));

							if (!hit.isPrefix()) { // 不是词前缀，hit不需要继续匹配，移除
								tmpHits.remove(hit);
							}
						} else if (hit.isUnmatch()) {
							// 未匹配，移除
							tmpHits.remove(hit);
						}
					}
				}

				// 再对当前指针位置的字符进行单字匹配
				Hit singleCharHit = Dictionary.matchInUserRuleDict(segmentBuff, i, 1);
				if (singleCharHit.isMatch()) { // 首字成词
					rulePatterns.add(new UserRulePattern(i, 1, singleCharHit.getUserRule()));
				}

				// 前缀匹配则放入hit列表
				if (singleCharHit.isPrefix()) {
					tmpHits.add(singleCharHit);
				}
			}
		}

		// 移除无效的人工干预项
		private void removeInvalidRulePattern() {
			for (int i = 0; i < rulePatterns.size();) {
				if (!matchResults(rulePatterns.get(i))) { // 不是有效划分，移除
					rulePatterns.remove(i);
				} else {
					++i;
				}
			}

			for (int i = 0; i < rulePatterns.size() - 1;) {
				// 人工干预出现重叠的情况，取最长匹配
				if (rulePatterns.get(i + 1).begin < rulePatterns.get(i).begin + rulePatterns.get(i).length) {
					if (rulePatterns.get(i).length < rulePatterns.get(i + 1).length) {
						rulePatterns.remove(i);
					} else {
						rulePatterns.remove(i + 1);
					}
				} else {
					++i;
				}
			}
		}

		/**
		 * 当命中人工干预词典时，需要确认该字符串是否是有效划分
		 * 
		 * @param pattern
		 * @return
		 */
		private boolean matchResults(UserRulePattern pattern) {
			for (int i = 0; i < results.size(); ++i) {
				if (pattern.matchBegin == -1 && results.get(i).getBegin() == pattern.begin
						&& results.get(i).getBegin() + results.get(i).getLength() <= pattern.begin + pattern.length) { // begin是结果集中某个词的begin
					pattern.matchBegin = i;
				}

				// 结果集中某个词的end刚好是pattern的end
				if (results.get(i).getBegin() + results.get(i).getLength() == pattern.begin + pattern.length) {
					pattern.matchEnd = i;
				}
			}

			return pattern.matchBegin != -1 && pattern.matchEnd != -1;
		}

		// 按照已配置规则进行分词
		private void splitAsRule() {
			Lexeme lex = null;
			Hit hit = null;
			// 为了保证人工干预结果放进results的相应位置，这里使用逆序遍历（后面的下标操作不会影响前面）
			for (int i = rulePatterns.size() - 1; i >= 0; --i) {
				int index = rulePatterns.get(i).matchBegin;
				for (int count = 0; count < rulePatterns.get(i).matchEnd - index + 1; ++count) {
					results.remove(index); // 先将结果集中的原始结果移除，稍后按照人工配置分词
				}

				int begin = rulePatterns.get(i).begin;
				for (int len : rulePatterns.get(i).rule) { // 按照人工配置分词
					hit = Dictionary.matchInAllDict(segmentBuff, begin, len);
					if (hit != null) { // 已配置在词典中，则获取词的各种属性
						lex = new Lexeme(buffOffset, begin, len, hit);
					} else {
						lex = new Lexeme(buffOffset, begin, len, Lexeme.TYPE_CNWORD);
					}

					results.add(index, lex); // 添加到相应的位置
					++index;
					begin += len;
				}

				// 记录起始范围
				userRuleRanges.add(rulePatterns.get(i).begin);
				userRuleRanges.add(rulePatterns.get(i).begin + rulePatterns.get(i).length);
			}
		}

		private void reset() {
			this.tmpHits.clear();
			this.rulePatterns.clear();
			this.userRuleRanges.clear();
		}

		private List<Hit> tmpHits;
		private ArrayList<UserRulePattern> rulePatterns;

		// 按照人工干预词典切分时，记录配置词的起始范围，在这范围内的字符就不做一些额外处理（如两两组合）
		private ArrayList<Integer> userRuleRanges;

		private boolean enable; // 是否启用人工干预
	}

	// 英文数字串处理
	private class LetterProcessor {
		public LetterProcessor() {
			letterLens = new ArrayList<Integer>();
			this.enable = Config.getInstance().getBoolean("analysis.process_letter", true);
		}

		public void process() {
			if (!this.enable) {
				return;
			}

			Lexeme lex = null;
			for (int i = 0; i < results.size();) {
				lex = results.get(i);
				if (lex.getLexemeType() != Lexeme.TYPE_LETTER || !isalnum(lex)) {
					++i;
					continue;
				}

				// 长度小于3的不处理
				if (lex.getLength() < 3) {
					++i;
					continue;
				}

				splitLetter(lex);
				// 1个数字+多个字母 || 1个字母+多个数字 || 多个数字+多个字母，切开
				if (this.letterLens.size() == 2) {
					i = addSplitLexeme(i, lex);
				} else if (this.letterLens.size() == 3) { // 数字+字母+数字 ||
															// 字母+数字+字母
					if (this.letterLens.get(1) + this.letterLens.get(2) == 2) { // 如iphone5s，切成iphone
																				// 5s
						i = addComposeLexeme(i, lex);
					} else { // 全部切开
						i = addSplitLexeme(i, lex);
					}
				} else if (this.letterLens.size() > 3) { // 多于4个英文、数字串的组合，全部切开
					i = addSplitLexeme(i, lex);
				} else {
					++i;
				}
			}
		}

		// 检查是否全部是英文数字（LetterSegmenter有些预先设定的连接符，这里先不处理存在连接符的情况）
		private boolean isalnum(Lexeme lex) {
			for (int i = lex.getBegin(); i < lex.getBegin() + lex.getLength(); ++i) {
				if (charTypes[i] != CharacterUtil.CHAR_ARABIC && charTypes[i] != CharacterUtil.CHAR_ENGLISH) {
					return false;
				}
			}

			return true;
		}

		// 切分英文数字串
		private void splitLetter(Lexeme lex) {
			this.letterLens.clear();
			int charType = charTypes[lex.getBegin()];
			int preIndex = lex.getBegin();
			int i = lex.getBegin() + 1;
			for (; i < lex.getBegin() + lex.getLength(); ++i) {
				if (charType != charTypes[i]) { // 类型已改变
					this.letterLens.add(i - preIndex); // 之前相同类型的作为一个结果
					charType = charTypes[i]; // 设置当前字符类型
					preIndex = i; // 设置下一次切分的起始位置
				}
			}

			this.letterLens.add(i - preIndex);
		}

		// 将切分的新结果加入结果集
		private int addSplitLexeme(int index, Lexeme lex) {
			if (segmentType == 0) { // 索引分词本身已有单独切分的结果，这里就不需要再处理了
				return index + 1;
			}

			int beginIndex = lex.getBegin();
			for (int i = 0; i < this.letterLens.size(); ++i) {
				results.add(index + 1 + i,
						new Lexeme(buffOffset, beginIndex, this.letterLens.get(i), charTypes[beginIndex]));
				beginIndex += this.letterLens.get(i);
			}

			results.remove(index); // query分词时去掉原始结果
			return index + this.letterLens.size();
		}

		// 将组合的新结果加入结果集
		// 处理类似iphone5s的情况，iphone单独切出，5s组合在一起
		private int addComposeLexeme(int index, Lexeme lex) {
			// 后两个组合
			results.add(index + 1, new Lexeme(buffOffset, lex.getBegin() + this.letterLens.get(0),
					this.letterLens.get(1) + this.letterLens.get(2), Lexeme.TYPE_LETTER));

			if (segmentType > 0) { // query分词，移除原始结果，且需要将首个词加入结果集（索引分词本身已包含了，不需要重复加入）
				results.add(index + 1,
						new Lexeme(buffOffset, lex.getBegin(), this.letterLens.get(0), charTypes[lex.getBegin()]));
				results.remove(index);
			}

			return index + 2;
		}

		// 存储英文数字串中按英文和数字切分后各个子串的长度
		private ArrayList<Integer> letterLens;
		private boolean enable; // 是否启用
	}
}