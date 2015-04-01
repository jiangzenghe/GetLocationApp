package com.myapp.getlocation.View;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.activity.Activity;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.ScenicSpotModel;
import com.myapp.getlocation.entity.SpotPointsModel;

public class InsertScenicPointLayout extends LinearLayout {

	private View layout;
	private Context context;
	private LayoutInflater mInflate;
	private TextView latitudeTxt;
	private TextView longitudeTxt;
	private TextView altitudeTxt;
	private TextView spotTypeTxt;
	private TextView scenicSpotTxt;
	private TextView scenicSpotIdTxt;
	private BaiduMap mBaiduMap;
	
	private Dao<ScenicSpotModel, Integer> daoSpot;
	private Dao<SpotPointsModel, Integer> daoSpotPoints;
	private ArrayList<ScenicSpotModel> listScenicSpots;
	private LatLng latLng;
	public InsertScenicPointLayout(Context context) {
		this(context, null);
	}

	public InsertScenicPointLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		mInflate = LayoutInflater.from(this.context);
		layout = mInflate.inflate(R.layout.layout_scenicpoint_add, null);
        layout.setLayoutParams(new LayoutParams(500,LayoutParams.WRAP_CONTENT));
        this.addView(layout);
        this.setLayoutParams(new LayoutParams(500,LayoutParams.WRAP_CONTENT));
        
        latitudeTxt = (TextView) this.findViewById(R.id.latitude);
        longitudeTxt = (TextView) this.findViewById(R.id.longitude);
        altitudeTxt = (TextView) this.findViewById(R.id.altitude);
        spotTypeTxt = (TextView) this.findViewById(R.id.spot_type);
        scenicSpotTxt = (TextView) this.findViewById(R.id.spot_point_name);
        scenicSpotIdTxt = (TextView) this.findViewById(R.id.spot_name);
        Button cancelBtn = (Button) this.findViewById(R.id.button_cancel);
        Button okBtn = (Button) this.findViewById(R.id.button_ok);
        
        listScenicSpots = new ArrayList<ScenicSpotModel>();
        searchSpotsData();
        
        okBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(context, "sure", Toast.LENGTH_SHORT).show();
				if(scenicSpotTxt.getText().toString().equals("") ||
						scenicSpotIdTxt.getText().toString().equals("")) {
					Toast.makeText(context, "请选择景点", Toast.LENGTH_SHORT).show();
					return;
				}
				insertData();
				mBaiduMap.clear();
				
			}
		});
        cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBaiduMap.hideInfoWindow();
			}
		});
        spotTypeTxt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final String[] items = {"1", "2", "3", "4"};
				Dialog alertDialog = new AlertDialog.Builder(context)
				.setTitle("类型列表")
				.setItems(items, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						spotTypeTxt.setText(items[which]);
						searchSpotsData();
						scenicSpotTxt.setText(listScenicSpots.get(0).getScenicspotName());
						scenicSpotIdTxt.setText(listScenicSpots.get(0).getSpotId());
					}
				})
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						spotTypeTxt.setText(items[which]);
						searchSpotsData();
						scenicSpotTxt.setText(listScenicSpots.get(0).getScenicspotName());
						scenicSpotIdTxt.setText(listScenicSpots.get(0).getSpotId());
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
				alertDialog.show();
			}
		});
        scenicSpotTxt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchSpotsData();
				if(listScenicSpots.size() > 0) {
					final String[] items = new String[listScenicSpots.size()];
					for(ScenicSpotModel each:listScenicSpots) {
						items[listScenicSpots.indexOf(each)] = each.getScenicspotName();
					}
					
					Dialog alertDialog = new AlertDialog.Builder(context)
					.setTitle("景点列表")
					.setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scenicSpotTxt.setText(items[which]);
							scenicSpotIdTxt.setText(listScenicSpots.get(which).getSpotId());
						}
					})
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scenicSpotTxt.setText(items[which]);
							scenicSpotIdTxt.setText(listScenicSpots.get(which).getSpotId());
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create();
					alertDialog.show();				
				} else {
					Toast.makeText(context, "抱歉，无数据", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void insertData() {
		if(daoSpotPoints == null) {
			return;
		}
		SpotPointsModel entity = new SpotPointsModel();
		double longitude = Double.parseDouble(longitudeTxt.getText().toString().equals("")?"0":longitudeTxt.getText().toString());
		double latitude = Double.parseDouble(latitudeTxt.getText().toString().equals("")?"0":latitudeTxt.getText().toString());
		double altitude = Double.parseDouble(altitudeTxt.getText().toString().equals("")?"0":altitudeTxt.getText().toString());
		Points point = new Points(longitude, latitude, altitude);
		
		entity.setSpotId(scenicSpotIdTxt.getText().toString());
		entity.setScenicspotName(scenicSpotTxt.getText().toString());
		entity.setPointsNum(1);
			
		ArrayList<Points> points = new ArrayList<Points>();
		points.add(point);
		entity.setSpotPoints(points);
		entity.setSubmited(false);
		try {
			boolean isHave=false;
			List<SpotPointsModel> tempModel=daoSpotPoints.queryForEq("spotId", entity.getSpotId());
			for(int i=0;i<tempModel.size();i++){
				if(tempModel.get(i).getSpotId().equals(entity.getSpotId()))
				{isHave=true;
				tempModel.get(i).getSpotPoints().add(point);
				tempModel.get(i).setPointsNum(tempModel.get(i).getPointsNum() + 1);
				daoSpotPoints.createOrUpdate(tempModel.get(i));
				break;}
			}
			if(!isHave){
				daoSpotPoints.create(entity);
				Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
				mBaiduMap.hideInfoWindow();
			}
			else{
				Toast.makeText(context, "已有该本地数据,修改成功",Toast.LENGTH_SHORT).show();
				mBaiduMap.hideInfoWindow();
			}
			

		} catch (SQLException e) {
			Toast.makeText(context, "添加本地数据错误",Toast.LENGTH_SHORT).show();
		}

	}
	
	//may be deprecated
	private void searchSpotsData() {
		listScenicSpots.clear();
		if (daoSpot != null) {
			CloseableIterator<ScenicSpotModel> iterator = daoSpot.iterator();
			
			while (iterator.hasNext()) {
				ScenicSpotModel entity = iterator.next();
				listScenicSpots.add(entity);
			}
		}
	}
	
	public View getLayout() {
		return layout;
	}

	public void setLayout(View layout) {
		this.layout = layout;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
		latitudeTxt.setText(latLng.latitude + "");
        longitudeTxt.setText(latLng.longitude + "");
	}

	public BaiduMap getmBaiduMap() {
		return mBaiduMap;
	}

	public void setmBaiduMap(BaiduMap mBaiduMap) {
		this.mBaiduMap = mBaiduMap;
	}

	public Dao<ScenicSpotModel, Integer> getDaoSpot() {
		return daoSpot;
	}

	public void setDaoSpot(Dao<ScenicSpotModel, Integer> daoSpot) {
		this.daoSpot = daoSpot;
	}

	public Dao<SpotPointsModel, Integer> getDaoSpotPoints() {
		return daoSpotPoints;
	}

	public void setDaoSpotPoints(Dao<SpotPointsModel, Integer> daoSpotPoints) {
		this.daoSpotPoints = daoSpotPoints;
	}

	public ArrayList<ScenicSpotModel> getListScenicSpots() {
		return listScenicSpots;
	}

	public void setListScenicSpots(ArrayList<ScenicSpotModel> listScenicSpots) {
		this.listScenicSpots = listScenicSpots;
	}
	
}
