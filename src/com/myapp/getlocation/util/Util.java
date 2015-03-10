package com.myapp.getlocation.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * 该类是个工具类，提供一些常用的方法.
 *
 */
@SuppressLint("DefaultLocale")
public class Util {
	
	/** The DE f_ pi. */
	static double DEF_PI = 3.14159265359; // PI
	
	/** The DE f_2 pi. */
	static double DEF_2PI = 6.28318530712; // 2*PI
	
	/** The DE f_ p i180. */
	static double DEF_PI180 = 0.01745329252; // PI/180.0
	
	/** The DE f_ r. */
	static double DEF_R = 6370693.5; // radius of earth

	// 检测字符串是否为空或无内容
	/**
	 * Checks if is null or empty.
	 *
	 * @param srcString the src string
	 * @return true, if is null or empty
	 */
	public static boolean isNullOrEmpty(String srcString) {
		if (srcString == null || ("").equals(srcString)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Find field.
	 *
	 * @param clazz the clazz
	 * @param annotation the annotation
	 * @return the list
	 */
	public static List<Field> findField(Class<?> clazz,
			Class<? extends Annotation> annotation) {
		List<Field> list = new ArrayList<Field>();
		if (clazz.getName().equals(Object.class.getName())) {
			return null;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(annotation)) {
				list.add(field);
			}
		}
		List<Field> supFields = findField(clazz.getSuperclass(), annotation);
		if (supFields != null) {
			for (Field field : supFields) {
				list.add(field);
			}

		}
		return list;
	}
	/**
	 * 比较两个对象是否相等，如果left = null 且  right=null 则返回true
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean isEquals(Object left, Object right) {
		if (left == null && right == null) {
			return true;
		}
		
		if (left != null && right != null) {
			return left.equals(right);
		}
		else {
			return false;
		}
	}

	/**
	 * 设置指定的bean对象的指定propertyName属性的值。.
	 *
	 * @param bean bean对象
	 * @param propertyName propertyName属性名
	 * @param value 属性值
	 * @throws SecurityException the security exception
	 * @throws NoSuchMethodException the no such method exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void setBeanPropertyValue(Object bean, String propertyName,
			Object value) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method setter = getSetterMethod(bean.getClass(), propertyName,
				value.getClass());
		setter.invoke(bean, value);
	}

	/**
	 * 获取指定clazz类型propertyName参数指定的属性的设置器函数对象。.
	 *
	 * @param clazz bean类型对象
	 * @param propertyName bean类型对象的属性名
	 * @param type 设置器方法参数类型
	 * @return the setter method
	 * @throws SecurityException the security exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	public static Method getSetterMethod(Class<?> clazz, String propertyName,
			Class<?> type) throws SecurityException, NoSuchMethodException {
		String firstChar = propertyName.substring(0, 1).toUpperCase();
		String setterName = "set" + firstChar + propertyName.substring(1);

		try {
			return clazz.getMethod(setterName, type);
		} catch (NoSuchMethodException e) {
			if (type.isPrimitive()) {
				return clazz.getMethod(setterName, getWrapper(type));
			} else {
				type = getPrimitive(type);
				if (type != null) {
					return clazz.getMethod(setterName, type);
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 * 返回指定包装类型的原始类型对象。.
	 *
	 * @param type 包装类型
	 * @return 原始类型对象，如果参数type不是原始类型的包装类则返回空引用
	 */
	public static Class<?> getPrimitive(Class<?> type) {
		if (Byte.class.equals(type)) {
			return byte.class;
		} else if (Short.class.equals(type)) {
			return short.class;
		} else if (Integer.class.equals(type)) {
			return int.class;
		} else if (Long.class.equals(type)) {
			return long.class;
		} else if (Character.class.equals(type)) {
			return char.class;
		} else if (Float.class.equals(type)) {
			return float.class;
		} else if (Double.class.equals(type)) {
			return double.class;
		} else if (Boolean.class.equals(type)) {
			return boolean.class;
		}
		return null;
	}

	/**
	 * 返回指定原始类型的包装类型。.
	 *
	 * @param type 原始类型
	 * @return 包装类型对象，如果参数type不是原始类型则返回空引用
	 */
	public static Class<?> getWrapper(Class<?> type) {
		if (byte.class.equals(type)) {
			return Byte.class;
		} else if (short.class.equals(type)) {
			return Short.class;
		} else if (int.class.equals(type)) {
			return Integer.class;
		} else if (long.class.equals(type)) {
			return Long.class;
		} else if (char.class.equals(type)) {
			return Character.class;
		} else if (float.class.equals(type)) {
			return Float.class;
		} else if (double.class.equals(type)) {
			return Double.class;
		} else if (boolean.class.equals(type)) {
			return Boolean.class;
		}
		return null;
	}

	/**
	 * 返回指定bean参数指定的对象的参数propertyName指定的属性的值。.
	 *
	 * @param bean JavaBean对象引用
	 * @param propertyName 属性名
	 * @return the bean property value
	 * @throws SecurityException the security exception
	 * @throws NoSuchMethodException the no such method exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static Object getBeanPropertyValue(Object bean, String propertyName)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method getter = getGetterMethod(bean.getClass(), propertyName);
		if(getter == null){
			return null;
		}
		return getter.invoke(bean);
	}

	/**
	 * 获取clazz指定类型的propertyName属性值。.
	 *
	 * @param clazz the clazz
	 * @param propertyName the property name
	 * @return the getter method
	 * @throws SecurityException the security exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	public static Method getGetterMethod(Class<?> clazz, String propertyName)
			throws SecurityException, NoSuchMethodException {
		if(TextUtils.isEmpty(propertyName)){
			return null;
		}
		String firstChar = propertyName.substring(0, 1).toUpperCase();
		String getterName = "get" + firstChar + propertyName.substring(1);
		Method getter = null;
		try {
			return clazz.getMethod(getterName);
		} catch (NoSuchMethodException e) {

		}
		getterName = "is" + firstChar + propertyName.substring(1);
		getter = clazz.getMethod(getterName);

		return getter;
	}

	/**
	 * 获取Activity中的meta-data.
	 *
	 * @param packageManager 包管理器
	 * @param component the component
	 * @return the activity meta data bundle
	 */
	public static Bundle getActivityMetaDataBundle(
			PackageManager packageManager, ComponentName component) {
		Bundle bundle = null;
		try {
			ActivityInfo ai = packageManager.getActivityInfo(component,
					PackageManager.GET_META_DATA);
			bundle = ai.metaData;
		} catch (NameNotFoundException e) {
			Log.e("getMetaDataBundle", e.getMessage(), e);
		}

		return bundle;
	}

	/**
	 * 获取Service中的meta-data.
	 *
	 * @param packageManager 包管理器
	 * @param component the component
	 * @return the service meta data bundle
	 */
	public static Bundle getServiceMetaDataBundle(
			PackageManager packageManager, ComponentName component) {
		Bundle bundle = null;
		try {
			ServiceInfo si = packageManager.getServiceInfo(component,
					PackageManager.GET_META_DATA);
			bundle = si.metaData;
		} catch (NameNotFoundException e) {
			Log.e("getMetaDataBundle", e.getMessage(), e);
		}

		return bundle;
	}
	public static Bundle getReceiverMetaDataBundle(PackageManager packageManager, ComponentName component) {
		Bundle bundle = null;
		ActivityInfo ai;
		try {
			ai = packageManager.getReceiverInfo(component, PackageManager.GET_META_DATA);
			bundle = ai.metaData;
		} catch (NameNotFoundException e) {
			Log.e("getReceiverMetaDataBundle", e.getMessage(), e);
		}
		return bundle;
	}

	/**
	 * 获取Application中的meta-data.
	 *
	 * @param packageManager 包管理器
	 * @param packageName the package name
	 * @return the app meta data bundle
	 */
	public static Bundle getAppMetaDataBundle(PackageManager packageManager,
			String packageName) {
		Bundle bundle = null;
		try {
			ApplicationInfo ai = packageManager.getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			bundle = ai.metaData;
		} catch (NameNotFoundException e) {
			Log.e("getMetaDataBundle", e.getMessage(), e);
		}

		return bundle;
	}

	/**
	 * 计算两点短距离.
	 *
	 * @param lon1 第一点的经度
	 * @param lat1 第一点的纬度
	 * @param lon2 第二点的经度
	 * @param lat2 第二点的纬度
	 * @return the double
	 */
	public static double GetShortDistance(double lon1, double lat1, double lon2,
			double lat2) {
		double ew1, ns1, ew2, ns2;
		double dx, dy, dew;
		double distance;
		// 角度转换为弧度
		ew1 = lon1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lon2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 经度差
		dew = ew1 - ew2;
		// 若跨东经和西经180 度，进行调整
		if (dew > DEF_PI)
			dew = DEF_2PI - dew;
		else if (dew < -DEF_PI)
			dew = DEF_2PI + dew;
		dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
		dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
		// 勾股定理求斜边长
		distance = Math.sqrt(dx * dx + dy * dy);
		return distance;
	}
	
	/**
	 * 计算两点长距离.
	 *
	 * @param lon1 第一点的经度
	 * @param lat1 第一点的纬度
	 * @param lon2 第二点的经度
	 * @param lat2 第二点的纬度
	 * @return the double
	 */
	public static double getLongDistance(double lon1, double lat1, double lon2,
			double lat2) {
		double ew1, ns1, ew2, ns2;
		double distance;
		// 角度转换为弧度
		ew1 = lon1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lon2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 求大圆劣弧与球心所夹的角(弧度)
		distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1)
				* Math.cos(ns2) * Math.cos(ew1 - ew2);
		// 调整到[-1..1]范围内，避免溢出
		if (distance > 1.0)
			distance = 1.0;
		else if (distance < -1.0)
			distance = -1.0;
		// 求大圆劣弧长度
		distance = DEF_R * Math.acos(distance);
		return distance;
	}
	
