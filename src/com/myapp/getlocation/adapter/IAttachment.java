package com.myapp.getlocation.adapter;
/**
 * 一个类型化的列表适配器接口
 * 
 * @param <T> 对象类型
 * 
 */
import android.content.Context;
import android.view.View;

public interface IAttachment<T> {
	public void attachToView(Context context, View view, T bean);
}
