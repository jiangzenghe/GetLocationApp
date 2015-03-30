package com.myapp.getlocation.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONException;

/**
 * 访问服务时后台运行的结果对象
 * @author
 *
 * <p>------------------------------------------------------------------</p>  
 */
public class AsynResult {
	private HttpResponse httpResponse;//应答
	private Exception exception;//异常
	private HttpServiceHandler handler;//访问服务得到数据后的处理句柄
	
	public AsynResult(HttpServiceHandler handler, HttpResponse response, Exception exp) {
		this.handler = handler;
		this.httpResponse = response;
		this.exception = exp;
	}
	
	public AsynResult(HttpServiceHandler handler, HttpResponse response) {
		this(handler, response, (Exception)null);
	}
	
	public AsynResult(HttpServiceHandler handler, Exception exp) {
		this(handler, null, exp);
	}	
	

	/**
	 * 处理应答
	 * @throws ParseException
	 * @throws JSONException
	 * @throws IOException
	 */
	public void handlerHttpServiceResponse() throws ParseException, JSONException, IOException {
		
		if(getException() != null){
			getHandler().onHttpServiceError(getException());
		}
		
		if (getHttpResponse() != null) {
			getHandler().onHttpServiceFinished(getHttpResponse());
		}
	}
	
	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
	public Exception getException() {
		return exception;
	}
	public HttpServiceHandler getHandler() {
		return handler;
	}
	
}
