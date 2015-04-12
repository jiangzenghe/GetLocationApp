package com.myapp.getlocation;

import com.baidu.mapapi.SDKInitializer;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.application.ApplicationInitializationChain;
import com.myapp.getlocation.application.HttpServiceComponentInitialization;
import com.myapp.getlocation.application.SQLiteDataBaseInitialization;
import com.myapp.getlocation.application.SynchronismSupport;
import com.myapp.getlocation.util.AppConfigFileLoader;

public class GetLocationApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this); 
		Constants.scenicspotMarkertypeMap.put("1", "景点");
		Constants.scenicspotMarkertypeMap.put("2", "洗手间");
		Constants.scenicspotMarkertypeMap.put("3", "索道");
		Constants.scenicspotMarkertypeMap.put("4", "码头");
		Constants.scenicspotMarkertypeMap.put("5", "游客中心");
		Constants.scenicspotMarkertypeMap.put("6", "停车场");
		Constants.scenicspotMarkertypeMap.put("7", "换乘中心");
		Constants.scenicspotMarkertypeMap.put("8", "售票处");
		Constants.scenicspotMarkertypeMap.put("9", "出入口");
		predefineAppInitChain();
//		int rqVersion = this.getMetaDataInt(DATABASE_FILE_VERSION, DATABASE_DEFAULT_VERSION);
//		EntityHelper entityHelper = new EntityHelper(this, target.getPath(), null, 1);
//		this.setEntityHelper(entityHelper);
	}

	/**
	 * 提供了预定义的应用组件初始化链构建与执行。
	 */
	public void predefineAppInitChain() {
		ApplicationInitializationChain root = new SynchronismSupport();
		ApplicationInitializationChain next = root;
//		
//		next.setNext(new MimeTypeLoader());
//		next = next.getNext();
//		
//		next.setNext(new GsonBuilderComponentInitialization());
//		next = next.getNext();
//		
		next.setNext(new HttpServiceComponentInitialization());
		next = next.getNext();
//		
		next.setNext(new SQLiteDataBaseInitialization());
		next = next.getNext();

		next.setNext(new AppConfigFileLoader());
		next = next.getNext();
//		
//		next.setNext(new BaiduMapComponentInitialization());
//		next = next.getNext();
//		
		root.doProcess(this);
	}
	
}