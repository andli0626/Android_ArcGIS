package com.andli.querymethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.identify.IdentifyParameters;
import com.esri.core.tasks.ags.identify.IdentifyResult;
import com.esri.core.tasks.ags.identify.IdentifyTask;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

public class QueryMethodDemo extends Activity implements OnClickListener {

	private String tag = "andli";

	MapView mMapView = null;

	ArcGISTiledMapServiceLayer mTileMapLayer = null;
	GraphicsLayer graphicsLayer = null;

	MyTouchListener mTouchListener = null;

	Button clearButton = null;
	Button countButton;

	/******** 图层的URL **********/

	// 瓦片图层
	private String tileLayerURL = "http://58.54.244.254:9000/ArcGIS/rest/services/JZ_BaseLayer/MapServer";
	// 查询图层QueryTask
	private String queryURL = "http://58.54.244.254:9000/ArcGis/rest/services/JZ_BuJian/FeatureServer/10";
	// QueryTask查询的条件：根据OBJNAME
	private String queryCondition = "OBJNAME='消防栓'";

	// 查询图层IdentifyTask
	private String identifyURL = "http://58.54.244.254:9000/ArcGis/rest/services/JZ_BuJian/MapServer";

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.map);
		mTouchListener = new MyTouchListener(QueryMethodDemo.this, mMapView);

		clearButton = (Button) findViewById(R.id.clearbutton);
		clearButton.setOnClickListener(this);

		countButton = (Button) findViewById(R.id.countbutton);
		countButton.setOnClickListener(this);

		mTileMapLayer = new ArcGISTiledMapServiceLayer(tileLayerURL);
		graphicsLayer = new GraphicsLayer();

		mMapView.addLayer(mTileMapLayer);
		mMapView.addLayer(graphicsLayer);// 添加客户端要素图层

		// 地图加载的时候被调用：检查地图是否可用
		mTileMapLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
			public void onStatusChanged(Object arg0, STATUS status) {

				// if
				// (status.equals(OnStatusChangedListener.STATUS.INITIALIZED)) {
				// geoBtn.setEnabled(true);// 可用绘制按钮可用
				// }
			}
		});

		mMapView.setOnTouchListener(mTouchListener);

	}

	private IdentifyParameters params;
	Point clickPoint;

	// 地图触摸事件
	class MyTouchListener extends MapOnTouchListener {

		public MyTouchListener(Context context, MapView view) {
			super(context, view);
		}

		// 点击执行IdentifyTask查询:实现点击弹出对话框效果
		public boolean onSingleTap(MotionEvent e) {
			if (mMapView.isLoaded()) {
				clickPoint = mMapView.toMapPoint(new Point(e.getX(), e.getY()));

				// mMapView.centerAt(mPoint, false);

				params = new IdentifyParameters();
				params.setTolerance(10);
				params.setDPI(96);
				params.setLayers(new int[] { 10 });
				params.setLayerMode(IdentifyParameters.ALL_LAYERS);
				params.setGeometry(clickPoint);
				params.setSpatialReference(mMapView.getSpatialReference());
				params.setMapHeight(mMapView.getHeight());
				params.setMapWidth(mMapView.getWidth());
				Envelope env = new Envelope();
				mMapView.getExtent().queryEnvelope(env);
				params.setMapExtent(env);

				MyIdentifyTask mTask = new MyIdentifyTask(clickPoint);
				mTask.execute(params);
			}
			return true;

		}

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			return super.onDragPointerMove(from, to);
		}

		// 绘制一个点后抬起
		@Override
		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			// 缩放小于10000比例，查询并显示部件信息
			if (mMapView.getScale() < 10000) {
				doQueryTask(null, queryURL);// 执行QueryTask，显示部件点
			}
			return super.onDragPointerUp(from, to);
		}

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
	public void onClick(View v) {
		if (v == clearButton) {
			graphicsLayer.removeAll();
		} else if (v == countButton) {
			countNetData("测试");
		}

	}

	protected void countNetData(String appname) {

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

			if (appname.equals(uidName)) {
				Log.i("andli", "[" + i + "]" + "uid----->" + uid
						+ "uidName------>" + uidName + "\n");
			}
			i++;
			long recv = TrafficStats.getUidRxBytes(uid);
			long sent = TrafficStats.getUidTxBytes(uid);

			String rx;
			String tx = null;

			if (appname.equals(uidName)) {
				Log.i("andli", recv + "");
			}
			if (sent > 0) {
				// Log.v("tmp", "uid=" + uidName + " tx=" + sent);

				if ("Query查询方法".equals(uidName)) {
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

				if (appname.equals(uidName)) {
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
		Log.i("andli",
				"totalRx sum=" + totalRx + " api="
						+ TrafficStats.getTotalRxBytes());
		Log.i("andli",
				"totalTx sum=" + totalTx + " api="
						+ TrafficStats.getTotalTxBytes());
		Log.i("andli",
				"totalRx sum=" + totalRx + " api="
						+ (TrafficStats.getTotalRxBytes() / 1024.0f / 1024.0f)
						+ "MB");
		Log.i("andli",
				"totalTx sum=" + totalTx + " api="
						+ (TrafficStats.getTotalTxBytes() / 1024.0f / 1024.0f)
						+ "MB");

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doQueryTask(Graphic mGraphic, String identifyURL) {
		MyQueryTask myQueryTask = new MyQueryTask();
		HashMap map = new HashMap();
		map.put("url", queryURL);// 要查询的图层
		myQueryTask.execute(map);
	}

	// private ProgressDialog dialog = null;

	// 异步QueryTask查询方法
	@SuppressWarnings("rawtypes")
	private class MyQueryTask extends AsyncTask<HashMap, Void, FeatureSet> {

		protected void onPreExecute() {
			// dialog = ProgressDialog.show(QueryMethodDemo.this, "",
			// "正在查询...");

		}

		protected FeatureSet doInBackground(HashMap... queryParams) {

			HashMap mHashMap = queryParams[0];

			// 设置查询参数
			Query mQuery = new Query();
			mQuery.setWhere(queryCondition);// 查询的条件
			mQuery.setReturnGeometry(true);// 是否返回几何对象
			mQuery.setInSpatialReference(mMapView.getSpatialReference());// 输出坐标系
			// 设置要查询的几何对象：这里是一个矩形框
			// mQuery.setGeometry(mGraphic.getGeometry());// 根据所选择的矩形框查询
			mQuery.setGeometry(mMapView.getExtent());// 根据当前地图范围查询
			mQuery.setSpatialRelationship(SpatialRelationship.INTERSECTS);

			// 实例化查询对象
			QueryTask mQueryTask = new QueryTask(mHashMap.get("url").toString());
			FeatureSet mFeatureSet = null;

			try {
				// 执行查询
				mFeatureSet = mQueryTask.execute(mQuery);
			} catch (Exception e) {
				e.printStackTrace();
				return mFeatureSet;
			}
			return mFeatureSet;

		}

		protected void onPostExecute(FeatureSet result) {

			dealResult(result);

		}

	}

	/**
	 * @author lilin
	 * @date 2013-1-4 下午5:01:05
	 * @annotation
	 */
	public void dealResult(FeatureSet result) {
		String message = "Query结果为空";
		if (result != null) {
			Graphic[] graphics = result.getGraphics();
			result.getDisplayFieldName();
			result.getFieldAliases();
			result.getFields();

			if (graphics.length > 0) {
				// lightGraphicsLayer.addGraphics(graphics);
				message = (graphics.length == 1 ? "查到1条记录" : "查到"
						+ Integer.toString(graphics.length) + "记录");
				// 高亮显示查询结果
				for (int i = 0; i < graphics.length; i++) {
					Graphic mGraphic = graphics[i];// 得到一个几何对象
					mGraphic.getAttributeNames();
					Geometry geom = mGraphic.getGeometry();
					String typeName = geom.getType().name();// 图层类型
					Drawable d = getResources().getDrawable(R.drawable.xfs);
					AMapHelp.lightShowQueryResult(d, i, typeName, geom,
							graphicsLayer);// 高亮显示

					Log.i(tag, "---QueryInfo---");
					Log.i(tag, typeName + "图层");
					Log.i(tag, "第" + i + "个部件");
					Log.i(tag, mGraphic.getAttributes() + "");
					String attributeNames[] = mGraphic.getAttributeNames();
					if (attributeNames.length > 0) {
						for (int i1 = 0; i1 < attributeNames.length; i1++) {
							Log.i(tag, "属性名称=" + attributeNames[i1]);
						}
					}
				}
			}
		}
		// dialog.dismiss();

		Toast.makeText(QueryMethodDemo.this, message, 2000).show();
	}

	// 异步IdentifyTask查询类
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
				mMapView.getCallout().show(mAnchor,
						createIdentifyContent(resultList));
				showInfoDialog(results[0]);

			} else {
				// Toast.makeText(QueryMethodDemo.this, "查询结果为空！", 5000).show();
				mMapView.getCallout().hide();
			}

		}

		@Override
		protected void onPreExecute() {
			mIdentifyTask = new IdentifyTask(identifyURL);
		}

	}

	String BGCODE = "";
	String DEPTNAME1 = "";
	String OBJNAME = "";
	String OBJPOS = "";
	String OBJCODE = "";
	String OBJECTID = "";

	private ViewGroup createIdentifyContent(final List<IdentifyResult> results) {

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.HORIZONTAL);

		if (results != null && results.size() > 0) {

			// graphicsLayer.removeAll();// 清空要素图层

			TextView titleTextView = new TextView(this);
			// titleTextView.setBackgroundResource(R.drawable.bottom_selector);
			IdentifyResult curResult = results.get(0);// 只取查到的第一个值
			// Geometry geom = curResult.getGeometry();
			// lightGraphicId = MapHelp.lightShowQueryResult(geom,
			// graphicsLayer,
			// lightGraphicId);// 高亮显示选中要素对象
			String geometryType = curResult.getGeometry().getType().name();// 查询到的结果对象的类型：点，线，面
			Log.i("andli", "对象类型=" + curResult.getGeometry().getType().name());
			Log.i("andli", "图层名称=" + curResult.getLayerName());
			Log.i("andli", "图层编号=" + curResult.getLayerId());
			Log.i("andli", "部件属性=" + curResult.getAttributes());

			// Attributes={OBJPHOTO=, DEPTNAME2=城管局, DEPTNAME3=城管局,
			// BGCODE=32062110120102, DATASOURCE=实测, DEPTNAME1=城管局,
			// OBJNAME=雨水井盖, SHAPE=点, NOTE=, CHDATE=2012/01/01,
			// OBJPOS=荆州某镇某社区门口, DEPTCODE3=42100000001, DEPTCODE2=42100000001,
			// ORDATE=, DEPTCODE1=42100000001, OBJCODE=3206210103001866,
			// OBJSTATE=完好, OBJECTID=1}

			if (geometryType.equals("POINT")) {
				if (curResult.getAttributes().containsKey("OBJNAME")) {
					OBJNAME = curResult.getAttributes().get("OBJNAME")
							.toString();
				}
				if (curResult.getAttributes().containsKey("BGCODE")) {
					BGCODE = curResult.getAttributes().get("BGCODE").toString();
				}
				if (curResult.getAttributes().containsKey("DEPTNAME1")) {
					DEPTNAME1 = curResult.getAttributes().get("DEPTNAME1")
							.toString();
				}
				if (curResult.getAttributes().containsKey("OBJCODE")) {
					OBJCODE = curResult.getAttributes().get("OBJCODE")
							.toString();
				}
				if (curResult.getAttributes().containsKey("OBJPOS")) {
					OBJPOS = curResult.getAttributes().get("OBJPOS").toString();
				}

				titleTextView.setText(OBJNAME);
				titleTextView.setTextSize(18);
				// titleTextView.setOnClickListener(new OnClickListener() {
				// public void onClick(View v) {
				// getPointInfo(clickPoint);// 获取点坐标
				// }
				// });

				layout.addView(titleTextView);
			}

		}
		return layout;
	}

	public void showInfoDialog(IdentifyResult curResult) {

		// LinearLayout layout = new LinearLayout(this);
		// layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		// layout.setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.dialog_bujianinfo, null);

		TextView objnameTextView = (TextView) layout.findViewById(R.id.objname);
		TextView objcodeTextView = (TextView) layout.findViewById(R.id.objcode);
		TextView objposTextView = (TextView) layout.findViewById(R.id.objpos);
		TextView bjcodeTextView = (TextView) layout.findViewById(R.id.bjcode);
		TextView deptnameTextView = (TextView) layout
				.findViewById(R.id.deptname);

		// Geometry geom = curResult.getGeometry();
		// lightGraphicId = MapHelp.lightShowQueryResult(geom, graphicsLayer,
		// lightGraphicId);// 高亮显示选中要素对象
		String geometryType = curResult.getGeometry().getType().name();// 查询到的结果对象的类型：点，线，面

		if (geometryType.equals("POINT")) {
			if (curResult.getAttributes().containsKey("OBJNAME")) {
				OBJNAME = curResult.getAttributes().get("OBJNAME").toString();
			}
			if (curResult.getAttributes().containsKey("BGCODE")) {
				BGCODE = curResult.getAttributes().get("BGCODE").toString();
			}
			if (curResult.getAttributes().containsKey("DEPTNAME1")) {
				DEPTNAME1 = curResult.getAttributes().get("DEPTNAME1")
						.toString();
			}
			if (curResult.getAttributes().containsKey("OBJCODE")) {
				OBJCODE = curResult.getAttributes().get("OBJCODE").toString();
			}
			if (curResult.getAttributes().containsKey("OBJPOS")) {
				OBJPOS = curResult.getAttributes().get("OBJPOS").toString();
			}
			objnameTextView.setText(OBJNAME);
			objcodeTextView.setText(OBJCODE);
			objposTextView.setText(OBJPOS);
			bjcodeTextView.setText(BGCODE);
			deptnameTextView.setText(DEPTNAME1);

		}

		AlertDialog alertDialog = new AlertDialog.Builder(QueryMethodDemo.this)
				.setIcon(R.drawable.icon).setView(layout).setTitle(OBJNAME)
				.setPositiveButton("确定", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// getPointInfo(clickPoint);
					}
				}).setNegativeButton("取消", null).create();
		Window window = alertDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);// 设置显示位置
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.8f;// 设置透明度:取值范围是从0到1.0。0表 示完全透明,1.0表示不透明
		window.setAttributes(lp);
		alertDialog.show();

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-26 下午2:31:48
	 * @annotation 获取点的信息
	 */
	// protected void getPointInfo(Point clickPoint2) {
	// try {
	//
	// String whichview = getIntent().getStringExtra(Which_View);
	// Intent intent = new Intent(AMap_BuJianView.this,
	// Class.forName(whichview));
	//
	// BujianModel bm = new BujianModel();
	// bm.loc_x = clickPoint.getX();
	// bm.loc_y = clickPoint.getY();
	// bm.OBJNAME = OBJNAME;
	// bm.BGCODE = BGCODE;
	// bm.OBJCODE = OBJCODE;
	// bm.DEPTNAME1 = DEPTNAME1;
	// bm.OBJPOS = OBJPOS;
	//
	// intent.putExtra("model", bm);
	//
	// Log.i("andli", "OBJNAME=" + OBJNAME);
	// Log.i("andli", "BGCODE=" + BGCODE);
	// Log.i("andli", "OBJCODE=" + OBJCODE);
	// Log.i("andli", "DEPTNAME1=" + DEPTNAME1);
	// Log.i("andli", "OBJPOS=" + OBJPOS);
	// Log.i("andli", "x=" + clickPoint.getX());
	// Log.i("andli", "y=" + clickPoint.getY());
	//
	// // mLog.LogI("标记的点 x=" + clickPoint.getX() + " y=" +
	// // clickPoint.getY()
	// // + "--objectid=" + OBJECTID);
	// setResult(RESULT_OK, intent);
	// finish();
	// } catch (Exception e) {
	// // handleException(e);
	// e.printStackTrace();
	// }
	//
	// }

}