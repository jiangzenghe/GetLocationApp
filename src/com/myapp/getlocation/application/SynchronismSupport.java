package com.myapp.getlocation.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;

/**
 * 应用同步网络调用请求支持，这个类调用的代码在android4.0以下的系统上可以实现同步网络请求，在android4.1以上不能同步调用网络请求。
 * 
 */
public class SynchronismSupport extends ApplicationInitializationChainSupport {

	@SuppressLint("NewApi")
	@Override
	public void doProcess(Context context) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk > 8) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
																			// 就包括了磁盘读写和网络I/O
					.penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
					.penaltyLog() // 打印logcat
					.penaltyDeath().build());
		}
		doNext(context);
	}

}
