

package com.esri.arcgis.android.samples.nearby2;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnStatusChangedListener.STATUS;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;

public class GPSView extends Activity {

	final static double SEARCH_RADIUS = 5;

	MapView map = null;

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		map = (MapView) findViewById(R.id.map);

		String mapurl = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
		map.addLayer(new ArcGISTiledMapServiceLayer(mapurl));

		// 地图状态的改变
		map.setOnStatusChangedListener(new OnStatusChangedListener() {
			public void onStatusChanged(Object source, STATUS status) {
				statusChange(source, status);
			}
		});
	}

	protected void statusChange(Object source, STATUS status) {

		if (source == map && status == STATUS.INITIALIZED) {
			LocationService locService = map.getLocationService();
			locService.setAutoPan(false);

			// 监听
			locService.setLocationListener(new LocationListener() {

				boolean locationChanged = false;

				// Zooms to the current location when first GPS fix arrives.
				public void onLocationChanged(Location loc) {
					if (!locationChanged) {
						locationChanged = true;
						double locy = 0.0;
						double locx = 0.0;
						// locy = loc.getLatitude();
						// locx = loc.getLongitude();
						Log.i("andli", locx + "," + locy);
						locx = 120.54186;
						locy = 31.89647;

						Point wgspoint = new Point(locx, locy);
						// GPS坐标转ArcGis坐标
						Point mapPoint = (Point) GeometryEngine.project(
								wgspoint, SpatialReference.create(4326),
								map.getSpatialReference());
						Unit mapUnit = map.getSpatialReference().getUnit();
						double zoomWidth = Unit.convertUnits(SEARCH_RADIUS,
								Unit.create(LinearUnit.Code.MILE_US), mapUnit);
						Envelope zoomExtent = new Envelope(mapPoint, zoomWidth,
								zoomWidth);
						map.setExtent(zoomExtent);

					}

				}

				public void onProviderDisabled(String arg0) {

				}

				public void onProviderEnabled(String arg0) {
				}

				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

				}
			});
			// 开启服务
			locService.start();

		}

	}

	protected void onDestroy() {
		super.onDestroy();
		map = null;
	}

}