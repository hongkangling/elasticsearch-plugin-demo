package org.elasticsearch.plugin.analysis.util;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StringHelper {
	public static String readerToString(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		while (true) {
			int i = reader.read();
			if (i == -1) {
				break;
			}
			char c = (char) i;
			builder.append(c);
		}
		return builder.toString();
	}

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	public static String getDateTime(long timeMillis) {
		return format.format(timeMillis);
	}

	public static long getTime(String date) {
		try {
			return format.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
