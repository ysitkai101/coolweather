package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	private ProgressDialog progressDialog; // 进度条对话框
	private EditText editText; // 搜索编辑框
	private ListView listView; // 城市ListView
	private ArrayAdapter<String> adapter; // ListView适配器
	private CoolWeatherDB coolWeatherDB; // 数据库操作对象
	private List<String> dataList = new ArrayList<String>(); // 用于存放与输入的内容相匹配的城市名称字符串

	private List<City> cityList; // 用于存放与输入的内容相匹配的城市名称对象
	private City selectedCity; // 选中的城市

	private SharedPreferences sharedPreferences; // 本地存储
	private SharedPreferences.Editor editor; // 本地存储

	private static final int NONE_DATA = 0; // 标识是否有初始化城市数据

	private boolean isFromWeatherActivity; // 是否从WeatherActivity中跳转出来

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		editText = (EditText) findViewById(R.id.edit_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this); // 获取数据库处理对象
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);// 获取本地存储对象
		editor = sharedPreferences.edit();// 获取本地存储对象

		// 先检查本地是否已同步过城市数据，如果没有，则从服务器同步
		if (coolWeatherDB.checkDataState() == NONE_DATA) {
			queryCitiesFromServer();
		}

		cityList = queryCitiesFromLocal("");// 获取本地存储的所有的城市

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				cityList = queryCitiesFromLocal(s.toString());// 每次文本变化就去本地数据库查询匹配的城市
				adapter.notifyDataSetChanged();// 通知更新
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				selectedCity = cityList.get(index); // 根据点击的位置获取对应的City对象
				queryWeatherFromServer(); // 根据点击的城市从服务器获取天气数据
				Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
				String cityCode = cityList.get(index).getCityCode();
				intent.putExtra("city_code", cityCode);
				startActivity(intent);
				finish();
			}

		});
	}

	// 从本地数据库取出相似的城市名称
	private List<City> queryCitiesFromLocal(String name) {
		List<City> cities = coolWeatherDB.loadCitiesByName(name);
		dataList.clear();
		for (City city : cities) {
			dataList.add(city.getCityName());
		}
		return cities;
	}

	// 从服务器取出所有的城市信息
	private void queryCitiesFromServer() {
		String address = " https://api.heweather.com/x3/citylist?search=allchina&key=" + WeatherActivity.WEATHER_KEY;
		showProgressDialog();

		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				if (Utility.handleCitiesResponse(coolWeatherDB, response)) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							coolWeatherDB.updateDataState();
						}
					});
				}
			}

			@Override
			public void onError(final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	// 从服务器获取天气数据
	private void queryWeatherFromServer() {

		String address = "https://api.heweather.com/x3/weather?cityid=" + selectedCity.getCityCode() + "&key="
				+ WeatherActivity.WEATHER_KEY;
		showProgressDialog();

		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				// 将从服务器获取的JSON数据进行解析
				if (Utility.handleWeatherResponse(editor, response)) {
					// 注意这里对线程的处理
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							// 处理完天气数据，说明已经保存到本地，我们不用再把数据封装到Intent里面返回给WeatherActivity
							// 可以在onActivityResult里面从本地存储中获取
							setResult(RESULT_OK);
							finish();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// 注意这里对线程的处理
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "数据同步失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(isFromWeatherActivity){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
		}
		finish();
	}
}
