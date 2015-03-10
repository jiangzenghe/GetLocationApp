package com.myapp.getlocation.application;

import android.content.Context;

/**
 * 应用初始化链式接口，这个链接口中每一个实现必须通过getPrevious()方法返回链上的前一个节点对象。
 * doProcess()方法是一个链节点的入口，用于执行对应用特定资源、组件的初始化操作。
 * 
 */

public interface ApplicationInitializationChain {
	/**
	 * 进入这个链节点的入口，并且传入应用类对象与链上的前一个节点对象。实现类应在这个方法中完成应用特定资源、组件的初始化工作。
	 * @param application 应用类对象。
	 * @param previous 链上前一个节点对象，getPrevious()方法返回值应等于这个参数。
	 */
	public void doProcess(Context context);
	/**
	 * 返回当前链节点的前一个节点对象。
	 * @return
	 */
	public ApplicationInitializationChain getPrevious();
	/**
	 * 设置当前应用初始化环节的前一个节点对象。
	 * @param previous
	 */
	public void setPrevious(ApplicationInitializationChain previous);
	/**
	 * 获取当前应用初始化环节的下一个环节对象。
	 * @return
	 */
	public ApplicationInitializationChain getNext();
	/**
	 * 设置当前环节的一下环节对象。
	 * @param next
	 */
	public void setNext(ApplicationInitializationChain next);
}
