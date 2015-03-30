package com.myapp.getlocation.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

/**
 * 这个类是HttpServer类的一个扩展类，这个类通过调用HttpService类成员的方法重新实现了
 * HttpService类定义的方法，并且加入了通信过程中显示进度对话框的功能。这是装饰模式的一个实现。
 * @author
 * 
 **/
public class HttpServiceProgressWrapper extends DefaultHttpService {
	public static abstract class ProgressHandler implements HttpServiceHandler {
		private HttpServiceHandler handler;
		
		protected ProgressHandler() {
			this(null);
		}
		protected ProgressHandler(HttpServiceHandler handler) {
			this.handler = handler;
		}
		public abstract void open();
		public abstract void close();
		
		@Override
		public void onHttpServicePrepare(HttpResponse response) {
			if (this.handler != null) {
				this.handler.onHttpServicePrepare(response);
			}
		}

		@Override
		public void onHttpServiceFinished(HttpResponse response) {
			close();
			if (this.handler != null) {
				this.handler.onHttpServiceFinished(response);
			}
		}

		@Override
		public void onHttpServiceError(Exception e) {
			close();
			if (this.handler != null) {
				this.handler.onHttpServiceError(e);
			}
			
		}
		public HttpServiceHandler getHandler() {
			return handler;
		}
		public void setHandler(HttpServiceHandler handler) {
			this.handler = handler;
		}
		
	}
	
	/**
	 * 进程对话框处理器
	 */
	public static class ProgressDialogHandler extends ProgressHandler {
		private ProgressDialog progressDialog;
		
		public ProgressDialogHandler(Context context) {
			progressDialog = new ProgressDialog(context);
		}
		/**
		 * 
		 * @param progressDialog
		 * @param handler
		 */
		public ProgressDialogHandler(ProgressDialog progressDialog) {
			this.progressDialog = progressDialog;
		}

		@Override
		public void open() {
			this.progressDialog.show();
			
		}

		@Override
		public void close() {
			this.progressDialog.cancel();	
		}
	}
	
	/**
	 * 进度条处理器
	 */
	public static class ProgressBarHandler extends ProgressHandler {
		private ProgressBar progressBar;
		
		public ProgressBarHandler(ProgressBar progressBar) {
			this.progressBar = progressBar;
		}
	
		@Override
		public void open() {
			this.progressBar.setVisibility(View.VISIBLE);
			this.progressBar.setIndeterminate(true);
		}

		@Override
		public void close() {
			this.progressBar.setIndeterminate(false);
			this.progressBar.setProgress(this.progressBar.getMax());
			this.progressBar.setVisibility(View.GONE);
		}
	}
		
	private ProgressHandler progressHandler;
	private HttpService httpService;
	
	public HttpServiceProgressWrapper(Context context, HttpService httpService) {
		this(new ProgressDialogHandler(context), httpService);
	}
	public HttpServiceProgressWrapper(Context context) {
		this(context, DefaultHttpService.getInstance());
	}
	public HttpServiceProgressWrapper(ProgressDialog dialog) {
		this(new ProgressDialogHandler(dialog));
	}
	public HttpServiceProgressWrapper(ProgressDialog dialog, HttpService httpService) {
		this(new ProgressDialogHandler(dialog), httpService);
	}
	public HttpServiceProgressWrapper(ProgressBar progressbar, HttpService httpService) {
		this(new ProgressBarHandler(progressbar), httpService);
	}
	public HttpServiceProgressWrapper(ProgressBar progressbar) {
		this(new ProgressBarHandler(progressbar));
	}
	
	public HttpServiceProgressWrapper(ProgressHandler handler, HttpService httpService) {
		this.setProgressHandler(handler);
		this.httpService = httpService;
	}
	public HttpServiceProgressWrapper(ProgressHandler handler) {
		this(handler, DefaultHttpService.getInstance());
	}
	
	@Override
	public HttpResponse synCallService(HttpUriRequest request) throws ClientProtocolException, IOException {
		getProgressHandler().open();
		HttpResponse response = httpService.synCallService(request);
		getProgressHandler().close();
		return response;
	}
	@Override
	public HttpResponse synCallGetService(String svcName,
			Map<String, Object> args) throws ClientProtocolException,
			IOException {
		getProgressHandler().open();
		HttpResponse response = httpService.synCallGetService(svcName, args);
		getProgressHandler().close();
		return response;
	}
	@Override
	public HttpResponse synCallPostService(String svcName,
			Map<String, Object> args) throws ClientProtocolException,
			IOException {
		getProgressHandler().open();
		HttpResponse response = httpService.synCallPostService(svcName, args);
		getProgressHandler().close();
		return response;
	}
	@Override
	public void callGetService(String svcName, Map<String, Object> args,
			HttpServiceHandler handler) throws ClientProtocolException,
			IOException {
		getProgressHandler().setHandler(handler);
		getProgressHandler().open();
		httpService.callGetService(svcName, args, getProgressHandler());
		
	}
	@Override
	public void callPostService(String svcName, Map<String, Object> args,
			HttpServiceHandler handler) throws UnsupportedEncodingException {
		getProgressHandler().setHandler(handler);
		getProgressHandler().open();
		httpService.callPostService(svcName, args, getProgressHandler());
	}
	

	@Override
	public void callService(HttpUriRequest request, HttpServiceHandler handler) {
		getProgressHandler().setHandler(handler);
		getProgressHandler().open();
		httpService.callService(request, getProgressHandler());
	}

	@Override
	public String getBaseAddress() {
		return httpService.getBaseAddress();
	}
	@Override
	public void setBaseAddress(String baseAddress) {
		httpService.setBaseAddress(baseAddress);
	}
	@Override
	public String getEncoding() {
		return httpService.getEncoding();
	}
	@Override
	public void setEncoding(String encoding) {
		httpService.setEncoding(encoding);
	}
	@Override
	public void callPostService(String svcName, Map<String, Object> args,
			boolean multipart, HttpServiceHandler handler)
			throws UnsupportedEncodingException {
		getProgressHandler().setHandler(handler);
		getProgressHandler().open();
		httpService.callPostService(svcName, args, multipart, getProgressHandler());
		
	}
	public ProgressHandler getProgressHandler() {
		return progressHandler;
	}
	public void setProgressHandler(ProgressHandler progressHandler) {
		this.progressHandler = progressHandler;
	}

}
