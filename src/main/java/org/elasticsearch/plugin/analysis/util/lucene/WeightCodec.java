package org.elasticsearch.plugin.analysis.util.lucene;

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
 * @Create-at 2012-4-23 11:57:36
 * 
 * @Modification-history <br>
 * 						Date Author Version Description <br>
 * 						----------------------------------------------------------
 *                       <br>
 * 						2012-4-23 11:57:36 li_yao 1.0 Newly created
 */
public class WeightCodec {
	public final static String PAYLOAD_TAG = "xxxxxxxxx";
	final static char ZERO_CHAR = 'a';

	public static byte get(String text) {
		if (text != null && text.startsWith(PAYLOAD_TAG) && text.length() - PAYLOAD_TAG.length() < 8) {
			byte payload = 0;
			byte[] exps = new byte[] { 1, 2, 4, 8, 16, 32, 64 };
			for (int i = text.length() - 1, k = 0; i >= PAYLOAD_TAG.length(); i--, k++) {
				int b = (text.charAt(i) - ZERO_CHAR);
				if (b != 1 && b != 0) {
					return -1;
				}
				payload += b * exps[k];
			}
			return payload;
		}
		return -1;
	}

	public static void append(StringBuilder sb, String text, float weight) {
		int payload = (int) weight;
		if (payload < 1) {
			payload = 1;
		} else if (payload > Byte.MAX_VALUE) {
			payload = Byte.MAX_VALUE;
		}
		sb.append(PAYLOAD_TAG);
		char[] abs = new char[8];
		int index = 7;
		do {
			abs[index--] = (char) ((payload & 1) + ZERO_CHAR);
			payload >>>= 1;
		} while (payload > 0);
		for (index += 1; index < 8; index++) {
			sb.append(abs[index]);
		}
		sb.append(' ');
		sb.append(text);
		sb.append(' ');
	}
}
