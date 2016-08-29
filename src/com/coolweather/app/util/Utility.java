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
	 * 解析和处理服务器返回的城市数据
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

	//处理从服务器返回的天气信息（可以自己注册和风天气查看具体的数据）
    //这里的数据是JSON，而且返回的JSON数据相对比较复杂
    //在util包下面有一个北京市天气数据的样例
    //这个JSON里面包含了非常多的天气相关的数据，但我们这里只获取基础信息就行了，有兴趣的可以继续扩展
    public synchronized static boolean handleWeatherResponse(SharedPreferences.Editor editor, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //先把JSON数据加载成数组，因为根部HeWeather data service 3.0后面是[符号，说明是以数组形式存放，只是这个数组里面只有一个元素
                JSONArray jsonArray = new JSONObject(response).getJSONArray("HeWeather data service 3.0");
                //那么既然知道这个数组里面只有一个元素，所以我们直接取出第一个元素为JSONObject
                JSONObject weather_info_all = jsonArray.getJSONObject(0);
                //首先，我们看到，城市名称和数据更新的时间是在basic下面，所以可以直接获取
                JSONObject weather_info_basic = weather_info_all.getJSONObject("basic");
                /*"basic": {
                    "city": "北京",
                    "cnty": "中国",
                    "id": "CN101010100",
                    "lat": "39.904000",
                    "lon": "116.391000",
                    "update":
                    {
                       "loc": "2016-06-30 08:51",
                       "utc": "2016-06-30 00:51"
                    }
                },*/

                //我们发现，有city和update，其中，city可以直接通过名称获取到信息
                editor.putString("city_name_ch", weather_info_basic.getString("city"));
                editor.putString("city_code", weather_info_basic.getString("id"));
                //但是，更新的时间是不能获取的，因为这里update后面是｛｝，表明这是一个对象
                //所以先根据名称获取这个对象
                JSONObject weather_info_basic_update = weather_info_basic.getJSONObject("update");
                //然后再根据这个对象获取名称是loc的数据信息
                editor.putString("update_time", weather_info_basic_update.getString("loc"));

                //关于天气的所有信息都是在daily_forecast名称下面，仔细查看，发现，daily_forecast后面是[符号，说明，这也是一个JSON数组
                //所以先根据名称获取JSONArray对象
                JSONArray weather_info_daily_forecast = weather_info_all.getJSONArray("daily_forecast");
                //我们发现，[]里面是由很多个像下面这样的元素组成的
                /*
                {
                    "astro": {
                        "sr": "04:49",
                        "ss": "19:47"
                    },
                    "cond": {
                        "code_d": "302",
                        "code_n": "302",
                        "txt_d": "雷阵雨",
                        "txt_n": "雷阵雨"
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
                          "dir": "无持续风向",
                          "sc": "微风",
                          "spd": "4"
                    }
                },
                */

                //第一个元素是当前的日期相关的天气数据，目前我们只需要第一个，并且获取出来的是一个JSONObject
                JSONObject weather_info_now_forecast = weather_info_daily_forecast.getJSONObject(0);
                //你会发现，date是可以直接获取的，因为date后面是没有｛｝的
                editor.putString("data_now", weather_info_now_forecast.getString("date"));//当前日期
                //tmp节点是当前的温度，包含最低和最高，说明这是一个JSONObject
                JSONObject weather_info_now_forecast_tmp = weather_info_now_forecast.getJSONObject("tmp");
                editor.putString("tmp_min", weather_info_now_forecast_tmp.getString("min"));
                editor.putString("tmp_max", weather_info_now_forecast_tmp.getString("max"));

                //cond是当前的实际天气描述，获取方法和tmp是一样的
                JSONObject weather_info_now_forecast_cond = weather_info_now_forecast.getJSONObject("cond");
                editor.putString("txt_d", weather_info_now_forecast_cond.getString("txt_d"));//天气情况前
                editor.putString("txt_n", weather_info_now_forecast_cond.getString("txt_n"));//天气情况后

                //最后提交
                editor.commit();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
