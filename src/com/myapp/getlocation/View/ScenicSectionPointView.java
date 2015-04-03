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
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.R;
import com.myapp.getlocation.activity.MainActivity;
import com.myapp.getlocation.adapter.ClassAttachmentImpl;
import com.myapp.getlocation.adapter.IAttachment;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.SectionPointsModel;

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
//						LatLng p1 = new LatLng(39.97923, 116.357428);
//						LatLng p2 = new LatLng(39.94923, 116.397428);
//						LatLng p3 = new LatLng(39.97923, 116.437428);
//						List<LatLng> points = new ArrayList<LatLng>();
//						points.add(p1);
//						points.add(p2);
//						points.add(p3);
//						OverlayOptions ooPolyline = new PolylineOptions().width(10)
//								.color(0xAAFF0000).points(points);
//						mBaiduMap.addOverlay(ooPolyline);
						ArrayList<Points> points = bean.getSectionPoints();
						mBaiduMap.clear();
						List<LatLng> pts = new ArrayList<LatLng>(); 
						for(Points each : points) {//俩个点一样，就看不到线
							LatLng point = new LatLng(each.getAbsoluteLatitude(), each.getAbsoluteLongitude());//test
							if(point.latitude != 0 && point.longitude != 0) {
								pts.add(point);
							}
						}
//						pts.add(new LatLng(points.get(0).getAbsoluteLatitude(), points.get(0).getAbsoluteLongitude()+0.1));//test
						if(pts.size() >=2 && pts.size() <10000) {
							//构建用于绘制多边形的Option对象  
							OverlayOptions polygonOption = new PolylineOptions()  
							.width(8)
							.color(0xAAFF0000)
							.points(pts);
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
									Dao<SectionPointsModel, Integer> daoSectionPoints = activity.getEntityHelper().getDao(SectionPointsModel.class);
									if(daoSectionPoints == null) {
										return;
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
				txtViewName.setText(bean.getScenicLinename()+":"+bean.getAspotName()+"-"+bean.getBspotName());
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

