package org.elasticsearch.plugin.analysis.util.word;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.homedo.bigdata.analysis.config.Config;
import com.homedo.bigdata.analysis.util.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PinyinMapper {

	private volatile static PinyinMapper SINGLETON;

	private String[][] mapper;

	private final Config config;

	public static PinyinMapper get() throws IOException {
		if (SINGLETON == null) {
			synchronized (PinyinMapper.class) {
				if (SINGLETON == null) {
					SINGLETON = new PinyinMapper();
				}
			}
		}
		return SINGLETON;
	}

	private PinyinMapper() throws IOException {
		this.config = Config.getInstance();
		loadDict();
	}

	private void loadDict() throws IOException {
		// read file and find max code point
		InputStreamReader reader = new InputStreamReader(new FileInputStream(this.config.getConfigFile("pinyin.file").toFile()), Charsets.UTF_8);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		List<String> lines = Lists.newArrayListWithCapacity(10000);
		int maxCode = 0;
		char ch;
		while ((line = br.readLine()) != null) {
			lines.add(line);
			ch = line.charAt(0);
			if (ch > maxCode) {
				maxCode = ch;
			}
		}
		br.close();
		reader.close();

		// generate mapper by maxCode
		mapper = new String[maxCode + 1][];
		for (String str : lines) {
			mapper[str.charAt(0)] = str.substring(2).split(" ");
		}

	}

	public String[] get(char ch) {
		return ch > mapper.length ? null : mapper[ch];
	}

	public String[] getQuanpinShoupin(String word) {
		if (word == null || word.isEmpty()) {
			return null;
		}
		StringBuilder quanpin = new StringBuilder(word.length() * 6);
		StringBuilder shoupin = new StringBuilder(word.length());
		boolean hasPinyin = false;
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			String[] py = get(ch);
			if (py == null) {// not cn char
				quanpin.append(ch);
				shoupin.append(ch);
			} else {// cn char
				hasPinyin = true;
				quanpin.append(py[0]);
				shoupin.append(py[0].charAt(0));
			}
		}
		return hasPinyin ? new String[] { quanpin.toString(), shoupin.toString() } : null;
	}

	public String[] getQuanpins(String word) {
		if (word == null || word.isEmpty()) {
			return null;
		}
		String[] quanpins = new String[word.length()];
		boolean hasCn = false;
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			String[] py = get(ch);
			if (py == null) {// not a cn char
				quanpins[i] = String.valueOf(ch);
			} else {// cn char
				hasCn = true;
				quanpins[i] = py[0];
			}

		}
		return hasCn ? quanpins : null;
	}

	public boolean matchPinyin(char ch1, char ch2) {
		String[] pys1 = get(ch1);
		if (pys1 == null) {
			return false;
		}
		String[] pys2 = get(ch2);
		if (pys2 == null) {
			return false;
		}
		for (int i = 0; i < pys1.length; i++) {
			for (int j = 0; j < pys2.length; j++) {
				if (pys1[i].equals(pys2[j])) {
					return true;
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		PinyinMapper py = get();
		String[] s1 = py.get('中');
		s1 = py.get('麇');
		System.out.println(s1[0]);
	}

}
