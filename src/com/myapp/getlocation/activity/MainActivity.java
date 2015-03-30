package com.myapp.getlocation.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.capricorn.ArcMenu;
import com.capricorn.RayMenu;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.Constants;
import com.myapp.getlocation.R;
import com.myapp.getlocation.View.InsertScenicPointLayout;
import com.myapp.getlocation.View.ScenicPointListView;
import com.myapp.getlocation.View.ScenicSectionPointView;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.db.ScenicDataInitHelper;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.ScenicLineSectionModel;
import com.myapp.getlocation.entity.ScenicModel;
import com.myapp.getlocation.entity.ScenicSpotModel;
import com.myapp.getlocation.entity.SectionPointsModel;
import com.myapp.getlocation.entity.SpotPointsModel;
import com.myapp.getlocation.util.HttpUtil;

public class MainActivity extends Activity {

	private static final int[] ITEM_DRAWABLES = {
		R.drawable.composer_place, R.drawable.composer_thought, R.drawable.composer_with };
	
	private ImageView imgLoc;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private boolean isFirstLoc = true;// 
	private boolean locByHand = false;
	private boolean locFollow = false;
	private Marker mMarker;
	private InfoWindow mInfoWindow;
	
	private ScenicDataInitHelper dataInitHelper;
	private ArrayList<ScenicSpotModel> listScenicPoints;
	private ArrayList<ScenicModel> listScenics;
	private SectionPointsModel insertSection;
	//
	BitmapDescriptor bdLocation = BitmapDescriptorFactory
			.fromResource(R.drawable.location);
	//
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	
	private Dao<ScenicModel, Integer> daoScenics;
	private Dao<ScenicSpotModel, Integer> daoSpot;
	private Dao<ScenicLineSectionModel, Integer> daoSection;
	private Dao<SpotPointsModel, Integer> daoPoints;
	private Dao<SectionPointsModel, Integer> daoSectionPoints;
	
	private Points initPoint;//动态线的第一个点
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		listScenicPoints = new ArrayList<ScenicSpotModel>();
		listScenics = new ArrayList<ScenicModel>();
		
		RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);
        final int itemCount = ITEM_DRAWABLES.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(this);
			item.setImageResource(ITEM_DRAWABLES[i]);

			final int position = i;
			rayMenu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(position == 0) {
						locFollow = false;
						mBaiduMap.setMyLocationEnabled(false);
						imgLoc.setImageResource(R.drawable.main_location);
						insertSectionData();
						Toast.makeText(MainActivity.this, "收集路段内的点", Toast.LENGTH_SHORT).show();
					} else if(position == 1) {
						locFollow = false;
						mBaiduMap.setMyLocationEnabled(false);
						imgLoc.setImageResource(R.drawable.main_location);
						insertSectionData();
						Toast.makeText(MainActivity.this, "提交已收集的景点", Toast.LENGTH_SHORT).show();
					} else if(position == 2) {
						locFollow = false;
						mBaiduMap.setMyLocationEnabled(false);
						imgLoc.setImageResource(R.drawable.main_location);
						insertSectionData();
						Toast.makeText(MainActivity.this, "提交已收集的路段", Toast.LENGTH_SHORT).show();
					}
					functionList(position);
				}
			});// Add a menu item
		}
		
		//
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		//
		mBaiduMap.setMyLocationEnabled(false);
		mBaiduMap.getUiSettings().setCompassEnabled(false);
		mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false);
		mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(16);
		mBaiduMap.setMapStatus(u);
