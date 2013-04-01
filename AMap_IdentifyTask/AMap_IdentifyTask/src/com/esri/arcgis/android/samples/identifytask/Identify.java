package com.esri.arcgis.android.samples.identifytask;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.ags.identify.IdentifyParameters;
import com.esri.core.tasks.ags.identify.IdentifyResult;
import com.esri.core.tasks.ags.identify.IdentifyTask;

public class Identify extends Activity {

	private MapView map = null;

	private IdentifyParameters params;

	private String mapURL = "";

	private int test = 0;

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		map = new MapView(this);
		// http://192.168.200.183/ArcGIS/rest/services/HA_BaseLayer/MapServer
		// http://192.168.200.183/ArcGIS/rest/services/HA_BZW/MapServer
		// http://192.168.200.183/ArcGIS/rest/services/HA_BuJian/MapServer
		String tileLayerURL = "http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
		if (test == 1) {
			tileLayerURL = "http://192.168.200.183/ArcGIS/rest/services/HA_BaseLayer/MapServer";
		}

		ArcGISTiledMapServiceLayer basemap = new ArcGISTiledMapServiceLayer(
				tileLayerURL);

		map.addLayer(basemap);

		if (test == 0) {
			mapURL = "http://services.arcgisonline.com/ArcGIS/rest/services/Demographics/USA_Average_Household_Size/MapServer";
			ArcGISTiledMapServiceLayer layer = new ArcGISTiledMapServiceLayer(
					mapURL);
			map.addLayer(layer);
			Envelope env = new Envelope(-19332033.11, -3516.27, -1720941.80,
					11737211.28);
			map.setExtent(env);// 设置范围
		}
		if (test == 1) {
			Options mOptions = new Options();
			mOptions.mode = MODE.ONDEMAND;
			mOptions.outFields = new String[] { "OBJECTID", "OBJCODE",
					"OBJNAME", "DEPTCODE1", "DEPTNAME1", "DEPTCODE2",
					"DEPTNAME2", "DEPTCODE3", "DEPTNAME3", "OBJSTATE",
					"CHDATE", "DATASOURCE", "NOTE", "OBJPHOTO", "ORDATE",
					"OBJPOS", "BGCODE", "SHAPE", };
			// 属性图层
			String featureURL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSPetro/MapServer/1";
			featureURL = "http://192.168.200.183/ArcGIS/rest/services/HA_BuJian/FeatureServer/2";// 公用设施部件
			ArcGISFeatureLayer mFeatureLayer = new ArcGISFeatureLayer(
					featureURL, mOptions);

			// 设置选中属性的颜色
			SimpleFillSymbol selectFillSymbol = new SimpleFillSymbol(
					Color.MAGENTA);
			selectFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 1));
			mFeatureLayer.setSelectionSymbol(selectFillSymbol);

			map.addLayer(mFeatureLayer);
		}

		setContentView(map);

		// 单击事件
		map.setOnSingleTapListener(new OnSingleTapListener() {
			public void onSingleTap(final float x, final float y) {

				if (!map.isLoaded()) {
					return;
				}
				Point identifyPoint = map.toMapPoint(x, y);
				map.centerAt(identifyPoint, false);
				if (test == 0) {
					params = new IdentifyParameters();
					params.setTolerance(20);
					params.setDPI(98);
					params.setLayers(new int[] { 4 });// status图层
					params.setLayerMode(IdentifyParameters.ALL_LAYERS);
				}

				if (test == 1) {
					params = new IdentifyParameters();
					params.setTolerance(3);
					params.setDPI(96);
					params.setLayers(new int[] { 2 });// 社区图层
					params.setLayerMode(IdentifyParameters.ALL_LAYERS);
				}
				params.setGeometry(identifyPoint);
				params.setSpatialReference(map.getSpatialReference());
				params.setMapHeight(map.getHeight());
				params.setMapWidth(map.getWidth());
				Envelope env = new Envelope();
				map.getExtent().queryEnvelope(env);
				params.setMapExtent(env);

				MyIdentifyTask mTask = new MyIdentifyTask(identifyPoint);
				mTask.execute(params);
			}

		});

	}

	private ViewGroup createIdentifyContent(final List<IdentifyResult> results) {

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.HORIZONTAL);

		if (results != null && results.size() > 0) {
			// 用了封装的Spinner
			IdentifyResultSpinner spinner = new IdentifyResultSpinner(this,
					(List<IdentifyResult>) results);
			spinner.setClickable(true);

			IdentifyResultSpinnerAdp adapter = new IdentifyResultSpinnerAdp(
					this, results);
			spinner.setAdapter(adapter);
			spinner.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));

			layout.addView(spinner);
			showInfo(results);

		} else {
			TextView textView = new TextView(this);
			textView.setText("空");
			layout.addView(textView);
		}

		return layout;
	}

	private void showInfo(List<IdentifyResult> results) {
		for (int i = 0; i < results.size(); i++) {
			IdentifyResult mIR = results.get(i);
			String layername = mIR.getLayerName();
			String displayFieldName = mIR.getDisplayFieldName();
			int id = mIR.getLayerId();

			// Attributes={Land Area in Square Miles=97100.4005, Name=Wyoming,
			// ID=56, 2010 Average Household Size=2.43, Shape=Polygon, 2010
			// Total
			// Population=548154, State Abbreviation=WY}

			Log.i("andli", "-----图层信息-----");
			Log.i("andli", "图层名称=" + layername);
			Log.i("andli", "displayFieldName=" + displayFieldName);
			Log.i("andli", "id=" + id);
			Log.i("andli", "Attributes=" + mIR.getAttributes());

			if (test == 0) {
				Log.i("andli",
						"----------------------属性信息----------------------");
				if (mIR.getAttributes()
						.containsKey("Land Area in Square Miles")) {
					Log.i("andli",
							"|---地区面积="
									+ mIR.getAttributes()
											.get("Land Area in Square Miles")
											.toString());
				}
				if (mIR.getAttributes().containsKey("Name")) {
					Log.i("andli", "|---地区名称="
							+ mIR.getAttributes().get("Name").toString());
				}
				if (mIR.getAttributes().containsKey(
						"2010 Average Household Size")) {
					Log.i("andli",
							"|---住房面积="
									+ mIR.getAttributes()
											.get("2010 Average Household Size")
											.toString());
				}
				if (mIR.getAttributes().containsKey("Shape")) {
					Log.i("andli",
							"|---Shape="
									+ mIR.getAttributes().get("Shape")
											.toString());
				}
				if (mIR.getAttributes().containsKey("2010 Total Population")) {
					Log.i("andli",
							"|---2010总人口="
									+ mIR.getAttributes()
											.get("2010 Total Population")
											.toString());
				}
				if (mIR.getAttributes().containsKey("State Abbreviation")) {
					Log.i("andli", "|---State Abbreviation="
							+ mIR.getAttributes().get("State Abbreviation")
									.toString());
				}
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		map.unpause();
	}

	private class MyIdentifyTask extends
			AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

		IdentifyTask mIdentifyTask;
		Point mAnchor;

		MyIdentifyTask(Point anchorPoint) {
			mAnchor = anchorPoint;
		}

		@Override
		protected IdentifyResult[] doInBackground(IdentifyParameters... params) {
			IdentifyResult[] mResult = null;
			if (params != null && params.length > 0) {
				IdentifyParameters mParams = params[0];
				try {
					mResult = mIdentifyTask.execute(mParams);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return mResult;
		}

		@Override
		protected void onPostExecute(IdentifyResult[] results) {
			ArrayList<IdentifyResult> resultList = new ArrayList<IdentifyResult>();
			if (results != null && results.length > 0) {
				for (int index = 0; index < results.length; index++) {

					if (results[index].getAttributes().get(
							results[index].getDisplayFieldName()) != null)
						resultList.add(results[index]);
				}

			}
			map.getCallout().show(mAnchor, createIdentifyContent(resultList));
		}

		@Override
		protected void onPreExecute() {
			mIdentifyTask = new IdentifyTask(mapURL);
		}

	}

}