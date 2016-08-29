package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity implements OnClickListener{

	// 我的和风天气KEY（我隐藏了个人的KEY,如需使用，请自行注册免费的和风天气，会有个人的KEY，来这里替换就好了）
	public static final String WEATHER_KEY = "a4745bbab76341f4b9aaa5bc2c6a6827";

	private LinearLayout weatherInfoLayout;
	/**
	 * 用于显示城市名
	 */
	private TextView cityNameText;
	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/**
	 * 用于显示气温1
	 */
	private TextView minTempText;
	/**
	 * 用于显示气温2
	 */
	private TextView maxTempText;
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;

	private ProgressDialog mProgressDialog;//进度条
	private SharedPreferences sharedPreferences;// 数据存储对象
	private SharedPreferences.Editor editor;

	private City city_current = new City();// 当前显示的城市对象
	
	private Button switchCity; // 切换城市按钮
	private Button refreshWeather; // 刷新天气按钮

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);

		// 实例化本地存储
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPreferences.edit();

		// 初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		minTempText = (TextView) findViewById(R.id.minTemp);
		maxTempText = (TextView) findViewById(R.id.maxTemp);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		String cityCode = getIntent().getStringExtra("city_code");
		city_current.setCityCode(cityCode);
		
		if(!TextUtils.isEmpty(cityCode)){
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			updateWeatherFromServer();
		}

		// 这个是为了在在第一次安装的时候，判断本地存储还没有数据，所以默认获取北京的数据
		// 如果需要修改，可以从和风天气官网
		// http://www.heweather.com/documents/cn-city-list查询城市ID
		if (sharedPreferences.getString("city_code", null) == null) {
			city_current.setCityCode("CN101010100");
			updateWeatherFromServer();
		} else {
			// 有数据，则从本地取出来，也就是上次访问的城市，先确定这个
			loadWeatherData(sharedPreferences.getString("city_code", null),
					sharedPreferences.getString("city_name_ch", null), sharedPreferences.getString("update_time", null),
					sharedPreferences.getString("data_now", null), sharedPreferences.getString("txt_d", null),
					sharedPreferences.getString("txt_n", null), sharedPreferences.getString("tmp_min", null),
					sharedPreferences.getString("tmp_max", null));
			// 然后从服务器更新一次
			updateWeatherFromServer();// 可以注释掉，使用服务进行自动更新
		}
	}

	private void loadWeatherData(String city_code, String city_name, String update_time, String current_data,
			String txt_d, String txt_n, String tmp_min, String tmp_max) {

		cityNameText.setText(city_name);
		publishText.setText(update_time);
		currentDateText.setText(current_data);

		if (txt_d.equals(txt_n)) {
			weatherDespText.setText(txt_d);
		} else {
			weatherDespText.setText(txt_d + "转" + txt_n);
		}
		minTempText.setText(tmp_min + "℃");
		maxTempText.setText(tmp_max + "℃");

		city_current.setCityName(city_name);
		city_current.setCityCode(city_code);

	}

	private void updateWeatherFromServer() {
		 String address = "https://api.heweather.com/x3/weather?cityid=" + city_current.getCityCode() + "&key=" + WeatherActivity.WEATHER_KEY;
	        showProgressDialog();
	        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
	            @Override
	            public void onFinish(final String response) {
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        if (Utility.handleWeatherResponse(editor, response)) {
	                            loadWeatherData(sharedPreferences.getString("city_code", null), sharedPreferences.getString("city_name_ch", null), sharedPreferences.getString("update_time", null), sharedPreferences.getString("data_now", null), sharedPreferences.getString("txt_d", null), sharedPreferences.getString("txt_n", null), sharedPreferences.getString("tmp_min", null), sharedPreferences.getString("tmp_max", null));
	                            closeProgressDialog();
	                            weatherInfoLayout.setVisibility(View.VISIBLE);
	                			cityNameText.setVisibility(View.VISIBLE);
	                        }
	                    }
	                });
	            }

	            @Override
	            public void onError(final Exception e) {
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        closeProgressDialog();
	                        Toast.makeText(WeatherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
	                    }
	                });
	            }
	        });
	}

	   private void showProgressDialog() {

	        if (mProgressDialog == null) {

	            mProgressDialog = new ProgressDialog(this);
	            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            mProgressDialog.setMessage("正在同步数据...");
	            mProgressDialog.setCanceledOnTouchOutside(false);
	        }
	        mProgressDialog.show();
	    }

	    private void closeProgressDialog() {
	        if (mProgressDialog != null)
	            mProgressDialog.dismiss();
	    }

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.switch_city:
				Intent intent = new Intent(this, ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity", true);
				startActivity(intent);
				finish();
				break;
			case R.id.refresh_weather:
				publishText.setText("同步中");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String cityCode = prefs.getString("city_code", "");
				if(!TextUtils.isEmpty(cityCode)){
					city_current.setCityCode(cityCode);
					updateWeatherFromServer();
				}
			default:
				break;
			}
		}
}
