package com.andli.attributedemo;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Field;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

public class MyMapView extends Activity {

	private MapView mMapView;

	private ArcGISTiledMapServiceLayer tileLayer;
	private ArcGISFeatureLayer featureLayer;
	private ArcGISDynamicMapServiceLayer dynamicLayer;

	private String tileLayerURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
	private String dynamicLayerURL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSFields/MapServer";
	private String featureLayerURL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSFields/FeatureServer/0";

	private Point pointClicked;

	private LayoutInflater inflator;

	private PointAdp pointAdp;

	private Envelope initextent;

	private ListView lvData;

	private View listLayout;

	public static final String TAG = "andli";

	private static final int ATTRIBUTE_EDITOR_DIALOG_ID = 1;

	@SuppressWarnings("serial")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// tileLayerURL =
		// "http://192.168.1.109/ArcGIS/rest/services/HA_BaseLayer/MapServer";
		// dynamicLayerURL =
		// "http://192.168.1.109/ArcGIS/rest/services/HA_BaseLayer/MapServer";
		// featureLayerURL =
		// "http://192.168.1.109/ArcGIS/rest/services/HA_BaseLayer/FeatureServer/5";

		// tileLayerURL =
		// "http://192.168.1.109/ArcGIS/rest/services/HA_BaseLayer/MapServer";
		// dynamicLayerURL =
		// "http://192.168.1.109/ArcGIS/rest/services/HA_BuJian/MapServer";
		// featureLayerURL =
		// "http://192.168.1.109/ArcGIS/rest/services/HA_BuJian/FeatureServer/4";

		mMapView = new MapView(this);

		initextent = new Envelope(-10868502.895856911, 4470034.144641369,
				-10837928.084542884, 4492965.25312689);
		mMapView.setExtent(initextent, 0);

		addLayers();

		setContentView(mMapView);