//		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
//				mCurrentMode, true, null));
		
		dataInitHelper = new ScenicDataInitHelper(MainActivity.this);
		dataInitHelper.setListScenicPoints(listScenicPoints);
		dataInitHelper.setListScenics(listScenics);
		dataInitHelper.onCreate();
		
		//
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);//
		option.setAddrType("all");
		option.setCoorType("bd09ll"); //
		option.setScanSpan(20000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		imgLoc = (ImageView)findViewById(R.id.img_location);
		imgLoc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(locFollow) {
					locFollow = false;
					mBaiduMap.setMyLocationEnabled(false);
					imgLoc.setImageResource(R.drawable.main_location);
					insertSectionData();
				} else {
					locByHand = true;
					mBaiduMap.clear();
					mLocClient.requestLocation();
				}
				
			}
		});
		
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
				
				InsertScenicPointLayout layout = new InsertScenicPointLayout(MainActivity.this);
				LatLng ll = marker.getPosition();
				layout.setLatLng(ll);
				layout.setmBaiduMap(mBaiduMap);
				if(daoSpot != null && daoPoints != null) {
					layout.setDao(daoSpot);
					layout.setDaoPoints(daoPoints);
				}
				layout.setListScenicPoints(listScenicPoints);
				mInfoWindow = new InfoWindow(layout, ll, 0);
				mBaiduMap.showInfoWindow(mInfoWindow);
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		mMapView.onPause();
		locFollow = false;
		mBaiduMap.setMyLocationEnabled(false);
		imgLoc.setImageResource(R.drawable.main_location);
		insertSectionData();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		//
		mLocClient.stop();
		locFollow = false;
		//
		mBaiduMap.setMyLocationEnabled(false);
		insertSectionData();
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}
	
	private void functionList(int position) {
		if(position == 0) {
			final ArrayList<ScenicLineSectionModel> listPoints = new ArrayList<ScenicLineSectionModel>();
			if(daoSection != null) {
				CloseableIterator<ScenicLineSectionModel> iterator = daoSection.iterator();
				
				while (iterator.hasNext()) {
					ScenicLineSectionModel entity = iterator.next();
					listPoints.add(entity);
				}
				final String[] items = new String[listPoints.size()];
				for(ScenicLineSectionModel each:listPoints) {
					items[listPoints.indexOf(each)] = each.getScenicLinename()+":"+each.getAspotName()+"-"+each.getBspotName();
				}
				Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("收集路段内含点列表")
				.setItems(items, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						locFollow = true;
						InitDynamicLine();
						insertSection = new SectionPointsModel();
						insertSection.setAspotId(listPoints.get(which).getAspotId());
						insertSection.setBspotId(listPoints.get(which).getBspotId());
						insertSection.setAspotName(listPoints.get(which).getAspotName());
						insertSection.setBspotName(listPoints.get(which).getBspotName());
						insertSection.setLinesectionId(listPoints.get(which).getLinesectionId());
						insertSection.setScenicLineId(listPoints.get(which).getScenicLineId());
						insertSection.setScenicLinename(listPoints.get(which).getScenicLinename());
						insertSection.setRouteOrder(listPoints.get(which).getRouteOrder());
						insertSection.setScenicId(listPoints.get(which).getScenicId());
						ArrayList<Points> points = new ArrayList<Points>();
						insertSection.setSectionPoints(points);
						insertSection.setPointsNum(0);
						mBaiduMap.setMyLocationEnabled(true);
						mLocClient.requestLocation();
						imgLoc.setImageResource(R.drawable.pause);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
				alertDialog.show();
			} else {
				Toast.makeText(MainActivity.this, "无路段数据", Toast.LENGTH_SHORT).show();
			}
			
		} else if(position ==1) {
			final ArrayList<SpotPointsModel> listSpotPoints = new ArrayList<SpotPointsModel>();
			if(daoPoints != null) {
				CloseableIterator<SpotPointsModel> iterator = daoPoints.iterator();
				
				while (iterator.hasNext()) {
					SpotPointsModel entity = iterator.next();
					listSpotPoints.add(entity);
				}
			}
			ScenicPointListView scenicPointsView = new ScenicPointListView(MainActivity.this, mBaiduMap, listSpotPoints);
			final Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("已采集点数据库列表提交")
			.setView(scenicPointsView)
			.setPositiveButton("提交", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String data = converToSpotJson(listSpotPoints);
					if (data.equals("")) return;
					int result = HttpUtil.doPost(Constants.API_SPOT_SUBMIT, data);
					if(result == 0) {
						Toast.makeText(MainActivity.this, "提交出错", Toast.LENGTH_SHORT).show();
					} else {
						if(daoPoints != null) {
							try {
								for(int i=0;i<listSpotPoints.size();i++){
									listSpotPoints.get(i).setSubmited(true);
									daoPoints.createOrUpdate(listSpotPoints.get(i));
								}
								Toast.makeText(MainActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
							} catch (SQLException e) {
								Toast.makeText(MainActivity.this, "提交成功,修改提交状态出错",Toast.LENGTH_SHORT).show();
							}
						}
					}
					
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();
			scenicPointsView.setAlertDialog(alertDialog);
			alertDialog.show();
		} else if(position ==2) {
			final ArrayList<SectionPointsModel> listSectionPoints = new ArrayList<SectionPointsModel>();
			if(daoSectionPoints != null) {
				CloseableIterator<SectionPointsModel> iterator = daoSectionPoints.iterator();
				
				while (iterator.hasNext()) {
					SectionPointsModel entity = iterator.next();
					listSectionPoints.add(entity);
				}
			}
			ScenicSectionPointView sectionPointsView = new ScenicSectionPointView(MainActivity.this, mBaiduMap, listSectionPoints);
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("已采集路段内点数据提交")
			.setView(sectionPointsView)
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String data = converToSectionJson(listSectionPoints);
					if (data.equals("")) return;
					int result = HttpUtil.doPost(Constants.API_SECTION_SUBMIT, data);
					if(result == 0) {
						Toast.makeText(MainActivity.this, "提交出错", Toast.LENGTH_SHORT).show();
					} else {
						if(daoPoints != null) {
							try {
								for(int i=0;i<listSectionPoints.size();i++){
									listSectionPoints.get(i).setSubmited(true);
									daoSectionPoints.createOrUpdate(listSectionPoints.get(i));
								}
								Toast.makeText(MainActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
							} catch (SQLException e) {
								Toast.makeText(MainActivity.this, "提交成功,修改提交状态出错",Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();
			sectionPointsView.setAlertDialog(alertDialog);
			alertDialog.show();
		}
	}
	
	public String converToSpotJson(ArrayList<SpotPointsModel> list) { 
		JSONArray array = new JSONArray(); 
		for (SpotPointsModel spot : list) { 
			if(!spot.isSubmited()) {
				JSONObject obj = new JSONObject(); 
				try { 
					obj.put("scenicId", spot.getScenicId()); 
					obj.put("spotId", spot.getSpotId());
					obj.put("spotType", spot.getSpotType());
					JSONArray arrayPoints = new JSONArray(); 
					for (Points point : spot.getSpotPoints()) {
						JSONObject objPoint = new JSONObject(); 
						objPoint.put("logitude", point.getAbsoluteLongitude());
						objPoint.put("latitude", point.getAbsoluteLatitude());
						objPoint.put("altitude", point.getAbsoluteAltitude());
						arrayPoints.put(objPoint);
					}
					obj.put("spotPoints", arrayPoints);
				} catch (JSONException e) { // TODO Auto-generated catch block 
					e.printStackTrace(); 
				} 
				array.put(obj); 
			}
		} 
		System.out.println(array.toString()); 
		return array.toString(); 
	} 
	
	public String converToSectionJson(ArrayList<SectionPointsModel> list) { 
		JSONArray array = new JSONArray(); 
		for (SectionPointsModel section : list) { 
			if(!section.isSubmited()) {
				JSONObject obj = new JSONObject(); 
				try { 
					obj.put("scenicId", section.getScenicId()); 
					obj.put("scenicLineId", section.getScenicLineId());
					obj.put("linesectionId", section.getLinesectionId());
					obj.put("aspotId", section.getAspotId());
					obj.put("bspotId", section.getBspotId());
					JSONArray arrayPoints = new JSONArray(); 
					for (Points point : section.getSectionPoints()) {
						JSONObject objPoint = new JSONObject(); 
						objPoint.put("logitude", point.getAbsoluteLongitude());
						objPoint.put("latitude", point.getAbsoluteLatitude());
						objPoint.put("altitude", point.getAbsoluteAltitude());
						arrayPoints.put(objPoint);
					}
					obj.put("spotPoints", arrayPoints);
				} catch (JSONException e) { // TODO Auto-generated catch block 
					e.printStackTrace(); 
				} 
				array.put(obj); 
			}
		} 
		System.out.println(array.toString()); 
		return array.toString(); 
	} 
	
	/**
	 *　获取应用全局的实体处理器对象
	 * @return EntityHelper实体处理器对象
	 */
	public EntityHelper getEntityHelper() {
		Application app = (Application)getApplication();
		return app.getEntityHelper();
	}
	
	private void addOverlay(LatLng ll) {
		OverlayOptions oo = new MarkerOptions().position(ll).icon(bdLocation)
				.zIndex(9).draggable(false);
		mMarker = (Marker) (mBaiduMap.addOverlay(oo));
	}
	
	/**
	 * 
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					//
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude())
					.build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				
//				Toast.makeText(MainActivity.this, location.getAddrStr()+","
//						+location.getCity()+","+location.getDistrict(), Toast.LENGTH_SHORT).show();
				if(location.getCity() == null || location.getDistrict() == null) {
					return;
				}
				
 				if(listScenics.size() > 0) {
 					String city = location.getCity().substring(0, location.getCity().length()-1);
 					String district = location.getDistrict().substring(0, location.getCity().length()-1);
 					final ArrayList<ScenicModel> lists = searchEnableScenics(city, district);
 					if(lists.size() >0 ) {
						final String[] items = new String[lists.size()];
						for(ScenicModel each:lists) {
							items[lists.indexOf(each)] = each.getScenicName();
						}
						Dialog dialogChooseArea = new AlertDialog.Builder(MainActivity.this)
						.setTitle("请选择景区")
						.setItems(items, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dataInitHelper.initSpotAndLine(lists.get(which).getScenicId());
							}
						})
						.create();
						dialogChooseArea.show();
						//此标志在if进入之后的一开始就赋值为false，但是放在此处保证获取数据之后才置为false
						isFirstLoc = false;
 					}
				}
			}
			if(locByHand) {
				locByHand = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.setMapStatus(u);
				
				addOverlay(ll);
			}
			if(locFollow) {
				Toast.makeText(MainActivity.this, "数据："+ location.getLatitude()+","
						+ location.getLongitude(), Toast.LENGTH_SHORT).show();
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double altitude = location.getAltitude();
				Points point = new Points(longitude, latitude, altitude);
				DrawDynamicLine(point);
				
				insertSection.getSectionPoints().add(point);
				insertSection.setPointsNum(insertSection.getPointsNum() + 1);
			}
			
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void InitDynamicLine() {
		initPoint = new Points(0.0, 0.0, 0.0);
		MapStatusUpdate arg0 = MapStatusUpdateFactory.zoomTo(18);
		mBaiduMap.setMapStatus(arg0);
		mBaiduMap.clear();
	}
	
	private void DrawDynamicLine(Points point) {
		List<LatLng> pts = new ArrayList<LatLng>(); 
		LatLng arg0 = new LatLng(initPoint.getAbsoluteLatitude(), 
				initPoint.getAbsoluteLongitude());
		LatLng arg1 = new LatLng(point.getAbsoluteLatitude(), 
				point.getAbsoluteLongitude());
		if(arg0.latitude != 0 && arg0.longitude != 0
				&& arg1.latitude !=0 && arg1.longitude !=0) {
			pts.add(arg0);
		}
		if(arg1.latitude !=0 && arg1.longitude !=0) {
			pts.add(arg1);
			initPoint.setAbsoluteLatitude(point.getAbsoluteLatitude());
			initPoint.setAbsoluteLongitude(point.getAbsoluteLongitude());
		}
		//构建用户绘制多边形的Option对象  
		if(pts.size() >=2) {
			OverlayOptions polygonOption = new PolylineOptions()  
			.width(8)
			.color(0xAAFF0000)
			.points(pts);
			//在地图上添加Option，用于显示  
			mBaiduMap.addOverlay(polygonOption);
		}
	}
	
	private ArrayList<ScenicModel> searchEnableScenics(String city, String district) {
		ArrayList<ScenicModel> lists = new ArrayList<ScenicModel>();
		for(ScenicModel each:listScenics) {
			if(each.getScenicLocation().contains(district)) {
				lists.add(each);
			}
		}
		if(lists.size() == 0) {
			for(ScenicModel each:listScenics) {
				if(each.getScenicLocation().contains(city)) {
					lists.add(each);
				}
			}
		}
		return lists;
	}
	
	private void insertSectionData() {
		if(insertSection != null) {
			insertSection.setSubmited(false);
			try {
				boolean isHave=false;
				List<SectionPointsModel> tempModel=daoSectionPoints.queryForEq("linesectionId", insertSection.getLinesectionId());
				for(int i=0;i<tempModel.size();i++){
					if(tempModel.get(i).getLinesectionId().equals(insertSection.getLinesectionId()))
					{isHave=true;
					tempModel.get(i).setSectionPoints(insertSection.getSectionPoints());
					tempModel.get(i).setPointsNum(insertSection.getPointsNum());
					daoSectionPoints.createOrUpdate(tempModel.get(i));
					break;}
				}
				if(!isHave){
					daoSectionPoints.create(insertSection);
					Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(MainActivity.this, "已有该本地数据,修改成功",Toast.LENGTH_SHORT).show();
				}
				
			} catch (SQLException e) {
				Toast.makeText(MainActivity.this, "添加本地数据错误",Toast.LENGTH_SHORT).show();
			}
			insertSection = null;
		}

	}
	
	public Dao<ScenicSpotModel, Integer> getDaoSpot() {
		return daoSpot;
	}

	public void setDaoSpot(Dao<ScenicSpotModel, Integer> daoSpot) {
		this.daoSpot = daoSpot;
	}

	public Dao<ScenicModel, Integer> getDaoScenics() {
		return daoScenics;
	}

	public Dao<SpotPointsModel, Integer> getDaoPoints() {
		return daoPoints;
	}

	public void setDaoPoints(Dao<SpotPointsModel, Integer> daoPoints) {
		this.daoPoints = daoPoints;
	}

	public void setDaoScenics(Dao<ScenicModel, Integer> daoScenics) {
		this.daoScenics = daoScenics;
	}

	public Dao<SectionPointsModel, Integer> getDaoSectionPoints() {
		return daoSectionPoints;
	}

	public void setDaoSectionPoints(
			Dao<SectionPointsModel, Integer> daoSectionPoints) {
		this.daoSectionPoints = daoSectionPoints;
	}

	public Dao<ScenicLineSectionModel, Integer> getDaoSection() {
		return daoSection;
	}

	public void setDaoSection(Dao<ScenicLineSectionModel, Integer> daoSection) {
		this.daoSection = daoSection;
	}

	public ArrayList<ScenicSpotModel> getListScenicPoints() {
		return listScenicPoints;
	}

	public void setListScenicPoints(ArrayList<ScenicSpotModel> listScenicPoints) {
		this.listScenicPoints = listScenicPoints;
	}

	public ArrayList<ScenicModel> getListScenics() {
		return listScenics;
	}

	public void setListScenics(ArrayList<ScenicModel> listScenics) {
		this.listScenics = listScenics;
	}

	public MapView getmMapView() {
		return mMapView;
	}

	public void setmMapView(MapView mMapView) {
		this.mMapView = mMapView;
	}

	public BaiduMap getmBaiduMap() {
		return mBaiduMap;
	}

	public void setmBaiduMap(BaiduMap mBaiduMap) {
		this.mBaiduMap = mBaiduMap;
	}

}
