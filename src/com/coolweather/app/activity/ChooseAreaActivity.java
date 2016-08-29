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

	private ProgressDialog progressDialog; // �������Ի���
	private EditText editText; // �����༭��
	private ListView listView; // ����ListView
	private ArrayAdapter<String> adapter; // ListView������
	private CoolWeatherDB coolWeatherDB; // ���ݿ��������
	private List<String> dataList = new ArrayList<String>(); // ���ڴ���������������ƥ��ĳ��������ַ���

	private List<City> cityList; // ���ڴ���������������ƥ��ĳ������ƶ���
	private City selectedCity; // ѡ�еĳ���

	private SharedPreferences sharedPreferences; // ���ش洢
	private SharedPreferences.Editor editor; // ���ش洢

	private static final int NONE_DATA = 0; // ��ʶ�Ƿ��г�ʼ����������

	private boolean isFromWeatherActivity; // �Ƿ��WeatherActivity����ת����

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
		coolWeatherDB = CoolWeatherDB.getInstance(this); // ��ȡ���ݿ⴦�����
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);// ��ȡ���ش洢����
		editor = sharedPreferences.edit();// ��ȡ���ش洢����

		// �ȼ�鱾���Ƿ���ͬ�����������ݣ����û�У���ӷ�����ͬ��
		if (coolWeatherDB.checkDataState() == NONE_DATA) {
			queryCitiesFromServer();
		}

		cityList = queryCitiesFromLocal("");// ��ȡ���ش洢�����еĳ���

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				cityList = queryCitiesFromLocal(s.toString());// ÿ���ı��仯��ȥ�������ݿ��ѯƥ��ĳ���
				adapter.notifyDataSetChanged();// ֪ͨ����
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				selectedCity = cityList.get(index); // ���ݵ����λ�û�ȡ��Ӧ��City����
				queryWeatherFromServer(); // ���ݵ���ĳ��дӷ�������ȡ��������
				Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
				String cityCode = cityList.get(index).getCityCode();
				intent.putExtra("city_code", cityCode);
				startActivity(intent);
				finish();
			}

		});
	}

	// �ӱ������ݿ�ȡ�����Ƶĳ�������
	private List<City> queryCitiesFromLocal(String name) {
		List<City> cities = coolWeatherDB.loadCitiesByName(name);
		dataList.clear();
		for (City city : cities) {
			dataList.add(city.getCityName());
		}
		return cities;
	}

	// �ӷ�����ȡ�����еĳ�����Ϣ
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

	// �ӷ�������ȡ��������
	private void queryWeatherFromServer() {

		String address = "https://api.heweather.com/x3/weather?cityid=" + selectedCity.getCityCode() + "&key="
				+ WeatherActivity.WEATHER_KEY;
		showProgressDialog();

		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				// ���ӷ�������ȡ��JSON���ݽ��н���
				if (Utility.handleWeatherResponse(editor, response)) {
					// ע��������̵߳Ĵ���
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							// �������������ݣ�˵���Ѿ����浽���أ����ǲ����ٰ����ݷ�װ��Intent���淵�ظ�WeatherActivity
							// ������onActivityResult����ӱ��ش洢�л�ȡ
							setResult(RESULT_OK);
							finish();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// ע��������̵߳Ĵ���
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ͬ��ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * �رս��ȶԻ���
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
