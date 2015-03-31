package com.myapp.getlocation.activity;


import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myapp.getlocation.R;
import com.myapp.getlocation.http.HttpServiceHandler;
import com.myapp.getlocation.http.HttpServiceProgressWrapper.ProgressDialogHandler;
import com.myapp.getlocation.util.Util;

/**
 *该类是启动Activity 在该类中应在意图对象中附加相关信息标志
 *比如是否加密，是否保存账号，密码等等
 * <p>  </p>
 */
public class SplashActivity extends Activity {
	private static final String TAG = SplashActivity.class.getSimpleName();
	private static final String APP_TARGET_ACTIVITY = SplashActivity.class.getName()+".app.target.activity";
	private static final String WAITING_TIME = SplashActivity.class.getName()+".waiting.time";//单位毫秒
	
	private long sleepTime;
	private long interval;
	private AutoInit initer;
	
	public SplashActivity() {
		setSleepTime(3000);
	}
	
	private class DownFileHandler implements HttpServiceHandler {

		@Override
		public void onHttpServicePrepare(HttpResponse response) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onHttpServiceFinished(HttpResponse response) {
			// TODO Auto-generated method stub
			Toast.makeText(SplashActivity.this, "finish", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onHttpServiceError(Exception e) {
			// TODO Auto-generated method stub
			Toast.makeText(SplashActivity.this, "error", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.splash_activity);
		final LinearLayout layout = (LinearLayout)findViewById(R.id.splash_activity_layout_comp);
		int resId = R.drawable.splash;
		if (resId != 0) {
			layout.setBackgroundResource(resId);
		}
		
		// 远程连接时，使用进度对话框
		ProgressDialog defaultDialog = new ProgressDialog(this);
		defaultDialog
				.setMessage("等待中");
		ProgressDialogHandler handler = new ProgressDialogHandler(
				defaultDialog);
		DownFileHandler downHander = new DownFileHandler();
		
		try {
			this.getDefaultHttpService(handler).callPostService("allScenicScenicAreaAction.action", downHander);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		initer = new AutoInit(this);
//		initer.start();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		sleep();
	}
	/**
	 * 
	 * 让当前的Activity等待指定的时间，等持时间长度由sleepTime属性指定，单位毫秒。
	 */
	protected void sleep() {
		if(getSleepTime() == 0){
			setSleepTime(2000);
		}
		
		if(interval >= getSleepTime()){
			Intent intent = null;
			try {
				intent = makeTargetIntent();
			} catch (ClassNotFoundException e) {
				Log.e(TAG, e.getMessage());
				return;
			}
			startActivity(intent);
			finish();
		}else{
			new Thread(){
				@Override
				public void run() {
					try {
						Thread.sleep(getSleepTime() - interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Intent intent = null;
					try {
						intent = makeTargetIntent();
					} catch (ClassNotFoundException e) {
						Log.e(TAG, e.getMessage());
						return;
					}
					startActivity(intent);
					finish();
				};
			}.start();
		}

	}
		
	/**
	 * 在Activity等待结束后运行的意图。
	 * @return
	 */
	protected Intent makeTargetIntent() throws ClassNotFoundException {
		String target = getMetaDataString(APP_TARGET_ACTIVITY,"");
		if("".equals(target)){
			throw new ClassNotFoundException(
				SplashActivity.class.getName()+"快闪之后的目标界面未指定"+
							APP_TARGET_ACTIVITY);
		}
		Class<?> targetClass = null;
		try {
			targetClass = Class.forName(target);
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		}
		Intent intent = new Intent(SplashActivity.this, targetClass);
		return intent;
	}
	
	// 读取在Splash标签中配置的Meta_Data属性
	public Bundle getMetaDataBundle() {
		Bundle bundle = new Bundle();
		bundle = Util.getActivityMetaDataBundle(getPackageManager(), getComponentName());
		return bundle;
	}
	
	public String getMetaDataString(String key, String defValue) {
		if (getMetaDataBundle() != null && getMetaDataBundle().containsKey(key)) {
			return getMetaDataBundle().getString(key);
		}
		return defValue;
	}
	
	/**
	 * 返回等待线程等待的时间，单位毫秒。
	 * @return
	 */
	protected long getSleepTime() {
		return sleepTime;
	}
	/**
	 * 设置等待线程等待时间，单位毫秒。
	 * @param timeOut
	 */
	protected void setSleepTime(long timeOut) {
		this.sleepTime = timeOut;
	}
}
