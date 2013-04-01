/*
 * Copyright � 2010 ESRI
 * All rights reserved under the copyright laws of the United States and applicable international laws, treaties, and conventions.
 * You may freely redistribute and use this sample code, with or without modification, provided you include the original copyright notice and use restrictions.
 * Disclaimer: THE SAMPLE CODE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESRI OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) SUSTAINED BY YOU OR A THIRD PARTY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT ARISING IN ANY WAY OUT OF THE USE OF THIS SAMPLE CODE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts and Legal Services Department
 * 380 New York Street Redlands, California, 92373
 * USA
 * email: contracts@esri.com
 */
package com.esri.arcgis.android.samples.graphicelements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class DrawGraphicElements extends Activity implements OnClickListener {

	MapView mapView = null;
	ArcGISTiledMapServiceLayer mTileMapLayer = null;
	GraphicsLayer graphicsLayer = null;
	MyTouchListener mTouchListener = null;
	Button geometryButton = null;
	Button clearButton = null;
	String mapURL = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/PublicSafety/PublicSafetyBasemap/MapServer";
	final String[] geometryTypes = new String[] { "绘制点", "绘制线", "绘制区域" };
	int selectedGeometryIndex = -1;

	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mapView = (MapView) findViewById(R.id.map);
		mTouchListener = new MyTouchListener(DrawGraphicElements.this, mapView);
		mapView.setOnTouchListener(mTouchListener);

		geometryButton = (Button) findViewById(R.id.geometrybutton);
		geometryButton.setEnabled(false);
		geometryButton.setOnClickListener(this);

		clearButton = (Button) findViewById(R.id.clearbutton);
		clearButton.setOnClickListener(this);

		mTileMapLayer = new ArcGISTiledMapServiceLayer(mapURL);
		graphicsLayer = new GraphicsLayer();
		mapView.addLayer(mTileMapLayer);
		mapView.addLayer(graphicsLayer);// 添加客户端要素图层

		// 地图加载的时候被调用：检查地图是否可用
		mTileMapLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
			public void onStatusChanged(Object arg0, STATUS status) {
				if (status.equals(OnStatusChangedListener.STATUS.INITIALIZED)) {
					// 可用绘制按钮可用
					geometryButton.setEnabled(true);
				}
			}
		});

	}

	// 地图触摸事件
	class MyTouchListener extends MapOnTouchListener {

		MultiPath mMutiPath;//多路径
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
				graphicsLayer.removeAll();//清空要素图层

				Point point = mapView.toMapPoint(new Point(e.getX(), e.getY()));
				// 注意：因手机分辨率的差异，像素点的大小是有差别的
				SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(
						Color.BLUE, 30, STYLE.CIRCLE);
				Graphic graphic = new Graphic(point, markerSymbol);
				graphicsLayer.addGraphic(graphic);

				clearButton.setEnabled(true);
				return true;
			}
			return false;

		}

		//绘制一个点并移动
		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			if (type.length() > 1
					&& (type.equalsIgnoreCase("POLYLINE") || type
							.equalsIgnoreCase("POLYGON"))) {

				//终点
				Point toPoint = mapView.toMapPoint(to.getX(), to.getY());

				if (startPoint == null) {
					graphicsLayer.removeAll();
					//绘制线还是绘制区域
					mMutiPath = type.equalsIgnoreCase("POLYLINE") ? new Polyline(): new Polygon();
					//记录开始点
					startPoint = mapView.toMapPoint(from.getX(), from.getY());
					mMutiPath.startPath((float) startPoint.getX(),(float) startPoint.getY());

					SimpleLineSymbol lineSymbol=new SimpleLineSymbol(Color.BLUE,5);
					Graphic graphic = new Graphic(startPoint,lineSymbol);
					graphicsLayer.addGraphic(graphic);
				}

				mMutiPath.lineTo((float) toPoint.getX(), (float) toPoint.getY());

				return true;
			}
			return super.onDragPointerMove(from, to);

		}

		//绘制一个点后抬起
		@Override
		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			if (type.length() > 1
					&& (type.equalsIgnoreCase("POLYLINE") || type
							.equalsIgnoreCase("POLYGON"))) {
				if (type.equalsIgnoreCase("POLYGON")) {
					mMutiPath.lineTo((float) startPoint.getX(),(float) startPoint.getY());
					graphicsLayer.removeAll();
					//添加面
					Graphic graphic=new Graphic(mMutiPath,new SimpleFillSymbol(Color.WHITE)); 
					graphicsLayer.addGraphic(graphic);
					startPoint = null;
					clearButton.setEnabled(true);
				}
				else{
					//添加线
					Graphic graphic=new Graphic(mMutiPath,new SimpleLineSymbol(Color.BLUE, 5));
					graphicsLayer.addGraphic(graphic);
					startPoint = null;
					clearButton.setEnabled(true);
				}
				return true;
			}
			return super.onDragPointerUp(from, to);
		}
	}

	// 对话框
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(DrawGraphicElements.this).setTitle("绘制")
				.setItems(geometryTypes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						graphicsLayer.removeAll();
						String geomType = geometryTypes[which];
						selectedGeometryIndex = which;
						if (geomType.equalsIgnoreCase(geometryTypes[2])) {
							mTouchListener.setType("POLYGON");
						} else if (geomType.equalsIgnoreCase(geometryTypes[1])) {
							mTouchListener.setType("POLYLINE");
						} else if (geomType.equalsIgnoreCase(geometryTypes[0])) {
							mTouchListener.setType("POINT");
						}
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

	@Override
	public void onClick(View v) {
		if (v == geometryButton) {
			showDialog(0);
		} else if (v == clearButton) {
			graphicsLayer.removeAll();
			clearButton.setEnabled(false);
		}

	}

}