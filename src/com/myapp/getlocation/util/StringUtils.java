package com.myapp.getlocation.util;

import android.text.TextUtils;

public final class StringUtils {
	/**
	 * 按指定的位数插入指定分隔符到文本中， 返回插入分隔符后的文本。
	 * @param text 待插件分隔符的文本
	 * @param len 间隔位数
	 * @param separator 分隔符
	 * @return 插入分隔符后的文本，如果text长度小len参数值，则返回text参数原文本对象。
	 */
	public static String inserSeparator(String text, int len, char separator) {
		if (TextUtils.isEmpty(text)) {
			return text;
		}
		if (text.length() <= len) {
			return text;
		}
		
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (; i<text.length(); i+=len) {
			buffer.append(text.substring(i, i+len));
			if (i != 0) {
				buffer.append(separator);
			}
		}
		
		return buffer.toString();
	}

	/**
	 * 比较两个字符串是否相等。
	 * 
	 * @param l
	 * @param r
	 * @return
	 */
	public static final boolean isEquals(String l, String r) {
		if (l == null && r == null) {
			return true;
		}
	
		if (l != null) {
			return l.equals(r);
		} else {
			return r.equals(l);
		}
	}
}
