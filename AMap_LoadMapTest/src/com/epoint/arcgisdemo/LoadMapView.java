package com.epoint.arcgisdemo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

public class LoadMapView extends Activity {

	private String tag = "andli";

	private MapView mMapView;
	private ArcGISImageServiceLayer imageLayer;
	private ArcGISTiledMapServiceLayer tileLayer;
	ArcGISDynamicMapServiceLayer dynammicLayout;

	private ArcGISTiledMapServiceLayer newImageLayer;
	private ArcGISTiledMapServiceLayer baseLayer;
	private ArcGISDynamicMapServiceLayer DYWGLayer;

	Button button;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.map);

		// imageLayer = new ArcGISImageServiceLayer(MapHelp.getImageMapURL(this,
		// MapHelp.IMAGE), null);
		// mMapView.addLayer(imageLayer);

		// newImageLayer = new ArcGISTiledMapServiceLayer(
		// "http://192.168.200.183/ArcGIS/rest/services/ImageFQY/MapServer");
		// mMapView.addLayer(newImageLayer);
		//
		 baseLayer = new ArcGISTiledMapServiceLayer(AMapHelp.getMapURL(this,
		 AMapHelp.BASELAYER));
		 mMapView.addLayer(baseLayer);
		//
		// 添加单元网格图层
		DYWGLayer = new ArcGISDynamicMapServiceLayer(AMapHelp.getMapURL(this,
				AMapHelp.DYWG));
		mMapView.addLayer(DYWGLayer);

		// 加载属性图层
//		 dynammicLayout = new ArcGISDynamicMapServiceLayer(AMapHelp.getMapURL(
//		 this, AMapHelp.JZ_BZWBZD_BLACK));
//		 mMapView.addLayer(dynammicLayout);

		button = (Button) findViewById(R.id.start);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				countNetData();
			}
		});

	}

	/**
	 * @author lilin
	 * @date 2013-1-4 下午3:24:30
	 * @annotation
	 */
	protected void countNetData() {

		Map<Integer, String> users = new TreeMap<Integer, String>();

		PackageManager packageManager = getPackageManager();
		for (ApplicationInfo info : packageManager.getInstalledApplications(0)) {
			int uid = info.uid;
			if (!users.containsKey(uid))
				users.put(uid, packageManager.getApplicationLabel(info)
						.toString());
			// users.put(uid,
			// packageManager.getNameForUid(uid).split(":")[0]);
		}

		long totalRx = 0;
		long totalTx = 0;
		int i = 1;
		for (Entry<Integer, String> entry : users.entrySet()) {

			int uid = entry.getKey();
			String uidName = entry.getValue();

			if (getString(R.string.app_name).equals(uidName)) {
				Log.i("andli", "[" + i + "]" + "uid----->" + uid
						+ "uidName------>" + uidName + "\n");
			}
			i++;
			long recv = TrafficStats.getUidRxBytes(uid);
			long sent = TrafficStats.getUidTxBytes(uid);

			String rx;
			String tx = null;

			if (getString(R.string.app_name).equals(uidName)) {
				Log.i("andli", recv + "");
			}
			if (sent > 0) {
				// Log.v("tmp", "uid=" + uidName + " tx=" + sent);

				if (getString(R.string.app_name).equals(uidName)) {
					tx = "uid=" + uidName + " tx=" + (sent / 1024.0f / 1024.0f)
							+ "MB";
					Log.i("andli", tx);
				}
				totalTx += sent;
			} else {
				// Log.i("andli","uid=" + uidName);
				// Log.i("andli","暂时还未产生发送流量！");
			}
			if (recv > 0) {
				// Log.d("tmp", "uid=" + uidName + " rx=" + recv);

				if (getString(R.string.app_name).equals(uidName)) {
					rx = "uid=" + uidName + " rx=" + (recv / 1024.0f / 1024.0f)
							+ "MB";
					Log.i("andli", rx);
					Toast.makeText(getApplicationContext(), tx + "\n" + rx,
							5000).show();
				}
				totalRx += recv;
			} else {
				// System.out
				// .println("暂时还未产生接收流量！The server encountered an internal error () that prevented it from fulfilling this request");
			}
		}
		// Log.i("tmp",
		// "totalRx sum=" + totalRx + " api="
		// + TrafficStats.getTotalRxBytes());
		// Log.i("tmp",
		// "totalTx sum=" + totalTx + " api="
		// + TrafficStats.getTotalTxBytes());
		// Log.i("tracy", "totalRx sum=" + totalRx + " api="
		// + (TrafficStats.getTotalRxBytes() / 1024.0f / 1024.0f)
		// + "MB");
		// Log.i("tracy", "totalTx sum=" + totalTx + " api="
		// + (TrafficStats.getTotalTxBytes() / 1024.0f / 1024.0f)
		// + "MB");

	}

	@Override
	protected void onDestroy() {
		mMapView.destroyDrawingCache();
		mMapView.recycle();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

}