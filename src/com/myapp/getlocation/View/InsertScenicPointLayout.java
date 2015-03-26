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
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.ScenicModel;
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
	
	private Dao<ScenicSpotModel, Integer> dao;
	private Dao<ScenicModel, Integer> daoScenics;
	private Dao<SpotPointsModel, Integer> daoPoints;
	private ArrayList<ScenicModel> listScenics;
	private ArrayList<ScenicSpotModel> listScenicPoints;
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
        listScenics = new ArrayList<ScenicModel>();
        listScenicPoints = new ArrayList<ScenicSpotModel>();
        
        okBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(context, "sure", Toast.LENGTH_SHORT).show();
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
				
			}
		});
        scenicSpotTxt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchData();
				if(listScenicPoints.size() > 0) {
					final String[] items = new String[listScenicPoints.size()];
					for(ScenicSpotModel each:listScenicPoints) {
						items[listScenicPoints.indexOf(each)] = each.getScenicspotName();
					}
					
					Dialog alertDialog = new AlertDialog.Builder(context)
					.setTitle("景点列表")
					.setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scenicSpotTxt.setText(items[which]);
							scenicSpotIdTxt.setText(listScenicPoints.get(which).getSpotId());
						}
					})
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scenicSpotTxt.setText(items[which]);
							scenicSpotIdTxt.setText(listScenicPoints.get(which).getSpotId());
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
		if(daoPoints == null) {
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
			List<SpotPointsModel> tempModel=daoPoints.queryForEq("spotId", entity.getSpotId());
			for(int i=0;i<tempModel.size();i++){
				if(tempModel.get(i).getSpotId().equals(entity.getSpotId()))
				{isHave=true;
				tempModel.get(i).getSpotPoints().add(point);
				tempModel.get(i).setPointsNum(tempModel.get(i).getPointsNum() + 1);
				daoPoints.createOrUpdate(tempModel.get(i));
				break;}
			}
			if(!isHave){
				daoPoints.create(entity);
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
	private void searchData() {
//		if (daoScenics != null) {
//			CloseableIterator<ScenicModel> iterator = daoScenics.iterator();
//			
//			while (iterator.hasNext()) {
//				ScenicModel entity = iterator.next();
//
//				listScenics.add(entity);
//			}
//		}
		listScenicPoints.clear();
		if (dao != null) {
			CloseableIterator<ScenicSpotModel> iterator = dao.iterator();
			
			while (iterator.hasNext()) {
				ScenicSpotModel entity = iterator.next();

				listScenicPoints.add(entity);
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

	public Dao<SpotPointsModel, Integer> getDaoPoints() {
		return daoPoints;
	}

	public void setDaoPoints(Dao<SpotPointsModel, Integer> daoPoints) {
		this.daoPoints = daoPoints;
	}

	public ArrayList<ScenicSpotModel> getListScenicPoints() {
		return listScenicPoints;
	}

	public void setListScenicPoints(ArrayList<ScenicSpotModel> listScenicPoints) {
		this.listScenicPoints = listScenicPoints;
	}
	
}
