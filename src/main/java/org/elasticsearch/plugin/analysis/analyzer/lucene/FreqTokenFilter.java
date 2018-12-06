package org.elasticsearch.plugin.analysis.analyzer.lucene;

import com.homedo.bigdata.analysis.util.lucene.WeightCodec;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

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
 * @Create-at 2012-4-23 14:01:06
 * 
 * @Modification-history <br>
 *                       Date Author Version Description <br>
 *                       ----------------------------------------------------------
 *                       <br>
 *                       2012-4-23 14:01:06 li_yao 1.0 Newly created
 */
public class FreqTokenFilter extends AbstractTokenFilter {
	byte currWeight;
	byte lastWeight;

	public FreqTokenFilter(TokenStream source) {
		super(source);
		reinit();
	}

	@Override
	public void reinit() {
		super.reinit();
		lastWeight = 1;
		currWeight = 0;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (currWeight == 0) {
			while (true) {
				if (input.incrementToken()) {
					byte weight = WeightCodec.get(textAttr.toString());
					if (weight < 0) {// text
						currWeight = lastWeight;
						if (sourceHasPosIncrAttr || lastPosIncr != 2 && !first) {
							lastPosIncr += posIncrAttr.getPositionIncrement();
						}
						break;
					} else {// weight
						lastWeight = weight > 0 ? weight : 1;
						lastPosIncr = first ? 0 : 2;
					}
				} else {
					return false;
				}
			}
		}
		posIncrAttr.setPositionIncrement(lastPosIncr);
		--currWeight;
		lastPosIncr = 0;
		first = false;
		return true;
	}
}
