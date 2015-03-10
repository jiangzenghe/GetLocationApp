package com.myapp.getlocation.application;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

/**
* 应用初始化地图加载器
* <p>  </p>
*/
public class BaiduMapComponentInitialization extends
		ApplicationInitializationChainSupport {
	private static final String TAG = BaiduMapComponentInitialization.class.getSimpleName();

	@Override
	public void doProcess(Context context) {
		Application app = null;
		if (context instanceof Activity) {
			app = (Application)((Activity)context).getApplication();
		}
		else {
			app = (Application)context;
		}
		try {
			SDKInitializer.initialize(app.getApplicationContext()); 
		} catch (NotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		}
        
		if(getNext() != null){
			getNext().doProcess(app);
		}
	}
	
}
