/**
 * 
 */
package com.myapp.getlocation.db;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.Constants;
import com.myapp.getlocation.activity.Activity;
import com.myapp.getlocation.entity.Points;
import com.myapp.getlocation.entity.ScenicModel;
import com.myapp.getlocation.entity.ScenicSpotModel;
import com.myapp.getlocation.entity.SectionPointsModel;
import com.myapp.getlocation.entity.SpotPointsModel;
import com.myapp.getlocation.http.HttpServiceHandler;
import com.myapp.getlocation.http.HttpServiceProgressWrapper.ProgressDialogHandler;
import com.myapp.getlocation.util.FileUtil;
import com.myapp.getlocation.util.ZipUtil;

/**
 * @author Jiang
 *
 */
public class ScenicDataInitHelper {

	private Activity context;
	private Dao<ScenicModel, Integer> daoModel;
//	private Dao<ScenicSpotModel, Integer> daoSpot;
//	private Dao<ScenicLineSectionModel, Integer> daoSection;
//	private Dao<ScenicLineModel, Integer> daoLine;
//	private ArrayList<ScenicSpotModel> listScenicSpots;
	private Dao<SpotPointsModel, Integer> daoSpotPoints;
	private Dao<SectionPointsModel, Integer> daoSectionPoints;
	
	private ArrayList<ScenicModel> listScenics;
	
	public ScenicDataInitHelper(Activity context) {
		this.context = context;
	}
	
	private class DownSingleSpotFileHandler implements HttpServiceHandler {

		public DownSingleSpotFileHandler(String scenicId) {
			this.scenicId = scenicId;
		}
		
		String scenicId;

		@Override
		public void onHttpServiceFinished(HttpResponse response) {
//			Message message = new Message();
//            handlerSpotLine.sendMessage(message);
			//从解压出来的目录中读取json文件的内容
			try {
				String json = EntityUtils.toString(response.getEntity(), "gbk");
	            if (TextUtils.isEmpty(json)) {
	                Toast.makeText(context, "spot和line数据加载解压出错", Toast.LENGTH_SHORT).show();
	            } else {
	                //json解析并保存的手机的SQLite 数据库
	                dealSpotData(json);
//	                dealLineData(json);
	            }
			}catch (Exception ex){
				ex.printStackTrace();
			}
            
		}

		@Override
		public void onHttpServiceError(Exception e) {
			Toast.makeText(context, "调用远程服务失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onHttpServicePrepare(HttpResponse response) {
//			if (response == null) {
//				return ;
//			}
//			
//			InputStream inputStream = null;
//			try {
//				inputStream = response.getEntity().getContent();
//				FileUtil fileUtil = new FileUtil();
//				File resultFile = fileUtil.write2SDFromInput(Constants.SCENIC_ROUTER_FILE_PATH, 
//    					Constants.SCENIC + scenicId + Constants.ALL_SCENIC_ZIP, inputStream);
//			} catch (IOException e) {
//				
//			} catch (Exception e) {
//				
//			} finally {
//				try {
//					if (inputStream != null)
//						inputStream.close();
//				} catch (Exception e2) {
//					
//				}
//			}
		}
		
		public void dealSpotData(String json) {
			try {
				// consume an optional byte order mark (BOM) if it exists 
				if (json != null && json.startsWith("\ufeff")) { 
					json = json.substring(1); 
				}
//                JSONTokener jsonParser = new JSONTokener(json);
//                JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
				ArrayList<SpotPointsModel> arg0 = (ArrayList)daoSpotPoints.queryForAll();
				if(arg0 != null) {
					daoSpotPoints.delete(arg0);
				}
				ArrayList<SectionPointsModel> arg1 = (ArrayList)daoSectionPoints.queryForAll();
				if(arg1 != null) {
					daoSectionPoints.delete(arg1);
				}
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                	String scenicName = jsonArray.getJSONObject(i).getString("scenicSpotName");
                	String scenicMapId = jsonArray.getJSONObject(i).getString("spotId");
//                	if(!jsonArray.getJSONObject(i).get("absoluteLongitude").toString().equals("null")
//                			&&!jsonArray.getJSONObject(i).get("absoluteLatitude").toString().equals("null")) {
//                			continue;
//                	}
//                	if(jsonArray.getJSONObject(i).getDouble("absoluteLongitude") != 0
//                			&&jsonArray.getJSONObject(i).getDouble("absoluteLatitude") != 0) {
//                		continue;
//                	}
                    if (!TextUtils.isEmpty(scenicName) ) {
                    	SpotPointsModel scenicMapModel = new SpotPointsModel();
                        scenicMapModel.setSpotId(scenicMapId);
                        scenicMapModel.setScenicId(jsonArray.getJSONObject(i).getString("scenicId"));
                        scenicMapModel.setScenicspotName(jsonArray.getJSONObject(i).getString("scenicSpotName"));
                        scenicMapModel.setSpotType(jsonArray.getJSONObject(i).getString("scenicSpotMarkerType"));
                        scenicMapModel.setSubmited(false);
                        if(jsonArray.getJSONObject(i).getDouble("absoluteLongitude") != 0
                        		&&jsonArray.getJSONObject(i).getDouble("absoluteLatitude") != 0) {
                        	Points object = new Points(jsonArray.getJSONObject(i).getDouble("absoluteLongitude"), 
                        			jsonArray.getJSONObject(i).getDouble("absoluteLatitude"), 0.0);
                        	scenicMapModel.getSpotPoints().add(object);
                        	scenicMapModel.setPointsNum(1);
                        }
                        daoSpotPoints.create(scenicMapModel);
                    }
                }
                
                Toast.makeText(context, "spot数据加载解压成功", Toast.LENGTH_SHORT).show();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(context, "spot数据加载解压出错", Toast.LENGTH_SHORT).show();
            } finally {
            	
            }
			
		}
		
	}
	
