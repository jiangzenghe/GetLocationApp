package com.myapp.getlocation.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.app.Activity;

/**
 * Activity管理组件，记录处于未关闭状态的Activity页面。
 * 1.记录打开的Activity
 * 2.该实例只提供单实例
 * 3.提供可以关闭全部记录的Activity的方法
 */
public class ActivitysManager {
	
	private static ActivitysManager instance = null;// 单实例对象
	
	private List<Activity> activityList;// 保存打开的Activity的列表
	
	private ActivitysManager(){
		activityList = new ArrayList<Activity>();
	}
	
	/**
	 * 获取ActivitysManager的单实例.
	 *
	 * @return single instance of ActivitysManager
	 */
	public static ActivitysManager getInstance(){
		if (instance == null) {
			instance = new ActivitysManager();
		}
		return instance;
	}
	
	/**
	 * 添加一个Activity
	 *
	 * @param activity the activity
	 */
	public void addActivity(Activity activity){
		activityList.add(activity);
	}
	
	/**
	 * 移除 activiyt.
	 *
	 * @param activity the activity
	 */
	public void removeActiviyt(Activity activity) {
		activityList.remove(activity);
	}
	
	/**
	 * 关闭所有的Activity
	 */
	public void finishAll(){
		Iterator<Activity> iter = activityList.iterator();
		while(iter.hasNext()){
			Activity activity = iter.next();
			iter.remove();
			activity.finish();
		}
	}

	public List<Activity> getActivityList() {
		return activityList;
	}


}
