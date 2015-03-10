package com.myapp.getlocation.adapter;

import android.view.View;
/**
 * 视图组件绑定器接口，这个接口用于定义将一个视图组件与一个对象的属性绑定在一起。
 *
 */
public interface ViewBinder extends Cloneable {
	/**
	 * 绑定一个JavaBean对象的属性到一个指定的视图组件上.
	 * @param view 视图组件
	 * @param bean JavaBean对象
	 * @param property JavaBean对象的属性名
	 */
	public void setViewValue(View view, Object bean, String property);
	public Object clone() throws CloneNotSupportedException; 
}
