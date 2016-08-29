package com.coolweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

	//City表建表语句
	public static final String CREATE_CITY = "create table City (" //
			+ "id integer primary key autoincrement, "//
			+ "city_name text,"//
			+ "city_code text)";

	// 创建有无数据状态表
	private static final String DATA_STATE = "CREATE TABLE DATA_STATE(STATE INTEGER PRIMARY KEY)";
	// 更新状态表数据为0表示暂无数据
	private static final String INSERT_DATA_STATE = "INSERT INTO DATA_STATE VALUES(0)";

	public CoolWeatherOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CITY);// 创建City表
		db.execSQL(DATA_STATE);
		db.execSQL(INSERT_DATA_STATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
