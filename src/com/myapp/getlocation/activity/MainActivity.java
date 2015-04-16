package com.myapp.getlocation.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
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
	private SoundPool soundPool;
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
//			daoSpotPoints = getEntityHelper().getDao(SpotPointsModel.class);
		daoSpotPoints = dataInitHelper.getDaoSpotPoints();
		daoSectionPoints = dataInitHelper.getDaoSectionPoints();
		listScenics = dataInitHelper.searchScenicsData();
		soundPool = new SoundPool(1, // maxStreams参数，该参数为设置同时能够播放多少音效
            AudioManager.STREAM_MUSIC, // streamType参数，该参数设置音频类型，在游戏中通常设置为：STREAM_MUSIC
            0 // srcQuality参数，该参数设置音频文件的质量，目前还没有效果，设置为0为默认值。
			);
		
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
		imgLoc.setVisibility(View.GONE);
		imgLoc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(locFollow) {
					locFollow = false;
					mBaiduMap.setMyLocationEnabled(false);
					mLocClient.stop();
					imgLoc.setImageResource(R.drawable.main_location);
					insertSectionData();
				} else {
					locByHand = true;
					mBaiduMap.clear();
					mLocClient.start();
					mLocClient.requestLocation();
				}
				
			}
		});
		
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Toast.makeText(MainActivity.this, "添加景点数据", Toast.LENGTH_SHORT).show();
				
				InsertScenicPointLayout layout = new InsertScenicPointLayout(MainActivity.this, scenicId);
				LatLng ll = marker.getPosition();
				layout.setLatLng(ll);
				layout.setmBaiduMap(mBaiduMap);
				if(daoSpotPoints != null) {
					layout.setDaoSpotPoints(daoSpotPoints);
					layout.searchSpotType();
				}
				mInfoWindow = new InfoWindow(layout, ll, 0);
				mBaiduMap.showInfoWindow(mInfoWindow);
				return true;
			}
		});
		
		scenicId = "221";
