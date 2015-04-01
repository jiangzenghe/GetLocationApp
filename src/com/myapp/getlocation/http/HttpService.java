package com.myapp.getlocation.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * HttpService 接口.
 * @author
 * 
 */
public interface HttpService {
	
	/**
	 * 异步调用服务
	 *
	 * @param request 服务请求
	 * @param handler 处理器
	 */
	public void callService(final HttpUriRequest request, HttpServiceHandler handler);
	
	/**
	 * 异步get调用服务
	 *
	 * @param svcName 服务地址
	 * @param args the 请求参数
	 * @param handler 处理器
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void callGetService(String svcName, Map<String, Object> args, HttpServiceHandler handler) throws ClientProtocolException, IOException;
	
	/**
	 * 异步post调用服务
	 *
	 * @param svcName 服务地址
	 * @param args the 请求参数
	 * @param handler 处理器
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void callPostService(String svcName, Map<String, Object> args, HttpServiceHandler handler) throws UnsupportedEncodingException;
	
	public void callPostService(String svcName, HttpServiceHandler handler) throws UnsupportedEncodingException;
	
	public String getBaseAddress();
	
	public void setBaseAddress(String baseAddress);
	
	public String getEncoding();

	public void setEncoding(String encoding);
}
