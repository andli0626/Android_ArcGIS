package com.andli.attribute;

/* Copyright 2011 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.andli.attribute.FeatureLayerUtils.FieldType;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

/**
 * Main activity class for the Attribute Editor Sample
 */
public class AttributeEditorActivity extends Activity {

	MapView mapView;

	ArcGISFeatureLayer featureLayer;
	ArcGISDynamicMapServiceLayer dmsl;

	Point pointClicked;

	LayoutInflater inflator;

	AttributeListAdp listAdp;

	Envelope initextent;

	ListView listView;

	View listLayout;

	public static final String TAG = "andli";

	static final int ATTRIBUTE_EDITOR_DIALOG_ID = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mapView = new MapView(this);

		initextent = new Envelope(-10868502.895856911, 4470034.144641369,
				-10837928.084542884, 4492965.25312689);
		mapView.setExtent(initextent, 0);
		ArcGISTiledMapServiceLayer tmsl = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
		mapView.addLayer(tmsl);

		dmsl = new ArcGISDynamicMapServiceLayer(
				"http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSFields/MapServer");
		mapView.addLayer(dmsl);

		featureLayer = new ArcGISFeatureLayer(
				"http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSFields/FeatureServer/0",
				MODE.SELECTION);
		setContentView(mapView);

		SimpleFillSymbol sfs = new SimpleFillSymbol(Color.TRANSPARENT);
		sfs.setOutline(new SimpleLineSymbol(Color.YELLOW, 3));
		featureLayer.setSelectionSymbol(sfs);

		// set up local variables
		inflator = LayoutInflater.from(getApplicationContext());
		listLayout = inflator.inflate(R.layout.list_layout, null);
		listView = (ListView) listLayout.findViewById(R.id.list_view);

		// Create a new AttributeListAdapter when the feature layer is
		// initialized
		if (featureLayer.isInitialized()) {
			listAdp = new AttributeListAdp(this, featureLayer.getFields(),
					featureLayer.getTypes(), featureLayer.getTypeIdField());
		} else {

			featureLayer
					.setOnStatusChangedListener(new OnStatusChangedListener() {
						public void onStatusChanged(Object source, STATUS status) {
							if (status == STATUS.INITIALIZED) {
								listAdp = new AttributeListAdp(
										AttributeEditorActivity.this,
										featureLayer.getFields(), featureLayer
												.getTypes(), featureLayer
												.getTypeIdField());
							}
						}
					});
		}

