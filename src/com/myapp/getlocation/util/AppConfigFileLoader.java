package com.myapp.getlocation.util;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.application.ApplicationInitializationChainSupport;
/**
* 应用初始化应用配置加载器
* <p>  </p>
*/
public class AppConfigFileLoader extends ApplicationInitializationChainSupport {
	public static final String APP_CONFIG_RAW = "framework.config.properties.app.config";
	private static final String TAG = AppConfigFileLoader.class.getSimpleName();
	
	@Override
	public void doProcess(Context context) {
		Application app = null;
		if (context instanceof Activity) {
			app = (Application)((Activity)context).getApplication();
		}
		else {
			app = (Application)context;
		}
		
		int resId = app.getMetaDataInt(APP_CONFIG_RAW,0);
		if(resId == 0){
			if (getNext() != null) {
				getNext().doProcess(app);
			}
			return;
		}
		
		InputStream isInputStream = app.getResources().openRawResource(resId);
		AppConfigParser parser = new AppConfigParser();
		try {
			AppInitConfig config = parser.parse(isInputStream);
			// 注册sqlite实体
			if(app.getEntityHelper() != null){
				for (int i = 0; i < config.getListSqliteConfigs().size(); i++) {
					Class<?> type = Class.forName(config.getListSqliteConfigs().get(i));
					app.getEntityHelper().registerEntity(type);
				}
			}
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		if (getNext() != null) {
			getNext().doProcess(app);
		}
	}

}
