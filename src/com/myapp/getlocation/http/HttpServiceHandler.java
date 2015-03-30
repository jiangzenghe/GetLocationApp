package com.myapp.getlocation.http;

import org.apache.http.HttpResponse;

/**
 * 网络请求响应类处理器接口
 * @author
 * 
 **/
public interface HttpServiceHandler {
	/**
	 * 该方法运行在与网络请求相同的线程中，用于对数据进行一个预处理.
	 * @param response
	 */
	public void onHttpServicePrepare(HttpResponse response);
	/**
	 * 网络请求成功完成获取响应信息后调用这个方法.
	 * @param response
	 */
	public void onHttpServiceFinished(HttpResponse response);
	/**
	 * 在网络请求过程中或预处理过程中出错则调用这个方法.
	 * @param e
	 */
	public void onHttpServiceError(Exception e);
	
}
