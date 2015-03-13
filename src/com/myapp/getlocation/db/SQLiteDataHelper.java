package com.myapp.getlocation.db;

import com.j256.ormlite.dao.Dao;
import com.myapp.getlocation.entity.ScenicModel;

public class SQLiteDataHelper {
	private Dao<ScenicModel, Integer> db;

    public SQLiteDataHelper() {
    }
	
	public ScenicModel getModelByScenicId(String sscenicId) {
//        Cursor cursor = db.query(ScenicSqliteHelper.TB_NAME, null, ScenicModel.ScenicId + " == ? ", new String[]{sscenicId}, null, null, ScenicModel.ScenicId + " ASC");
//
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            ScenicModel model = new ScenicModel();
//            model.setId(cursor.getInt(0));
//            model.setCreatedTime(cursor.getLong(1));
//            model.setScenicId(cursor.getString(2));
//            model.setScenicName(cursor.getString(3));
//            model.setScenicLocation(cursor.getString(4));
//            model.setCenterabsoluteLongitude(cursor.getDouble(5));
//            model.setCenterabsoluteLatitude(cursor.getDouble(6));
//            model.setCenterrelativeLongitude(cursor.getDouble(7));
//            model.setCenterrelativeLatitude(cursor.getDouble(8));
//            model.setAbsoluteLongitude(cursor.getDouble(9));
//            model.setAbsoluteLatitude(cursor.getDouble(10));
//            model.setScenicNote(cursor.getString(11));
//            model.setScenicMapurl(cursor.getString(12));
//            model.setScenicSmallpic(cursor.getString(13));
//            model.setScenicmapMaxx(cursor.getInt(14));
//            model.setScenicmapMaxy(cursor.getInt(15));
//            model.setLineColor(cursor.getString(16));
//            cursor.close();
//            return model;
//        }
        return null;
    }
}