//		dataInitHelper.initSpotAndLine(lists.get(which).getScenicId());
		imgLoc.setVisibility(View.VISIBLE);
		initRayMenu();
		AudioManager am = (AudioManager) MainActivity.this
                .getSystemService(Context.AUDIO_SERVICE);// 实例化AudioManager对象
        float audioMaxVolumn = // 返回当前AudioManager对象的最大音量值
        		am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
        float audioCurrentVolumn = am // 返回当前AudioManager对象的音量值
                        .getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolumn / audioMaxVolumn;
		soundPool.play(soundPool.load(MainActivity.this, R.raw.dontpanic, 1), 
				volumnRatio, volumnRatio, 1, 0, 1);
		soundPool.release();
		mLocClient.start();
		mLocClient.requestLocation();
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
							mLocClient.stop();
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
							return;
						}
						
						Toast.makeText(MainActivity.this, "收集路段内的点", Toast.LENGTH_SHORT).show();
					} else if(position == 1) {
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							mLocClient.stop();
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
						}
						Toast.makeText(MainActivity.this, "提交已收集的景点", Toast.LENGTH_SHORT).show();
					} else if(position == 2) {
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							mLocClient.stop();
							imgLoc.setImageResource(R.drawable.main_location);
							insertSectionData();
						}
						Toast.makeText(MainActivity.this, "提交已收集的路段", Toast.LENGTH_SHORT).show();
					} else if(position == 3) {
						if(locFollow) {
							locFollow = false;
							mBaiduMap.setMyLocationEnabled(false);
							mLocClient.stop();
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
		mLocClient.stop();
		imgLoc.setImageResource(R.drawable.main_location);
		insertSectionData();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

//	@Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {// 当keyCode等于退出事件值时
//            return false;
//        } else {
//            return super.onKeyDown(keyCode, event);
//        }
//    }
	
	@Override
	public void onBackPressed() {
		if (testDataSubmited()) {
			return;
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onDestroy() {
		//
		mLocClient.stop();
		locFollow = false;
		//
		mBaiduMap.setMyLocationEnabled(false);
		imgLoc.setImageResource(R.drawable.main_location);
		insertSectionData();
		
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}
	
	private boolean testDataSubmited() {
		boolean result = false;
		ArrayList<SpotPointsModel> unsubListSpot = new ArrayList<SpotPointsModel>();
		if(daoSpotPoints != null) {
			CloseableIterator<SpotPointsModel> iterator = daoSpotPoints.iterator();
			
			while (iterator.hasNext()) {
				SpotPointsModel entity = iterator.next();
				if(!entity.isSubmited() && entity.isColleted()) {
					unsubListSpot.add(entity);
				}
			}
		}
		ArrayList<SectionPointsModel> unsubListSection = new ArrayList<SectionPointsModel>();
		if(daoSectionPoints != null) {
			CloseableIterator<SectionPointsModel> iterator = daoSectionPoints.iterator();
			
			while (iterator.hasNext()) {
				SectionPointsModel entity = iterator.next();
				if(!entity.isSubmited()) {
					unsubListSection.add(entity);
				}
			}
		}
		if(unsubListSection.size()>0 || unsubListSpot.size()>0) {
			result = true;
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("警告")
			.setMessage("有未提交数据，是否继续退出？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Application app = (Application) getApplication();
					app.exit();
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create();
			alertDialog.show();
		}
		
		return result;
	}
	
	private void functionList(int position) {
		if(position == 0) {
			createCollectDialog();
		} else if(position ==1) {
			final ArrayList<SpotPointsModel> listSpotPoints = new ArrayList<SpotPointsModel>();
			if(daoSpotPoints != null) {
				CloseableIterator<SpotPointsModel> iterator = daoSpotPoints.iterator();
				
				while (iterator.hasNext()) {
					SpotPointsModel entity = iterator.next();
					if(entity.isColleted()) {
						listSpotPoints.add(entity);
					}
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
					new SubmitLineTask().execute(submitSectionPoints);
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
			final String[] items = {"0","1","2"};
			final String[] itemsValue = {"全部","景点","路段"};
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("绘制物标")
			.setItems(itemsValue, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<SpotPointsModel> listSpotPoints = new ArrayList<SpotPointsModel>();
					if(daoSpotPoints != null) {
						CloseableIterator<SpotPointsModel> iterator = daoSpotPoints.iterator();
						
						while (iterator.hasNext()) {
							SpotPointsModel entity = iterator.next();
							if(entity.isColleted()) {
								listSpotPoints.add(entity);
							}
						}
					}
					ArrayList<SectionPointsModel> listSectionPoints = new ArrayList<SectionPointsModel>();
					if(daoSectionPoints != null) {
						CloseableIterator<SectionPointsModel> iterator = daoSectionPoints.iterator();
						
						while (iterator.hasNext()) {
							SectionPointsModel entity = iterator.next();
							listSectionPoints.add(entity);
						}
					}
					if(items[which].equals("0")) {
						drawPoint(listSpotPoints);
						drawLine(listSectionPoints);
					} else if(items[which].equals("1")) {
						drawPoint(listSpotPoints);
					} else if(items[which].equals("2")) {
						drawLine(listSectionPoints);
					}
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
	
	private void drawLine(ArrayList<SectionPointsModel> listSectionPoints) {
		if(listSectionPoints.size()>0) {
			for(SectionPointsModel each:listSectionPoints) {//路段
				if(each.getSectionPoints().size()>1) {//点
					DrawToolUtil.drawDynamicLineName(each.getSectionPoints(),
							each.getScenicLinename(), mBaiduMap);
					Points initPoint = null;
					for(Points eachPoints:each.getSectionPoints()) {
						DrawToolUtil.drawDynamicLine(initPoint, eachPoints, mBaiduMap);
						initPoint = new Points(eachPoints.getAbsoluteLongitude(), 
								eachPoints.getAbsoluteLatitude(), eachPoints.getAbsoluteAltitude());
					}
				}
			}
		} else {
			Toast.makeText(MainActivity.this, "没有收集路段数据", Toast.LENGTH_SHORT).show();
		}
	}
	private void drawPoint(ArrayList<SpotPointsModel> listSpotPoints) {
		if(listSpotPoints.size()>0) {
			for(SpotPointsModel each:listSpotPoints) {
				if(each.getSpotPoints()!=null&&each.getSpotPoints().size()>0) {
					DrawToolUtil.drawSinglePoint(each.getSpotPoints().get(0),
							each.getScenicspotName(), mBaiduMap);
				}
			}
		} else {
			Toast.makeText(MainActivity.this, "没有收集点数据", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void createCollectDialog() {
		LayoutInflater inflater = (LayoutInflater) LayoutInflater.from(this);  
		View sectionName = inflater.inflate(R.layout.dialog_section_name, null); 
		final EditText sectionNameEdit = (EditText)sectionName.findViewById(R.id.section_name);
		Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
		.setTitle("收集路段内含点列表")
		.setMessage("是否开始收集路段内含点列表？")
		.setView(sectionName)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(sectionNameEdit.getText().toString().equals("")) {
					Toast.makeText(MainActivity.this, "请输入路线名称", Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					List<SectionPointsModel> tempModel = daoSectionPoints.queryForEq("scenicLinename", 
							sectionNameEdit.getText().toString());
					if(tempModel.size() > 0) {
						Toast.makeText(MainActivity.this, "已经收集过该路线", Toast.LENGTH_SHORT).show();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				locFollow = true;
				insertSection = new SectionPointsModel();
				insertSection.setScenicLinename(sectionNameEdit.getText().toString());
				insertSection.setScenicId(scenicId);
				ArrayList<Points> points = new ArrayList<Points>();
				insertSection.setSectionPoints(points);
				insertSection.setPointsNum(0);
				mBaiduMap.setMyLocationEnabled(true);
				mLocClient.start();
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
//								dataInitHelper.initSpotAndLine(lists.get(which).getScenicId());
								dataInitHelper.testSpotDataSubmited(lists.get(which).getScenicId());
								imgLoc.setVisibility(View.VISIBLE);
								initRayMenu();
							}
						})
						.create();
						dialogChooseArea.show();
						//此标志在if进入之后的一开始就赋值为false，但是放在此处保证获取数据之后才置为false
						isFirstLoc = false;
						mLocClient.stop();
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
				mLocClient.stop();
			}
			if(locFollow) {
				Toast.makeText(MainActivity.this, "数据："+ location.getLatitude()+","
						+ location.getLongitude(), Toast.LENGTH_SHORT).show();
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double altitude = location.getAltitude();
				latitude = 4.9E-324;
				if (longitude< 0.00001 || latitude <0.00001
						|| longitude==0.0 || latitude==0.0 ) {
					Toast.makeText(MainActivity.this, "数据未正常获取到！", Toast.LENGTH_SHORT).show();
					mLocClient.stop();
					new Thread() {
						@Override
						public void run() {
							AudioManager am = (AudioManager) MainActivity.this
		                            .getSystemService(Context.AUDIO_SERVICE);// 实例化AudioManager对象
				            float audioMaxVolumn = // 返回当前AudioManager对象的最大音量值
				            		am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
				            float audioCurrentVolumn = am // 返回当前AudioManager对象的音量值
				                            .getStreamVolume(AudioManager.STREAM_MUSIC);
				            float volumnRatio = audioCurrentVolumn / audioMaxVolumn;
							soundPool.play(soundPool.load(MainActivity.this, R.raw.dontpanic, 1), 
									volumnRatio, volumnRatio, 1, 0, 1);
							soundPool.release();
							mLocClient.start();
							mLocClient.requestLocation();
						};
					}.start();
					
					return;
				}
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
				insertSection = null;
				return;
			}
			insertSection.setSubmited(false);
			try {
				final List<SectionPointsModel> tempModel=daoSectionPoints.queryForEq("scenicLinename", insertSection.getScenicLinename());
				if(tempModel.size() > 0) {

					Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("提示")
					.setMessage("该路线数据已经收集过")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							tempModel.get(0).setSectionPoints(insertSection.getSectionPoints());
							tempModel.get(0).setPointsNum(insertSection.getPointsNum());
							tempModel.get(0).setSubmited(false);
							try {
								daoSectionPoints.createOrUpdate(tempModel.get(0));
								Toast.makeText(MainActivity.this, "已有该本地数据,修改成功",Toast.LENGTH_SHORT).show();
								insertSection = null;
							} catch (SQLException e) {
								e.printStackTrace();
								insertSection = null;
							}
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							insertSection = null;
						}
					}).create();
					alertDialog.show();
				} else {
					daoSectionPoints.create(insertSection);
					Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
					insertSection = null;
				}
				
			} catch (SQLException e) {
				Toast.makeText(MainActivity.this, "添加本地数据错误",Toast.LENGTH_SHORT).show();
				insertSection = null;
			}
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
