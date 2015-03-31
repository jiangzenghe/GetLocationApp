package com.myapp.getlocation.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.haiyisoft.mobile.android.Application;
import com.haiyisoft.mobile.android.R;
import com.haiyisoft.mobile.android.activity.Activity;
import com.haiyisoft.mobile.android.activity.UpdateActivity;
import com.haiyisoft.mobile.android.common.AutoUpdateMode;
import com.haiyisoft.mobile.android.common.MobileOSType;
import com.haiyisoft.mobile.android.common.VersionInfo;
import com.haiyisoft.mobile.android.common.VersionResult;
import com.haiyisoft.mobile.android.http.DefaultJsonHttpService;
import com.haiyisoft.mobile.android.http.FileDownloadServiceHandler;
import com.haiyisoft.mobile.android.http.HttpServiceHandler;
import com.haiyisoft.mobile.android.http.JsonHttpServiceHandler;
import com.haiyisoft.mobile.android.progress.FileDownloadProgressDialog;
import com.haiyisoft.mobile.android.update.util.PatchTool;
import com.haiyisoft.mobile.android.util.FileIntentFactory;
import com.haiyisoft.mobile.android.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class.
 * 
 * @author DingBaoSheng
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

	public AutoInit(Activity context) {
		this.context = context;
		if (mediator == null) {
			mediator = new UpdateHandler(context);
		}
	}

	/**
	 * 查询最新版本返回的结果.
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
	private class AutoUpdateServiceHandler implements
			JsonHttpServiceHandler<VersionResult> {

		/** The auto update. */
		private AutoInit autoUpdate;

		/**
		 * Instantiates a new auto update service handle.
		 * 
		 * @param autoUpdate
		 *            the auto update
		 */
		public AutoUpdateServiceHandler(AutoInit autoUpdate) {
			this.autoUpdate = autoUpdate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.haiyisoft.mobile.android.http.JsonHttpServiceHandler#
		 * onServiceFinished(java.lang.Object)
		 */
		@Override
		public void onServiceFinished(VersionResult result) {
			// phoneID: 0--未找到记录
			if (result == null || result.getPhoneId() == 0) {
				return;
			}
			String updateTag = result.getForceUpdate();
			topVersion = result.getTopVersion();
			if (updateTag == null) {
				Toast.makeText(autoUpdate.context,
						R.string.auto_update_null_server_return_update_flag,
						Toast.LENGTH_SHORT).show();
				return;
			}
			Intent intent = new Intent(autoUpdate.context, UpdateActivity.class);
			intent.putExtra(MOBILE_INFO_KEY, result);
			// 强制更新
			if (AutoUpdateMode.FORCE_UPDATE.equals(updateTag)) {
				autoUpdate.context.startActivity(intent);
			} else if (AutoUpdateMode.NON_FORCE_UPDATE.equals(updateTag)) {
				// 非强制性更新
				autoUpdate.createNotification(intent);
			} else {
				Toast.makeText(autoUpdate.context,
						R.string.auto_update_error_server_return_update_flag,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.haiyisoft.mobile.android.http.JsonHttpServiceHandler#onServiceError
		 * (java.lang.Exception)
		 */
		@Override
		public void onServiceError(Exception e) {
			if (e != null) {
				Log.e(TAG, e.getMessage(), e);
			}
			Toast.makeText(autoUpdate.context,
					R.string.auto_update_error_query_server_version,
					Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * Update.检测版本更新地址是否配置，若已配置，调用queryServerVersion方法进行版本升级检测
	 */

	public void start() {
		ComponentName componentName = new ComponentName(getContext(),
				UpdateActivity.class);
		Bundle bundle = Util.getActivityMetaDataBundle(getContext()
				.getPackageManager(), componentName);
		String queryVersionURL = bundle
				.getString(UpdateActivity.UPDATE_QUERY_SERVER_VERSION_URL);
		if (!TextUtils.isEmpty(queryVersionURL)) {
			queryServerVersion(queryVersionURL);
		}
	}

	/**
	 * 读取服务，获取服务端有关客户程序最新版本的信息，并且与当前程序版本比较，提示用户下载文件.
	 */
	private void queryServerVersion(String queryServerURL) {
		VersionInfo versionInfo = new VersionInfo();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		versionInfo.setMobileOSType(MobileOSType.ANDROID);
		versionInfo.setAppVersion(readCurrentVersion());
		versionInfo.setOsVersion(Build.VERSION.SDK_INT);
		if (TextUtils.isEmpty(queryServerURL)) {
			return;
		}
		try {
			context.getJsonHttpService().callPostService(queryServerURL,
					versionInfo, VersionResult.class,
					new AutoUpdateServiceHandler(this), gsonBuilder.create());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage(), e);
			Toast.makeText(context,
					R.string.auto_update_error_query_server_version,
					Toast.LENGTH_SHORT).show();
			return;
		}
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
		ComponentName componentName = new ComponentName(getContext(),
				UpdateActivity.class);
		Bundle bundle = Util.getActivityMetaDataBundle(getContext()
				.getPackageManager(), componentName);
		NotificationManager notiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		String tickerText = bundle
				.getString(UpdateActivity.AUTO_UPATE_NOTIFICATION);
		String title = bundle.getString(UpdateActivity.AUTO_UPATE_TITLE);
		String content = bundle.getString(UpdateActivity.AUTO_UPATE_CONTENT);
		// 创建通知对象,并设置区显示的图片,显示的信息
		Notification notification = new Notification(R.drawable.ic_emaf,
				tickerText, System.currentTimeMillis());
		// 设置通知来时的铃声为默认
		notification.defaults = Notification.DEFAULT_SOUND;
		// 创建PendingIntent的对象
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		notification.contentIntent = contentIntent;
		notification.setLatestEventInfo(context, title, content, contentIntent);
		// 发送通知
		notiManager.notify(NOTIFICATON_UPDATE_FLAG, notification);
		return notification;
	}

	/**
	 * 生成下载任务，开始下文件.
	 * 
	 * @param mobileInfo
	 *            the mobile info
	 */
	public void downloadFile(final VersionResult mobileInfo,
			String downloadURL, String saveFileName) {
		File saveFile = null;
		// 如果手机安装了sdcard
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 判断sdcard是没有足够空间
			if (getAvailSdCardMemory() < MEMORY_SPACE_SIZE_MIN_LIMIT) {
				// 检测手机内存是否能够有足够空间
				if (getAvailRomMemory() < MEMORY_SPACE_SIZE_MIN_LIMIT) {
					Toast.makeText(getContext(),
							R.string.auto_update_phone_space_not_enough,
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (createTempFile(saveFileName)) {
					saveFile = context.getFileStreamPath(saveFileName);
				}
			}
			saveFile = new File(Environment.getExternalStorageDirectory(),
					saveFileName);
		} else {
			// 没有安装sdcard且手机可用ROM不足
			if (getAvailRomMemory() < MEMORY_SPACE_SIZE_MIN_LIMIT) {
				Toast.makeText(getContext(),
						R.string.auto_update_not_install_sdcard,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (createTempFile(saveFileName)) {
				saveFile = context.getFileStreamPath(saveFileName);
			}
		}
		startDownloadFile(saveFile, mobileInfo, downloadURL);
	}

	/**
	 * 根据从服务端返回的id值去请求服务
	 * 
	 * @param file
	 *            the file
	 * @param mobileInfo
	 *            the mobile info
	 */
	private void startDownloadFile(File file, VersionResult mobileInfo,
			String downloadURL) {
		HttpServiceHandler handler = null;
		if (AutoUpdateMode.FORCE_UPDATE.equals(mobileInfo.getForceUpdate())) {
			// 创建强制下载Handler
			FileDownloadProgressDialog fdlpd = new FileDownloadProgressDialog(
					context);
			handler = new FileDownServiceHandler(file, fdlpd);
		} else {
			// 创建非强制行下载Handler
			NoticationBarProgressHandler nbph = new NoticationBarProgressHandler(
					context);
			handler = new FileDownServiceHandler(file, nbph);
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MOBILE_PHONE_ID, mobileInfo.getPhoneId());

		// 获取配置的更新类型，判断是何种更新，
		ComponentName componentName = new ComponentName(getContext(),
				UpdateActivity.class);
		Bundle bundle = Util.getActivityMetaDataBundle(getContext()
				.getPackageManager(), componentName);
		if (bundle != null && bundle.containsKey(UpdateActivity.UPATE_TYPE)) {
			updateType = bundle.getString(UpdateActivity.UPATE_TYPE);// 如果配置了该属性，则根据配置选择全量或增量
		}

		if (AutoUpdateMode.UPDATE_TYPE_DIFF.equals(updateType)) {
			// 为增量更新
			paramMap.put(MOBILE_PHONE_UPDATE_TYPE,
					AutoUpdateMode.UPDATE_TYPE_DIFF);// 服务端接收增量更新 标志
			paramMap.put(MOBILE_PHONE_APP_VERSION, readCurrentVersion());// 服务端接收客户端版本号
																			// 标志
		} else {
			// 为全量更新
			updateType = AutoUpdateMode.UPDATE_TYPE_ALL;
			paramMap.put(MOBILE_PHONE_UPDATE_TYPE,
					AutoUpdateMode.UPDATE_TYPE_ALL);// 服务端接收全量更新 标志
		}

		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		DefaultJsonHttpService downloadFileService = new DefaultJsonHttpService();

		try {
			downloadFileService.callGetService(downloadURL, paramMap, handler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			Toast.makeText(context,
					R.string.auto_update_error_client_server_protocol,
					Toast.LENGTH_SHORT).show();
			return;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			Toast.makeText(context, R.string.auto_udpate_fail_network_connect,
					Toast.LENGTH_SHORT).show();
			return;
		}
	}

	/**
	 * 该类主要对下载后文件进行安装操作
	 * 
	 * @author DingBaoSheng
	 * 
	 *         <p>
	 *         Modification History:
	 *         </p>
	 *         <p>
	 *         Date Author Description
	 *         </p>
	 *         <p>
	 *         ----------------------------------------------------------------
	 *         --
	 *         </p>
	 *         <p>
	 *         2013.4.3 DingBaoSheng 新建
	 *         </p>
	 *         <p>
	 *         </p>
	 */
	private class FileDownServiceHandler extends FileDownloadServiceHandler {
		/**
		 * Instantiates a new file down service handler.
		 * 
		 * @param file
		 *            the file
		 * @param handler
		 *            the handler
		 */
		public FileDownServiceHandler(File file, Handler handler) {
			super(file, handler);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.haiyisoft.mobile.android.http.FileDownloadServiceHandler#
		 * onFileDonwloadFinish(java.io.File)
		 */
		@Override
		protected void onFileDonwloadFinish(File file) {
			// 安装新的程序
			if (file != null) {
				if (AutoUpdateMode.UPDATE_TYPE_ALL.equals(updateType)) {
					// 全量更新，
					FileIntentFactory factory = FileIntentFactory.getInstance();
					Intent intent = factory.create(file);
					if (intent != null) {
						context.startActivity(intent);
						Application app = (Application) getContext()
								.getApplication();
						app.exit();
						return;
					}

				} else if (AutoUpdateMode.UPDATE_TYPE_DIFF.equals(updateType)) {
					// 增量更新
					String sdcard = Environment.getExternalStorageDirectory()
							.getAbsolutePath();
					String old = readCurrentVersionPath();// 旧版本的安装路径
					String copyOldPath = sdcard + "oldApp.apk";
					copyAppFile(old, copyOldPath);
					String n = sdcard + "/newAppV" + topVersion + ".apk";// 合成的新版本的路径
					String patch = file.getAbsolutePath();// 下载的patch文件路径
					PatchTool tool = new PatchTool();
					tool.applyPatch(copyOldPath, n, patch);

					File f = new File(n);
					if (f.exists()) {

						Intent intent = FileIntentFactory.getInstance().create(
								file);
						if (intent != null) {
							context.startActivity(intent);
							Application app = (Application) getContext()
									.getApplication();
							app.exit();
						}
						return;
					}
				} else {
					Toast.makeText(context,
							R.string.auto_update_fail_find_intent,
							Toast.LENGTH_SHORT).show();
				}
			}
			Toast.makeText(context, R.string.auto_update_null_file,
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 从data/app系统路径下拷贝出旧版本的安装文件，放在指定的目录下
	 * 
	 * @param path
	 *            旧版本的apk路径
	 * @return copyOldPath 拷贝后的文件放置路径
	 */
	public void copyAppFile(String path, String copyOldPath) {
		File file = new File(path);
		File file2 = new File(copyOldPath);
		FileInputStream in;
		FileOutputStream out;
		try {
			in = new FileInputStream(file);
			out = new FileOutputStream(file2);
			byte[] buff = new byte[512];
			int n = 0;
			while ((n = in.read(buff)) != -1) {
				out.write(buff, 0, n);
			}
			out.flush();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the temp file.
	 * 
	 * @param fileName
	 *            the file name
	 * @return true, if successful
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
	private boolean createTempFile(String fileName) {
		FileOutputStream stream = null;
		try {
			stream = context.openFileOutput(fileName,
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			return true;
		} catch (FileNotFoundException e1) {
			Toast.makeText(getContext(),
					R.string.auto_update_fail_create_temp_file,
					Toast.LENGTH_SHORT).show();
			return false;
		} finally {
			try {
				stream.close();
			} catch (IOException e1) {
			}
		}
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
