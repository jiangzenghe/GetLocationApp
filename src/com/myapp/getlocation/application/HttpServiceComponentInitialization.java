package com.myapp.getlocation.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.myapp.getlocation.R;
import com.myapp.getlocation.activity.Activity;
import com.myapp.getlocation.http.DefaultHttpService;
/**
* 应用初始化远程连接加载器
* <p>  </p>
*/
public class HttpServiceComponentInitialization extends ApplicationInitializationChainSupport {
	private static final String TAG = HttpServiceComponentInitialization.class.getSimpleName();
	/**
	 * 用于在SharedPreferences中存放服务地址的Key键名。
	 */
	public static final String PRE_SERVER_ADDR_CONFIG = "SERVER.ADDR";
	public static final String SERVICE_BASE_ADDRESS = "framework.config.service.base.address";
	public static final String SERVICE_ENCODING = "framework.config.service.encoding";
	
	/**
	 * 用于存储指定的服务基地址存放到SharedPreferences对象中存储，SharedPreferences对象文件为应用的包名最后部分的名称。
	 * @param context
	 * @param baseAddress
	 */
	public static void saveServerAddress(Context context, String baseAddress) {
		String packageName = context.getPackageName();
		int pos = packageName.lastIndexOf('.');
		if (pos >= 0) {
			packageName = packageName.substring(pos+1);
		}
		SharedPreferences sharePreferen = context.getSharedPreferences(packageName, Context.MODE_APPEND);
		SharedPreferences.Editor editor = sharePreferen.edit();
		editor.putString(HttpServiceComponentInitialization.PRE_SERVER_ADDR_CONFIG, baseAddress);
		editor.commit();
	}
	
	@Override
	public void doProcess(Context context) {
		Application app = null;
		if (context instanceof Activity) {
			app = (Application)((Activity)context).getApplication();
		}
		else {
			app = (Application)context;
		}
		
		String packageName = app.getPackageName();
		int pos = packageName.lastIndexOf('.');
		if (pos >= 0) {
			packageName = packageName.substring(pos+1);
		}
		
		String baseAddress = app.getMetaDataString(SERVICE_BASE_ADDRESS, null);
		SharedPreferences sharePreferen = app.getSharedPreferences(packageName, Context.MODE_APPEND);
		baseAddress = sharePreferen.getString(PRE_SERVER_ADDR_CONFIG, baseAddress);
		
		// 获取配置的网络地址及编码
		try {
			String encode = app.getMetaDataString(SERVICE_ENCODING, "GBK");
			if (!TextUtils.isEmpty(baseAddress)) {
				DefaultHttpService defaultHttpService = DefaultHttpService.getInstance();
				defaultHttpService.setBaseAddress(baseAddress);
				defaultHttpService.setEncoding(encode);
				defaultHttpService.setFileMimeType(app.getFileMimeType());
				app.setHttpService(defaultHttpService);
			}
			else {
				Log.w(TAG, "没有指定服务基地址参数，应用将不能访问网络资源。");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		doNext(context);
	}

}