		mapView.setOnSingleTapListener(new OnSingleTapListener() {
			public void onSingleTap(float x, float y) {
				editMethod(x, y);
			}
		});

	}

	protected void editMethod(float x, float y) {
		pointClicked = mapView.toMapPoint(x, y);

		// 为点击的属性创建一个查询
		Query query = new Query();
		query.setOutFields(new String[] { "*" });
		query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
		query.setGeometry(pointClicked);
		query.setInSpatialReference(mapView.getSpatialReference());

		// call the select features method and implement the
		// callbacklistener
		featureLayer.selectFeatures(query,
				ArcGISFeatureLayer.SELECTION_METHOD.NEW,
				new CallbackListener<FeatureSet>() {

					// handle any errors
					public void onError(Throwable e) {
						Log.i(TAG,
								"Select Features Error"
										+ e.getLocalizedMessage());
					}

					public void onCallback(FeatureSet queryResults) {

						if (queryResults.getGraphics().length > 0) {
							Log.i(TAG, "查询结果"
									+ queryResults.getGraphics().length);
							Log.i(TAG,
									"Feature found id="
											+ queryResults.getGraphics()[0]
													.getAttributeValue(featureLayer
															.getObjectIdField()));

							listAdp.setFeatureSet(queryResults);
							listAdp.notifyDataSetChanged();

							// This callback is not run in the main UI
							// thread. All GUI
							// related events must run in the UI thread,
							// therefore use the
							// Activity.runOnUiThread() method. See
							// http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)
							// for more information.
							AttributeEditorActivity.this
									.runOnUiThread(new Runnable() {
										public void run() {
											showDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
										}
									});
						} else {
							Log.i(TAG, "查询结果0"
									+ queryResults.getGraphics().length);
						}
					}
				});

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case ATTRIBUTE_EDITOR_DIALOG_ID:
			Dialog dialog = new Dialog(this);
			listView.setAdapter(listAdp);
			dialog.setContentView(listLayout);
			dialog.setTitle("属性编辑");

			Button btnEditCancel = (Button) listLayout
					.findViewById(R.id.btn_edit_discard);
			btnEditCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
				}
			});

			Button btnEditApply = (Button) listLayout
					.findViewById(R.id.btn_edit_apply);
			btnEditApply
					.setOnClickListener(returnOnClickApplyChangesListener());

			return dialog;
		}
		return null;
	}

	// 提交数据
	public OnClickListener returnOnClickApplyChangesListener() {

		return new OnClickListener() {

			public void onClick(View v) {
				commitData();
			}
		};

	}

	protected void commitData() {

		boolean isTypeField = false;
		boolean hasEdits = false;
		boolean updateMapLayer = false;
		Map<String, Object> attrs = new HashMap<String, Object>();

		// loop through each attribute and set the new values if they have
		// changed
		for (int i = 0; i < listAdp.getCount(); i++) {

			AttributeItem item = (AttributeItem) listAdp.getItem(i);
			String value = "";

			// check to see if the View has been set
			if (item.getView() != null) {

				// TODO implement applying domain fields values if required
				// determine field type and therefore View type
				if (item.getField().getName()
						.equals(featureLayer.getTypeIdField())) {
					// drop down spinner

					Spinner spinner = (Spinner) item.getView();
					// get value for the type
					String typeName = spinner.getSelectedItem().toString();
					value = FeatureLayerUtils.returnTypeIdFromTypeName(
							featureLayer.getTypes(), typeName);

					// update map layer as for this featurelayer the type change
					// will
					// change the features symbol.
					isTypeField = true;

				} else if (FieldType.determineFieldType(item.getField()) == FieldType.DATE) {
					// date

					Button dateButton = (Button) item.getView();
					value = dateButton.getText().toString();

				} else {
					// edit text

					EditText editText = (EditText) item.getView();
					value = editText.getText().toString();

				}

				// try to set the attribute value on the graphic and see if it
				// has
				// been changed
				boolean hasChanged = FeatureLayerUtils.setAttribute(attrs,
						listAdp.featureSet.getGraphics()[0], item.getField(),
						value, listAdp.formatter);

				// if a value has for this field, log this and set the hasEdits
				// boolean to true
				if (hasChanged) {

					Log.i(TAG, "Change found for field="
							+ item.getField().getName() + " value = " + value
							+ " applyEdits() will be called");
					hasEdits = true;

					// If the change was from a Type field then set the dynamic
					// map
					// service to update when the edits have been applied, as
					// the
					// renderer of the feature will likely change
					if (isTypeField) {

						updateMapLayer = true;

					}
				}

				// check if this was a type field, if so set boolean back to
				// false
				// for next field
				if (isTypeField) {

					isTypeField = false;
				}
			}
		}

		// check there have been some edits before applying the changes
		if (hasEdits) {

			// set objectID field value from graphic held in the featureset
			attrs.put(featureLayer.getObjectIdField(), listAdp.featureSet
					.getGraphics()[0].getAttributeValue(featureLayer
					.getObjectIdField()));
			Graphic newGraphic = new Graphic(null, null, attrs, null);
			featureLayer.applyEdits(null, null, new Graphic[] { newGraphic },
					createEditCallbackListener(updateMapLayer));
		}

		// close the dialog
		dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

	}

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

					Log.i(AttributeEditorActivity.TAG,
							"Success updating feature with id="
									+ result[2][0].getObjectId());

					// see if we want to update the dynamic layer to get new
					// symbols for
					// updated features
					if (updateLayer) {

						dmsl.refresh();

					}
				}
			}

			public void onError(Throwable e) {

				Log.i(AttributeEditorActivity.TAG, "error updating feature: "
						+ e.getLocalizedMessage());

			}
		};
	}
}