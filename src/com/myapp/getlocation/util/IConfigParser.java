package com.myapp.getlocation.util;

import java.io.InputStream;
/**
 * Config文件解析接口
 *
 */
public interface IConfigParser {
	/**
	 * 解析输入流 得到AppInitConfig对象集合
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public AppInitConfig parse(InputStream is) throws Exception;
	
	/**
	 * 序列化AppInitConfig对象集合 得到XML形式的字符串
	 * @param AppInitConfig
	 * @return
	 * @throws Exception
	 */
	public String serialize(AppInitConfig appInitConfig) throws Exception;
}
