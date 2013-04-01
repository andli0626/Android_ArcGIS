package com.epoint.arcgisdemo;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class AMapHelp {
	// 荆州社管通地图
	public static final String IMAGE = "JZ_Image";// 影像图JZ_Image
	public static final String NEWIMAGE = "JZ_NewImage";// 影像图:合并了单元网格，道路，标志物
	public static final String BASELAYER = "JZ_BaseLayer";// 地图
	public static final String FANGWU = "JZ_FangWu";// 房屋：0标志位，1房屋
	public static final String JZ_BZWBZD_WHITE = "JZ_BZWBZD_White";// 标志物（白色标注）
	public static final String JZ_BZWBZD_BLACK = "JZ_BZWBZD_Black";// 标志物（黑色标注）
	public static final String BUJIAN = "JZ_BuJian";// 部件图层
	public static final String ROAD = "JZ_Road";// 道路图层
	public static final String DYWG = "JZ_DYWG";// 单元网格

	public static final int YXTINDEX = 0;// 影像图编号
	public static final int SLTINDEX = 1;// 矢量图图编号

	public static final int defaultLoad = 0;// 0默认加载影像图

	public static final int TIME = 2;// 定时时间--用于自动定位

	public static String getMapURL(Context context, String layername) {
		return "http://" + getMapIP(context) + "/ArcGIS/rest/services/"
				+ layername + "/MapServer";
	}

	public static String getMapIP(Context context) {
		// DBHelp dbHelp = new DBHelp(context);
		// String ip = "";
		// if (TextUtils.isEmpty(dbHelp.getConfigValue(ConfigKey.mapip))) {
		// ip = context.getString(R.string.mapip);
		// } else {
		// ip = dbHelp.getConfigValue(ConfigKey.mapip);
		// }
		 return "58.54.244.254:9000";
//		return "192.168.200.188";
	}

	public static String getFeatureMapURL(Context context, String layername,
			int id) {
		return "http://" + getMapIP(context) + "/ArcGIS/rest/services/"
				+ layername + "/FeatureServer/" + id;

	}

	public static String getFeatureMapURL(Context context, String layername) {
		return "http://" + getMapIP(context) + "/ArcGIS/rest/services/"
				+ layername + "/FeatureServer";

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-27 下午12:17:34
	 * @annotation 显示指定图层
	 */
	public static void showLayerByID(ArcGISDynamicMapServiceLayer dynamicLayer,
			int id) {
		// 获取动态图层的所有子图层
		if (dynamicLayer.isInitialized()) {
			ArcGISLayerInfo[] layers = dynamicLayer.getAllLayers();
			if (layers != null && layers.length > 0) {
				Log.i("andli", "个数=" + layers.length);
				for (int i = 0; i < layers.length; i++) {
					if (!TextUtils.isEmpty(layers[i].getName())) {
						if (layers[i].getId() == id) {
							layers[i].setVisible(true);
							Log.i("andli", "显示    " + layers[i].getName()
									+ "ID=" + layers[i].getId());

						} else {
							Log.i("andli", "隐藏    " + layers[i].getName()
									+ "ID=" + layers[i].getId());
							layers[i].setVisible(false);
						}
					}
				}
			}
		}

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-27 下午12:17:34
	 * @annotation 隐藏指定图层
	 */
	public static void hideLayerByID(ArcGISDynamicMapServiceLayer dynamicLayer,
			int id) {
		if (dynamicLayer.isInitialized()) {
			ArcGISLayerInfo[] layers = dynamicLayer.getAllLayers();
			if (layers != null && layers.length > 0) {
				for (int i = 0; i < layers.length; i++) {
					if (layers[i].getId() == id) {
						layers[i].setVisible(false);
						Log.i("andli", "隐藏   " + layers[i].getName());
					}
				}
			}
		}

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-27 下午12:17:34
	 * @annotation 显示指定图层
	 */
	public static void showLayerByName(
			ArcGISDynamicMapServiceLayer dynamicLayer, String name) {
		// 获取动态图层的所有子图层
		if (dynamicLayer.isInitialized()) {
			ArcGISLayerInfo[] layers = dynamicLayer.getAllLayers();
			if (layers != null && layers.length > 0) {
				Log.i("andli", "个数=" + layers.length);
				for (int i = 0; i < layers.length; i++) {
					if (!TextUtils.isEmpty(layers[i].getName())) {
						if (layers[i].getName().equals(name)) {
							layers[i].setVisible(false);
							Log.i("andli", "隐藏" + layers[i].getName() + "ID="
									+ layers[i].getId());

						} else {
							Log.i("andli", "显示" + layers[i].getName() + "ID="
									+ layers[i].getId());
						}
					}
				}
			}
		}

	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-7 上午10:18:53
	 * @annotation 在要素图层上绘制一个点
	 */
	// public static int drawPoint(Context con, Point point,
	// GraphicsLayer graphicsLayer, int id) {
	// // graphicsLayer.removeAll();
	// graphicsLayer.removeGraphic(id);
	// Drawable drawable = con.getResources().getDrawable(R.drawable.gps2);
	// PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(
	// drawable);
	// Graphic mGraphic = new Graphic(point, pictureMarkerSymbol);
	// return graphicsLayer.addGraphic(mGraphic);
	// }

	public static int drawPointByID(Context con, Point point,
			GraphicsLayer graphicsLayer, int id, int imageid) {
		// graphicsLayer.removeAll();
		graphicsLayer.removeGraphic(id);
		Drawable drawable = con.getResources().getDrawable(imageid);
		PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(
				drawable);
		Graphic mGraphic = new Graphic(point, pictureMarkerSymbol);
		return graphicsLayer.addGraphic(mGraphic);
	}

	// public static void drawPoint(Context con, Point point,
	// GraphicsLayer graphicsLayer) {
	// graphicsLayer.removeAll();
	// Drawable drawable = con.getResources().getDrawable(R.drawable.gps2);
	// PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(
	// drawable);
	// Graphic mGraphic = new Graphic(point, pictureMarkerSymbol);
	// graphicsLayer.addGraphic(mGraphic);
	//
	// }

	public static void drawPointByID(Context con, Point point,
			GraphicsLayer graphicsLayer, int imageiD) {
		graphicsLayer.removeAll();
		Drawable drawable = con.getResources().getDrawable(imageiD);
		PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(
				drawable);
		Graphic mGraphic = new Graphic(point, pictureMarkerSymbol);
		graphicsLayer.addGraphic(mGraphic);

	}

	// // 绘制前：清空要素图层
	// public static void drawGPSPoint(Context con, Point point,
	// GraphicsLayer graphicsLayer) {
	// // 清空要素图层
	// graphicsLayer.removeAll();
	// Drawable drawable = con.getResources().getDrawable(R.drawable.location);
	// PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(
	// drawable);
	// Graphic mGraphic = new Graphic(point, pictureMarkerSymbol);
	// graphicsLayer.addGraphic(mGraphic);
	//
	// }

	// // 根据ID删除
	// public static int drawGPSPoint(Context con, Point point,
	// GraphicsLayer graphicsLayer, int id) {
	// graphicsLayer.removeGraphic(id);
	// Drawable drawable = con.getResources().getDrawable(R.drawable.location);
	// PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(
	// drawable);
	// Graphic mGraphic = new Graphic(point, pictureMarkerSymbol);
	// return graphicsLayer.addGraphic(mGraphic);
	//
	// }

	// http://192.168.200.188/ArcGIS/rest/services/JZ_Image/ImageServer
	public static String getImageMapURL(Context context, String layername) {
		return "http://" + getMapIP(context) + "/ArcGIS/rest/services/"
				+ layername + "/ImageServer";
	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-14 下午4:31:44
	 * @annotation 高亮显示查询结果
	 */
	public static void lightShowQueryResult(Geometry geom,
			GraphicsLayer lightGraphicsLayer) {
		Type layerType = geom.getType();
		String typeName = layerType.name();// 图层类型
		// 高亮显示查询结果
		Random r = new Random();
		Graphic graphic = null;
		int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));// 生成随机色
		Log.i("andli", "随机色" + color);
		// 随机色-12786487--浅蓝色
		// 随机色-6340889 紫色

		// color = -12786487;
		color = 0x38092f;
		if (typeName.equalsIgnoreCase("point")) {
			SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20,
					STYLE.SQUARE);
			graphic = new Graphic(geom, sms);
		} else if (typeName.equalsIgnoreCase("polyline")) {
			SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
			graphic = new Graphic(geom, sls);
		} else if (typeName.equalsIgnoreCase("polygon")) {
			SimpleFillSymbol sfs = new SimpleFillSymbol(color);
			sfs.setAlpha(100);// 设置透明度
			graphic = new Graphic(geom, sfs);
		}
		lightGraphicsLayer.addGraphic(graphic);
	}

	public static int lightShowQueryResult(Geometry geom,
			GraphicsLayer lightGraphicsLayer, int id) {
		lightGraphicsLayer.removeGraphic(id);
		Type layerType = geom.getType();
		String typeName = layerType.name();// 图层类型
		// 高亮显示查询结果
		Random r = new Random();
		Graphic graphic = null;
		int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));// 生成随机色
		Log.i("andli", "随机色" + color);
		// 随机色-12786487--浅蓝色
		// 随机色-6340889 紫色

		// color = -12786487;
		color = 0x38092f;
		if (typeName.equalsIgnoreCase("point")) {
			SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20,
					STYLE.SQUARE);
			graphic = new Graphic(geom, sms);
		} else if (typeName.equalsIgnoreCase("polyline")) {
			SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
			graphic = new Graphic(geom, sls);
		} else if (typeName.equalsIgnoreCase("polygon")) {
			SimpleFillSymbol sfs = new SimpleFillSymbol(color);
			sfs.setAlpha(100);// 设置透明度
			graphic = new Graphic(geom, sfs);
		}
		return lightGraphicsLayer.addGraphic(graphic);
	}

	public enum FieldType {
		NUMBER, STRING, DECIMAL, DATE;

		public static FieldType determineFieldType(Field field) {

			if (field.getFieldType() == Field.esriFieldTypeString) {
				return FieldType.STRING;
			} else if (field.getFieldType() == Field.esriFieldTypeSmallInteger
					|| field.getFieldType() == Field.esriFieldTypeInteger) {
				return FieldType.NUMBER;
			} else if (field.getFieldType() == Field.esriFieldTypeSingle
					|| field.getFieldType() == Field.esriFieldTypeDouble) {
				return FieldType.DECIMAL;
			} else if (field.getFieldType() == Field.esriFieldTypeDate) {
				return FieldType.DATE;
			}
			return null;
		}
	}

	/**
	 * 
	 * @author lilin
	 * @date 2012-9-19 下午2:26:54
	 * @annotation 属性是否可编辑
	 */
	public static boolean isFieldValidForEditing(Field field) {

		int fieldType = field.getFieldType();

		if (field.isEditable() && fieldType != Field.esriFieldTypeOID
				&& fieldType != Field.esriFieldTypeGeometry
				&& fieldType != Field.esriFieldTypeBlob
				&& fieldType != Field.esriFieldTypeRaster
				&& fieldType != Field.esriFieldTypeGUID
				&& fieldType != Field.esriFieldTypeXML) {

			return true;

		}

		return false;
	}

	/**
	 * Helper method to set attributes on a graphic. Only sets the attributes on
	 * the newGraphic variable if the value has changed. Returns true if the
	 * value has changed and has been set on the graphic.
	 * 
	 * @return boolean hasValueChanged
	 */
	public static boolean setAttribute(Map<String, Object> attrs,
			Graphic oldGraphic, Field field, String value, DateFormat formatter) {

		boolean hasValueChanged = false;

		// if its a string, and it has changed from the oldGraphic value
		if (FieldType.determineFieldType(field) == FieldType.STRING) {

			if (!value.equals(oldGraphic.getAttributeValue(field.getName()))) {

				// set the value as it is
				attrs.put(field.getName(), value);
				hasValueChanged = true;

			}
		} else if (FieldType.determineFieldType(field) == FieldType.NUMBER) {

			// if its an empty string, its a 0 number value (nulls not
			// supported), check this is a
			// change before making it a 0
			if (value.equals("")
					&& oldGraphic.getAttributeValue(field.getName()) != Integer
							.valueOf(0)) {

				// set a null value on the new graphic
				attrs.put(field.getName(), new Integer(0));
				hasValueChanged = true;

			} else {

				// parse as an int and check this is a change
				int intValue = Integer.parseInt(value);
				if (intValue != Integer.parseInt(oldGraphic.getAttributeValue(
						field.getName()).toString())) {

					attrs.put(field.getName(), Integer.valueOf(intValue));
					hasValueChanged = true;

				}
			}
		} else if (FieldType.determineFieldType(field) == FieldType.DECIMAL) {

			// if its an empty string, its a 0 double value (nulls not
			// supported), check this is a
			// change before making it a 0
			if ((value.equals("") && oldGraphic.getAttributeValue(field
					.getName()) != Double.valueOf(0))) {

				// set a null value on the new graphic
				attrs.put(field.getName(), new Double(0));
				hasValueChanged = true;

			} else {

				// parse as an double and check this is a change
				double dValue = Double.parseDouble(value);
				if (dValue != Double.parseDouble(oldGraphic.getAttributeValue(
						field.getName()).toString())) {

					attrs.put(field.getName(), Double.valueOf(dValue));
					hasValueChanged = true;

				}
			}
		} else if (FieldType.determineFieldType(field) == FieldType.DATE) {

			// if its a date, get the milliseconds value
			Calendar c = Calendar.getInstance();
			long dateInMillis = 0;

			try {

				// parse to a double and check this is a change
				c.setTime(formatter.parse(value));
				dateInMillis = c.getTimeInMillis();

				if (dateInMillis != Long.parseLong(oldGraphic
						.getAttributeValue(field.getName()).toString())) {

					attrs.put(field.getName(), Long.valueOf(dateInMillis));
					hasValueChanged = true;
				}
			} catch (ParseException e) {
				// do nothing
			}
		}
		// }

		return hasValueChanged;

	}

	/**
	 * Helper method to find the Types actual value from its name
	 */
	public static String returnTypeIdFromTypeName(FeatureType[] types,
			String name) {

		for (FeatureType type : types) {
			if (type.getName().equals(name)) {
				return type.getId();
			}
		}
		return null;
	}

	/**
	 * Helper method to setup the editable field indexes and store them for
	 * later retrieval in getItem() method.
	 */
	public static int[] createArrayOfFieldIndexes(Field[] fields) {

		// process count of fields and which are available for editing
		ArrayList<Integer> list = new ArrayList<Integer>();
		int fieldCount = 0;

		for (int i = 0; i < fields.length; i++) {

			if (isFieldValidForEditing(fields[i])) {

				list.add(Integer.valueOf(i));
				fieldCount++;

			}
		}

		int[] editableFieldIndexes = new int[fieldCount];

		for (int x = 0; x < list.size(); x++) {

			editableFieldIndexes[x] = list.get(x).intValue();

		}

		return editableFieldIndexes;
	}

	/**
	 * Helper method to create a String array of Type values for populating a
	 * spinner
	 */
	public static String[] createTypeNameArray(FeatureType[] types) {

		String[] typeNames = new String[types.length];
		int i = 0;
		for (FeatureType type : types) {

			typeNames[i] = type.getName();
			i++;

		}

		return typeNames;

	}

	/**
	 * Helper method to create a HashMap of types using the Id (value) as the
	 * key
	 */
	public static HashMap<String, FeatureType> createTypeMapByValue(
			FeatureType[] types) {

		HashMap<String, FeatureType> typeMap = new HashMap<String, FeatureType>();

		for (FeatureType type : types) {

			typeMap.put(type.getId(), type);

		}

		return typeMap;

	}

}
