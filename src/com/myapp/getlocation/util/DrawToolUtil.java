package com.myapp.getlocation.util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.myapp.getlocation.entity.Points;



/**
 */
public class DrawToolUtil {

	private BaiduMap mBaiduMap;
	
	public static void drawDynamicLineName(ArrayList<Points> points, String name, BaiduMap mBaiduMap) {
		int index = points.size()/2 + 1;
		Points point = points.get(index);
		LatLng pointLabel = new LatLng(point.getAbsoluteLatitude(), 
				point.getAbsoluteLongitude());
		OverlayOptions textOption = new TextOptions()  
	    .bgColor(0xAAFFFF00)  
	    .fontSize(24)  
	    .fontColor(0xFFFF00FF)  
	    .text(name)
	    .position(pointLabel);  
		//在地图上添加该文字对象并显示  
		mBaiduMap.addOverlay(textOption);
	}
	
	public static void drawDynamicLine(ArrayList<Points> points, BaiduMap mBaiduMap) {
		List<LatLng> pts = new ArrayList<LatLng>();
		
		for(Points each : points) {//俩个点一样，就看不到线
			LatLng point = new LatLng(each.getAbsoluteLatitude(), each.getAbsoluteLongitude());//test
			if(point.latitude != 0 && point.longitude != 0) {
				pts.add(point);
			}
		}
		
		if(pts.size() >=2 && pts.size() <10000) {
			//构建用于绘制多边形的Option对象  
			OverlayOptions polygonOption = new PolylineOptions()  
			.width(8)
			.color(0xAAFF0000)
			.points(pts);
			//在地图上添加Option，用于显示  
			mBaiduMap.addOverlay(polygonOption);
		}
	}
	
	public static void drawDynamicLine(Points start,Points end, BaiduMap mBaiduMap) {
		if(start == null) {
			return;
		}
		List<LatLng> pts = new ArrayList<LatLng>(); 
		LatLng arg0 = new LatLng(start.getAbsoluteLatitude(), 
				start.getAbsoluteLongitude());
		LatLng arg1 = new LatLng(end.getAbsoluteLatitude(), 
				end.getAbsoluteLongitude());
		pts.add(arg0);
		pts.add(arg1);
		//构建用于绘制多边形的Option对象  
		if(pts.size() >=2) {
			OverlayOptions polygonOption = new PolylineOptions()  
			.width(8)
			.color(0xAAFF0000)
			.points(pts);
			//在地图上添加Option，用于显示  
			mBaiduMap.addOverlay(polygonOption);
		}
	}
	
	public static void drawSinglePoint(Points point, String name, BaiduMap mBaiduMap) {
		LatLng pointLabel = new LatLng(point.getAbsoluteLatitude(), 
				point.getAbsoluteLongitude());
		//构建用于绘制point的Option对象  
		OverlayOptions dotOption = new DotOptions()  
		.center(pointLabel)
		.color(0xAAFF0000);
//		.color(Color.parseColor("#FFF000"));
		
		//在地图上添加Option，用于显示  
		mBaiduMap.addOverlay(dotOption);
//		OverlayOptions textOption = new TextOptions()  
//		.bgColor(0xAAFFFF00) 
//		.fontSize(24)  
//		.fontColor(0xFFFF00FF)  
//		.text(name)
//		.zIndex(9)
//		.position(pointLabel);  
//		//在地图上添加该文字对象并显示  
//		mBaiduMap.addOverlay(textOption);
	}
   
	public static void drawSinglePoint(ArrayList<Points> points, String name, BaiduMap mBaiduMap) {
		double sumLatitude = 0.0;
		double sumLongitude = 0.0;
		if(points.size() > 0) {
			for(Points each : points) {
				sumLatitude += each.getAbsoluteLatitude();
				sumLongitude += each.getAbsoluteLongitude();
			}
			LatLng pointLabel = new LatLng(sumLatitude/points.size(), 
					sumLongitude/points.size());
			OverlayOptions textOption = new TextOptions()  
		    .bgColor(0xAAFFFF00)  
		    .fontSize(24)  
		    .fontColor(0xFFFF00FF)  
		    .text(name)
		    .position(pointLabel);  
			//在地图上添加该文字对象并显示  
			mBaiduMap.addOverlay(textOption);
			LatLng point = new LatLng(sumLatitude/points.size(), 
					sumLongitude/points.size());
			//构建用于绘制point的Option对象  
			OverlayOptions dotOption = new DotOptions()  
			.center(point)
			.color(Color.parseColor("#FFF000"));
			//在地图上添加Option，用于显示  
			mBaiduMap.addOverlay(dotOption);
		}
	}
	
//	public void initDynamicLine(String startSpotId, String endSpotId) {
//		MapStatusUpdate arg0 = MapStatusUpdateFactory.zoomTo(18);
//		mBaiduMap.setMapStatus(arg0);
//		mBaiduMap.clear();
//		
//		try {
//			List<SpotPointsModel> tempModel = daoSpotPoints.queryForEq("spotId", startSpotId);
//			//起点画好
//			if(tempModel.size()>0) {
//				drawSinglePoint(tempModel.get(0).getSpotPoints(),
//						tempModel.get(0).getScenicspotName());
//			}
//			tempModel = daoSpotPoints.queryForEq("spotId", endSpotId);
//			//始点画好
//			if(tempModel.size()>0) {
//				drawSinglePoint(tempModel.get(0).getSpotPoints(),
//						tempModel.get(0).getScenicspotName());
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
}
