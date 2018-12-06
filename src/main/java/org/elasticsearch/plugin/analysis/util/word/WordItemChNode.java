package org.elasticsearch.plugin.analysis.util.word;

/**
 * @Description
 * 
 * @Copyright Copyright (c)2011
 * 
 * @Company homedo.com
 * 
 * @Author li_yao
 * 
 * @Version 1.0
 * 
 * @Create-at 2012-5-24 21:29:08
 * 
 * @Modification-history <br>
 * 						Date Author Version Description <br>
 * 						----------------------------------------------------------
 *                       <br>
 * 						2012-5-24 21:29:08 li_yao 1.0 Newly created
 */
public abstract class WordItemChNode extends AbstractChNode<WordItem> {

	public WordItemChNode(char ch) {
		super(ch);
	}

	@Override
	public WordItemChNode addWord(String word, int index, WordItem info) {
		WordItemChNode subNode = (WordItemChNode) addChar(word.charAt(index));
		if (++index < word.length()) {// before the end of the word
			subNode.addWord(word, index, info);
		} else {// reach the end of the word
			subNode.setWordItem(info);
		}
		return subNode;
	}

	public abstract void setWordItem(WordItem wordItem);

}
