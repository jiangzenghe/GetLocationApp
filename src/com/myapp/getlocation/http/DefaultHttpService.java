package com.myapp.getlocation.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 请求服务类
 * @author 
 * 
 */
@SuppressLint("DefaultLocale")
public class DefaultHttpService implements HttpService {
	@SuppressWarnings("unused")
	private static final String TAG = DefaultHttpService.class.getSimpleName();
	private static final String USERAGENT = "haiyi.mobile.android.app.framework";
	private static DefaultHttpService instance = null;//单例对象
	/**
	 * 获取单例对象
	 * @return
	 */
	public static DefaultHttpService getInstance() {
		if (instance == null) {
			synchronized (DefaultHttpService.class) {
				if (instance == null) {
					instance = new DefaultHttpService();
				}
			}			
		}
		return instance;
	}	
	
	public static HttpService getProgressInstance(Context context) {
		return new HttpServiceProgressWrapper(context);
	}
	
	private HttpClient httpClient;//客户端类
	private String baseAddress;
	private String encoding;
	private HttpContext httpContext;
	private Properties fileMimeType;//属性配置文件
	
	public DefaultHttpService() {
		this("");
	}
	public DefaultHttpService(String baseAddress) {
		super();
		this.baseAddress = baseAddress;
		this.encoding = HTTP.UTF_8;
		this.httpContext = new BasicHttpContext();
		CookieStore cookieStore = new BasicCookieStore();
		this.httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		this.httpClient = getNewHttpClient();
	}
	
	public DefaultHttpService(DefaultHttpService httpService) {
		super();
		this.baseAddress = httpService.baseAddress;
		this.encoding = httpService.encoding;
		this.httpContext = httpService.httpContext;
		this.httpClient = getNewHttpClient();
	}

	@Override
	public String getBaseAddress() {
		return baseAddress;
	}

	@Override
	public void setBaseAddress(String baseAddress) {
		this.baseAddress = baseAddress;

	}
	
	@Override
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding=encoding;
	}
	
	public Properties getFileMimeType() {
		return fileMimeType;
	}

	public void setFileMimeType(Properties fileMimeType) {
		this.fileMimeType = fileMimeType;
	}
	
	/**
	 * 获取新的客户端类
	 * @return HttpClient
	 */
	public HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, getEncoding());
	        HttpProtocolParams.setUserAgent(params, USERAGENT);
	        
	        //设置超时
			HttpConnectionParams.setConnectionTimeout(params, 120000);
			HttpConnectionParams.setSoTimeout(params, 120000);
			
			//设置信任端口
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));
	        
	        // 发现通过ThreadSafeClientConnManager类生成的DefaultHttpClient对象在访问服务时
	        // 如果请求的地址无效时，多次请求该地址时DefaultHttpClient.execute方法挂起无响应。
	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
	        return new DefaultHttpClient(ccm, params);
	        
