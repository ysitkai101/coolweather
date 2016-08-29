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

	// �ҵĺͷ�����KEY���������˸��˵�KEY,����ʹ�ã�������ע����ѵĺͷ����������и��˵�KEY���������滻�ͺ��ˣ�
	public static final String WEATHER_KEY = "a4745bbab76341f4b9aaa5bc2c6a6827";

	private LinearLayout weatherInfoLayout;
	/**
	 * ������ʾ������
	 */
	private TextView cityNameText;
	/**
	 * ������ʾ����ʱ��
	 */
	private TextView publishText;
	/**
	 * ������ʾ����������Ϣ
	 */
	private TextView weatherDespText;
	/**
	 * ������ʾ����1
	 */
	private TextView minTempText;
	/**
	 * ������ʾ����2
	 */
	private TextView maxTempText;
	/**
	 * ������ʾ��ǰ����
	 */
	private TextView currentDateText;

	private ProgressDialog mProgressDialog;//������
	private SharedPreferences sharedPreferences;// ���ݴ洢����
	private SharedPreferences.Editor editor;

	private City city_current = new City();// ��ǰ��ʾ�ĳ��ж���
	
	private Button switchCity; // �л����а�ť
	private Button refreshWeather; // ˢ��������ť

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);

		// ʵ�������ش洢
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPreferences.edit();

		// ��ʼ�����ؼ�
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
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			updateWeatherFromServer();
		}

		// �����Ϊ�����ڵ�һ�ΰ�װ��ʱ���жϱ��ش洢��û�����ݣ�����Ĭ�ϻ�ȡ����������
		// �����Ҫ�޸ģ����ԴӺͷ���������
		// http://www.heweather.com/documents/cn-city-list��ѯ����ID
		if (sharedPreferences.getString("city_code", null) == null) {
			city_current.setCityCode("CN101010100");
			updateWeatherFromServer();
		} else {
			// �����ݣ���ӱ���ȡ������Ҳ�����ϴη��ʵĳ��У���ȷ�����
			loadWeatherData(sharedPreferences.getString("city_code", null),
					sharedPreferences.getString("city_name_ch", null), sharedPreferences.getString("update_time", null),
					sharedPreferences.getString("data_now", null), sharedPreferences.getString("txt_d", null),
					sharedPreferences.getString("txt_n", null), sharedPreferences.getString("tmp_min", null),
					sharedPreferences.getString("tmp_max", null));
			// Ȼ��ӷ���������һ��
			updateWeatherFromServer();// ����ע�͵���ʹ�÷�������Զ�����
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
			weatherDespText.setText(txt_d + "ת" + txt_n);
		}
		minTempText.setText(tmp_min + "��");
		maxTempText.setText(tmp_max + "��");

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
	            mProgressDialog.setMessage("����ͬ������...");
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
				publishText.setText("ͬ����");
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
