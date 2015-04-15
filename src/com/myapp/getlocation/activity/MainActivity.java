package com.myapp.getlocation.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
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
import com.myapp.getlocation.entity.ScenicModel;
import com.myapp.getlocation.entity.SectionPointsModel;
import com.myapp.getlocation.entity.SpotPointsModel;
import com.myapp.getlocation.util.DrawToolUtil;
import com.myapp.getlocation.util.HttpUtil;

public class MainActivity extends Activity {

	private static final int[] ITEM_DRAWABLES = {
		R.drawable.composer_place, R.drawable.composer_thought, R.drawable.composer_with
		,R.drawable.composer_line};
	
	private ImageView imgLoc;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private boolean isFirstLoc = true;//
	private boolean locByHand = false;
	private boolean locFollow = false;
	private Marker mMarker;
	private InfoWindow mInfoWindow;
	
	private String scenicId="";
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
	
	private Points initPointAll;//动态线的第一个点
	private ProgressDialog defaultDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
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
		option.setScanSpan(3000);
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
					layout.searchSpotType();
				}
				mInfoWindow = new InfoWindow(layout, ll, 0);
				mBaiduMap.showInfoWindow(mInfoWindow);
				return true;
			}
		});
	}

	private void initRayMenu() {
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
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
							return;
						}
						
						Toast.makeText(MainActivity.this, "收集路段内的点", Toast.LENGTH_SHORT).show();
					} else if(position == 1) {
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
						}
						Toast.makeText(MainActivity.this, "提交已收集的景点", Toast.LENGTH_SHORT).show();
					} else if(position == 2) {
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
						}
						Toast.makeText(MainActivity.this, "提交已收集的路段", Toast.LENGTH_SHORT).show();
					} else if(position == 3) {
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
						}
						Toast.makeText(MainActivity.this, "绘制已收集的路段", Toast.LENGTH_SHORT).show();
					}
					functionList(position);
				}
			});// Add a menu item
		}
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
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("收集路段内含点列表")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					locFollow = true;
//					initDynamicLine(listPoints.get(which).getAspotId(), 
//							listPoints.get(which).getBspotId());
					insertSection = new SectionPointsModel();
