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
 * @Create-at 2011-8-5 10:03:21
 * 
 * @Modification-history <br>
 * 						Date Author Version Description <br>
 * 						----------------------------------------------------------
 *                       <br>
 * 						2011-8-5 10:03:21 li_yao 1.0 Newly created
 */
public class DefaultWordItem implements WordItem {

	private String word;

	byte boost;

	public DefaultWordItem(String word, byte boost) {
		this.word = word;
		this.boost = boost;
	}

	@Override
	public String getWord() {
		return word;
	}

	@Override
	public byte boost() {
		return boost;
	}

}