		initFeatureLayer();// 初始化属性图层

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			public void onSingleTap(float x, float y) {
				MySingleTapListener(x, y);
			}
		});

	}

	@SuppressWarnings("serial")
	private void initFeatureLayer() {
		// 注意属性图层是用于查询的
		featureLayer = new ArcGISFeatureLayer(featureLayerURL, MODE.SELECTION);

		SimpleFillSymbol sfs = new SimpleFillSymbol(Color.TRANSPARENT);
		sfs.setOutline(new SimpleLineSymbol(Color.YELLOW, 3));
		featureLayer.setSelectionSymbol(sfs);

		if (featureLayer.isInitialized()) {
			Field[] mFields = featureLayer.getFields();
			for (int i = 0; i < mFields.length; i++) {
				Log.i(TAG, i + "属性名称=" + mFields[i].getName());
			}
			FeatureType[] types = featureLayer.getTypes();
			for (int i = 0; i < types.length; i++) {
				Log.i(TAG, i + "types=" + types[i].getName());
			}
			String typeIdFieldName = featureLayer.getTypeIdField();
			Log.i(TAG, "typeIdFieldName=" + typeIdFieldName);
			pointAdp = new PointAdp(this, mFields, types, typeIdFieldName);
		} else {
			featureLayer
					.setOnStatusChangedListener(new OnStatusChangedListener() {
						public void onStatusChanged(Object source, STATUS status) {
							if (status == STATUS.INITIALIZED) {
								pointAdp = new PointAdp(MyMapView.this, //
										featureLayer.getFields(),//
										featureLayer.getTypes(), //
										featureLayer.getTypeIdField());
							}
						}
					});
		}

	}

	// 添加图层
	private void addLayers() {
		tileLayer = new ArcGISTiledMapServiceLayer(tileLayerURL);
		mMapView.addLayer(tileLayer);

		dynamicLayer = new ArcGISDynamicMapServiceLayer(dynamicLayerURL);
		mMapView.addLayer(dynamicLayer);

	}

	protected void MySingleTapListener(float x, float y) {
		pointClicked = mMapView.toMapPoint(x, y);
		// 缓冲为多边形处理
		Geometry geo2 = GeometryEngine.buffer(pointClicked,
				mMapView.getSpatialReference(), 2000000, null);

		// 对该点创建查询
		Query query = new Query();
		query.setOutFields(new String[] { "*" });
		query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
		query.setGeometry(pointClicked);

		query.setInSpatialReference(mMapView.getSpatialReference());
		// featureLayer.getGraphicIDs(x, y, 10);

		// 执行查询
		featureLayer.selectFeatures(query,
				ArcGISFeatureLayer.SELECTION_METHOD.NEW,
				new CallbackListener<FeatureSet>() {// 回调函数

					// 对错误的处理
					public void onError(Throwable e) {
						Log.i(TAG, "报错--" + e.getLocalizedMessage());
					}

					public void onCallback(FeatureSet queryResults) {
						// 对查询的结果进行处理
						dealQueryResults(queryResults);
					}
				});

	}

	protected void dealQueryResults(FeatureSet queryResults) {

		if (queryResults.getGraphics().length > 0) {
			Log.i(TAG, "查询结果" + queryResults.getGraphics().length);
			int id = (Integer) queryResults.getGraphics()[0]
					.getAttributeValue(featureLayer.getObjectIdField());
			Log.i(TAG, "Feature found id=" + id);

			pointAdp.setFeatureSet(queryResults);// 传递参数该适配器
			pointAdp.notifyDataSetChanged();
			// 运行在非主线程
			MyMapView.this.runOnUiThread(new Runnable() {
				public void run() {
					// 弹出对话框
					showDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
				}
			});
		} else {
			Log.i(TAG, "查询结果0");
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case ATTRIBUTE_EDITOR_DIALOG_ID:
			Dialog dialog = new Dialog(this);

			inflator = LayoutInflater.from(getApplicationContext());
			listLayout = inflator.inflate(R.layout.list_layout, null);
			lvData = (ListView) listLayout.findViewById(R.id.list_view);
			lvData.setAdapter(pointAdp);

			dialog.setContentView(listLayout);
			dialog.setTitle("属性编辑");

			Button cancelBtn = (Button) listLayout
					.findViewById(R.id.btn_edit_discard);
			cancelBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
				}
			});

			Button applyBtn = (Button) listLayout
					.findViewById(R.id.btn_edit_apply);
			applyBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// commitData();
				}
			});

			return dialog;
		}
		return null;
	}

	// protected void commitData() {
	//
	// boolean isTypeField = false;
	// boolean hasEdits = false;
	// boolean updateMapLayer = false;
	// Map<String, Object> attrs = new HashMap<String, Object>();
	//
	// // loop through each attribute and set the new values if they have
	// // changed
	// for (int i = 0; i < pointAdp.getCount(); i++) {
	//
	// PointInfo item = (PointInfo) pointAdp.getItem(i);
	// String value = "";
	//
	// // check to see if the View has been set
	// if (item.getView() != null) {
	//
	// // TODO implement applying domain fields values if required
	// // determine field type and therefore View type
	// if (item.getField().getName()
	// .equals(featureLayer.getTypeIdField())) {
	// // drop down spinner
	//
	// Spinner spinner = (Spinner) item.getView();
	// // get value for the type
	// String typeName = spinner.getSelectedItem().toString();
	// value = FeatureLayerUtils.returnTypeIdFromTypeName(
	// featureLayer.getTypes(), typeName);
	//
	// // update map layer as for this featurelayer the type change
	// // will
	// // change the features symbol.
	// isTypeField = true;
	//
	// } else if (FieldType.determineFieldType(item.getField()) ==
	// FieldType.DATE) {
	// // date
	//
	// Button dateButton = (Button) item.getView();
	// value = dateButton.getText().toString();
	//
	// } else {
	// // edit text
	//
	// EditText editText = (EditText) item.getView();
	// value = editText.getText().toString();
	//
	// }
	//
	// // try to set the attribute value on the graphic and see if it
	// // has
	// // been changed
	// boolean hasChanged = FeatureLayerUtils.setAttribute(attrs,
	// pointAdp.mFeatureSet.getGraphics()[0], item.getField(),
	// value, pointAdp.formatter);
	//
	// // if a value has for this field, log this and set the hasEdits
	// // boolean to true
	// if (hasChanged) {
	//
	// Log.i(TAG, "Change found for field="
	// + item.getField().getName() + " value = " + value
	// + " applyEdits() will be called");
	// hasEdits = true;
	//
	// // If the change was from a Type field then set the dynamic
	// // map
	// // service to update when the edits have been applied, as
	// // the
	// // renderer of the feature will likely change
	// if (isTypeField) {
	//
	// updateMapLayer = true;
	//
	// }
	// }
	//
	// // check if this was a type field, if so set boolean back to
	// // false
	// // for next field
	// if (isTypeField) {
	//
	// isTypeField = false;
	// }
	// }
	// }
	//
	// // check there have been some edits before applying the changes
	// if (hasEdits) {
	//
	// // set objectID field value from graphic held in the featureset
	// attrs.put(featureLayer.getObjectIdField(), pointAdp.mFeatureSet
	// .getGraphics()[0].getAttributeValue(featureLayer
	// .getObjectIdField()));
	// Graphic newGraphic = new Graphic(null, null, attrs, null);
	// featureLayer.applyEdits(null, null, new Graphic[] { newGraphic },
	// createEditCallbackListener(updateMapLayer));
	// }
	//
	// // close the dialog
	// dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
	//
	// }

	/**
	 * Helper method to create a CallbackListener<FeatureEditResult[][]>
	 * 
	 * @return CallbackListener<FeatureEditResult[][]>
	 */
	CallbackListener<FeatureEditResult[][]> createEditCallbackListener(
			final boolean updateLayer) {

		return new CallbackListener<FeatureEditResult[][]>() {

			public void onCallback(FeatureEditResult[][] result) {

				// check the response for success or failure
				if (result[2] != null && result[2][0] != null
						&& result[2][0].isSuccess()) {

					Log.i(MyMapView.TAG, "Success updating feature with id="
							+ result[2][0].getObjectId());

					// see if we want to update the dynamic layer to get new
					// symbols for
					// updated features
					if (updateLayer) {
						dynamicLayer.refresh();
					}
				}
			}

			public void onError(Throwable e) {

				Log.i(MyMapView.TAG,
						"error updating feature: " + e.getLocalizedMessage());

			}
		};
	}
}