package com.andli.arcgis.offlinemap;

import android.app.Activity;
import android.os.Bundle;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;

public class OfflineMap extends Activity {

	MapView map = null;
	ArcGISLocalTiledLayer local;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		map = (MapView) findViewById(R.id.map);

		// 加载本地切片地图
//		local = new ArcGISLocalTiledLayer("file:///mnt/sdcard/Parcel Map");
//		local = new ArcGISLocalTiledLayer("file:///mnt/sdcard/jzimg");
		local = new ArcGISLocalTiledLayer("file:///mnt/sdcard/sgtmap/001");
		map.addLayer(local);

	}
}