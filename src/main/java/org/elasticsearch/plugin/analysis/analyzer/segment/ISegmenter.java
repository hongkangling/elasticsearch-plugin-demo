package org.elasticsearch.plugin.analysis.analyzer.segment;

import com.homedo.bigdata.analysis.analyzer.Context;

public interface ISegmenter {

	/**
	 * 从分析器读取下一个可能分解的词元对象
	 * 
	 * @param context
	 *            分词算法上下文
	 */
	void analyze(Context context);

	/**
	 * 重置子分析器状态
	 */
	void reset();
}
