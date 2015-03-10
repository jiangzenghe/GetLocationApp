package com.myapp.getlocation.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.View;

import com.myapp.getlocation.util.StringUtils;
/**
 * 一个类型化的列表适配器接口实现类，实现通过类处理方式进行控件与实体类的绑定
 * 
 * @param <T> 对象类型
 * 
 */
public class ClassAttachmentImpl<T> implements IAttachment<T> {

	protected Map<Key, ViewBinder> map;
	
	public ClassAttachmentImpl(){
		map = new HashMap<ClassAttachmentImpl.Key, ViewBinder>();
	}
	@Override
	public void attachToView(Context context, View view, T bean) {
		Set<Key> setKeys = map.keySet();
		for (Key key : setKeys) {
			int resId = key.getViewResId();
			View v = view.findViewById(resId);
			
			ViewBinder viewBinderEntity;
			try {
				viewBinderEntity = (ViewBinder)(map.get(key)).clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e.getMessage());
			}
			
			viewBinderEntity.setViewValue(v,bean,key.getProperty());

		}
	}
	
	/** 添加一个绑定
	 * @param viewResId 绑定的资源id
	 * @param property 绑定的实体属性名
	 * @param t 绑定器类
	 */
	public void addBinderMapItem(int viewResId, String property, ViewBinder viewBinder){
		Key key = new Key();
		key.setViewResId(viewResId);
		key.setProperty(property);
		map.put(key, viewBinder);
	}
	
	/** 移除一个绑定
	 * @param viewResId 绑定的资源id
	 * @param property 绑定的实体属性名
	 */
	public void removeBinderMapItem(int viewResId, String property){
		Key key = new Key();
		key.setViewResId(viewResId);
		key.setProperty(property);
		
		map.remove(key);
	}
	
	static protected final class Key {
		private Integer viewResId;
		private String property;
		
		public Integer getViewResId() {
			return viewResId;
		}
		public void setViewResId(Integer viewResId) {
			this.viewResId = viewResId;
		}
		public String getProperty() {
			return property;
		}
		public void setProperty(String property) {
			this.property = property;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			
			if (!(o instanceof Key)) {
				return false;
			}
			
			Key key = (Key)o;
			return this.viewResId.intValue() == key.viewResId.intValue()
					&& StringUtils.isEquals(this.property, key.property);
		}
		@Override
		public int hashCode() {
			int code = 17;
			
			code += 37 * viewResId.intValue();
			
			if (property != null) {
				code += 37 * property.hashCode();
			}
			
			return code;
		}
	}

}
