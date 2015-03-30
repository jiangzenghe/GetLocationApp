package com.myapp.getlocation.http;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * 访问服务时后台运行的入参
 * @author 
 *
 */
public class AsynInput {
	private HttpUriRequest request;//请求
	private HttpServiceHandler handler;//访问服务得到数据后的处理句柄
	
	public AsynInput(HttpServiceHandler handler, HttpUriRequest request) {
		this.handler = handler;
		this.request = request;
	}

	public HttpUriRequest getRequest() {
		return request;
	}

	public HttpServiceHandler getHandler() {
		return handler;
	}

}
