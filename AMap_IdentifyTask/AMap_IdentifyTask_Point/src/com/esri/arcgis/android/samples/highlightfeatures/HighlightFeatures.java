package com.esri.arcgis.android.samples.highlightfeatures;

import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.identify.IdentifyParameters;
import com.esri.core.tasks.ags.identify.IdentifyResult;
import com.esri.core.tasks.ags.identify.IdentifyTask;

/**
 * 
 * @author lilin
 * @date 2012-9-10 下午2:59:45
 * @annotation IdentifyTask--要素识别 ----长按某一点后查询，并将查询结果高亮显示
 */
public class HighlightFeatures extends Activity {

	private MapView mapView;
	private ArcGISTiledMapServiceLayer tiledMapServiceLayer;
	private ArcGISFeatureLayer mFeatureLayer;

	private GraphicsLayer graphicsLayer;
	private Graphic[] highlightGraphics;

	private String mapURL = "";

	private int test = 1;

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mapView = (MapView) findViewById(R.id.map);
		mapView.removeAll();
		mapView.setOnLongPressListener(new OnLongPressListener() {
			public void onLongPress(float x, float y) {
				identifyPoint(x, y);
			}
		});
		if (test == 0) {
			mapURL = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/PublicSafety/PublicSafetyBasemap/MapServer";
		}
		if (test == 1) {
			mapURL = "http://192.168.1.109/ArcGIS/rest/services/HA_BaseLayer/MapServer/";
		}
		tiledMapServiceLayer = new ArcGISTiledMapServiceLayer(mapURL);
		mapView.addLayer(tiledMapServiceLayer);

		if (test == 1) {
			Options mOptions = new Options();
			mOptions.mode = MODE.ONDEMAND;

			mOptions.outFields = new String[] { "OBJECTID", "OBJCODE",
					"OBJNAME", "XZQMJ" };

			// 属性图层
			mFeatureLayer = new ArcGISFeatureLayer(mapURL, mOptions);
			// 设置选中属性的颜色
			SimpleFillSymbol selectFillSymbol = new SimpleFillSymbol(
					Color.MAGENTA);
			selectFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 1));
			mFeatureLayer.setSelectionSymbol(selectFillSymbol);

			mapView.addLayer(mFeatureLayer);
		}

		graphicsLayer = new GraphicsLayer();
		mapView.addLayer(graphicsLayer);

	}

	// 要素识别长按点
	protected void identifyPoint(float x, float y) {
		ArcGISLayerInfo[] layers = tiledMapServiceLayer.getAllLayers();
		if (layers != null && layers.length > 0) {
			Log.i("andli", "----------瓦片图层-------------");
			Log.i("andli", "个数=" + layers.length);
			for (int i = 0; i < layers.length; i++) {
				if (!TextUtils.isEmpty(layers[i].getName())) {
					Log.i("andli", "name=" + layers[i].getName());
					Log.i("andli", "ID=" + layers[i].getId());
				}
			}
			Log.i("andli", "-----------------------");
		}

		Layer[] layers1 = mapView.getLayers();
		if (layers1 != null && layers1.length > 0) {
			Log.i("andli", "-----------------------");
			Log.i("andli", "图层的个数=" + layers1.length);
			for (int i = 0; i < layers1.length; i++) {
				if (!TextUtils.isEmpty(layers1[i].getName())) {
					Log.i("andli", "name=" + layers1[i].getName());
					Log.i("andli", "URL=" + layers1[i].getUrl());
					Log.i("andli", "ID=" + layers1[i].getID());
				}
			}
			Log.i("andli", "-----------------------");
		}

		graphicsLayer.removeAll();
		Point mPoint = mapView.toMapPoint(x, y);
		IdentifyParameters params = new IdentifyParameters();
		params.setGeometry(mPoint);
		int layerID = 0;
		if (test == 0) {
			layerID = 28;
		}
		if (test == 1) {
			layerID = 5;
		}
		params.setLayers(new int[] { layerID });
		Envelope env = new Envelope();
		mapView.getExtent().queryEnvelope(env);
//		params.setSpatialReference(mapView.getSpatialReference());
		params.setMapExtent(env);
		params.setDPI(96);// 分辨率
		params.setMapHeight(mapView.getHeight());
		params.setMapWidth(mapView.getWidth());
		params.setTolerance(10);

		// 执行查询任务
		MyIdentifyTask mIdenitfy = new MyIdentifyTask();
		mIdenitfy.execute(params);
		

	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.unpause();
	}

	private class MyIdentifyTask extends
			AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

		IdentifyTask mIdentifyTask;

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

			if (results != null && results.length > 0) {
				highlightGraphics = new Graphic[results.length];
				for (int i = 0; i < results.length; i++) {

					Geometry geom = results[i].getGeometry();// 得到几何对象
					String typeName = geom.getType().name();// 图层类型
					lightShow(i, typeName, geom);// 高亮显示

					IdentifyResult result = results[i];
					Log.i("andli", "图层名称---" + result.getLayerName());
					Log.i("andli", "图层id ---" + result.getLayerId());
					Log.i("andli", "图层类型---" + typeName);
				}
			} else {
				Toast.makeText(HighlightFeatures.this, "查询结果为空", 3000).show();
			}

		}

		@Override
		protected void onPreExecute() {
			mIdentifyTask = new IdentifyTask(mapURL);
		}

	}

	public void lightShow(int i, String typeName, Geometry geom) {
		// 高亮显示查询结果
		Random r = new Random();
		int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));// 生成随机色
		if (typeName.equalsIgnoreCase("point")) {
			SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20,
					STYLE.SQUARE);
			highlightGraphics[i] = new Graphic(geom, sms);
		} else if (typeName.equalsIgnoreCase("polyline")) {
			SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
			highlightGraphics[i] = new Graphic(geom, sls);
		} else if (typeName.equalsIgnoreCase("polygon")) {
			SimpleFillSymbol sfs = new SimpleFillSymbol(color);
			sfs.setAlpha(75);
			highlightGraphics[i] = new Graphic(geom, sfs);
		}
		graphicsLayer.addGraphic(highlightGraphics[i]);

	}
}
