package my.xu.firstproject.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import my.xu.firstproject.coolweather.R;
import my.xu.firstproject.coolweather.db.CoolWeatherDB;
import my.xu.firstproject.coolweather.model.City;
import my.xu.firstproject.coolweather.model.County;
import my.xu.firstproject.coolweather.model.Province;
import my.xu.firstproject.coolweather.util.HttpCallBackListener;
import my.xu.firstproject.coolweather.util.HttpUtil;
import my.xu.firstproject.coolweather.util.Utility;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private TextView titleText;
	private ListView list_view;

	private ProgressDialog progressDialog;
	private ArrayAdapter<String> adapter;
	private List<String> datalist = new ArrayList<String>();
	private CoolWeatherDB coolWeatherDB;

	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	// 点击的省份
	private Province selectProvince;
	// 点击的市
	private City selectCity;
	// 当前选中的级别
	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		list_view = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, datalist);
		list_view.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectCity = cityList.get(position);
					queryCounties();
				}
			}

		});
		queryProvincies();
	}

	private void queryProvincies() {
		// TODO Auto-generated method stub
		provinceList = coolWeatherDB.loadProvince();
		if (provinceList.size() > 0) {
			datalist.clear();
			for (Province province : provinceList) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			list_view.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	private void queryCities() {
		// TODO Auto-generated method stub
		cityList = coolWeatherDB.loadCity(selectProvince.getId());
		if (cityList.size() > 0) {
			datalist.clear();
			for (City city : cityList) {
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			list_view.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectProvince.getProvinceCode(), "city");
		}
	}

	private void queryCounties() {
		// TODO Auto-generated method stub
		countyList = coolWeatherDB.loadCounties(selectCity.getId());
		if (countyList.size() > 0) {
			datalist.clear();
			for (County county : countyList) {
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			list_view.setSelection(0);
			titleText.setText(selectCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(final String code, final String name) {
		// TODO Auto-generated method stub
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if ("province".equals(name)) {
					result = Utility.handleProvincesResponse(coolWeatherDB,
							response);
				} else if ("city".equals(name)) {
					result = Utility.handleCitiesResponse(coolWeatherDB,
							response, selectProvince.getId());
				} else if ("county".equals(name)) {
					result = Utility.handleCountyResponse(coolWeatherDB,
							response, selectCity.getId());
				}
				if (result) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(name)) {
								queryProvincies();
							} else if ("city".equals(name)) {
								queryCities();
							} else if ("county".equals(name)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("加载中...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvincies();
		} else {
			finish();
		}
	}
}