package com.coolweather.app.model;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.db.CoolWeatherOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {

	/**
	 * ���ݿ���
	 */
	public static final String DB_NAME = "cool_weather";

	/**
	 * ���ݿ�汾
	 */
	public static final int VERSION = 1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	/**
	 * �����췽��˽�л�
	 */
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * ��ȡCoolWeatherDB��ʵ��
	 */
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	/**
	 * ��Cityʵ���洢�����ݿ�
	 */
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			db.insert("City", null, values);
		}
	}

	/**
	 * �����ݿ��ȡĳʡ�����еĳ�����Ϣ
	 */
	public List<City> loadCities() {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
	
	//�������ƻ�ȡĳһ������ƥ��ĳ���
    public List<City> loadCitiesByName(String name) {

        List<City> cities = new ArrayList<>();
        Cursor cursor = db.query("City", null, "city_name like ?", new String[]{name + "%"}, null, null, "city_code");
        while (cursor.moveToNext()) {
            City city = new City();
            city.setId(cursor.getInt(cursor.getColumnIndex("id")));
            city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
			city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
            cities.add(city);
        }
        if (cursor != null)
            cursor.close();
        return cities;
    }

    //����Ƿ��ǵ�һ�ΰ�װ��0-�� 1-��
    public int checkDataState() {
        int data_state = -1;
        Cursor cursor = db.query("data_state", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                data_state = cursor.getInt(cursor.getColumnIndex("STATE"));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();

        return data_state;
    }

    //����״̬Ϊ��������
    public void updateDataState() {
        ContentValues values = new ContentValues();
        values.put("state", 1);
        db.update("data_state", values, null, null);
    }
}
