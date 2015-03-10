package com.myapp.getlocation.util;

import java.util.List;
import java.util.Map;
/**
 * appConfig.xml对应的实体类
 *
 */
public class AppInitConfig {
	private Map<String, String> mapAdapterConfigs;
	
	private List<String> listSqliteConfigs;

	public Map<String, String> getMapAdapterConfigs() {
		return mapAdapterConfigs;
	}

	public void setMapAdapterConfigs(Map<String, String> mapAdapterConfigs) {
		this.mapAdapterConfigs = mapAdapterConfigs;
	}

	public List<String> getListSqliteConfigs() {
		return listSqliteConfigs;
	}

	public void setListSqliteConfigs(List<String> listSqliteConfigs) {
		this.listSqliteConfigs = listSqliteConfigs;
	}
}
