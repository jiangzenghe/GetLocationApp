package com.myapp.getlocation.application;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.util.AppConfigFileLoader;
import com.myapp.getlocation.util.Util;
/**
* 应用初始化数据库连接加载器
* <p>  </p>
*/
public class SQLiteDataBaseInitialization extends
		ApplicationInitializationChainSupport implements ApplicationExitListener {
	public static final String TAG = SQLiteDataBaseInitialization.class.getName();
	
	public static final String DATABASE_DOWNLOAD_SERVICE_NAME = "framework.config.database.download.service.name";
	public static final String DATABASE_RESOURCE_ID = "framework.config.database.raw.resource.id";
	public static final String DATABASE_FILE_PATH = "framework.config.database.file.path";
	public static final String DATABASE_FILE_VERSION = "framework.config.database.version";
	public static final int DATABASE_DEFAULT_VERSION = 1;
	
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
			// 获取配置的默认打开数据库路径及名称
			String path = app.getMetaDataString(DATABASE_FILE_PATH, null);
			if (TextUtils.isEmpty(path)) {
				doNext(context);
				return ;
			}
			
			if (path.startsWith("/mnt/sdcard")) {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					Log.d(SQLiteDataBaseInitialization.class.getSimpleName(), 
							"外接存储卡不可用，将改为使用手机内置存储器做为缓存.");
					File dbFile = new File(path);
					String fileName = dbFile.getName();
					dbFile = context.getDatabasePath(fileName);
					path = dbFile.getAbsolutePath();
				}
				else {
					path = path.replace("/mnt/sdcard", 
							Environment.getExternalStorageDirectory().getAbsolutePath());
				}
			}
			File target = new File(path);
			if (target.exists()) {
				openDatabase(app, target);
				doNext(context);
				return ;
			}
			
			File dir = target.getParentFile();
			if (!dir.exists()) {
				Util.mkdir(dir);
			}
			
			openDatabase(app, target);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		doNext(context);
	}

	protected void openDatabase(Application app, File target) {
		int rqVersion = app.getMetaDataInt(DATABASE_FILE_VERSION, DATABASE_DEFAULT_VERSION);
		EntityHelper entityHelper = new EntityHelper(app, target.getPath(), null, rqVersion);
		app.setEntityHelper(entityHelper);
		app.addApplicationExitListener(this);
	}
	
	protected void doNext(Context context) {
		if (getNext() == null) {
			setNext(new AppConfigFileLoader());
		}
		getNext().doProcess(context);
	}
	@Override
	public boolean shutdown(Application app) {
		if (app.getEntityHelper()!=null) {
			app.getEntityHelper().close();
		}
		return true;
	}
}
