package com.myapp.getlocation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.http.DefaultHttpService;
import com.myapp.getlocation.http.HttpService;
import com.myapp.getlocation.util.ActivitysManager;
import com.myapp.getlocation.util.Util;

/**
 * APP入口
 * 
 * 
 */
@SuppressLint({ "WorldReadableFiles", "NewApi" })
public class Application extends android.app.Application {
	
	public static final String SERVICE_BASE_ADDRESS = "framework.config.service.base.address";
	public static final String TEXT_ENCODING = "framework.config.text.encoding";
	
	private List<ApplicationExitListener> exitListeners;
	private EntityHelper entityHelper;
	private Properties fileMimeType;
	private HttpService httpService;
	private Bundle bundle;
	private GsonBuilder gsonBuilder;
    
    /**
	 * 退出监听器列表
	 */
	public Application() {
		super();
		exitListeners = new ArrayList<ApplicationExitListener>();
	}

	public void addApplicationExitListener(ApplicationExitListener l) {
		if (!exitListeners.contains(l)) {
			exitListeners.add(l);
		}
	}

	public void removeApplicationExitListener(ApplicationExitListener l) {
		exitListeners.remove(l);
	}

	@Override
	public void onCreate() {
//		gsonBuilder = new GsonBuilder();
//		predefineAppInitChain();
		super.onCreate();
	}

	/**
	 * 打开指定的SQLite数据库文件。
	 * @param dbFile
	 * @param factory
	 * @param version
	 * @return
	 */
	public EntityHelper openEntityHelper(String dbFile, CursorFactory factory,	int version) {  
		EntityHelper entityHelperTemp = new EntityHelper(getApplicationContext(),dbFile,factory,version); 
		return entityHelperTemp;
	}

	public void exit() {
		try {
			for (ApplicationExitListener listener : exitListeners) {
				if (!listener.shutdown(this)) {
					return;
				}
			}
		} catch (Exception e) {
			Log.e("Application.exit", e.getMessage(), e);
			return;
		}

		ActivitysManager.getInstance().finishAll();
		System.exit(0);
	}

	// 读取在Application标签中配置的Meta_Data属性
	public Bundle getMetaDataBundle() {
		if (bundle == null) {
			bundle = Util.getAppMetaDataBundle(getPackageManager(), getPackageName());
		}
		return bundle;
	}
	
	public float getMetaDataFloat(String key, float defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getFloat(key);
		}
		return defValue;
	}

	public String getMetaDataString(String key, String defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getString(key);
		}
		return defValue;
	}
	public int getMetaDataInt(String key, int defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getInt(key);
		}
		return defValue;
	}
	public long getMetaDataLong(String key, long defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getLong(key);
		}
		return defValue;
	}
	
	public List<ApplicationExitListener> getExitListeners() {
		return exitListeners;
	}

	public void setExitListeners(List<ApplicationExitListener> exitListeners) {
		this.exitListeners = exitListeners;
	}
	
	public EntityHelper getEntityHelper() {
		return entityHelper;
	}
	public void setEntityHelper(EntityHelper entityHelper) {
		if (this.entityHelper != null) {
			this.entityHelper.close();
		}
		this.entityHelper = entityHelper;
	}
	
	public HttpService getHttpService() {
		return httpService;
	}
	
	public void setHttpService(HttpService httpService){
		this.httpService = httpService;
	}
	
	public Properties getFileMimeType() {
		return fileMimeType;
	}
	public void setFileMimeType(Properties fileMimeType) {
		this.fileMimeType = fileMimeType;
	}
	
	public GsonBuilder getGsonBuilder() {
		return gsonBuilder;
	}
	public void setGsonBuilder(GsonBuilder gsonBuilder) {
		this.gsonBuilder = gsonBuilder;
	}
}
