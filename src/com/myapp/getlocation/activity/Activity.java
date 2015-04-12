package com.myapp.getlocation.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.http.DefaultHttpService;
import com.myapp.getlocation.http.HttpServiceProgressWrapper;
import com.myapp.getlocation.http.HttpServiceProgressWrapper.ProgressHandler;
import com.myapp.getlocation.util.ActivitysManager;
import com.myapp.getlocation.util.Util;

// TODO: Auto-generated Javadoc
/**
 * 框架基类.
 *
 * @author 
 * 
 */
@SuppressLint({ "InlinedApi", "HandlerLeak" })
public class Activity extends FragmentActivity {
	/** The Constant INTENT_EXT_EXIT_APP. */
	public static final String EXIT_MODE_DIALOG = "dialog";
	public static final String EXIT_MODE_DOUBLE_BACK = "double_back";
	public static final String META_DATA_EXT_EXIT_APP_MESSAGE = Activity.class.getName()+".exit.app.message";
	public static final String META_DATA_LOGIN_ACTIVITY = Activity.class.getName()+".login.activity.class.name";
	public static final String META_DATA_EXT_EXIT_MODE = Activity.class.getName() + ".exit.app.mode";
	public static final String META_DATA_EXT_EXIT_INTERVAL = Activity.class.getName() + ".exit.app.interval";
	public static final String INTENT_LOGIN_SUCCESS_TARGET = Activity.class.getName() + "login.success.target.class.name";
	/** The default http service. */
	private HttpServiceProgressWrapper progressHttpService;
	
	private Bundle bundle;
	private ExitAppStrategy exitStrategy;
	
	/**
	 * Instantiates a new activity.
	 */
	public Activity() {
		super();
	}
	/**
	 * 加装指定的布局资源到Activity中。
	 * @param layoutResID
	 * @param inject　是否支持注释绑定
	 */
	public void setContentView(int layoutResID, boolean inject) {
		setContentView(layoutResID);
	}

	public void setContentView(View view, boolean inject) {
		setContentView(view);
	}
	
	public void setContentView(View view, LayoutParams params, boolean inject) {
		setContentView(view, params);
	}
	/**
	 * 重新进入界面时，未登录情况下从SharedPreferences中获取要保存的数据进行展示 登录情况想从UserView中获取要保存的数据进行展示.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	public Bundle getMetaDataBundle() {
		if (bundle == null) {
			bundle = Util.getActivityMetaDataBundle(getPackageManager(), getComponentName());
		}
		return bundle;
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
	public boolean getMetaDataBoolean(String key, boolean defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getBoolean(key);
		}
		return defValue;
	}
	public float getMetaDataFloat(String key, float defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getFloat(key);
		}
		return defValue;
	}
	/**
	 * 离开界面时，保存界面上相关控件的信息.
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		InputMethodManager imm = 
				(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive() && getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			getCurrentFocus().clearFocus();
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public void onBackPressed() {
		if (exitStrategy != null) {
			exitStrategy.backPress();
			return;
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return super.onTouchEvent(event);
	}
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		String exitMode = getMetaDataString(META_DATA_EXT_EXIT_MODE, null);
		if (EXIT_MODE_DIALOG.equalsIgnoreCase(exitMode)) {
			exitStrategy = new DialogStrategy();
		}
		else if (EXIT_MODE_DOUBLE_BACK.equalsIgnoreCase(exitMode)){
			exitStrategy = new DoubleBackStrategy();
		}
		else {
			exitStrategy = null;
		}
		ActivitysManager.getInstance().addActivity(this);
	}

	/**
	 * 关闭时从ActivitysManager的队列中移除.
	 */
	@Override
	public void finish() {
		ActivitysManager.getInstance().removeActiviyt(this);
		super.finish();
	}
	
	/**
	 * 获取网络服务对象.
	 * @return 网络服务对象
	 */
	public HttpServiceProgressWrapper getProgressHttpService() {
		if(progressHttpService==null){
			ProgressTitleHandler handler = new ProgressTitleHandler();
			progressHttpService = getProgressHttpService(handler);
		}
		return progressHttpService;
	}
	/**
	 * 建立特定进度样式的网络服务对象。
	 * @param progressStyle
	 * @return
	 */
	public HttpServiceProgressWrapper getProgressHttpService(ProgressHandler progressStyle) {
		Application app = (Application)getApplication();
		HttpServiceProgressWrapper wrapper = new HttpServiceProgressWrapper(progressStyle,app.getHttpService());
		return wrapper;
	}
	/**
	 *　获取应用全局的实体处理器对象
	 * @return EntityHelper实体处理器对象
	 */
	public EntityHelper getEntityHelper() {
		Application app = (Application)getApplication();
		return app.getEntityHelper();
	}
	
