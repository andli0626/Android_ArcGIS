package com.esri.arcgis.android.samples.identifytask;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.core.tasks.ags.identify.IdentifyResult;

public class IdentifyResultSpinnerAdp extends IdentifyResultSpinnerAdapter {
	String m_show = null;
	List<IdentifyResult> resultList;
	int currentDataViewed = -1;
	Context m_context;

	public IdentifyResultSpinnerAdp(Context context,
			List<IdentifyResult> results) {
		super(context, results);
		this.resultList = results;
		this.m_context = context;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		String Name = null;
		TextView txtView;
		IdentifyResult curResult = this.resultList.get(position);
		// String layername = curResult.getLayerName();
		// String displayFieldName = curResult.getDisplayFieldName();
		// int id = curResult.getLayerId();

		// Attributes={Land Area in Square Miles=97100.4005, Name=Wyoming,
		// ID=56, 2010 Average Household Size=2.43, Shape=Polygon, 2010 Total
		// Population=548154, State Abbreviation=WY}

		// Log.i("andli", "-----图层信息-----");
		// Log.i("andli", "图层名称=" + layername);
		// Log.i("andli", "displayFieldName=" + displayFieldName);
		// Log.i("andli", "id=" + id);
		// Log.i("andli", "-----属性信息-----");
		// if
		// (curResult.getAttributes().containsKey("Land Area in Square Miles"))
		// {
		// Log.i("andli",
		// "地区面积="
		// + curResult.getAttributes()
		// .get("Land Area in Square Miles")
		// .toString());
		// }
		if (curResult.getAttributes().containsKey("Name")) {
			Name = curResult.getAttributes().get("Name").toString();
			Log.i("andli", "地区名称=" + Name);
		}
		// if (curResult.getAttributes()
		// .containsKey("2010 Average Household Size")) {
		// Log.i("andli",
		// "平均住房面积="
		// + curResult.getAttributes()
		// .get("2010 Average Household Size")
		// .toString());
		// }
		// if (curResult.getAttributes().containsKey("Shape")) {
		// Log.i("andli", "Shape="
		// + curResult.getAttributes().get("Shape").toString());
		// }
		// if (curResult.getAttributes().containsKey("2010 Total Population")) {
		// Log.i("andli",
		// "2010总人口="
		// + curResult.getAttributes()
		// .get("2010 Total Population").toString());
		// }
		// if (curResult.getAttributes().containsKey("State Abbreviation")) {
		// Log.i("andli", "State Abbreviation="
		// + curResult.getAttributes().get("State Abbreviation")
		// .toString());
		// }
		// // Log.i("andli", "Attributes=" + curResult.getAttributes());
		//
		// if (curResult.getAttributes().containsKey("Name")) {
		// Name = curResult.getAttributes().get("Name").toString();
		// }

		txtView = new TextView(m_context);
		txtView.setText(Name);
		txtView.setTextColor(Color.BLACK);
		txtView.setLayoutParams(new ListView.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		txtView.setGravity(Gravity.CENTER_VERTICAL);

		return txtView;
	}

}
