package com.myapp.getlocation.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 * appConfig.xml解析类
 *
 */
public class AppConfigParser implements IConfigParser {

	@Override
	public AppInitConfig parse(InputStream is) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();  //取得SAXParserFactory实例  
        SAXParser parser = factory.newSAXParser();                  //从factory获取SAXParser实例  
        AppInitConfigHandler handler = new AppInitConfigHandler();                        //实例化自定义Handler  
        parser.parse(is, handler);                                  //根据自定义Handler规则解析输入流  
        return handler.getAppConfig();  
	}

	@Override
	public String serialize(AppInitConfig appInitConfig) throws Exception {
		return null;
	}
	
	private class AppInitConfigHandler extends DefaultHandler{

		private AppInitConfig appConfig;
		
		private Map<String, String> mapJsonAdapterConfigs;

		private List<String> listSqliteConfigs;
		
		public AppInitConfig getAppConfig() {
			return appConfig;
		}
		
		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			appConfig = new AppInitConfig();
			mapJsonAdapterConfigs = new HashMap<String, String>();
			listSqliteConfigs = new ArrayList<String>();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if("json-adapter".equals(localName)){
				mapJsonAdapterConfigs.put(attributes.getValue("class"), attributes.getValue("adapter"));
			}else if("entity".equals(localName)){
				listSqliteConfigs.add(attributes.getValue("class"));
			}
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
			appConfig.setMapAdapterConfigs(mapJsonAdapterConfigs);
			appConfig.setListSqliteConfigs(listSqliteConfigs);
		}
		
		
		
	}

}
