/**
 * 
 */
package com.myapp.getlocation.util;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.Constants;
import com.myapp.getlocation.activity.MainActivity;
import com.myapp.getlocation.entity.ScenicLineModel;
import com.myapp.getlocation.entity.ScenicLineSectionModel;
import com.myapp.getlocation.entity.ScenicModel;
import com.myapp.getlocation.entity.ScenicSpotModel;
import com.myapp.getlocation.entity.SectionPointsModel;
import com.myapp.getlocation.entity.SpotPointsModel;

/**
 * @author Jiang
 *
 */
public class ScenicDataInitHelper {

	private MainActivity context;
	private Dao<ScenicModel, Integer> daoModel;
	private Dao<ScenicSpotModel, Integer> daoSpot;
	private Dao<ScenicLineSectionModel, Integer> daoSection;
	private Dao<ScenicLineModel, Integer> daoLine;
	private Dao<SpotPointsModel, Integer> daoSpotPoints;
	private Dao<SectionPointsModel, Integer> daoSectionPoints;
	private ArrayList<ScenicSpotModel> listScenicPoints;
	private ArrayList<ScenicModel> listScenics;
	
	private SharedPreferences mySharedPreferences;
	private SharedPreferences.Editor editor;
	private ProgressDialog progressDialog;
	private Handler handler;
	private Handler handlerSpotLine;
	
	public ScenicDataInitHelper(MainActivity context) {
		this.context = context;
	}
	
	public void onCreate() {
		initData();
		handler = new MyScenicHandler();
		requestDataFromInternet();
		
		context.setDaoScenics(daoModel);
		context.setDaoSpot(daoSpot);
		context.setDaoPoints(daoSpotPoints);
		context.setDaoSectionPoints(daoSectionPoints);
		context.setListScenicPoints(listScenicPoints);
		context.setListScenics(listScenics);
		
	}
	
	private void initData() {
		try {
			daoModel = context.getEntityHelper().getDao(ScenicModel.class);
			daoSpot = context.getEntityHelper().getDao(ScenicSpotModel.class);
			daoLine = context.getEntityHelper().getDao(ScenicLineModel.class);
			daoSection = context.getEntityHelper().getDao(ScenicLineSectionModel.class);
			daoSpotPoints = context.getEntityHelper().getDao(SpotPointsModel.class);
			daoSectionPoints = context.getEntityHelper().getDao(SectionPointsModel.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initSpotAndLine(final String scenicId) {
		handlerSpotLine = new MyScenicSpotLineHandler(scenicId);
		new Thread() {
            @Override
            public void run() {
            	HttpUtil httpUtil = new HttpUtil();
    			int result = httpUtil.downFile(Constants.API_SINGLE_SCENIC_DOWNLOAD + scenicId, Constants.SCENIC_ROUTER_FILE_PATH, 
    					Constants.SCENIC + scenicId + Constants.ALL_SCENIC_ZIP);
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.API_MESSAGE_KEY, result);
                message.setData(bundle);
                handlerSpotLine.sendMessage(message);
            }
        }.start();
	}
	
	private void searchScenicsData() {
		listScenics.clear();
		if (daoModel != null) {
			CloseableIterator<ScenicModel> iterator = daoModel.iterator();
			
			while (iterator.hasNext()) {
				ScenicModel entity = iterator.next();

				listScenics.add(entity);
			}
		}
	}
	private void searchSpotsData() {
		listScenicPoints.clear();
		if (daoSpot != null) {
			CloseableIterator<ScenicSpotModel> iterator = daoSpot.iterator();
			
			while (iterator.hasNext()) {
				ScenicSpotModel entity = iterator.next();

				listScenicPoints.add(entity);

			}
		}
	}
	
	@SuppressLint("NewApi")
	private void requestDataFromInternet() {
		//将上次保存到手机的更新系统时间值取出
        mySharedPreferences = context.getSharedPreferences(Constants.SHAREDPREFERENCES_NAME, 0);
        long last = mySharedPreferences.getLong(Constants.INDEX_FLAG, 0);
        long now = System.currentTimeMillis();
        //判断当前手机网络是否可以用，并且距离上次更新是否超过24小时
//        if (HttpUtil.isNetworkAvailable(context) && (now - last) > 86400000) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("数据初始化加载中");
            progressDialog.setCancelable(false);
            progressDialog.show();
            editor = mySharedPreferences.edit();
            editor.putLong(Constants.INDEX_FLAG, now);
            editor.apply();
            //获取所有景区基本资料
            downAndParseData();
//        } else {
        	
//        }
	}
	
	private void downAndParseData() {
		new Thread() {
            @Override
            public void run() {
            	HttpUtil httpUtil = new HttpUtil();
    			int result = httpUtil.downFile(Constants.API_ALL_SCENIC_DOWNLOAD, Constants.SCENIC_ROUTER_FILE_PATH, 
    					Constants.SCENIC + Constants.ALL_SCENIC_ZIP);
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.API_MESSAGE_KEY, result);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
		
	}

	public ArrayList<ScenicSpotModel> getListScenicPoints() {
		return listScenicPoints;
	}

	public void setListScenicPoints(ArrayList<ScenicSpotModel> listScenicPoints) {
		this.listScenicPoints = listScenicPoints;
	}

	public ArrayList<ScenicModel> getListScenics() {
		return listScenics;
	}

	public void setListScenics(ArrayList<ScenicModel> listScenics) {
		this.listScenics = listScenics;
	}
	
	private class MyScenicHandler extends Handler{
		public MyScenicHandler() {
			
		}
		
		@Override
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);
            Bundle bundle = msg.getData();
            //获取api访问结果，-1为获取失败 0为下载成功；1为本地已经存在
            int result = bundle.getInt(Constants.API_MESSAGE_KEY);
			
	        switch (result) {
	        case -1:
	            Toast.makeText(context, "加载出错", Toast.LENGTH_SHORT).show();
	            progressDialog.dismiss();
	            break;
	        case 0:
//	            break;
	        default:  //1 已经存在
	        	try {
	                //根据路径解压缩下载zip文件
	                ZipUtil.upZipFile(new File(Environment.getExternalStorageDirectory() + "/" + 
	                	Constants.SCENIC_ROUTER_FILE_PATH + Constants.SCENIC + Constants.ALL_SCENIC_ZIP), 
	                		Environment.getExternalStorageDirectory() + "/" + Constants.SCENIC_ROUTER_FILE_PATH);
	                //从解压出来的目录中读取json文件的内容
	                String json = FileUtil.readFile(Constants.SCENIC_IMAGE_FILE_PATH + Constants.SCENIC + Constants.ALL_SCENIC_JSON);
	                if (TextUtils.isEmpty(json)) {
	                    Toast.makeText(context, "加载出错", Toast.LENGTH_SHORT).show();
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
	                        Toast.makeText(context, "数据加载成功", Toast.LENGTH_SHORT).show();
	                        progressDialog.dismiss();
	                        searchScenicsData();
	                        
	                    } catch (Exception ex) {
	                        ex.printStackTrace();
	                        Toast.makeText(context, "数据加载出错", Toast.LENGTH_SHORT).show();
	                        progressDialog.dismiss();
	                    } finally {
	                    	
	                    }
	                }
	            } catch (Exception e) {
	                Toast.makeText(context, "数据解压出错", Toast.LENGTH_SHORT).show();
	                progressDialog.dismiss();
	            }
	        }
		}
	}
	
