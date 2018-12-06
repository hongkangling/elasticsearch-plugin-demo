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
 * @Create-at 2011-8-5 10:03:03
 * 
 * @Modification-history <br>
 * 						Date Author Version Description <br>
 * 						----------------------------------------------------------
 *                       <br>
 * 						2011-8-5 10:03:03 li_yao 1.0 Newly created
 */
public interface ChNode<T> {

	ChNode<T> addWord(String word, int index, T info);

	public ChNode<T> prefixSearch(String word, int index);
}
