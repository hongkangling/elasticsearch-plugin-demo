package org.elasticsearch.plugin.analysis.analyzer.dict;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.ArrayUtils;
import org.elasticsearch.plugin.analysis.analyzer.CharacterUtil;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Dictionary {
	private static final Logger LOGGER = ESLoggerFactory.getLogger(Config.class);
	/*
	 * 词典单子实例
	 */
	private volatile static Dictionary instance;

	/**
	 * 词库保存到磁盘时字段之间的分隔符
	 */
	private static final String TAB_DELIMITER = "\t";

	private static final String COMMA_DELIMITER = ",";

	private final Config configuration;

	/**
	 * 构造词典
	 */
	public static Dictionary getInstance() {
		if (instance == null) {
			synchronized (Dictionary.class) {
				if (instance == null) {
					instance = new Dictionary();
				}
			}
		}
		return instance;
	}

	/**
	 * 重新加载词典(本方法只能重建索引完成后调用)
	 */
	public static synchronized void Reload() {
		instance = new Dictionary();
	}

	/*
	 * 主词典对象
	 */
	private DictSegment _MainDict;
	/*
	 * 姓氏词典
	 */
	private DictSegment _SurnameDict;
	/*
	 * 量词词典
	 */
	private DictSegment _QuantifierDict;
	/*
	 * 后缀词典
	 */
	private DictSegment _SuffixDict;
	/*
	 * 副词，介词词典
	 */
	private DictSegment _PrepDict;
	/*
	 * 停止词集合
	 */
	private DictSegment _StopWords;
	/*
	 * 地标词信息表
	 */
	public HashMap<Integer, HashMap<String, Word>> landmarks;

	/**
	 * 纠错映射表 HashMap<errorWord, correctWord>
	 */
	public HashMap<String, String> spellCheckWordMapping;
	/**
	 * 纠错映射表 HashMap<correctWord, List<errorWord>>
	 */
	public HashMap<String, List<String>> spellCheckWordConvertMapping;
	/**
	 * 同义词映射表
	 */
	public HashMap<String, String> synonymWordMapping;

	public HashMap<Integer, String> wordType;

	List<Word> wordList = Lists.newArrayList();

	/**
	 * 人工干预词表
	 */
	private DictSegment _userRuleDict;

	/**
	 * 多音字的拼音配置表
	 */
	private DictSegment _pinyinDict;

	private Dictionary() {
		this.configuration = Config.getInstance();

		_MainDict = new DictSegment((char) 0);
		_SurnameDict = new DictSegment((char) 0);
		_QuantifierDict = new DictSegment((char) 0);
		_SuffixDict = new DictSegment((char) 0);
		_PrepDict = new DictSegment((char) 0);
		_StopWords = new DictSegment((char) 0);
		landmarks = Maps.newHashMap();
		spellCheckWordMapping = Maps.newHashMap();
		spellCheckWordConvertMapping = Maps.newHashMap();
		synonymWordMapping = Maps.newHashMap();
		wordType = Maps.newHashMap();
		_userRuleDict = new DictSegment((char) 0);
		_pinyinDict = new DictSegment((char) 0);

		// 初始化系统词典
		this.loadWordMapping();
		this.loadWords();
		this.loadWordType();
		// this.loadUserRuleDict();
		this.loadPinyinDict();
	}

	/**
	 * 得到一类词集合
	 *
	 * @return
	 */
	private void loadWords() {
		this.wordList = this.loadWordsFromFile();
		if(CollectionUtils.isEmpty(this.wordList)) {
			return;
		}
		for (Word word : this.wordList) {
			switch (word.getType()) {
				case Word.type_PrepDict:
					this._PrepDict.fillSegment(word.getWordText().toCharArray(), Word.type_PrepDict);
					break;
				case Word.type_SurnameDict:
					this._SurnameDict.fillSegment(word.getWordText().toCharArray(), Word.type_SurnameDict);
					break;
				case Word.type_QuantifierDict:
					this._QuantifierDict.fillSegment(word.getWordText().toCharArray(), Word.type_QuantifierDict);
					break;
				case Word.type_StopWords:
					this._StopWords.fillSegment(word.getWordText().toCharArray(), Word.type_StopWords);
					break;
				case Word.type_SuffixDict:
					this._SuffixDict.fillSegment(word.getWordText().toCharArray(), Word.type_SuffixDict);
					break;
				case Word.type_Category1:
				case Word.type_Category2:
				case Word.type_Category3:
				case Word.type_Brand:
					if (this.synonymWordMapping.containsKey(word.getWordText())) {
						String[] wss = this.synonymWordMapping.get(word.getWordText()).split("\\|");
						for (String ws : wss) {
							this._MainDict.fillSegment(ws.toLowerCase().toCharArray(), word.getType(), word.getScore(),
									word.getTypeScore(), word.getAttributeid());
						}
					}
					if (this.spellCheckWordConvertMapping.containsKey(word.getWordText())) {
						List<String> list = this.spellCheckWordConvertMapping.get(word.getWordText());
						if (!CollectionUtils.isEmpty(list)) {
							for (String ws : list) {
								this._MainDict.fillSegment(ws.toLowerCase().toCharArray(), word.getType(), word.getScore(),
										word.getTypeScore(), word.getAttributeid());
							}
						}
					}
				default:
					this._MainDict.fillSegment(word.getWordText().toLowerCase().toCharArray(), word.getType(), word.getScore(),
							word.getTypeScore(), word.getAttributeid());
					break;
			}
		}
	}

	private List<Word> loadWordsFromFile() {
		Path file = this.configuration.getConfigFile("words.file");
		final String delimiter = this.configuration.getNoTrim("words.file.delimiter", TAB_DELIMITER);
		if (file == null) {
			LOGGER.warn("加载words.file文件为空");
			return null;
		}

		List<Word> result = Lists.newArrayList();
		try (InputStream is = new FileInputStream(file.toFile())) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8), 512);
			String line = br.readLine();
			int count = 1;
			while ((line = br.readLine()) != null) {
				String[] items = line.split(delimiter);
				int typeId = Integer.parseInt(items[0]);
				String text = items[1];
				if (StringUtils.isBlank(text)) {
					continue;
				}
				text = text.replace(TAB_DELIMITER, " ").trim();
				int attrId = NumberUtils.toInt(items[2]);
				Word wd = new Word(typeId, attrId, text);
				wd.setScore(NumberUtils.toInt(items[3]));
				wd.setTypeScore(NumberUtils.toInt(items[4]));
				if (typeId >= 6) {
					String hasNumberWordText = CharacterUtil.processNumber(text);
					if (!hasNumberWordText.isEmpty()) {
						result.add(new Word(typeId, attrId, hasNumberWordText));
					}
				}
				result.add(wd);
				if((count++ % 10000) == 0) {
					LOGGER.info("{} words loaded", count - 1);
				}
			}
			LOGGER.info("load {} words!", count - 1);
		} catch (FileNotFoundException e) {
			LOGGER.error("加载wordmapping.file文件失败，文件不存在", e);
		} catch (IOException e) {
			LOGGER.error("加载wordmapping.file文件异常", e);
		}
		return result;
	}

	private void loadWordMapping() {
		Path file = this.configuration.getConfigFile("wordmapping.file");
		final String delimiter = this.configuration.getNoTrim("wordmapping.file.delimiter", COMMA_DELIMITER);
		if(file == null) {
			LOGGER.warn("加载wordmapping.file文件为空");
			return;
		}

		this.loadFromLocalFile(file, delimiter, true, (items, container) -> {
			if (ArrayUtils.isEmpty(items) || items.length != 3) {
				return;
			}
			int type = NumberUtils.toInt(items[0], 1);
			String keyWord = items[1].toLowerCase();
			String mappingWord = items[2];

			if (type == 1) {
				// 纠错词
				spellCheckWordMapping.put(keyWord, mappingWord);
				if (!spellCheckWordConvertMapping.containsKey(mappingWord)) {
					spellCheckWordConvertMapping.put(mappingWord, Lists.newArrayList());
				}
				spellCheckWordConvertMapping.get(mappingWord).add(keyWord);
			} else if (type == 2) {
				// 同义词
				Set<String> wss = Sets.newHashSet(Lists.asList(keyWord, mappingWord.split("\\|")));
				for (String ws : wss) {
					if (StringUtils.isBlank(ws)) {
						continue;
					}
					Set<String> set = Sets.newHashSet(wss);
					set.remove(ws);
					synonymWordMapping.put(ws, StringUtils.join(set, "|"));
				}
			}
		}, null);
	}

	private void loadWordType() {
		this.wordType = loadWordTypeMapFromFile();
	}

	private HashMap<Integer, String> loadWordTypeMapFromFile() {
		Path file = this.configuration.getConfigFile("wordtype.file");
		final String delimiter = this.configuration.getNoTrim("wordtype.file.delimiter", COMMA_DELIMITER);
		if (file == null) {
			LOGGER.warn("加载wordtype.file文件为空");
			return null;
		}

		HashMap<Integer, String> result = Maps.newHashMap();

		this.loadFromLocalFile(file, delimiter, false, (items, container) -> {
			if (ArrayUtils.isEmpty(items) || items.length != 2) {
				return;
			}
			container.put(Integer.valueOf(items[0]), items[1]);
		}, result);
		return result;
	}

	/**
	 * 加载人工干预词表
	 *
	 * 暂没有
	 */
	private void loadUserRuleDict() {
		if (!Config.getInstance().getBoolean("analysis.human_intervention", false)) {
			LOGGER.info("human intervention is disabled");
			return;
		}
	}

	// 检查拼音配置
	private boolean checkPinyin(String[] strArray) {
		if (strArray.length < 2) { // 长度至少为2，一个中文及其对应的拼音
			return false;
		}

		if (strArray[0].length() + 1 != strArray.length) {
			return false;
		}

		return true;
	}

	// 加载多音字拼音表
	private void loadPinyinDict() {
		if (!Config.getInstance().getBoolean("analysis.multi_pinyin_config", false)) {
			LOGGER.info("multi pinyin config is disabled");
			return;
		}

		// TODO:加载远程词典

		loadPinyinDictFromDisk();
	}

	// 从磁盘加载多音字拼音表
	private void loadPinyinDictFromDisk() {
		this.loadFromLocalFile(this.configuration.getConfigFile("pinyin.file.path"), this.TAB_DELIMITER, false, (items, container) -> {
			if (items != null && this.checkPinyin(items)) {
				this._pinyinDict.fillSegment(items[0].toLowerCase().toCharArray(),
						Arrays.copyOfRange(items, 1, items.length));
			} else {
				LOGGER.error("skip invalid pinyin: {}", items);
			}
		}, null);
	}

	/**
	 * 检索匹配主词典
	 * 
	 * @param charArray
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInMainDict(char[] charArray) {
		return instance._MainDict.match(charArray);
	}

	/**
	 * 检索匹配主词典
	 * 
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInMainDict(char[] charArray, int begin, int length) {
		return instance._MainDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配主词典, 从已匹配的Hit中直接取出DictSegment，继续向下匹配
	 * 
	 * @param charArray
	 * @param currentIndex
	 * @param matchedHit
	 * @return Hit
	 */
	public static Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
		DictSegment ds = matchedHit.getMatchedDictSegment();
		return ds.match(charArray, currentIndex, 1, matchedHit);
	}

	/**
	 * 检索匹配姓氏词典
	 * 
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInSurnameDict(char[] charArray, int begin, int length) {
		return instance._SurnameDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配量词词典
	 * 
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInQuantifierDict(char[] charArray, int begin, int length) {
		return instance._QuantifierDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配在后缀词典
	 * 
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInSuffixDict(char[] charArray, int begin, int length) {
		return instance._SuffixDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配介词、副词词典
	 * 
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInPrepDict(char[] charArray, int begin, int length) {
		return instance._PrepDict.match(charArray, begin, length);
	}

	public static Hit matchInUserRuleDict(char[] charArray, int begin, int length) {
		return instance._userRuleDict.match(charArray, begin, length);
	}

	public static Hit matchInUserRuleDictWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
		DictSegment ds = matchedHit.getMatchedDictSegment();
		return ds.match(charArray, currentIndex, 1, matchedHit);
	}

	public static Hit matchInPinyinDict(char[] charArray, int begin, int length) {
		return instance._pinyinDict.match(charArray, begin, length);
	}

	public static Hit matchInPinyinDictWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
		DictSegment ds = matchedHit.getMatchedDictSegment();
		return ds.match(charArray, currentIndex, 1, matchedHit);
	}

	// 在所有词典中查找
	// 匹配人工干预词表时，需要在其他词典里查找切出的词，若存在则设置词的属性（类型、分数等）
	public static Hit matchInAllDict(char[] charArray, int begin, int length) {
		DictSegment[] dictArray = { instance._MainDict, instance._SurnameDict, instance._QuantifierDict,
				instance._SuffixDict, instance._PrepDict, instance._StopWords };

		Hit hit = null;
		for (DictSegment dict : dictArray) {
			hit = dict.match(charArray, begin, length);
			if (hit.isMatch()) {
				return hit;
			}
		}

		return null;
	}

	/**
	 * 判断是否是停止词
	 * 
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return boolean
	 */
	public static boolean isStopWord(char[] charArray, int begin, int length) {
		return instance._StopWords.match(charArray, begin, length).isMatch();
	}

	public static void main(String[] args) throws Exception {
		Dictionary.getInstance();
	}

	/**
	 * 从本地文件加载信息
	 * @param configFile - 文件Path信息
	 * @param delimiter - 文件中field分隔符
	 * @param ignoreFirst - 是否忽略首行
	 * @param action - 每行的处理逻辑
	 * @param contaner - 每行数据处理后的存放的容器
	 * @param <T> - 容器类型 如 List/Map
	 */
	private <T> void loadFromLocalFile(Path configFile, String delimiter, boolean ignoreFirst, RowMapAction<T> action, T contaner) {
		try (InputStream is = new FileInputStream(configFile.toFile())) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8), 512);
			if(ignoreFirst) {
				br.readLine();
			}
			String line;
			while ((line = br.readLine()) != null) {
				action.map(line.split(delimiter), contaner);
			}
			br.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("加载{}文件失败，文件不存在", configFile, e);
		} catch (IOException e) {
			LOGGER.error("加载{}文件异常", configFile, e);
		} catch (Exception e) {
			LOGGER.error("加载{}文件信息异常", configFile, e);
		}
	}

	/**
	 * 读取到文件中每行数据后续处理action
	 * @param <T>
	 */
	private interface RowMapAction<T> {
		void map(String[] rowItems, T container);
	}
}
