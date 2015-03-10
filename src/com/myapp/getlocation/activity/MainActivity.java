package com.myapp.getlocation.activity;

import java.sql.SQLException;
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
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.View.InsertScenicPointLayout;
import com.myapp.getlocation.View.ScenePointListView;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.entity.ScenicPointModel;

public class MainActivity extends Activity {

	private static final int[] ITEM_DRAWABLES = { R.drawable.composer_camera, R.drawable.composer_music,
		R.drawable.composer_place, R.drawable.composer_sleep, R.drawable.composer_thought, R.drawable.composer_with };
	
	private ImageView imgLoc;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private boolean isFirstLoc = true;// 
	private boolean locByHand = false;
	private Marker mMarker;
	private InfoWindow mInfoWindow;
	private ArrayList<ScenicPointModel> listScenicPoints;
	//
	BitmapDescriptor bdLocation = BitmapDescriptorFactory
			.fromResource(R.drawable.composer_place);
	//
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	
	private Dao<ScenicPointModel, Integer> dao;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		ArcMenu arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
//        ArcMenu arcMenu2 = (ArcMenu) findViewById(R.id.arc_menu_2);

//        initArcMenu(arcMenu, ITEM_DRAWABLES);
//        initArcMenu(arcMenu2, ITEM_DRAWABLES);
		try {
			dao = getEntityHelper().getDao(ScenicPointModel.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		listScenicPoints = new ArrayList<ScenicPointModel>();
		initData();

		RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);
        final int itemCount = ITEM_DRAWABLES.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(this);
			item.setImageResource(ITEM_DRAWABLES[i]);

			final int position = i;
			rayMenu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(MainActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
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
				
				InsertScenicPointLayout layout = new InsertScenicPointLayout(getApplicationContext());
				LatLng ll = marker.getPosition();
				layout.setLatLng(ll);
				layout.setmBaiduMap(mBaiduMap);
				if(dao != null) {
					layout.setDao(dao);
				}
//				OnInfoWindowClickListener listener = null;
//				listener = new OnInfoWindowClickListener() {
//					public void onInfoWindowClick() {
//						mBaiduMap.hideInfoWindow();
//					}
//				};
				mInfoWindow = new InfoWindow(layout, ll, -47);
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
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("线内含点列表收集")
			.setView(new ScenePointListView(MainActivity.this, listScenicPoints))
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
		} else if(position ==1) {
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("点数据库列表提交")
			.setView(new ScenePointListView(MainActivity.this, listScenicPoints))
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
		} else if(position ==2) {
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("线数据库列表提交")
			.setView(new ScenePointListView(MainActivity.this, listScenicPoints))
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
	
	private void initData() {
		listScenicPoints.clear();
		if (dao != null) {
			CloseableIterator<ScenicPointModel> iterator = dao.iterator();
			
			while (iterator.hasNext()) {
				ScenicPointModel entity = iterator.next();

				listScenicPoints.add(entity);

			}
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
				.zIndex(9).draggable(true);
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
			Toast.makeText(MainActivity.this, "" + location.getAltitude() + ","
					+ location.getLatitude(), Toast.LENGTH_SHORT)
				.show();
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					//
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
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

}
