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
 * @Create-at 2011-8-5 10:03:09
 * 
 * @Modification-history <br>
 * 						Date Author Version Description <br>
 * 						----------------------------------------------------------
 *                       <br>
 * 						2011-8-5 10:03:09 li_yao 1.0 Newly created
 */
public interface WordItem {

	String getWord();

	byte boost();
}
