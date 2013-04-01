package com.epoint.arcgisdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnStatusChangedListener.STATUS;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class ArcGisDemoView extends Activity {

	private MapView mMapView;
	// private ArcGISTiledMapServiceLayer tileLayer;// 基于Rest的地图服务
	// private ArcGISLocalTiledLayer local;// 本地切片地图
	final static double SEARCH_RADIUS = 5;

	// 用户客户端进行要素的绘制
	private GraphicsLayer graphicsLayer;// 要素图层
	private final String[] geometryTypes = new String[] { "绘制点", "绘制线", "绘制区域" };
	// private int selectedGeometryIndex = -1;
	private MyTouchListener mTouchListener = null;

	private int test = 1;

	private float gpsX = -1;
	private float gpsY = -1;
	private float locX = -1;
	private float locY = -1;

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.map);

		String mapurl = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
		if (test == 1) {
			mapurl = "http://192.168.200.183/ArcGIS/rest/services/HA_BaseLayer/MapServer";
		}
		ArcGISTiledMapServiceLayer tileLayer = new ArcGISTiledMapServiceLayer(
				mapurl);
		mMapView.addLayer(tileLayer);

		if (test == 1) {
			mapurl = "http://192.168.200.183/ArcGIS/rest/services/HA_BZW/MapServer";
			Options mOptions = new Options();
			mOptions.mode = MODE.ONDEMAND;
			mOptions.outFields = new String[] { "OBJECTID", "OBJCODE",
					"OBJNAME", "DEPTCODE1", "DEPTNAME1", "DEPTCODE2",
					"DEPTNAME2", "DEPTCODE3", "DEPTNAME3", "OBJSTATE",
					"CHDATE", "DATASOURCE", "NOTE", "OBJPHOTO", "ORDATE",
					"OBJPOS", "BGCODE", "SHAPE", };

			// 属性图层
			String featureURL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSPetro/MapServer/1";
			featureURL = "http://192.168.200.183/ArcGIS/rest/services/HA_BuJian/MapServer/4";// 公用设施部件
			ArcGISFeatureLayer mFeatureLayer = new ArcGISFeatureLayer(
					featureURL, mOptions);

			// 设置选中属性的颜色
			SimpleFillSymbol selectFillSymbol = new SimpleFillSymbol(
					Color.MAGENTA);
			selectFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 1));
			mFeatureLayer.setSelectionSymbol(selectFillSymbol);

			mMapView.addLayer(mFeatureLayer);

			// mapurl =
			// "http://192.168.200.183/ArcGIS/rest/services/HA_BuJian/MapServer";
			// ArcGISTiledMapServiceLayer tileLayer3 = new
			// ArcGISTiledMapServiceLayer(
			// mapurl);
			// mMapView.addLayer(tileLayer3);
		}

		// String localPath = "";
		// localPath =
		// "file:///mnt/sdcard/arcmap/v101/Parcel Map/_alllayers/L00";
		// localPath = "file:///mnt/sdcard/arcmap/esriinfo/thumbnail/thumbnail";
		// local = new ArcGISLocalTiledLayer(localPath);
		// mMapView.addLayer(local);

		// 设置加载的范围
		// Envelope initextext = new Envelope(493135.2403327083,
		// 3601121.7947442774, 494523.7444976646, 3600029.549286888);
		// mMapView.setExtent(initextext);

		// 长按获取坐标
		this.mMapView.setOnLongPressListener(new OnLongPressListener() {

			@Override
			public void onLongPress(float x, float y) {
				locX = x;
				locY = y;
				Point point = mMapView.toMapPoint(locX, locY);
				String gps = "X=" + point.getX() + "\n Y=" + point.getY();
				Log.i("andli", gps);

				// 移动到该点为中心
				// Unit mapUnit = mMapView.getSpatialReference().getUnit();
				// double zoomWidth = Unit.convertUnits(SEARCH_RADIUS,
				// Unit.create(LinearUnit.Code.MILE_US), mapUnit);
				// Envelope zoomExtent = new Envelope(point, zoomWidth,
				// zoomWidth);
				// mMapView.setExtent(zoomExtent);

				// 注意开启动画后容易导致标记错位
				mMapView.centerAt(point, false);// 已该点为中心:是否开启动画效果
				ViewGroup gpsView = createGPSView(point.getX(), point.getY());
				// mMapView.getCallout().show(point, gpsView);// 增加标记:显示坐标
				mMapView.getCallout().show(point, gpsView);// 增加标记

				// 在要素图层上绘制一个点
				drawPointOnGraphicsLayer(point);
			}

		});

		// 地图状态的改变:GPS定位
		if (test == 0) {
			mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
				public void onStatusChanged(Object source, STATUS status) {
					statusChange(source, status);
				}
			});
		}

		// 在原有的图层上添加一个要素图层
		graphicsLayer = new GraphicsLayer();
		mMapView.addLayer(graphicsLayer);
		// 注册地图触摸事件
		mTouchListener = new MyTouchListener(ArcGisDemoView.this, mMapView);
		mMapView.setOnTouchListener(mTouchListener);

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-7 上午10:18:53
	 * @annotation 在要素图层上绘制一个点
	 */
	protected void drawPointOnGraphicsLayer(Point point) {
		// 清空要素图层
		graphicsLayer.removeAll();
		// 开始绘制
		SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.RED, 25,
				STYLE.CIRCLE);
		Graphic graphic = new Graphic(point, markerSymbol);
		graphicsLayer.addGraphic(graphic);

	}

	// 创建一个查看GPS坐标的视图
	private ViewGroup createGPSView(double x, double y) {

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.HORIZONTAL);

		TextView textView = new TextView(this);
		textView.setTextSize(17);
		textView.setText("(" + x + "," + y + ")");

		layout.addView(textView);

		return layout;
	}

	protected void statusChange(Object source, STATUS status) {

		if (source == mMapView && status == STATUS.INITIALIZED) {
			LocationService locService = mMapView.getLocationService();
			locService.setAutoPan(false);

			// 监听
			locService.setLocationListener(new LocationListener() {

				boolean locationChanged = false;

				// Zooms to the current location when first GPS fix arrives.
				public void onLocationChanged(Location loc) {
					if (!locationChanged) {
						try {
							locationChanged = true;
							double locy = 0.0;
							double locx = 0.0;
							locy = loc.getLatitude();
							locx = loc.getLongitude();
							gpsX = locX;
							gpsY = locY;
							Log.i("andli", locx + "," + locy);
							locx = 120.54186;
							locy = 31.89647;
							// 如果定位点超出地图范围会报错
							Point wgspoint = new Point(locx, locy);
							// GPS坐标转ArcGis坐标
							Point mapPoint = (Point) GeometryEngine.project(
									wgspoint, SpatialReference.create(4326),
									mMapView.getSpatialReference());
							Unit mapUnit = mMapView.getSpatialReference()
									.getUnit();
							double zoomWidth = Unit.convertUnits(SEARCH_RADIUS,
									Unit.create(LinearUnit.Code.MILE_US),
									mapUnit);
							Envelope zoomExtent = new Envelope(mapPoint,
									zoomWidth, zoomWidth);
							mMapView.setExtent(zoomExtent);

						} catch (Exception e) {
							e.printStackTrace();
						}

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

	@Override
	protected void onDestroy() {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "几何图形的绘制");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:// 绘制几何图形
			showGeometryMenu();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 几何图形绘制菜单
	private void showGeometryMenu() {
		new AlertDialog.Builder(ArcGisDemoView.this).setTitle("绘制")
				.setItems(geometryTypes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						graphicsLayer.removeAll();
						String geomType = geometryTypes[which];
						// selectedGeometryIndex = which;
						if (geomType.equalsIgnoreCase(geometryTypes[2])) {
							mTouchListener.setType("POLYGON");
						} else if (geomType.equalsIgnoreCase(geometryTypes[1])) {
							mTouchListener.setType("POLYLINE");
						} else if (geomType.equalsIgnoreCase(geometryTypes[0])) {
							mTouchListener.setType("POINT");
						}
					}
				}).create().show();

	}

	// 地图触摸事件
	class MyTouchListener extends MapOnTouchListener {

		MultiPath mMutiPath;// 多路径
		String type = "";
		Point startPoint = null;

		public MyTouchListener(Context context, MapView view) {
			super(context, view);
		}

		public void setType(String geometryType) {
			this.type = geometryType;
		}

		public String getType() {
			return this.type;
		}

		// 绘制一个点
		public boolean onSingleTap(MotionEvent e) {
			if (type.length() > 1 && type.equalsIgnoreCase("POINT")) {
				graphicsLayer.removeAll();// 清空要素图层

				Point point = mMapView
						.toMapPoint(new Point(e.getX(), e.getY()));
				// 注意：因手机分辨率的差异，像素点的大小是有差别的
				SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(
						Color.BLUE, 30, STYLE.CIRCLE);
				Graphic graphic = new Graphic(point, markerSymbol);
				graphicsLayer.addGraphic(graphic);

				return true;
			}
			return false;

		}

		// 绘制一个点并移动
		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			if (type.length() > 1
					&& (type.equalsIgnoreCase("POLYLINE") || type
							.equalsIgnoreCase("POLYGON"))) {

				// 终点
				Point toPoint = mMapView.toMapPoint(to.getX(), to.getY());

				if (startPoint == null) {
					graphicsLayer.removeAll();
					// 绘制线还是绘制区域
					mMutiPath = type.equalsIgnoreCase("POLYLINE") ? new Polyline()
							: new Polygon();
					// 记录开始点
					startPoint = mMapView.toMapPoint(from.getX(), from.getY());
					mMutiPath.startPath((float) startPoint.getX(),
							(float) startPoint.getY());

					SimpleLineSymbol lineSymbol = new SimpleLineSymbol(
							Color.BLUE, 5);
					Graphic graphic = new Graphic(startPoint, lineSymbol);
					graphicsLayer.addGraphic(graphic);
				}

				mMutiPath
						.lineTo((float) toPoint.getX(), (float) toPoint.getY());

				return true;
			}
			return super.onDragPointerMove(from, to);

		}

		// 绘制一个点后抬起
		@Override
		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			if (type.length() > 1
					&& (type.equalsIgnoreCase("POLYLINE") || type
							.equalsIgnoreCase("POLYGON"))) {
				if (type.equalsIgnoreCase("POLYGON")) {
					mMutiPath.lineTo((float) startPoint.getX(),
							(float) startPoint.getY());
					graphicsLayer.removeAll();
					// 添加面
					Graphic graphic = new Graphic(mMutiPath,
							new SimpleFillSymbol(Color.WHITE));
					graphicsLayer.addGraphic(graphic);
					startPoint = null;
				} else {
					// 添加线
					Graphic graphic = new Graphic(mMutiPath,
							new SimpleLineSymbol(Color.BLUE, 5));
					graphicsLayer.addGraphic(graphic);
					startPoint = null;
				}
				return true;
			}
			return super.onDragPointerUp(from, to);
		}
	}

}