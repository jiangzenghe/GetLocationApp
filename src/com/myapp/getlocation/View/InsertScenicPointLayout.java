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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.Constants;
import com.myapp.getlocation.R;
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
	private AutoCompleteTextView scenicSpotTxt;
	private TextView scenicSpotIdTxt;
	private BaiduMap mBaiduMap;
	private String strType;
	private String[] typeItems;//key 类型
	private String[] typeValueItems;//value 类型
	private String[] items;//景点列表（标识是否已采集）
	
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
        scenicSpotTxt = (AutoCompleteTextView) this.findViewById(R.id.spot_point_name);
        scenicSpotIdTxt = (TextView) this.findViewById(R.id.spot_name);
        Button cancelBtn = (Button) this.findViewById(R.id.button_cancel);
        Button okBtn = (Button) this.findViewById(R.id.button_ok);
        
        listScenicSpots = new ArrayList<ScenicSpotModel>();
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
				if(typeItems == null || typeValueItems == null) {
					Toast.makeText(context, "抱歉，无数据", Toast.LENGTH_SHORT).show();
					return;
				}
				// TODO Auto-generated method stub
				Dialog alertDialog = new AlertDialog.Builder(context)
				.setTitle("类型列表")
				.setItems(typeValueItems, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						spotTypeTxt.setText(typeValueItems[which]);
						strType = typeItems[which];
						searchSpotsData(strType);
						if(listScenicSpots.size() == 0) {
							scenicSpotTxt.setText("");
							scenicSpotIdTxt.setText("");
						} else {
							scenicSpotTxt.setText(listScenicSpots.get(0).getScenicspotName());
							scenicSpotIdTxt.setText(listScenicSpots.get(0).getSpotId());
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
		});
        scenicSpotTxt.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
			}
        	
        });
//        scenicSpotTxt.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				searchSpotsData(strType);
//				if(listScenicSpots.size() > 0) {
//					
//					Dialog alertDialog = new AlertDialog.Builder(context)
//					.setTitle("景点列表")
//					.setItems(items, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							scenicSpotTxt.setText(items[which]);
//							scenicSpotIdTxt.setText(listScenicSpots.get(which).getSpotId());
//						}
//					})
//					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//						}
//					}).create();
//					alertDialog.show();				
//				} else {
//					Toast.makeText(context, "抱歉，无数据", Toast.LENGTH_SHORT).show();
//				}
//			}
//		});
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
		entity.setSpotType(strType);
//		entity.setScenicspotName(scenicSpotTxt.getText().toString());
//		entity.setScenicId(scenicId);
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
				tempModel.get(i).setSubmited(false);
				daoSpotPoints.createOrUpdate(tempModel.get(i));
				break;}
			}
			if(!isHave){
				entity.setScenicspotName(scenicSpotTxt.getText().toString());
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
	
	public void searchSpotType() {
		int size = Constants.scenicspotMarkertypeMap.size();
		typeItems = new String[size];
		typeValueItems = new String[size];
		for(int index=0;index<size;index++) {
			typeItems[index] = index+1+"";
			typeValueItems[index] = Constants.scenicspotMarkertypeMap.get(typeItems[index]);
		}
		strType = "1";
		searchSpotsData(strType);
		initTextData();
	}
	
	//may be deprecated
	private void searchSpotsData(String index) {
		items = null;
		listScenicSpots.clear();
		if (daoSpot != null&&daoSpotPoints!=null) {
			try {
				List<ScenicSpotModel> tempModel=daoSpot.queryForEq("spotType", index);
				items = new String[tempModel.size()];
				for(ScenicSpotModel each : tempModel) {
					
					ScenicSpotModel entity = new ScenicSpotModel();
					entity.setAbsoluteAltitude(each.getAbsoluteAltitude());
					entity.setAbsoluteLatitude(each.getAbsoluteLatitude());
					entity.setAbsoluteLongitude(each.getAbsoluteLongitude());
					entity.setId(each.getId());
					entity.setScenicId(each.getScenicId());
					entity.setScenicspotName(each.getScenicspotName());
					entity.setSpotId(each.getSpotId());
					entity.setSpotType(each.getSpotType());
					
					items[tempModel.indexOf(each)] = each.getScenicspotName();
					List<SpotPointsModel> tempagaModel=daoSpotPoints.queryForEq("spotId", each.getSpotId());
					if(tempagaModel.size() > 0) {
						items[tempModel.indexOf(each)] += "  ------  已采集";
					}
					listScenicSpots.add(entity);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.layout_list_item, items);//配置Adaptor
					scenicSpotTxt.setAdapter(adapter);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void initTextData() {
		spotTypeTxt.setText(Constants.scenicspotMarkertypeMap.get(strType));
		if(listScenicSpots.size()>0) {
			scenicSpotTxt.setText(listScenicSpots.get(0).getScenicspotName());
			scenicSpotIdTxt.setText(listScenicSpots.get(0).getSpotId());
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
	
}
