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
import com.myapp.getlocation.entity.ScenicModel;
import com.myapp.getlocation.entity.ScenicSpotModel;

public class InsertScenicPointLayout extends LinearLayout {

	private View layout;
	private Context context;
	private LayoutInflater mInflate;
	private TextView latitudeTxt;
	private TextView longitudeTxt;
	private TextView scenicSpotTxt;
	private TextView scenicSpotIdTxt;
	private BaiduMap mBaiduMap;
	
	private Dao<ScenicSpotModel, Integer> dao;
	private Dao<ScenicModel, Integer> daoScenics;
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
        scenicSpotTxt = (TextView) this.findViewById(R.id.scenic_point_name);
        scenicSpotIdTxt = (TextView) this.findViewById(R.id.scenic_name);
        Button cancelBtn = (Button) this.findViewById(R.id.button_cancel);
        Button okBtn = (Button) this.findViewById(R.id.button_ok);
        listScenics = new ArrayList<ScenicModel>();
        
        okBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(context, "sure", Toast.LENGTH_SHORT).show();
				insertData();
				
			}
		});
        cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBaiduMap.hideInfoWindow();
			}
		});
        scenicSpotTxt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchData();
				if(listScenics.size() > 0) {
					final String[] items = new String[listScenics.size()];
					for(ScenicModel each:listScenics) {
						items[listScenics.indexOf(each)] = each.getScenicName();
					}
					
					Dialog alertDialog = new AlertDialog.Builder(context)
					.setTitle("景点列表")
					.setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scenicSpotTxt.setText(items[which]);
							scenicSpotIdTxt.setText(listScenics.get(which).getScenicId());
						}
					})
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scenicSpotTxt.setText(items[which]);
							scenicSpotIdTxt.setText(listScenics.get(which).getScenicId());
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
		if(dao == null) {
			return;
		}
		ScenicSpotModel entity = new ScenicSpotModel();
		
//		entity.setScenicId("221");//test
//		entity.setSpotId("10001");//test
//		entity.setScenicspotName("南山牌坊");
		entity.setScenicId(scenicSpotIdTxt.getText().toString());
		entity.setScenicspotName(scenicSpotTxt.getText().toString());
		entity.setAbsoluteLatitude(Double.parseDouble(latitudeTxt.getText().toString()));
		entity.setAbsoluteLongitude(Double.parseDouble(longitudeTxt.getText().toString()));
		entity.setSubmited(false);
		try {
			boolean isHave=false;
			List<ScenicSpotModel> tempModel=dao.queryForEq("scenicId", entity.getScenicId());
			for(int i=0;i<tempModel.size();i++){
				if(tempModel.get(i).getSpotId().equals(entity.getSpotId()))
				{isHave=true;
				break;}
			}
			if(!isHave){
				dao.create(entity);
				listScenicPoints.add(entity);
				Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
				mBaiduMap.hideInfoWindow();
			}
			else{
				Toast.makeText(context, "已有该本地数据",Toast.LENGTH_SHORT).show();
			}
			

		} catch (SQLException e) {
			Toast.makeText(context, "添加本地数据错误",Toast.LENGTH_SHORT).show();
		}

	}
	
	//may be deprecated
	private void searchData() {
		if (daoScenics != null) {
			CloseableIterator<ScenicModel> iterator = daoScenics.iterator();
			
			while (iterator.hasNext()) {
				ScenicModel entity = iterator.next();

				listScenics.add(entity);
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

	public ArrayList<ScenicSpotModel> getListScenicPoints() {
		return listScenicPoints;
	}

	public void setListScenicPoints(ArrayList<ScenicSpotModel> listScenicPoints) {
		this.listScenicPoints = listScenicPoints;
	}
	
}
