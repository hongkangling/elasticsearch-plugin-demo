package org.elasticsearch.plugin.analysis.analyzer;


import com.homedo.bigdata.analysis.analyzer.dict.DictSegment;
import com.homedo.bigdata.analysis.analyzer.segment.ISegmenter;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public final class ICSegmentation {

	private Reader input;
	// 分词器上下文
	private Context context;
	// 分词处理器列表
	private List<ISegmenter> segmenters;
	private int segmenterType = 1;
	private boolean isMaxLength = true;
	private boolean isBackwardMatch = true; // 歧义裁决时，是否后向匹配
	private ICArbitrator arbitrator;

	public ICSegmentation() {
		this(1);
	}

	public ICSegmentation(int segmenter_type) {
		segmenters = DictSegment.loadSegmenter();
		this.segmenterType = segmenter_type;
		context = new Context(segmenter_type);
		this.arbitrator = new ICArbitrator();

	}

	public ICSegmentation(boolean ismaxLength, int segmenter_type) {
		this(segmenter_type);
		isMaxLength = ismaxLength;
	}

	public ICSegmentation(boolean ismaxLength) {
		this(ismaxLength, 1);
	}

	public ICSegmentation(boolean ismaxLength, int segmenter_type, boolean isBackwardMatch) {
		this(ismaxLength, segmenter_type);
		this.isBackwardMatch = isBackwardMatch;
	}

	/**
	 * 获取下一个语义单元
	 * 
	 * @return 没有更多的词元，则返回null
	 * @throws IOException
	 */
	public synchronized Lexeme next() throws IOException {
		Lexeme l = null;
		while ((l = context.getNextLexeme()) == null) {
			/*
			 * 从reader中读取数据，填充buffer 如果reader是分次读入buffer的，那么buffer要进行移位处理
			 * 移位处理上次读入的但未处理的数据
			 */
			int available = context.fillBuffer(this.input);
			if (available <= 0) {
				// reader已经读完
				context.reset();
				return null;

			} else {
				// 初始化指针
				context.initCursor();
				do {
					// 遍历子分词器
					for (ISegmenter segmenter : segmenters) {
						segmenter.analyze(context);
					}
					// 字符缓冲区接近读完，需要读入新的字符
					if (context.needRefillBuffer()) {
						break;
					}
					// 向前移动指针
				} while (context.moveCursor());
				// 重置子分词器，为下轮循环进行初始化
				for (ISegmenter segmenter : segmenters) {
					segmenter.reset();
				}
			}
			// 对分词进行歧义处理
			this.arbitrator.process(context, segmenterType > 0, isMaxLength, isBackwardMatch);
			// 处理未切分CJK字符
			context.outputToResult();
			// 记录本次分词的缓冲区位移
			context.markBufferOffset();
		}
		return l;
	}

	/**
	 * 重置分词器到初始状态
	 * 
	 * @param input
	 */
	public synchronized void reset(Reader input) {
		this.input = input;
		context.reset();
		for (ISegmenter segmenter : segmenters) {
			segmenter.reset();
		}
	}

}
