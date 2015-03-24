package com.myapp.getlocation.View;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.activity.MainActivity;
import com.myapp.getlocation.adapter.ClassAttachmentImpl;
import com.myapp.getlocation.adapter.IAttachment;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.SpotPointsModel;

/**
 * 
 * @author Jiang
 * 
 *         <p>
 *         Modification History:
 *         </p>
 *         <p>
 *         Date Author Description
 *         </p>
 *         <p>
 */
public class ScenicPointListView extends LinearLayout {
	private Context context;
	private BaiduMap mBaiduMap;
	private LayoutInflater mInflate;
	private View layout;
	private SpotViewAdapter viewAdapter;
	private Dialog alertDialog;
	
	private ArrayList<SpotPointsModel> scenicPoints;
	private ListView list = null;
	
	public ScenicPointListView(Context context, BaiduMap mBaiduMap,ArrayList<SpotPointsModel> scenicPoints) {
		super(context);
		this.context = context;
		this.mBaiduMap = mBaiduMap;
		this.scenicPoints = scenicPoints;
		
		// 设置对话框使用的布局文件
		mInflate = LayoutInflater.from(this.context);
		layout = mInflate.inflate(R.layout.layout_scenicspot_list, null);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,500));
		this.addView(layout);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,500));

		list = (ListView) findViewById(R.id.listview);

		viewAdapter = new SpotViewAdapter(this.context, scenicPoints);
		if(scenicPoints != null) {
			list.setAdapter(viewAdapter);
		}
		
	}
	
	public Dialog getAlertDialog() {
		return alertDialog;
	}

	public void setAlertDialog(Dialog alertDialog) {
		this.alertDialog = alertDialog;
	}

	class SpotViewAdapter extends ArrayAdapter<SpotPointsModel> {

		public SpotViewAdapter(Context context, List<SpotPointsModel> objects) {
			super(context, 0, objects);
			this.context = context;
			fileList = objects;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		private LayoutInflater mInflater;
		private List<SpotPointsModel> fileList;
		private Context context;

		public View getView(final int position, View convertView, ViewGroup parent) {
			// 获取需要关联数据的View
			View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.layout_scenicspot_item, parent, false);
			} else {
				view = convertView;
			}
			
			// 获取数据bean
			final SpotPointsModel bean = fileList.get(position);
			TextView txtViewId = (TextView) view.findViewById(R.id.txtScenicPointId);
			TextView txtViewName = (TextView) view.findViewById(R.id.txtScenicPoint);
			TextView txtViewtype = (TextView) view.findViewById(R.id.txtScenicPointtype);
			TextView txtViewNum = (TextView) view.findViewById(R.id.point_num);
			CheckBox ckbSubmit = (CheckBox) view.findViewById(R.id.checkBox_submit);
			ImageView delImage = (ImageView) view.findViewById(R.id.delImage);
			ckbSubmit.setEnabled(false);
			
			/*
			 * 声明的类改为ClassAttachmentImpl
			 */
			IAttachment<SpotPointsModel> binder = new ClassAttachmentImpl<SpotPointsModel>();
			try {
				binder.attachToView(context, view, bean);
				view.setOnLongClickListener(new View.OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						ArrayList<Points> points = bean.getSpotPoints();
						mBaiduMap.clear();
						for(Points each : points) {
							LatLng point = new LatLng(each.getAbsoluteLatitude(), each.getAbsoluteLongitude());
							//构建用户绘制多边形的Option对象  
							OverlayOptions polygonOption = new DotOptions()  
							.center(point)
							.color(Color.parseColor("#FF0000"));
							//在地图上添加Option，用于显示  
							mBaiduMap.addOverlay(polygonOption);
						}
						alertDialog.dismiss();
						return true;//
					}
				});
				delImage.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Dialog alertDelDialog = new AlertDialog.Builder(context)
						.setTitle("删除确认")
						.setMessage("确定删除？")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									MainActivity activity = (MainActivity)context;
									Dao<SpotPointsModel, Integer> daoSpotPoints = activity.getEntityHelper().getDao(SpotPointsModel.class);
									if(daoSpotPoints == null) {
										return;
									}
									daoSpotPoints.deleteById(bean.getId());
									alertDialog.dismiss();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						})
						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).create();
						alertDelDialog.show();
					}
				});
				txtViewId.setText(bean.getSpotId());
				txtViewName.setText(bean.getScenicspotName());
				txtViewNum.setText(bean.getPointsNum()+"");
				ckbSubmit.setChecked(bean.isSubmited());
				
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
			}
			return view;
		}

	}
}

