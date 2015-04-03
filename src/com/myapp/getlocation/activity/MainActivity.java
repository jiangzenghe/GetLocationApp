package com.myapp.getlocation.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.baidu.mapapi.map.DotOptions;
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
import com.capricorn.RayMenu;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.Constants;
import com.myapp.getlocation.R;
import com.myapp.getlocation.View.InsertScenicPointLayout;
import com.myapp.getlocation.View.ScenicPointListView;
import com.myapp.getlocation.View.ScenicSectionPointView;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.db.ScenicDataInitHelper;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.ScenicLineSectionModel;
import com.myapp.getlocation.entity.ScenicModel;
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
	
	private SectionPointsModel insertSection;
	//
	BitmapDescriptor bdLocation = BitmapDescriptorFactory
			.fromResource(R.drawable.location);
	//
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	
	private Dao<SpotPointsModel, Integer> daoSpotPoints;
	private Dao<SectionPointsModel, Integer> daoSectionPoints;
	private ArrayList<ScenicModel> listScenics;
	private ScenicDataInitHelper dataInitHelper;
	
	private Points initPoint;//动态线的第一个点
	private ProgressDialog defaultDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
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
		dataInitHelper.onCreate();
		try {
			daoSpotPoints = getEntityHelper().getDao(SpotPointsModel.class);
			daoSectionPoints = getEntityHelper().getDao(SectionPointsModel.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		listScenics = dataInitHelper.searchScenicsData();
		
		Log.e("Main oncreate", "Main oncreate");
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
				Toast.makeText(MainActivity.this, "添加景点数据", Toast.LENGTH_SHORT).show();
				
				InsertScenicPointLayout layout = new InsertScenicPointLayout(MainActivity.this);
				LatLng ll = marker.getPosition();
				layout.setLatLng(ll);
				layout.setmBaiduMap(mBaiduMap);
				if(daoSpotPoints != null) {
					layout.setDaoSpotPoints(daoSpotPoints);
					layout.setDaoSpot(dataInitHelper.getDaoSpot());
				}
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
			final ArrayList<ScenicLineSectionModel> listPoints = dataInitHelper.searchSectionsData();
			if(listPoints.size() != 0) {
				final String[] items = new String[listPoints.size()];
				for(ScenicLineSectionModel each:listPoints) {
					
					items[listPoints.indexOf(each)] = each.getScenicLinename()+":"+each.getAspotName()+"-"+each.getBspotName();
					//遍历以判断是否已采集 用文字来描述
					try {
						List<SectionPointsModel> tempModel=daoSectionPoints.queryForEq("linesectionId", each.getLinesectionId());
						if(tempModel.size() > 0) {
							items[listPoints.indexOf(each)] += "  ------  已采集";
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("收集路段内含点列表")
				.setItems(items, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						locFollow = true;
						initDynamicLine(listPoints.get(which).getAspotId(), 
								listPoints.get(which).getBspotId());
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
			if(daoSpotPoints != null) {
				CloseableIterator<SpotPointsModel> iterator = daoSpotPoints.iterator();
				
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
					defaultDialog = new ProgressDialog(MainActivity.this);
					defaultDialog.setMessage("提交数据中");
					defaultDialog.show();
					new SubmitDataTask().execute(listSpotPoints);
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
					defaultDialog = new ProgressDialog(MainActivity.this);
					defaultDialog.setMessage("等待中");
					defaultDialog.show();
					new SubmitLineTask().execute(listSectionPoints);
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
	
	class SubmitDataTask extends AsyncTask<ArrayList<SpotPointsModel>, Void, Integer> {
		private ArrayList<SpotPointsModel> listSpotPoints;

		protected Integer doInBackground(ArrayList<SpotPointsModel>... data) {
			try {
				listSpotPoints = data[0];
				Application app = (Application)MainActivity.this.getApplication();
				String baseAdd = app.getMetaDataString("framework.config.service.base.address", "");
				baseAdd = "http://www.imyuu.com:8080/";
				int result = HttpUtil.postListByRestTemplate(
						baseAdd + Constants.API_SPOT_SUBMIT, listSpotPoints);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		protected void onPostExecute(Integer result) {
			// TODO: check this.exception
			Log.i("doInBackground", "execute result is:" + result);
			if(defaultDialog != null) {
				defaultDialog.cancel();
			}
			if (result == 0) {
				Toast.makeText(MainActivity.this, "提交出错", Toast.LENGTH_SHORT)
						.show();
			} else {
				if (daoSpotPoints != null) {
					try {
						for (int i = 0; i < listSpotPoints.size(); i++) {
							listSpotPoints.get(i).setSubmited(true);
							daoSpotPoints.createOrUpdate(listSpotPoints.get(i));
						}
						Toast.makeText(MainActivity.this, "提交成功",
								Toast.LENGTH_SHORT).show();
					} catch (SQLException e) {
						Toast.makeText(MainActivity.this, "提交成功,修改提交状态出错",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	class SubmitLineTask extends AsyncTask<ArrayList<SectionPointsModel>, Void, Integer> {
		private int result;
		private ArrayList<SectionPointsModel> listSectionPoints = null;

		protected Integer doInBackground(ArrayList<SectionPointsModel>... data) {
			try {
				listSectionPoints = data[0];
				Application app = (Application)MainActivity.this.getApplication();
				String baseAdd = app.getMetaDataString("framework.config.service.base.address", "");
				baseAdd = "http://www.imyuu.com:8080/";
				result = HttpUtil.postByRestTemplate(
						baseAdd + Constants.API_SECTION_SUBMIT, listSectionPoints);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		protected void onPostExecute(Integer feed) {
			// TODO: check this.exception
			Log.i("doInBackground", "execute result is:" + feed);
			if(defaultDialog != null) {
				defaultDialog.cancel();
			}
			if (result == 0) {
				Toast.makeText(MainActivity.this, "提交出错", Toast.LENGTH_SHORT)
						.show();
			} else {
				if (daoSectionPoints != null) {
					try {
						for (int i = 0; i < listSectionPoints.size(); i++) {
							listSectionPoints.get(i).setSubmited(true);
							daoSectionPoints.createOrUpdate(listSectionPoints
									.get(i));
						}
						Toast.makeText(MainActivity.this, "提交成功",
								Toast.LENGTH_SHORT).show();
					} catch (SQLException e) {
						Toast.makeText(MainActivity.this, "提交成功,修改提交状态出错",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
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
 					if(lists.size() > 0 ) {
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
				drawDynamicLine(point);
				
				insertSection.getSectionPoints().add(point);
				insertSection.setPointsNum(insertSection.getPointsNum() + 1);
			}
			
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void initDynamicLine(String startSpotId, String endSpotId) {
		initPoint = new Points(0.0, 0.0, 0.0);
		MapStatusUpdate arg0 = MapStatusUpdateFactory.zoomTo(18);
		mBaiduMap.setMapStatus(arg0);
		mBaiduMap.clear();
		
		try {
			List<SpotPointsModel> tempModel = daoSpotPoints.queryForEq("spotId", startSpotId);
			//起点画好
			if(tempModel.size()>0) {
				drawSinglePoint(tempModel.get(0).getSpotPoints());
			}
			tempModel = daoSpotPoints.queryForEq("spotId", endSpotId);
			//始点画好
			if(tempModel.size()>0) {
				drawSinglePoint(tempModel.get(0).getSpotPoints());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void drawSinglePoint(ArrayList<Points> points) {
		for(Points each : points) {
			LatLng point = new LatLng(each.getAbsoluteLatitude(), each.getAbsoluteLongitude());
			//构建用户绘制多边形的Option对象  
			OverlayOptions polygonOption = new DotOptions()  
			.center(point)
			.color(Color.parseColor("#FFF000"));
			//在地图上添加Option，用于显示  
			mBaiduMap.addOverlay(polygonOption);
		}
	}
	
	private void drawDynamicLine(Points point) {
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
