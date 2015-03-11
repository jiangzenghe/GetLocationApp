package com.myapp.getlocation.View;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.application.Application;
import com.myapp.getlocation.db.EntityHelper;
import com.myapp.getlocation.entity.ScenicSpotModel;

public class InsertScenicPointLayout extends LinearLayout {

	private View layout;
	private Context context;
	private LayoutInflater mInflate;
	private TextView latitudeTxt;
	private TextView longitudeTxt;
	private BaiduMap mBaiduMap;
	
	private Dao<ScenicSpotModel, Integer> dao;
	private LatLng latLng;
	public InsertScenicPointLayout(Context context) {
		this(context, null);

	}

	public InsertScenicPointLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		mInflate = LayoutInflater.from(this.context);
		layout = mInflate.inflate(R.layout.layout_scenicpoint_add, null);
        layout.setLayoutParams(new LayoutParams(388,LayoutParams.WRAP_CONTENT));
        this.addView(layout);
        this.setLayoutParams(new LayoutParams(388,LayoutParams.WRAP_CONTENT));
        
        latitudeTxt = (TextView) this.findViewById(R.id.latitude);
        longitudeTxt = (TextView) this.findViewById(R.id.longitude);
        Button cancelBtn = (Button) this.findViewById(R.id.button_cancel);
        Button okBtn = (Button) this.findViewById(R.id.button_ok);
        
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
	}
	
	public void insertData() {
		if(dao == null) {
			return;
		}
		ScenicSpotModel entity = new ScenicSpotModel();
		
		entity.setScenicId("221");//test
		entity.setSpotId("10001");//test
		entity.setScenicspotName("南山牌坊");
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
	
}
