package com.myapp.getlocation.entity;

public class ServiceState {
	public static String OK="Success";
	private Integer stateCode;
	private String serviceMsg;
	public ServiceState(Integer code, String msg) {
		stateCode = code;
		serviceMsg = msg;
	}
	public ServiceState() {

	}
	public Integer getStateCode() {
		return stateCode;
	}
	public void setStateCode(Integer stateCode) {
		this.stateCode = stateCode;
	}
	public String getServiceMsg() {
		return serviceMsg;
	}
	public void setServiceMsg(String serviceMsg) {
		this.serviceMsg = serviceMsg;
	}
	@Override
	public String toString()
	{
		return "servicestate:"+this.stateCode+" msg:"+this.serviceMsg;
	}
}
