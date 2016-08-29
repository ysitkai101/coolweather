package com.coolweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Utility {

	/**
	 * �����ʹ�����������صĳ�������
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray jsonArray = new JSONObject(response).getJSONArray("city_info");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject city_info = jsonArray.getJSONObject(i);
					City city = new City();
					String city_name_ch = city_info.getString("city");
					String city_code = city_info.getString("id");
					city.setCityCode(city_code);
					city.setCityName(city_name_ch);
					coolWeatherDB.saveCity(city);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	//����ӷ��������ص�������Ϣ�������Լ�ע��ͷ������鿴��������ݣ�
    //�����������JSON�����ҷ��ص�JSON������ԱȽϸ���
    //��util��������һ���������������ݵ�����
    //���JSON��������˷ǳ����������ص����ݣ�����������ֻ��ȡ������Ϣ�����ˣ�����Ȥ�Ŀ��Լ�����չ
    public synchronized static boolean handleWeatherResponse(SharedPreferences.Editor editor, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //�Ȱ�JSON���ݼ��س����飬��Ϊ����HeWeather data service 3.0������[���ţ�˵������������ʽ��ţ�ֻ�������������ֻ��һ��Ԫ��
                JSONArray jsonArray = new JSONObject(response).getJSONArray("HeWeather data service 3.0");
                //��ô��Ȼ֪�������������ֻ��һ��Ԫ�أ���������ֱ��ȡ����һ��Ԫ��ΪJSONObject
                JSONObject weather_info_all = jsonArray.getJSONObject(0);
                //���ȣ����ǿ������������ƺ����ݸ��µ�ʱ������basic���棬���Կ���ֱ�ӻ�ȡ
                JSONObject weather_info_basic = weather_info_all.getJSONObject("basic");
                /*"basic": {
                    "city": "����",
                    "cnty": "�й�",
                    "id": "CN101010100",
                    "lat": "39.904000",
                    "lon": "116.391000",
                    "update":
                    {
                       "loc": "2016-06-30 08:51",
                       "utc": "2016-06-30 00:51"
                    }
                },*/

                //���Ƿ��֣���city��update�����У�city����ֱ��ͨ�����ƻ�ȡ����Ϣ
                editor.putString("city_name_ch", weather_info_basic.getString("city"));
                editor.putString("city_code", weather_info_basic.getString("id"));
                //���ǣ����µ�ʱ���ǲ��ܻ�ȡ�ģ���Ϊ����update�����ǣ�������������һ������
                //�����ȸ������ƻ�ȡ�������
                JSONObject weather_info_basic_update = weather_info_basic.getJSONObject("update");
                //Ȼ���ٸ�����������ȡ������loc��������Ϣ
                editor.putString("update_time", weather_info_basic_update.getString("loc"));

                //����������������Ϣ������daily_forecast�������棬��ϸ�鿴�����֣�daily_forecast������[���ţ�˵������Ҳ��һ��JSON����
                //�����ȸ������ƻ�ȡJSONArray����
                JSONArray weather_info_daily_forecast = weather_info_all.getJSONArray("daily_forecast");
                //���Ƿ��֣�[]�������ɺܶ��������������Ԫ����ɵ�
                /*
                {
                    "astro": {
                        "sr": "04:49",
                        "ss": "19:47"
                    },
                    "cond": {
                        "code_d": "302",
                        "code_n": "302",
                        "txt_d": "������",
                        "txt_n": "������"
                    },
                    "date": "2016-06-30",
                    "hum": "30",
                    "pcpn": "0.2",
                    "pop": "39",
                    "pres": "1002",
                    "tmp": {
                        "max": "31",
                        "min": "22"
                    },
                    "vis": "10",
                    "wind": {
                          "deg": "204",
                          "dir": "�޳�������",
                          "sc": "΢��",
                          "spd": "4"
                    }
                },
                */

                //��һ��Ԫ���ǵ�ǰ��������ص��������ݣ�Ŀǰ����ֻ��Ҫ��һ�������һ�ȡ��������һ��JSONObject
                JSONObject weather_info_now_forecast = weather_info_daily_forecast.getJSONObject(0);
                //��ᷢ�֣�date�ǿ���ֱ�ӻ�ȡ�ģ���Ϊdate������û�У�����
                editor.putString("data_now", weather_info_now_forecast.getString("date"));//��ǰ����
                //tmp�ڵ��ǵ�ǰ���¶ȣ�������ͺ���ߣ�˵������һ��JSONObject
                JSONObject weather_info_now_forecast_tmp = weather_info_now_forecast.getJSONObject("tmp");
                editor.putString("tmp_min", weather_info_now_forecast_tmp.getString("min"));
                editor.putString("tmp_max", weather_info_now_forecast_tmp.getString("max"));

                //cond�ǵ�ǰ��ʵ��������������ȡ������tmp��һ����
                JSONObject weather_info_now_forecast_cond = weather_info_now_forecast.getJSONObject("cond");
                editor.putString("txt_d", weather_info_now_forecast_cond.getString("txt_d"));//�������ǰ
                editor.putString("txt_n", weather_info_now_forecast_cond.getString("txt_n"));//���������

                //����ύ
                editor.commit();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