	/**
	 * dp转像素.
	 *
	 * @param context 上下文
	 * @param dp 输入的dp值
	 * @return 转换为像素
	 */
	public static int Dp2Px(Context context, float dp) {  
	    final float scale = context.getResources().getDisplayMetrics().density;  
	    return (int) (dp * scale + 0.5f);  
	}  
	   
	/**
	 * 像素转dp.
	 *
	 * @param context 上下文
	 * @param px 输入的像素值
	 * @return 转换为dp
	 */
	public static int Px2Dp(Context context, float px) {  
	    final float scale = context.getResources().getDisplayMetrics().density;  
	    return (int) (px / scale + 0.5f);  
	} 
	
	/**
	 * 比较两个日期大小.
	 *
	 * @param 第一个日期
	 * @param 第二个日期
	 * @return 返回0代表两个日期在年月日相等，返回1代表date1>date2.返回-1代表date1<date2
	 */
	@SuppressLint("SimpleDateFormat")
	public static int compare_date(Date date1, Date date2) {
		int year_c = 0;
		int month_c = 0;
		int day_c = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		String date11 = sdf.format(date1); // 当期日期
		year_c = Integer.parseInt(date11.split("-")[0]);
		month_c = Integer.parseInt(date11.split("-")[1]);
		day_c = Integer.parseInt(date11.split("-")[2]);
		
		int year_s = 0;
		int month_s = 0;
		int day_s = 0;
		String date22 = sdf.format(date2); // 当期日期
		year_s = Integer.parseInt(date22.split("-")[0]);
		month_s = Integer.parseInt(date22.split("-")[1]);
		day_s = Integer.parseInt(date22.split("-")[2]);
		//首先比较年
		//年相等，再比较月
		//年月相等，再比较日
		if(year_c>year_s){
			return 1;
		}else if(year_c==year_s && month_c>month_s){
			return 1;
		}else if (year_c==year_s && month_c==month_s && day_c>day_s) {
			return 1;
		}else if(year_c<year_s){
			return -1;
		}else if(year_c==year_s && month_c<month_s){
			return -1;
		}else if (year_c==year_s && month_c==month_s && day_c<day_s) {
			return -1;
		}else {
			return 0;
		}
	}
	/**
	 * 简单判断当前是否存在可用的活动网络。
	 * @param context
	 * @return
	 */
	public static boolean isNetworkActivity(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null;
	}
	
	public static void mkdir(File file) {
		if (file.exists()) {
			return ;
		}
		
		if (!file.getParentFile().exists()) {
			mkdir(file.getParentFile());
		}
		file.mkdir();
	}
	
	/**
	 * 简单判断当前是否存在可用的wifi。
	 * @param context
	 * @return
	 */
	public static boolean isWifiActive(Context icontext) {
		Context context = icontext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info;
		if (connectivity != null) {
			info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
							&& info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
