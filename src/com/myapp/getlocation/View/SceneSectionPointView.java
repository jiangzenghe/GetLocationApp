package com.myapp.getlocation.View;

import java.util.ArrayList;
import java.util.List;

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

import com.myapp.getlocation.R;
import com.myapp.getlocation.adapter.ClassAttachmentImpl;
import com.myapp.getlocation.adapter.IAttachment;
import com.myapp.getlocation.entity.ScenicSpotModel;

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
public class SceneSectionPointView extends LinearLayout {
	private Context context;
	private LayoutInflater mInflate;
	private View layout;
	private SectionViewAdapter viewAdapter;
	
	private ArrayList<ScenicSpotModel> scenicPoints;
	private ListView list = null;
	
	public SceneSectionPointView(Context context, ArrayList<ScenicSpotModel> scenicPoints) {
		super(context);
		this.context = context;
		this.scenicPoints = scenicPoints;
		
		// 设置对话框使用的布局文件
		mInflate = LayoutInflater.from(this.context);
		layout = mInflate.inflate(R.layout.layout_scenicspot_list, null);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,500));
		this.addView(layout);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,500));

		list = (ListView) findViewById(R.id.listview);

		viewAdapter = new SectionViewAdapter(this.context, scenicPoints);
		if(scenicPoints != null) {
			list.setAdapter(viewAdapter);
		}

	}
}

class SectionViewAdapter extends ArrayAdapter<ScenicSpotModel> {

	public SectionViewAdapter(Context context, List<ScenicSpotModel> objects) {
		super(context, 0, objects);
		this.context = context;
		fileList = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private LayoutInflater mInflater;
	private List<ScenicSpotModel> fileList;
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
		ScenicSpotModel bean = fileList.get(position);
		TextView txtViewId = (TextView) view.findViewById(R.id.txtScenicPointId);
		TextView txtViewName = (TextView) view.findViewById(R.id.txtScenicPoint);
		CheckBox ckbSubmit = (CheckBox) view.findViewById(R.id.checkBox_submit);
		ckbSubmit.setEnabled(false);
		
		/*
		 * 声明的类改为ClassAttachmentImpl
		 */
		IAttachment<ScenicSpotModel> binder = new ClassAttachmentImpl<ScenicSpotModel>();
		try {
			binder.attachToView(context, view, bean);
			txtViewId.setText(bean.getScenicId());
			txtViewName.setText(bean.getScenicspotName());
			ckbSubmit.setChecked(bean.isSubmited());
			
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}
		return view;
	}

}