package com.myapp.getlocation.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class EntityHelper extends OrmLiteSqliteOpenHelper { 
	private List<Class<?>> types;
	
	//游标工厂为null，代表使用系统默认
	public EntityHelper(Context context, String dbFilePath,
			CursorFactory factory, int databaseVersion) {
		super(dbFilePath, factory, databaseVersion);
		types = new ArrayList<Class<?>>();
	}

	public EntityHelper(Context context, String dbFilePath) {
		this(context, dbFilePath, null, 1);
	}

	public void registerEntity(Class<?> type) {
		if (!types.contains(type)) {
			types.add(type);
		}
	}
	public void unregisterEntity(Class<?> type) {
		types.remove(type);
	}
	
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {

		try {
			for (Class<?> type : types) {
				TableUtils.createTable(connectionSource, type);
			}
		} catch (SQLException e) {
			Log.e(EntityHelper.class.getName(), "创建数据库失败", e);
		}
	}

	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {

		try {
			for (Class<?> type : types) {
				TableUtils.dropTable(connectionSource, type, true);
			}
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(EntityHelper.class.getName(), "创建数据库失败", e);
		}

	}
}
