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