	/**
	 *该类继承了ProgressHandler类实现了open,close方法
	 *以控制Activity的标题栏上的进度的状态 
	 *open表示当前正在请求网络服务，close表示请求网络服务结束
	 * @author
	 * 
	 * <p>Modification History:</p>
	 * <p>Date       Author      Description</p>
	 * <p>------------------------------------------------------------------</p>
	 * <p>     </p>
	 * <p>  </p>
	 */
	private class ProgressTitleHandler extends ProgressHandler{
		
		/* (non-Javadoc)
		 * @see http.HttpServiceProgressWrapper.ProgressHandler#open()
		 */
		@Override
		public void open() {
			Activity.this.setProgressBarIndeterminate(true);
			Activity.this.setProgressBarIndeterminateVisibility(true);
			Activity.this.setProgressBarVisibility(true);
		}
		
		/* (non-Javadoc)
		 * @see http.HttpServiceProgressWrapper.ProgressHandler#close()
		 */
		@Override
		public void close() {
			Activity.this.setProgressBarIndeterminate(false);
			Activity.this.setProgressBarIndeterminateVisibility(false);
			Activity.this.setProgressBarVisibility(false);
		}
		
	}
	
	/**
	 * @author
	 *
	 */
	private interface ExitAppStrategy {
		public void backPress();
	}
	
	/**
	 * @author
	 *
	 */
	private class DoubleBackStrategy implements ExitAppStrategy {
		private long lastPressTime;
		private String message;
		private int interval;
		private CloseAppHandler handler;
		
		public DoubleBackStrategy() {
			lastPressTime = -1;
			message = getMetaDataString(META_DATA_EXT_EXIT_APP_MESSAGE, 
					"确定退出？");
			interval = getMetaDataInt(META_DATA_EXT_EXIT_INTERVAL, 2);
			handler = new CloseAppHandler();
		}
		
		public void backPress() {
			if (lastPressTime == -1) {
				lastPressTime = System.currentTimeMillis();
				Toast.makeText(Activity.this, message, Toast.LENGTH_SHORT).show();
				Timer timer = new Timer(true);
				TimerTask task = new TimerTask(){  
				      public void run() {  
				      Message messageTemp = new Message();      
				      messageTemp.what = 1;      
				      handler.sendMessage(messageTemp);    
				   }  
				};
				timer.schedule(task, interval * 1000);
			}else{
			
				long disSeconds = (System.currentTimeMillis()-lastPressTime)/1000;
				
				if (disSeconds > interval) {
					lastPressTime = -1;
					return ;
				}
				
				Application app = (Application) getApplication();
				app.exit();
			}
			
		}
		
		/**
		 * @author
		 *
		 */
		class CloseAppHandler extends Handler{
			@Override
	        public void handleMessage(Message msg) {
				lastPressTime = -1;
			}
		}

	}
	

	
	/**
	 * @author
	 *
	 */
	private class DialogStrategy implements ExitAppStrategy {
		public void backPress() {
			// 获取PackageManager 实例
			PackageManager packageManager = Activity.this.getPackageManager();
			// 获得context所属类的包名，0表示获取版本信息
			ApplicationInfo appInfo = null;
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(
						Activity.this.getPackageName(), 0);
				appInfo = packageInfo.applicationInfo;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (appInfo == null) {
				return;
			}
			String msg = getMetaDataString(META_DATA_EXT_EXIT_APP_MESSAGE, 
					"确认退出？");
			AlertDialog.Builder dialog = new AlertDialog.Builder(Activity.this);
			dialog.setTitle(appInfo.labelRes);
			dialog.setMessage(msg);
			dialog.setIcon(appInfo.icon);
			dialog.setCancelable(true);
			dialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Application app = (Application) getApplication();
							app.exit();
						}
					});
			dialog.setNegativeButton("退出",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.show();
		}
	}
}