	private class MyScenicSpotLineHandler extends Handler{
		public MyScenicSpotLineHandler(String scenicId) {
			this.scenicId = scenicId;
		}
		
		private String scenicId;
		
		@Override
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);
            Bundle bundle = msg.getData();
            //获取api访问结果，-1为获取失败 0为下载成功；1为本地已经存在
            int result = bundle.getInt(Constants.API_MESSAGE_KEY);
			
	        switch (result) {
	        case -1:
	            Toast.makeText(context, "spot和line数据加载出错", Toast.LENGTH_SHORT).show();
	            progressDialog.dismiss();
	            break;
	        case 0:
//	            break;
	        default:  //1 已经存在
	        	try {
	                //根据路径解压缩下载zip文件
	                ZipUtil.upZipFile(new File(Environment.getExternalStorageDirectory() + "/" + 
	                	Constants.SCENIC_ROUTER_FILE_PATH + Constants.SCENIC + scenicId + Constants.ALL_SCENIC_ZIP), 
	                		Environment.getExternalStorageDirectory() + "/" + Constants.SCENIC_ROUTER_FILE_PATH);
	                //从解压出来的目录中读取json文件的内容
	                String json = FileUtil.readFile(Constants.SCENIC_SINGLE_FILE_PATH + scenicId +
	                		"/" + Constants.SCENIC + scenicId + Constants.ALL_SCENIC_JSON);
	                if (TextUtils.isEmpty(json)) {
	                    Toast.makeText(context, "spot和line数据加载出错", Toast.LENGTH_SHORT).show();
	                } else {
	                    //json解析并保存的手机的SQLite 数据库
                        dealSpotData(json);
                        dealLineData(json);
	                }
	            } catch (Exception e) {
	                Toast.makeText(context, "spot和line数据加载出错", Toast.LENGTH_SHORT).show();
	                progressDialog.dismiss();
	            }
	        }
		}
		
		private void dealSpotData(String json) {
			try {
                JSONTokener jsonParser = new JSONTokener(json);
                JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
                JSONArray jsonArray = jsonObject.getJSONArray("scenicMap");
                for (int i = 0; i < jsonArray.length(); i++) {
                	String scenicMapId = jsonArray.getJSONObject(i).getString("id");
                    if (!TextUtils.isEmpty(scenicMapId)) {
                    	 ScenicSpotModel scenicMapModel = new ScenicSpotModel();
                         scenicMapModel.setSpotId(scenicMapId);
                         scenicMapModel.setScenicId(jsonArray.getJSONObject(i).getString("scenicId"));
                         scenicMapModel.setScenicspotName(jsonArray.getJSONObject(i).getString("scenicspotName"));
                         scenicMapModel.setAbsoluteLongitude(jsonArray.getJSONObject(i).getDouble("absoluteLongitude"));
                         scenicMapModel.setAbsoluteLatitude(jsonArray.getJSONObject(i).getDouble("absoluteLatitude"));
                        
                        boolean isHave=false;
                        List<ScenicSpotModel> temp=daoSpot.queryForEq("spotId", scenicMapModel.getSpotId());
                        for(int j=0;j<temp.size();j++){
                        	if(temp.get(j).getSpotId().equals(scenicMapModel.getSpotId()))
                        	{isHave=true;
                        	break;}
                        }
                        if(!isHave){
                        	daoSpot.create(scenicMapModel);
                        }
                        else{//已经存在   
                        	
                        }
                    }
                }
                
                Toast.makeText(context, "spot和line数据加载成功", Toast.LENGTH_SHORT).show();
                searchSpotsData();
                progressDialog.dismiss();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(context, "spot和linet数据加载出错", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            } finally {
            	
            }
			
		}
		
		private void dealLineData(String json) {
			try {
                JSONTokener jsonParser = new JSONTokener(json);
                JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
                JSONArray jsonArray = jsonObject.getJSONArray("scenicRecommendLine");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray jsonArrayL = jsonArray.getJSONArray(i);
                    for (int j = 0; j < jsonArrayL.length(); j++) {
                        if (j == 0) {  //路线
                            String scenicRecommendLineId = jsonArrayL.getJSONObject(j).getString("id");
                            if (!TextUtils.isEmpty(scenicRecommendLineId)) {
                                ScenicLineModel scenicRecommendLineModel = new ScenicLineModel();
                                scenicRecommendLineModel.setScenicId(jsonArrayL.getJSONObject(j).getString("scenicId"));
                                scenicRecommendLineModel.setScenicLineId(scenicRecommendLineId);
                                scenicRecommendLineModel.setScenicLinename(jsonArrayL.getJSONObject(j).getString("recommendRoutename"));
                                
                                boolean isHave=false;
                                List<ScenicLineModel> temp=daoLine.queryForEq("scenicLineId", scenicRecommendLineModel.getScenicLineId());
                                for(int k=0;k<temp.size();k++){
                                	if(temp.get(k).getScenicLineId().equals(scenicRecommendLineModel.getScenicLineId()))
                                	{isHave=true;
                                	break;}
                                }
                                if(!isHave){
                                	daoLine.create(scenicRecommendLineModel);
                                }
                                else{//已经存在   
                                	
                                }
                            }
                        } else {  //路线中的路段
                            String recommendLinesectionId = jsonArrayL.getJSONObject(j).getString("id");
                            if (!TextUtils.isEmpty(recommendLinesectionId)) {
                            	ScenicLineSectionModel recommendLinesectionModel = new ScenicLineSectionModel();
                                recommendLinesectionModel.setLinesectionId(recommendLinesectionId);
                                recommendLinesectionModel.setScenicId(jsonArrayL.getJSONObject(j).getString("scenicId"));
                                recommendLinesectionModel.setScenicLineId(jsonArrayL.getJSONObject(j).getString("recommendrouteId"));
                                recommendLinesectionModel.setRouteOrder(jsonArrayL.getJSONObject(j).getInt("routeOrder"));
                                recommendLinesectionModel.setAspotId(jsonArrayL.getJSONObject(j).getString("aspotId"));
                                recommendLinesectionModel.setBspotId(jsonArrayL.getJSONObject(j).getString("bspotId"));
                                recommendLinesectionModel.setAspotName(jsonArrayL.getJSONObject(j).getString("ascenicspotName"));
                                recommendLinesectionModel.setBspotName(jsonArrayL.getJSONObject(j).getString("bscenicspotName"));
                                
                                boolean isHave=false;
                                List<ScenicLineSectionModel> temp=daoSection.queryForEq("linesectionId", recommendLinesectionModel.getLinesectionId());
                                for(int k=0;k<temp.size();k++){
                                	if(temp.get(k).getLinesectionId().equals(recommendLinesectionModel.getLinesectionId()))
                                	{isHave=true;
                                	break;}
                                }
                                if(!isHave){
                                	daoSection.create(recommendLinesectionModel);
                                }
                                else{//已经存在   
                                	
                                }
                            }
                        }
                    }
                }
                Toast.makeText(context, "spot和line数据加载成功", Toast.LENGTH_SHORT).show();
                searchSpotsData();
                progressDialog.dismiss();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(context, "spot和line数据加载出错", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            } finally {
            	
            }
		}
	}
}


