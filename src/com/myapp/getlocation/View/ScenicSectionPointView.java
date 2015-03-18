package com.myapp.getlocation.View;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.myapp.getlocation.R;
import com.myapp.getlocation.adapter.ClassAttachmentImpl;
import com.myapp.getlocation.adapter.IAttachment;
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
			SectionPointsModel bean = fileList.get(position);
			TextView txtViewId = (TextView) view.findViewById(R.id.txtScenicPointId);
			TextView txtViewName = (TextView) view.findViewById(R.id.txtScenicPoint);
			CheckBox ckbSubmit = (CheckBox) view.findViewById(R.id.checkBox_submit);
			ckbSubmit.setEnabled(false);
			
			/*
			 * 声明的类改为ClassAttachmentImpl
			 */
			IAttachment<SectionPointsModel> binder = new ClassAttachmentImpl<SectionPointsModel>();
			try {
				binder.attachToView(context, view, bean);
				txtViewId.setText(bean.getScenicId());
				
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
			}
			return view;
		}

	}
}