//	        ClientConnectionManager ccm = new SingleClientConnManager(params, registry);
//	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}

	/**
	 * 同步访问服务
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	@Override
	public HttpResponse synCallService(HttpUriRequest request) throws ClientProtocolException, IOException {
		HttpResponse response = httpClient.execute(request);
		return response;
	}
	/**
	 * 同步Post请求服务
	 */
	@Override
	public HttpResponse synCallPostService(String svcName, Map<String, Object> args) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(createAddress(svcName).toString());
		
		List<NameValuePair> formparams = createFormData(args);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, getEncoding());
		request.setEntity(entity);
		return synCallService(request);
	}
	/**
	 * 同步Get请求服务
	 */
	@Override
	public HttpResponse synCallGetService(String svcName, Map<String, Object> args) throws ClientProtocolException,	IOException {
		StringBuffer uri = createGetRequest(svcName, args);
		HttpGet request = new HttpGet(uri.toString());
		return synCallService(request);
	}

	/**
	 * 异步请求服务
	 * @param request 请求对象
	 * @param handler 请求结果处理器
	 */
	@Override
	public void callService(final HttpUriRequest request, HttpServiceHandler handler) {
		ServiceRequestTask task = new ServiceRequestTask();
		task.execute(new AsynInput(handler, request));
	}
	/**
	 * 异步Get请求服务
	 */
	@Override
	public void callGetService(String svcName, Map<String, Object> args, HttpServiceHandler handler)
			throws ClientProtocolException, IOException {
		StringBuffer uri = createGetRequest(svcName, args);
		HttpGet request = new HttpGet(uri.toString());
		callService(request, handler);
		
	}
	/**
	 * 异步Post请求服务
	 */
	@Override
	public void callPostService(String svcName, Map<String, Object> args, HttpServiceHandler handler) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost(createAddress(svcName).toString());
		
		List<NameValuePair> formparams = createFormData(args);
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, getEncoding());
		request.setEntity(entity);
		callService(request, handler);
		
	}
	
	public void callPostService(String svcName, Map<String, Object> args, boolean multipart, HttpServiceHandler handler) throws UnsupportedEncodingException {
		if (!multipart) {
			callPostService(svcName, args, handler);
		}
		else {
			MultipartEntity entity = buildMultipartEntity(args);
			HttpPost request = new HttpPost(createAddress(svcName).toString());
			request.setEntity(entity);
			callService(request, handler);
		}
	}
	/**
	 * 根据传入的映射参数构建HTTP分段请求实体对象。
	 * @param args http请求映射参数。
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected MultipartEntity buildMultipartEntity(Map<String, Object> args) throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		for (Map.Entry<String, Object> arg : args.entrySet()) {
			String key = arg.getKey();
			Object value = arg.getValue();
			
			buildCotentBody(entity, key, value);
		}
		return entity;
	}
	/**
	 * 根据value参数的值类型，生成ContentBody对象加入到entity参数指定的对象中去。
	 * @param entity 一个HTTP请求实体。
	 * @param key 参数名
	 * @param value 参数值
	 * @throws UnsupportedEncodingException 
	 */
	private void buildCotentBody(MultipartEntity entity, String key, Object value) throws UnsupportedEncodingException {
		ContentBody body = null;
		if (value instanceof File) {
			body = buildFileBody((File)value);
		}
		else if (value instanceof InputStream) {
			body = new InputStreamBody((InputStream)value, key);
		}
		else if (value instanceof byte[]) {
			body = new ByteArrayBody((byte[])value, key);
		}
		else if (value instanceof Set<?>) {
			Set<?> collection = (Set<?>)value;
			for (Object subItem : collection) {
				buildCotentBody(entity, key, subItem);
			}
		}
		else if (value instanceof List<?>) {
			List<?> collection = (List<?>)value;
			for (Object subItem : collection) {
				buildCotentBody(entity, key, subItem);
			}
		}
		else {
			body = new StringBody(value.toString(), Charset.forName(getEncoding()));
		}
		
		if (body != null) {
			entity.addPart(key, body);
		}
	}
	/**
	 * 获取文件扩展名。
	 * @param file
	 * @return
	 */
	private String getFileExtName(File file) {
		int pos = file.getName().lastIndexOf(".");
		if (pos == -1) {
			return null;
		}
		else {
			return file.getName().substring(pos).toLowerCase();
		}
	}
	
	/**
	 * 通过文件对象，生成一个FileBody对象，用于文件的上传。
	 * @param file
	 * @return
	 */
	private FileBody buildFileBody(File file) {
		String fileExtName = getFileExtName(file);
		if (fileExtName == null) {
			return new FileBody(file);
		}
		
		String mimeType = this.getFileMimeType().getProperty(fileExtName);
		if (mimeType == null) {
			return new FileBody(file);	
		}
		
		return new FileBody(file, mimeType);
	}
	
	/**
	 * 该方法将构建一个服务地址字符串缓冲器对象，这个方法的主要功能就是分析了服务基地址尾部与服务名头部是否存有"\"，
	 * 并且根据情况拼出一个合法的HTTP地址串。
	 * @param svcName
	 * @return
	 */
	protected StringBuffer createAddress(String svcName) {
		StringBuffer buffer = new StringBuffer();
		if("".equals(getBaseAddress())){
			buffer.append(svcName);
		}else{
			buffer.append(getBaseAddress());
			
			if (getBaseAddress().endsWith("/")) {
				if (svcName.startsWith("/")) {
					buffer.append(svcName.substring(1));
				}
				else {
					buffer.append(svcName);
				}
			}
			else {
				if (svcName.startsWith("/")) {
					buffer.append(svcName);
				}
				else {
					buffer.append("/");
					buffer.append(svcName);
				}
			}
		}

		return buffer;
	}
	
	/**
	 * 该方法要主用于Post请求，用于构建一个Form请求参数名值对列表。
	 * @param args
	 * @return
	 */
	protected List<NameValuePair> createFormData(Map<String, Object> args) {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (args != null) {
			for (Map.Entry<String, Object> arg : args.entrySet()) {
				formparams.add(new BasicNameValuePair(arg.getKey(), arg.getValue().toString()));
			}
		}
		return formparams;
	}
	
	/**
	 * 该方法主要用于Get请求，用于构建一个合法的带请求参数的HTTP服务地址。
	 * @param svcName
	 * @param args
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected StringBuffer createGetRequest(String svcName, Map<String, Object> args) throws UnsupportedEncodingException {
		StringBuffer requestUri = createAddress(svcName);		
		if (args != null && !args.isEmpty()) {
			if (requestUri.indexOf("?") == -1) {
				requestUri.append("?");
			}
			else {
				char lastChar = requestUri.charAt(requestUri.length()-1);
				if (lastChar != '?' && lastChar != '&') {
					requestUri.append("&");
				}
			}
			
			int i=0;
			for (Map.Entry<String, Object> arg : args.entrySet()) {
				if (i++ != 0) {
					requestUri.append("&");
				}
				requestUri.append(arg.getKey());
				requestUri.append("=");
				requestUri.append(URLEncoder.encode(arg.getValue().toString(), getEncoding()));
			}
		}
		return requestUri;
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	/**
	 * 处理请求服务的类，异步处理返回结果
	 * @author 
	 *
	 */
	private class ServiceRequestTask extends AsyncTask<AsynInput, Void, AsynResult> {
		
		/**
		 * 处理结果
		 * @param result
		 */
		@Override
		protected void onPostExecute(AsynResult result) {
			super.onPostExecute(result);
			
			try {
				result.handlerHttpServiceResponse();
			} catch (ParseException e) {
				Log.e("HttpService", e.getMessage(), e);
			} catch (JSONException e) {
				Log.e("HttpService", e.getMessage(), e);
			} catch (IOException e) {
				Log.e("HttpService", e.getMessage(), e);
			}
		}
		
		/**
		 * 后台调用请求服务
		 * @param params
		 */
		@Override
		protected AsynResult doInBackground(AsynInput... params) {
			HttpResponse response = null;
			try {				
				synchronized (httpClient) {
					response = httpClient.execute(params[0].getRequest(), httpContext);
				}
								
				params[0].getHandler().onHttpServicePrepare(response);
				return new AsynResult(params[0].getHandler(), response);
			} catch (ClientProtocolException e) {
				params[0].getHandler().onHttpServicePrepare(null);
				return new AsynResult(params[0].getHandler(), e);
			} catch (IOException e) {
				params[0].getHandler().onHttpServicePrepare(null);
				return new AsynResult(params[0].getHandler(), e);
			} catch (Exception e) {
				params[0].getHandler().onHttpServicePrepare(null);
				return new AsynResult(params[0].getHandler(), e);
			}
		}		
		
	}

}