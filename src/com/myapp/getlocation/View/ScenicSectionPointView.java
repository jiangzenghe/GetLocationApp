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
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.activity.MainActivity;
import com.myapp.getlocation.adapter.ClassAttachmentImpl;
import com.myapp.getlocation.adapter.IAttachment;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.SectionPointsModel;
import com.myapp.getlocation.util.DrawToolUtil;

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
public class ScenicSectionPointView extends LinearLayout {
	private Context context;
	private LayoutInflater mInflate;
	private BaiduMap mBaiduMap;
	private View layout;
	private SectionViewAdapter viewAdapter;
	
	private ArrayList<SectionPointsModel> scenicPoints;
	private ListView list = null;
	private Dialog alertDialog;
	
	public ScenicSectionPointView(Context context, BaiduMap mBaiduMap, ArrayList<SectionPointsModel> scenicPoints) {
		super(context);
		this.context = context;
		this.mBaiduMap = mBaiduMap;
		this.scenicPoints = scenicPoints;
		
		// 设置对话框使用的布局文件
		mInflate = LayoutInflater.from(this.context);
		layout = mInflate.inflate(R.layout.layout_sectionpoints_list, null);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,500));
		this.addView(layout);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,500));

		list = (ListView) findViewById(R.id.listview);

		viewAdapter = new SectionViewAdapter(this.context, scenicPoints);
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
	
	class SectionViewAdapter extends ArrayAdapter<SectionPointsModel> {

		public SectionViewAdapter(Context context, List<SectionPointsModel> objects) {
			super(context, 0, objects);
			this.context = context;
			fileList = objects;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		private LayoutInflater mInflater;
		private List<SectionPointsModel> fileList;
		private Context context;

		public View getView(final int position, View convertView, ViewGroup parent) {
			// 获取需要关联数据的View
			View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.layout_sectionpoints_item, parent, false);
			} else {
				view = convertView;
			}
			
			// 获取数据bean
			final SectionPointsModel bean = fileList.get(position);
			TextView txtViewId = (TextView) view.findViewById(R.id.txtScenicSectionId);
			TextView txtViewName = (TextView) view.findViewById(R.id.txtSectionPoint);
			TextView txtViewNum = (TextView) view.findViewById(R.id.point_num);
			TextView txtSubmit = (TextView) view.findViewById(R.id.txt_sub);
			ImageView delImage = (ImageView) view.findViewById(R.id.delImage);
			
			/*
			 * 声明的类改为ClassAttachmentImpl
			 */
			IAttachment<SectionPointsModel> binder = new ClassAttachmentImpl<SectionPointsModel>();
			try {
				binder.attachToView(context, view, bean);
				view.setOnLongClickListener(new View.OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						mBaiduMap.clear();
						
						ArrayList<Points> points = bean.getSectionPoints();
						if (points.size() == 0) return true;	
						DrawToolUtil.drawDynamicLineName(points, bean.getScenicLinename(), mBaiduMap);
						int index = points.size()/2 + 1;
						Points point = points.get(index);
						LatLng center = new LatLng(point.getAbsoluteLatitude(), point.getAbsoluteLongitude());
						MapStatusUpdate arg0 = MapStatusUpdateFactory.newLatLng(center);
						mBaiduMap.animateMapStatus(arg0);
						
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
									Dao<SectionPointsModel, Integer> daoSectionPoints = activity.getEntityHelper().getDao(SectionPointsModel.class);
									if(daoSectionPoints == null) {
										return;
									}
									if(bean.isSubmited()) {
										//如果已经提交，需要添加针对服务端的删除处理
									}
									daoSectionPoints.deleteById(bean.getId());
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
				txtViewId.setText(bean.getLinesectionId());
				txtViewName.setText(bean.getScenicLinename());
				txtViewNum.setText(bean.getPointsNum()+"");
				String subString = bean.isSubmited()?"已提交":"未提交";
				txtSubmit.setText(subString);
				
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
			}
			return view;
		}

	}
}

