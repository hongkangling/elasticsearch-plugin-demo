package org.elasticsearch.plugin.analysis.analyzer.pinyin;

import com.homedo.bigdata.analysis.config.Config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * To store pinyin as a sorted array which can speed up searching.
 * 
 * @author kmchu
 * @create 2013/09/18
 * 
 */
public class PinYinBags {
	public static PinYinBags instance = new PinYinBags();

	Map<String, Integer> map = new TreeMap<String, Integer>();
	private final List<String> sortedPinYinArray;

	private PinYinBags() {
		Set<String> set = PinYinResourceLoader.loadDict();
		sortedPinYinArray = new ArrayList<String>(set.size());
		sortedPinYinArray.addAll(set);
		Collections.sort(sortedPinYinArray);
		initMap();
	}

	private void initMap() {
		char c = '0';
		for (int i = 0; i < sortedPinYinArray.size(); ++i) {
			if (sortedPinYinArray.get(i).charAt(0) != c) {
				c = sortedPinYinArray.get(i).charAt(0);
				map.put(String.valueOf(c), i);
			}
		}
	}

	public PinYinIndex search(String str) {
		String firstChar = String.valueOf(str.charAt(0));
		String followingChar = String.valueOf((char) (str.charAt(0) + 1));
		Integer index = map.get(firstChar);
		if (index == null) {
			return PinYinIndex.NOT_PINYIN;
		}
		Integer endIndex = sortedPinYinArray.size();
		if (str.charAt(0) != 'z' && map.containsKey(followingChar)) {
			endIndex = map.get(followingChar);
		}

		for (int i = index; i < endIndex; ++i) {
			if (sortedPinYinArray.get(i).startsWith(str)) {
				if (sortedPinYinArray.get(i).equals(str)) {
					return new PinYinIndex(i, true);
				}
				return new PinYinIndex(i, false);
			}
		}
		return PinYinIndex.NOT_FOUND;
	}

	public boolean isJianPinSequences(String str) {
		if (str == null) {
			return false;
		}
		for (char c : str.toCharArray()) {
			if (!isJianPinChar(c)) {
				return false;
			}
		}
		return true;
	}

	public boolean isJianPinChar(char c) {
		Integer index = map.get(String.valueOf(c));
		if (index == null) {
			return false;
		}
		return sortedPinYinArray.get(index).equals(String.valueOf(c));
	}

	public boolean isPotentialPrefix(String str) {
		PinYinIndex index = search(str);

		if (index == PinYinIndex.NOT_FOUND) {
			throw new IllegalArgumentException("We don't expect input like [" + str + "] which out of search table");
		}

		if (index.isPinYin()) {
			for (int i = index.getIndex() + 1; i < sortedPinYinArray.size(); i++) {
				if (sortedPinYinArray.get(i).startsWith(str)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Load gb2312 files to extract all pinyins.
	 * 
	 */
	static class PinYinResourceLoader {
		public static Set<String> loadDict() {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(Config.getInstance().get("pinyin.file.path")), "utf8"));
				Set<String> set = new HashSet<String>();

				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					String[] arrays = line.split("\\s");
					for (int i = 1; i < arrays.length; ++i) {
						set.add(arrays[i]);
					}
				}
				return set;
			} catch (IOException io) {

			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return Collections.emptySet();
		}
	}
}
