package com.esri.arcgis.android.samples.selectfeatures;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.ags.ArcGISFeatureLayer.SELECTION_METHOD;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

/**
 * 
 * @author lilin
 * @date 2012-9-7 下午5:14:20
 * @annotation 框选查询要素
 */
public class SelectFeatures extends Activity {

	private MapView map = null;
	private ArcGISFeatureLayer mFeatureLayer = null;
	private GraphicsLayer mGraphicsLayer = null;

	private SimpleFillSymbol mSimpleFillSymbol;

	int test = 0;

	private CallbackListener<FeatureSet> callback = new CallbackListener<FeatureSet>() {

		public void onCallback(FeatureSet fSet) {

		}

		public void onError(Throwable arg0) {
			mGraphicsLayer.removeAll();
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// http://192.168.200.183/ArcGIS/rest/services/HA_BaseLayer/MapServer
		// http://192.168.200.183/ArcGIS/rest/services/HA_BZW/MapServer
		// http://192.168.200.183/ArcGIS/rest/services/HA_BuJian/MapServer
		map = new MapView(this);
		if (test == 0) {
			// 设置加载范围
			map.setExtent(new Envelope(-10868502.895856911, 4470034.144641369,
					-10837928.084542884, 4492965.25312689), 0);
		}

		String tileLayerURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";// 瓦片图层
		if (test == 1) {
			tileLayerURL = "http://192.168.200.183/ArcGIS/rest/services/HA_BaseLayer/MapServer";
		}
		ArcGISTiledMapServiceLayer tileLayer = new ArcGISTiledMapServiceLayer(
				tileLayerURL);
		map.addLayer(tileLayer);

		addFeatureLayer();// 添加属性图层
		addGraphicesLayer();// 添加要素图层
		// 注册图层触摸事件
		MyTouchListener touchListener = new MyTouchListener(this, map);
		map.setOnTouchListener(touchListener);

	}

	private void addGraphicesLayer() {
		mGraphicsLayer = new GraphicsLayer();
		mSimpleFillSymbol = new SimpleFillSymbol(Color.BLACK);// 填充色
		mSimpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLUE, 1));// 外框线
		mSimpleFillSymbol.setAlpha(100);

		map.addLayer(mGraphicsLayer);
		setContentView(map);

	}

	private void addFeatureLayer() {
		Options mOptions = new Options();
		mOptions.mode = MODE.ONDEMAND;
		mOptions.outFields = new String[] { "FIELD_KID", "APPROXACRE",
				"FIELD_NAME", "STATUS", "PROD_GAS", "PROD_OIL", "ACTIVEPROD",
				"CUMM_OIL", "MAXOILWELL", "LASTOILPRO", "LASTOILWEL",
				"LASTODATE", "CUMM_GAS", "MAXGASWELL", "LASTGASPRO",
				"LASTGASWEL", "LASTGDATE", "AVGDEPTH", "AVGDEPTHSL",
				"FIELD_TYPE", "FIELD_KIDN" };

		if (test == 1) {
			mOptions = new Options();
			mOptions.mode = MODE.ONDEMAND;
			mOptions.outFields = new String[] { "OBJECTID", "OBJCODE",
					"OBJNAME", "DEPTCODE1", "DEPTNAME1", "DEPTCODE2",
					"DEPTNAME2", "DEPTCODE3", "DEPTNAME3", "OBJSTATE",
					"CHDATE", "DATASOURCE", "NOTE", "OBJPHOTO", "ORDATE",
					"OBJPOS", "BGCODE", "SHAPE", };
		}

		// 属性图层
		String featureURL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSPetro/MapServer/1";
		if (test == 1) {
			featureURL = "http://192.168.200.183/ArcGIS/rest/services/HA_BuJian/MapServer/4";// 公用设施部件
		}
		mFeatureLayer = new ArcGISFeatureLayer(featureURL, mOptions);

		// 设置选中属性的颜色
		SimpleFillSymbol selectFillSymbol = new SimpleFillSymbol(Color.MAGENTA);
		selectFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 1));
		mFeatureLayer.setSelectionSymbol(selectFillSymbol);

		map.addLayer(mFeatureLayer);

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
				mGraphic = new Graphic(null, mSimpleFillSymbol);
				mFromPoint = map.toMapPoint(from.getX(), from.getY());
				uid = mGraphicsLayer.addGraphic(mGraphic);

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
					mFeatureLayer.clearSelection();
					Query mQuery = new Query();
					// 设置查询参数
					if (test == 1) {
						mQuery.setWhere("OBJNAME='上水井盖'");
					}
					if (test == 0) {
						mQuery.setWhere("PROD_GAS='Yes'");// 查询条件Yes or No
					}
					mQuery.setReturnGeometry(true);
					mQuery.setInSpatialReference(map.getSpatialReference());
					mQuery.setGeometry(mGraphic.getGeometry());
					mQuery.setSpatialRelationship(SpatialRelationship.INTERSECTS);
					// 查询到的信息高亮显示
					mFeatureLayer.selectFeatures(mQuery, SELECTION_METHOD.NEW,
							callback);
				}
				mGraphicsLayer.removeAll();
			}

			mFromPoint = null;
			// 重置
			uid = -1;
			return true;

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

}