package org.elasticsearch.plugin.analysis.analyzer.dict;

import java.util.HashMap;

public class DictLandmark {

	/*
	 * 得到地标信息
	 */
	public static Word getLandmarkInfo(Integer cityid, String wordtext) {
		Word landmarkInfo;
		if (Dictionary.getInstance().landmarks.containsKey(cityid)) {
			HashMap<String, Word> citylandmark = Dictionary.getInstance().landmarks.get(cityid);
			if (citylandmark.containsKey(wordtext)) {
				landmarkInfo = citylandmark.get(wordtext);
				return landmarkInfo;
			}
		}
		return null;
	}
}