	public void onCreate() {
		initData();
	}
	
	private void initData() {
		try {
			daoModel = context.getEntityHelper().getDao(ScenicModel.class);
			daoSpotPoints = context.getEntityHelper().getDao(SpotPointsModel.class);
			daoSectionPoints = context.getEntityHelper().getDao(SectionPointsModel.class);
//			daoLine = context.getEntityHelper().getDao(ScenicLineModel.class);
//			daoSection = context.getEntityHelper().getDao(ScenicLineSectionModel.class);
			
			listScenics = new ArrayList<ScenicModel>();
//			listScenicSpots = new ArrayList<ScenicSpotModel>();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initSpotAndLine(final String scenicId) {
		// 远程连接时，使用进度对话框
		ProgressDialog defaultDialog = new ProgressDialog(context);
		defaultDialog.setMessage("等待中");
		ProgressDialogHandler handler = new ProgressDialogHandler(
				defaultDialog);
		DownSingleSpotFileHandler downHander = new DownSingleSpotFileHandler(scenicId);
		
		try {
			context.getProgressHttpService(handler).callGetService(Constants.API_SINGLE_SCENIC_DOWNLOAD + scenicId, null, downHander);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<ScenicModel> searchScenicsData() {
		listScenics.clear();
		if (daoModel != null) {
			CloseableIterator<ScenicModel> iterator = daoModel.iterator();
			
			while (iterator.hasNext()) {
				ScenicModel entity = iterator.next();

				listScenics.add(entity);
			}
		}
		return listScenics;
	}
//	public ArrayList<ScenicSpotModel> searchSpotsData() {
//		listScenicSpots.clear();
//		if (daoSpot != null) {
//			CloseableIterator<ScenicSpotModel> iterator = daoSpot.iterator();
//			
//			while (iterator.hasNext()) {
//				ScenicSpotModel entity = iterator.next();
//
//				listScenicSpots.add(entity);
//
//			}
//		}
//		return listScenicSpots;
//	}
	
	public void parseData() {
		Log.e("parse start", "parse start");
        try {
        	//从解压出来的目录中读取json文件的内容
        	String json = FileUtil.readFile(Constants.SCENIC_IMAGE_FILE_PATH + Constants.SCENIC + Constants.ALL_SCENIC_JSON);
        	if (TextUtils.isEmpty(json)) {
        		Toast.makeText(context, "解压出错", Toast.LENGTH_SHORT).show();
        	} else {
        		//json解析并保存的手机的SQLite 数据库
        		try {
        			JSONTokener jsonParser = new JSONTokener(json);
        			JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
        			JSONArray jsonArray = jsonObject.getJSONArray("scenics");
        			for (int i = 0; i < jsonArray.length(); i++) {
        				String scenicId = jsonArray.getJSONObject(i).getString("id");
        				if (!TextUtils.isEmpty(scenicId)) {
        					ScenicModel scenicModel = new ScenicModel();
        					scenicModel.setScenicId(scenicId);
        					scenicModel.setScenicName(jsonArray.getJSONObject(i).getString("scenicName"));
        					scenicModel.setAbsoluteLongitude(jsonArray.getJSONObject(i).getDouble("absoluteLongitude"));
        					scenicModel.setAbsoluteLatitude(jsonArray.getJSONObject(i).getDouble("absoluteLatitude"));
        					scenicModel.setScenicMapurl(jsonArray.getJSONObject(i).getString("scenicMapurl"));
        					scenicModel.setScenicLocation(jsonArray.getJSONObject(i).getString("scenicLocation"));
        					
        					boolean isHave=false;
        					List<ScenicModel> temp=daoModel.queryForEq("scenicId", scenicModel.getScenicId());
        					for(int j=0;j<temp.size();j++){
        						if(temp.get(j).getScenicId().equals(scenicModel.getScenicId()))
        						{isHave=true;
        						break;}
        					}
        					if(!isHave){
        						daoModel.create(scenicModel);
        					}
        					else{//已经存在   
        						
        					}
        				}
        			}
        			Toast.makeText(context, "数据加载解压成功", Toast.LENGTH_SHORT).show();
        			Log.e("parse end", "parse end");
        			
        		} catch (Exception ex) {
        			ex.printStackTrace();
        			Toast.makeText(context, "数据加载解压出错", Toast.LENGTH_SHORT).show();
        		} finally {
        			
        		}
        	}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public Dao<SpotPointsModel, Integer> getDaoSpotPoints() {
		return daoSpotPoints;
	}
	public Dao<SectionPointsModel, Integer> getDaoSectionPoints() {
		return daoSectionPoints;
	}
}


