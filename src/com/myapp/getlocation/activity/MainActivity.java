package com.myapp.getlocation.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.capricorn.ArcMenu;
import com.capricorn.RayMenu;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.View.InsertScenicPointLayout;
import com.myapp.getlocation.View.ScenePointListView;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.entity.ScenicModel;
import com.myapp.getlocation.entity.ScenicSpotModel;
import com.myapp.getlocation.util.ScenicDataInitHelper;

public class MainActivity extends Activity {

	private static final int[] ITEM_DRAWABLES = {
		R.drawable.composer_place, R.drawable.composer_thought, R.drawable.composer_with };
	
	private ImageView imgLoc;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private boolean isFirstLoc = true;// 
	private boolean locByHand = false;
	private Marker mMarker;
	private InfoWindow mInfoWindow;
	
	private ScenicDataInitHelper dataInitHelper;
	private ArrayList<ScenicSpotModel> listScenicPoints;
	private ArrayList<ScenicModel> listScenics;
	//
	BitmapDescriptor bdLocation = BitmapDescriptorFactory
			.fromResource(R.drawable.location);
	//
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	
	private Dao<ScenicSpotModel, Integer> dao;
	private Dao<ScenicModel, Integer> daoScenics;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		listScenicPoints = new ArrayList<ScenicSpotModel>();
		listScenics = new ArrayList<ScenicModel>();
		dataInitHelper = new ScenicDataInitHelper(MainActivity.this);
		dataInitHelper.setListScenicPoints(listScenicPoints);
		dataInitHelper.setListScenics(listScenics);
		dataInitHelper.onCreate();

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
						Toast.makeText(MainActivity.this, "收集路段内的点", Toast.LENGTH_SHORT).show();
					} else if(position == 1) {
						Toast.makeText(MainActivity.this, "提交已收集的景点", Toast.LENGTH_SHORT).show();
					} else if(position == 2) {
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
		mBaiduMap.setMyLocationEnabled(true);
		//
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);//
		option.setCoorType("bd09ll"); //
		option.setScanSpan(10000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		imgLoc = (ImageView)findViewById(R.id.img_location);
		imgLoc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				locByHand = true;
				mLocClient.requestLocation();
				
			}
		});
		
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
				
				InsertScenicPointLayout layout = new InsertScenicPointLayout(MainActivity.this);
				LatLng ll = marker.getPosition();
				layout.setLatLng(ll);
				layout.setmBaiduMap(mBaiduMap);
				if(daoScenics != null) {
					layout.setDaoScenics(daoScenics);
					layout.setDao(dao);
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
		//
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}
	
	private void functionList(int position) {
		if(position == 0) {
			String[] items = {"1-秦皇宫至明城陵","2-明城陵至三仙观"};
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("收集路段内含点列表")
			.setMessage("开始收集某路段的构成点")
			.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
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
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("已采集点数据库列表提交")
			.setView(new ScenePointListView(MainActivity.this, listScenicPoints))
			.setPositiveButton("提交", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();
			alertDialog.show();
		} else if(position ==2) {
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("路段内点数据提交")
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();
			alertDialog.show();
		}
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
	
	private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(itemDrawables[i]);

            final int position = i;
            menu.addItem(item, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
//			Toast.makeText(MainActivity.this, "" + location.getAltitude() + ","
//					+ location.getLatitude(), Toast.LENGTH_SHORT)
//				.show();
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					//
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				
//				Toast.makeText(MainActivity.this, "" + listScenics.size() + ","
//						+ location.getLatitude(), Toast.LENGTH_SHORT).show();
 				if(listScenics.size() > 0) {
					final String[] items = new String[listScenics.size()];
					for(ScenicModel each:listScenics) {
						items[listScenics.indexOf(each)] = each.getScenicName();
					}
					Dialog dialogChooseArea = new AlertDialog.Builder(MainActivity.this)
					.setTitle("请选择景区")
					.setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Toast.makeText(MainActivity.this, which+ "," + listScenics.get(which).getScenicId(), Toast.LENGTH_SHORT).show();
							dataInitHelper.initSpotAndLine(listScenics.get(which).getScenicId());
						}
					})
					.create();
					dialogChooseArea.show();
					//此标志在if进入之后的一开始就赋值为false，但是放在此处保证获取数据之后才置为false
					isFirstLoc = false;
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
			
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	public Dao<ScenicSpotModel, Integer> getDao() {
		return dao;
	}

	public void setDao(Dao<ScenicSpotModel, Integer> dao) {
		this.dao = dao;
	}

	public Dao<ScenicModel, Integer> getDaoScenics() {
		return daoScenics;
	}

	public void setDaoScenics(Dao<ScenicModel, Integer> daoScenics) {
		this.daoScenics = daoScenics;
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

}
