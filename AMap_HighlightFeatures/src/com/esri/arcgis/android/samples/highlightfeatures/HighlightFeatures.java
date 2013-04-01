package com.esri.arcgis.android.samples.highlightfeatures;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnStatusChangedListener;
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

public class HighlightFeatures extends Activity {

	MapView mapView;
	ArcGISTiledMapServiceLayer tiledMapServiceLayer;
	GraphicsLayer graphicsLayer;
	Graphic[] highlightGraphics;
	ArrayList<IdentifyResult> identifyResults;
	Button clearButton;
	Button layerButton;
	TextView label;
	TextView idRes;

	String mapURL = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/PublicSafety/PublicSafetyBasemap/MapServer";

	final String[] layerNames = new String[] { "Interstates", "US Highways",
			"Major and Minor Streets", "WaterBodies",
			"Cemetaries, Parks, Golf Courses" };

	final int[] layerIndexes = new int[] { 6, 7, 8, 28, 37 };

	int selectedLayerIndex = -1;

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mapView = (MapView) findViewById(R.id.map);
		mapView.setOnLongPressListener(new OnLongPressListener() {
			public void onLongPress(float x, float y) {
				try {
					if (tiledMapServiceLayer.isInitialized()
							&& selectedLayerIndex >= 0) {
						graphicsLayer.removeAll();
						Point pointClicked = mapView.toMapPoint(x, y);

						IdentifyParameters params = new IdentifyParameters();
						params.setGeometry(pointClicked);
						params.setLayers(new int[] { layerIndexes[selectedLayerIndex] });
						Envelope env = new Envelope();
						mapView.getExtent().queryEnvelope(env);
						params.setSpatialReference(mapView
								.getSpatialReference());
						params.setMapExtent(env);
						params.setDPI(96);
						params.setMapHeight(mapView.getHeight());
						params.setMapWidth(mapView.getWidth());
						params.setTolerance(10);

						MyIdentifyTask mIdenitfy = new MyIdentifyTask();
						mIdenitfy.execute(params);

					} else {
						Toast.makeText(
								getApplicationContext(),
								"Please select a layer to identify features from.",
								3000).show();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		layerButton = (Button) findViewById(R.id.layerbutton);
		layerButton.setEnabled(false);
		layerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(0);
			}
		});

		label = (TextView) findViewById(R.id.label);

		clearButton = (Button) findViewById(R.id.clearbutton);
		clearButton.setEnabled(false);
		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				graphicsLayer.removeAll();
				clearButton.setEnabled(false);
			}
		});

		Object[] init = (Object[]) getLastNonConfigurationInstance();
		if (init != null) {
			mapView.restoreState((String) init[0]);

			int index = ((Integer) init[1]).intValue();
			if (index != -1) {
				label.setText(layerNames[index]);
				selectedLayerIndex = index;
			}
		} else {
			mapView.setExtent(new Envelope(-85.61828847183895,
					38.19242311866144, -85.53589100936443, 38.31361605305102));

		}

		tiledMapServiceLayer = new ArcGISTiledMapServiceLayer(mapURL);
		graphicsLayer = new GraphicsLayer();

		tiledMapServiceLayer
				.setOnStatusChangedListener(new OnStatusChangedListener() {
					public void onStatusChanged(Object arg0, STATUS status) {
						if (status
								.equals(OnStatusChangedListener.STATUS.INITIALIZED)) {
							layerButton.setEnabled(true);
						}
					}
				});
		mapView.addLayer(tiledMapServiceLayer);
		mapView.addLayer(graphicsLayer);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Object[] objs = new Object[2];
		objs[0] = mapView.retainState();
		objs[1] = Integer.valueOf(selectedLayerIndex);
		return objs;
	}

	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(HighlightFeatures.this)
				.setTitle("Select a Layer")
				.setItems(layerNames, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						label.setText(layerNames[which] + " selected.");
						selectedLayerIndex = which;
					}
				}).create();
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
					Geometry geom = results[i].getGeometry();
					String typeName = geom.getType().name();

					Random r = new Random();
					int color = Color.rgb(r.nextInt(255), r.nextInt(255),
							r.nextInt(255));
					if (typeName.equalsIgnoreCase("point")) {
						SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color,
								20, STYLE.SQUARE);
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
					clearButton.setEnabled(true);
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"No features identified.", 3000).show();

			}

		}

		@Override
		protected void onPreExecute() {
			mIdentifyTask = new IdentifyTask(mapURL);
		}

	}

}
