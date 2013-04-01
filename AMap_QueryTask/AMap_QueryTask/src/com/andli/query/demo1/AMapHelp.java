package com.andli.query.demo1;

import java.util.Random;

import android.graphics.Color;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class AMapHelp {
	/**
	 * 
	 * @author lilin
	 * @date 2012-9-14 下午4:31:44
	 * @annotation 高亮显示查询结果
	 */
	public static void lightShowQueryResult(int i, String typeName,
			Geometry geom, GraphicsLayer lightGraphicsLayer) {
		// 高亮显示查询结果
		Random r = new Random();
		Graphic graphic = null;
		int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));// 生成随机色
		if (typeName.equalsIgnoreCase("point")) {
			SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20,
					STYLE.SQUARE);
			graphic = new Graphic(geom, sms);
		} else if (typeName.equalsIgnoreCase("polyline")) {
			SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
			graphic = new Graphic(geom, sls);
		} else if (typeName.equalsIgnoreCase("polygon")) {
			SimpleFillSymbol sfs = new SimpleFillSymbol(color);
			sfs.setAlpha(75);
			graphic = new Graphic(geom, sfs);
		}
		lightGraphicsLayer.addGraphic(graphic);
	}
}
