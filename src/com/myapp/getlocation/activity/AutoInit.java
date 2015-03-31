package com.myapp.getlocation.activity;

import java.io.File;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;

import com.myapp.getlocation.R;
import com.myapp.getlocation.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class.
 * 
 * @author 
 * 
 *         <p>
 *         Modification History:
 *         </p>
 *         <p>
 *         Date Author Description
 *         </p>
 *         <p>
 */
public class AutoInit {
	/** 通知标识符. */
	public static final int NOTIFICATON_UPDATE_FLAG = 0;

	/** 下载程序所需存储空间最低限度大小 (单位KB). */
	public static final int MEMORY_SPACE_SIZE_MIN_LIMIT = 20 * 1024;

	/** The context. */
	private Activity context;
	
//	public static UpdateHandler mediator;
	
	public AutoInit(Activity context) {
		this.context = context;
//		if (mediator == null) {
//			mediator = new UpdateHandler(context);
//		}
	}

	/**
	 * Update.检测版本更新地址是否配置，若已配置，调用queryServerVersion方法进行版本升级检测
	 */

	public void start() {
		ComponentName componentName = new ComponentName(getContext(),
				SplashActivity.class);
		Bundle bundle = Util.getActivityMetaDataBundle(getContext()
				.getPackageManager(), componentName);
//		if (!TextUtils.isEmpty(queryVersionURL)) {
//			queryServerVersion(queryVersionURL);
//		}
	}

	/**
	 * Creates the notification.
	 * 
	 * @param intent
	 *            the intent
	 * @return the notification
	 */
	@SuppressWarnings("deprecation")
	private Notification createNotification(Intent intent) {
//		ComponentName componentName = new ComponentName(getContext(),
//				SplashActivity.class);
//		Bundle bundle = Util.getActivityMetaDataBundle(getContext()
//				.getPackageManager(), componentName);
//		NotificationManager notiManager = (NotificationManager) context
//				.getSystemService(Context.NOTIFICATION_SERVICE);
//		String tickerText = bundle
//				.getString(SplashActivity.AUTO_UPATE_NOTIFICATION);
//		String title = bundle.getString(SplashActivity.AUTO_UPATE_TITLE);
//		String content = bundle.getString(SplashActivity.AUTO_UPATE_CONTENT);
//		// 创建通知对象,并设置区显示的图片,显示的信息
		Notification notification = new Notification(R.drawable.ic_launcher,
				"", System.currentTimeMillis());
//		// 设置通知来时的铃声为默认
//		notification.defaults = Notification.DEFAULT_SOUND;
//		// 创建PendingIntent的对象
//		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//				intent, 0);
//		notification.contentIntent = contentIntent;
//		notification.setLatestEventInfo(context, title, content, contentIntent);
//		// 发送通知
//		notiManager.notify(NOTIFICATON_UPDATE_FLAG, notification);
		return notification;
	}

	/**
	 * Gets the context.
	 * 
	 * @return the context
	 */
	public Activity getContext() {
		return context;
	}

	/**
	 * 获取手机可用存储空间大小(手机ROM).
	 * 
	 * @return 单位KB
	 */
	private long getAvailRomMemory() {
		File path = context.getFilesDir();
		StatFs sf = new StatFs(path.getParent());
		long availBlock = sf.getAvailableBlocks();
		long blockSize = sf.getBlockSize();
		return availBlock * blockSize / 1024;
	}

	/**
	 * 获取手机内存卡可用空间.
	 * 
	 * @return 可用空间单位KB
	 */
	private long getAvailSdCardMemory() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long availBlock = sf.getAvailableBlocks();
			long blockSize = sf.getBlockSize();
			return availBlock * blockSize / 1024;
		}
		return 0;
	}
}
