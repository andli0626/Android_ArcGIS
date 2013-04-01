/* Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the �Sample code usage restrictions� document for further information.
 *
 */

package com.esri.arcgis.android.samples.localtiledlayer;

import android.app.Activity;
import android.os.Bundle;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;

/**
* This sample illustrates the use of ArcGISLocatlTiledLayer where the data is stored locally on the device, therefore 
* this layer can function even when the device does not have any network connectivity. The data for this layer must be 
* in an ArcGIS Compact Cache format (Tile Packages or .tpk's are not currently supported).
* The typical compact cache structure is as  follows:
*	<CacheName><br>
*		Layers<br>
*			_allLayers<br>
*				conf.cdi,conf.xml<br>
* The path used in the constructor of the ArcGISLocalTiledLayer must point to the Layers folder e.g. <br>
*  ArcGISLocalTiledLayer local = new ArcGISLocalTiledLayer("file:///mnt/sdcard/<CacheName>/Layers");
*  
*  A sample data set has been created and is available via ArcGIS Online:
*  http://www.arcgis.com/home/item.html?id=d2d263a280164a039ef0a02e26ee0501
*  1) In order to use the data, download it from the url above
*  2) Copy the data to your sdcard
*  3) Set the path to the data by replacing <CacheName> with file:///mnt/sdcard/Parcels/v101/Parcel Map
*     on line60 below.
*  
*  You can also use your own data if it is in an ArcGIS Compact Cache format, for more information on 
*  this data format see this link: 
*  http://blogs.esri.com/Dev/blogs/arcgisserver/archive/2010/05/27/Introducing-the-compact-cache-storage-format.aspx
*  
**/

public class LocalTiledLayer extends Activity {

	MapView map = null;
	ArcGISLocalTiledLayer local;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		map = (MapView) findViewById(R.id.map);
		
		//加载本地切片地图
		local = new ArcGISLocalTiledLayer("file:///mnt/sdcard/Parcels/v101/Parcel Map");
		map.addLayer(local);
		
		
	}
}