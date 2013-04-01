package com.andli.query.demo1;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

/**
 * 
 * @author lilin
 * @date 2012-9-7 下午5:14:20
 * @annotation 框选查询要素
 */
public class QueryTaskDemo extends Activity {

	private MapView map = null;
	private GraphicsLayer mGraphicsLayer = null;
	private GraphicsLayer lightGraphicsLayer = null;

	private String tileLayerURL = "http://58.54.244.254:9000/ArcGis/rest/services/JZ_BaseLayer/MapServer";
	private String queryLayerURL = "http://58.54.244.254:9000/ArcGis/rest/services/JZ_BaseLayer/MapServer/4";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		map = new MapView(this);

		ArcGISTiledMapServiceLayer tileLayer = new ArcGISTiledMapServiceLayer(
				tileLayerURL);
		map.addLayer(tileLayer);

		lightGraphicsLayer = new GraphicsLayer();

		map.addLayer(lightGraphicsLayer);

		addGraphicesLayer();// 添加要素图层

		// 注册图层触摸事件
		MyTouchListener touchListener = new MyTouchListener(this, map);
		map.setOnTouchListener(touchListener);

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-14 下午4:43:10
	 * @annotation 添加要素图层：用于框选和高亮显示查询结果
	 */
	private void addGraphicesLayer() {
		mGraphicsLayer = new GraphicsLayer();
		map.addLayer(mGraphicsLayer);
		setContentView(map);

	}

	class MyTouchListener extends MapOnTouchListener {

		Graphic mGraphic;
		Point mFromPoint = null;// 起点
		int uid = -1;

		public MyTouchListener(Context arg0, MapView arg1) {
			super(arg0, arg1);
		}

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			if (uid == -1) { // first time
				SimpleFillSymbol mSimpleFillSymbol;
				mSimpleFillSymbol = new SimpleFillSymbol(Color.BLACK);// 填充色
				mSimpleFillSymbol
						.setOutline(new SimpleLineSymbol(Color.BLUE, 1));// 外框线
				mSimpleFillSymbol.setAlpha(100);

				mGraphic = new Graphic(null, mSimpleFillSymbol);
				
				mFromPoint = map.toMapPoint(from.getX(), from.getY());
				uid = mGraphicsLayer.addGraphic(mGraphic);
				lightGraphicsLayer.removeAll();

			} else {
				// 终点
				Point mToPoint = map
						.toMapPoint(new Point(to.getX(), to.getY()));
				Envelope envelope = new Envelope();
				envelope.merge(mFromPoint);
				envelope.merge(mToPoint);
				mGraphicsLayer.updateGraphic(uid, envelope);

			}

			return true;

		}

		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {

			if (uid != -1) {
				mGraphic = mGraphicsLayer.getGraphic(uid);
				if (mGraphic != null && mGraphic.getGeometry() != null) {
					doQueryGraphic(mGraphic, queryLayerURL);
				}
				mGraphicsLayer.removeAll();
			}
			mFromPoint = null;
			// 重置
			uid = -1;
			return true;

		}

	}

	private ProgressDialog dialog = null;

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-14 下午4:34:31
	 * @annotation Query方法
	 */
	@SuppressWarnings("rawtypes")
	private class MyQueryTask extends AsyncTask<HashMap, Void, FeatureSet> {

		protected void onPreExecute() {
			dialog = ProgressDialog.show(QueryTaskDemo.this, "", "正在查询...");

		}

		protected FeatureSet doInBackground(HashMap... queryParams) {

			HashMap mHashMap = queryParams[0];
			Query mQuery = new Query();
			// 设置查询参数
//			mQuery.setWhere("OBJNAME='海安县海安镇园庄村'");// 查询的条件
			mQuery.setReturnGeometry(true);// 是否返回几何对象
			mQuery.setInSpatialReference(map.getSpatialReference());// 输出坐标系
			Graphic mGraphic = (Graphic) mHashMap.get("geo");
			// 设置要查询的几何对象：这里是一个矩形框
			mQuery.setGeometry(mGraphic.getGeometry());
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

			String message = "Query结果为空";
			if (result != null) {

				Graphic[] graphics = result.getGraphics();

				if (graphics.length > 0) {
					// lightGraphicsLayer.addGraphics(graphics);
					message = (graphics.length == 1 ? "查到1条记录" : "查到"
							+ Integer.toString(graphics.length) + "记录");
					// 高亮显示查询结果
					for (int i = 0; i < graphics.length; i++) {
						Graphic mGraphic = graphics[i];
						Geometry geom = mGraphic.getGeometry();
						String typeName = geom.getType().name();// 图层类型
						AMapHelp.lightShowQueryResult(i, typeName, geom,
								lightGraphicsLayer);// 高亮显示
					}
				}

			}
			dialog.dismiss();

			Toast.makeText(QueryTaskDemo.this, message, 2000).show();

		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doQueryGraphic(Graphic mGraphic, String queryLayerURL2) {
		if (mGraphic != null) {
			MyQueryTask myQueryTask = new MyQueryTask();
			HashMap map = new HashMap();
			map.put("geo", mGraphic);
			map.put("url", queryLayerURL);// 要查询的图层
			myQueryTask.execute(map);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		map.unpause();
	}

	// private CallbackListener<FeatureSet> callback = new
	// CallbackListener<FeatureSet>() {
	//
	// public void onCallback(FeatureSet fSet) {
	//
	// }
	//
	// public void onError(Throwable arg0) {
	// mGraphicsLayer.removeAll();
	//
	// }
	// };

}