//					insertSection.setScenicLinename(listPoints.get(which).getScenicLinename());
					insertSection.setScenicId(scenicId);
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
			
		} else if(position ==1) {
			final ArrayList<SpotPointsModel> listSpotPoints = new ArrayList<SpotPointsModel>();
			if(daoSpotPoints != null) {
				CloseableIterator<SpotPointsModel> iterator = daoSpotPoints.iterator();
				
				while (iterator.hasNext()) {
					SpotPointsModel entity = iterator.next();
					listSpotPoints.add(entity);
				}
			}
			if(listSpotPoints.size() == 0) {
				Toast.makeText(MainActivity.this, "没有收集景点数据", Toast.LENGTH_SHORT).show();
				return;
			}
			ScenicPointListView scenicPointsView = new ScenicPointListView(MainActivity.this, mBaiduMap, listSpotPoints);
			final Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("已采集点数据库列表提交")
			.setView(scenicPointsView)
			.setPositiveButton("提交", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<SpotPointsModel> submitSpotPoints = new ArrayList<SpotPointsModel>();
					for(SpotPointsModel each:listSpotPoints) {
						if(!each.isSubmited()) {
							submitSpotPoints.add(each);
						}
					}
					if(submitSpotPoints.size() == 0) {
						Toast.makeText(MainActivity.this, "没有需要提交的景点数据", Toast.LENGTH_SHORT).show();
						return;
					}
					defaultDialog = new ProgressDialog(MainActivity.this);
					defaultDialog.setMessage("提交数据中");
					defaultDialog.show();
					new SubmitDataTask().execute(submitSpotPoints);
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
			if(listSectionPoints.size() == 0) {
				Toast.makeText(MainActivity.this, "没有收集路段数据", Toast.LENGTH_SHORT).show();
				return;
			}
			ScenicSectionPointView sectionPointsView = new ScenicSectionPointView(MainActivity.this, mBaiduMap, listSectionPoints);
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("已采集路段内点数据提交")
			.setView(sectionPointsView)
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<SectionPointsModel> submitSectionPoints = new ArrayList<SectionPointsModel>();
					for(SectionPointsModel each:listSectionPoints) {
						if(!each.isSubmited()) {
							submitSectionPoints.add(each);
						}
					}
					if(submitSectionPoints.size() == 0) {
						Toast.makeText(MainActivity.this, "没有需要提交的路段数据", Toast.LENGTH_SHORT).show();
						return;
					}
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
		} else if(position == 3) {
			HashMap<String, String> map = searchLine();
			if(!map.isEmpty()) {
				final String[] items = new String[map.size()];
				final String[] itemsValue = new String[map.size()];
				int i = 0;
				for(String key: map.keySet()) {
					items[i] = key;
					itemsValue[i] = map.get(key);
					i++;
				}
				Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("绘制路段列表")
				.setItems(itemsValue, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							List<SectionPointsModel> tempModel = new ArrayList<SectionPointsModel>();
							if(items[which].equals("0")) {
								tempModel=daoSectionPoints.queryForEq("scenicLineId", null);
							} else {
								tempModel=daoSectionPoints.queryForEq("scenicLineId", items[which]);
							}
							if(tempModel.size() > 0) {
								for(SectionPointsModel each:tempModel) {
									if(!items[which].equals("0")) {
//										initDynamicLine(each.getAspotId(), each.getBspotId());
									}
									if(each.getSectionPoints().size()>1) {
										Points initPoint = null;
										for(Points eachPoints:each.getSectionPoints()) {
											DrawToolUtil.drawDynamicLine(initPoint, eachPoints, mBaiduMap);
											initPoint = new Points(eachPoints.getAbsoluteLongitude(), 
													eachPoints.getAbsoluteLatitude(), eachPoints.getAbsoluteAltitude());
										}
									}
								}
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
		}
	}
	
	//寻找Line和未加入任何Line的Section
	public HashMap<String, String> searchLine() {
		HashMap<String, String> result = new HashMap<String, String>();
		if (daoSectionPoints != null) {
			CloseableIterator<SectionPointsModel> iterator = daoSectionPoints.iterator();
			
			while (iterator.hasNext()) {
				SectionPointsModel entity = iterator.next();
				if(entity.getScenicLineId() == null) {
					result.put("0", "其他");
				} else if(!result.containsKey(entity.getScenicLineId())) {
					result.put(entity.getScenicLineId(), entity.getScenicLinename());
				}

			}
		}
		return result;
	}
	
	class SubmitDataTask extends AsyncTask<ArrayList<SpotPointsModel>, Void, Integer> {
		private ArrayList<SpotPointsModel> listSpotPoints;

		protected Integer doInBackground(ArrayList<SpotPointsModel>... data) {
			try {
				listSpotPoints = data[0];
				Application app = (Application)MainActivity.this.getApplication();
				String baseAdd = app.getMetaDataString("framework.config.service.base.address", "");
//				baseAdd = "http://www.imyuu.com:8080/";
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
//				baseAdd = "http://www.imyuu.com:8080/";
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
								scenicId = lists.get(which).getScenicId();
								dataInitHelper.initSpotAndLine(lists.get(which).getScenicId());
								initRayMenu();
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
				if (longitude< 0.00001 || latitude <0.00001
						|| longitude==0.0 || latitude==0.0 ) return;
				Points point = new Points(longitude, latitude, altitude);
				DrawToolUtil.drawDynamicLine(initPointAll ,point, mBaiduMap);
				initPointAll = new Points(longitude, latitude, altitude);
				
				insertSection.getSectionPoints().add(point);
				insertSection.setPointsNum(insertSection.getPointsNum() + 1);
			}
			
		}

		public void onReceivePoi(BDLocation poiLocation) {
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
			if(insertSection.getSectionPoints()==null || insertSection.getSectionPoints().size()<2) {
				Toast.makeText(MainActivity.this, "未采集到路线数据", Toast.LENGTH_SHORT).show();
			}
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
