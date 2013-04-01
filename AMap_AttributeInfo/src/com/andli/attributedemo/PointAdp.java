package com.andli.attributedemo;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.andli.attributedemo.FeatureLayerUtils.FieldType;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Field;

public class PointAdp extends BaseAdapter {

	FeatureSet mFeatureSet;

	Field[] fields;

	FeatureType[] types;

	String typeIdFieldName;

	Context context;

	LayoutInflater lInflator;

	int[] editableFieldIndexes;

	String[] typeNames;

	HashMap<String, FeatureType> typeMap;

	PointInfo[] items;

	DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
			DateFormat.SHORT);

	/**
	 * Constructor
	 */
	public PointAdp(Context context, Field[] fields, FeatureType[] types,
			String typeIdFieldName) {

		this.context = context;
		this.lInflator = LayoutInflater.from(context);
		this.fields = fields;
		this.types = types;
		this.typeIdFieldName = typeIdFieldName;

		// this.fieldsTemplateMap = createInitialFieldTemplateMap(this.fields);
		// parseTypes();

		// Setup processed variables
		this.editableFieldIndexes = FeatureLayerUtils
				.createArrayOfFieldIndexes(this.fields);
		this.typeNames = FeatureLayerUtils.createTypeNameArray(this.types);
		this.typeMap = FeatureLayerUtils.createTypeMapByValue(this.types);

		// register dataset observer to track when the underlying data is
		// changed
		this.registerDataSetObserver(new DataSetObserver() {

			public void onChanged() {

				// clear the array of attribute items
				PointAdp.this.items = new PointInfo[PointAdp.this.editableFieldIndexes.length];

			}
		});
	}

	/**
	 * Implemented method from BaseAdapter class
	 */
	public int getCount() {

		return this.editableFieldIndexes.length;

	}

	/**
	 * Implemented method from BaseAdapter class. This method returns the actual
	 * data associated with a row in the list. In this case we return the field
	 * along with the field value as a custom object. We subsequently add the
	 * View which displays the value to this object so we can retrieve it when
	 * applying edits.
	 */
	public Object getItem(int position) {

		// get field associated with the position from the editableFieldIndexes
		// array created at startup
		int fieldIndex = this.editableFieldIndexes[position];

		PointInfo pointInfo = null;

		// check to see if we have already created an attribute item if not
		// create
		// one
		if (items[position] == null) {
			// create new Attribute item for persisting the data for subsequent
			// events
			pointInfo = new PointInfo();
			pointInfo.setField(this.fields[fieldIndex]);
			Object value = this.mFeatureSet.getGraphics()[0].getAttributeValue(fields[fieldIndex].getName());
			pointInfo.setValue(value);
			items[position] = pointInfo;
		} else {
			pointInfo = items[position];

		}

		return pointInfo;

	}

	/**
	 * Implemented method from BaseAdapter class
	 */
	public long getItemId(int position) {

		return position;

	}

	/**
	 * Implemented method from BaseAdapter class. This is the main method for
	 * returning a View which corresponds to a row in the list. This calls the
	 * getItem() method to get the data. It is called multiple times by the
	 * ListView and may be improved on by saving the previous result.
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		View container = null;

		PointInfo mPointInfo = (PointInfo) getItem(position);

		if (mPointInfo.getField().getName().equals(this.typeIdFieldName)) {

			container = lInflator.inflate(R.layout.item_spinner, null);
			// get the types name for this feature from the available values
			String typeStringValue = this.typeMap.get(
					mPointInfo.getValue().toString()).getName();
			Spinner spinner = createSpinnerViewFromArray(container,
					mPointInfo.getField(), typeStringValue, this.typeNames);
			
			mPointInfo.setView(spinner);


		} else if (FieldType.determineFieldType(mPointInfo.getField()) == FieldType.DATE) {
			// create date picker for date fields

			container = lInflator.inflate(R.layout.item_date, null);
			long date = Long.parseLong(mPointInfo.getValue().toString());

			Button dateButton = createDateButtonFromLongValue(container,
					mPointInfo.getField(), date);
			mPointInfo.setView(dateButton);

		} else {
			// create number and text fields
			// View object for saving in the AttrbuteItem once it has been set
			// up, for
			// accessing later when we apply edits.
			View valueView = null;

			if (FieldType.determineFieldType(mPointInfo.getField()) == FieldType.STRING) {

				// get the string specific layout
				container = lInflator.inflate(R.layout.item_text, null);
				valueView = createAttributeRow(container, mPointInfo.getField(),
						mPointInfo.getValue());

			} else if (FieldType.determineFieldType(mPointInfo.getField()) == FieldType.NUMBER) {

				// get the number specific layout
				container = lInflator.inflate(R.layout.item_number, null);
				valueView = createAttributeRow(container, mPointInfo.getField(),
						mPointInfo.getValue());

			} else if (FieldType.determineFieldType(mPointInfo.getField()) == FieldType.DECIMAL) {

				// get the decimal specific layout
				container = lInflator.inflate(R.layout.item_decimal, null);
				valueView = createAttributeRow(container, mPointInfo.getField(),
						mPointInfo.getValue());

			}

			// set the rows view onto the item so it can be received when
			// applying
			// edits
			mPointInfo.setView(valueView);

		}

		return container;

	}

	/**
	 * Sets the FeatureSet, called by the activity when a new queryResult is
	 * returned
	 * 
	 * @param featureSet
	 */
	public void setFeatureSet(FeatureSet featureSet) {

		this.mFeatureSet = featureSet;

	}

	/**
	 * Helper method to create a spinner for a field and insert it into the View
	 * container. This uses, the String[] to create the list, and selects the
	 * value that is passed in from the list (the features value). Can be used
	 * for domains as well as types.
	 */
	Spinner createSpinnerViewFromArray(View container, Field field,
			Object value, String[] values) {

		TextView fieldAlias = (TextView) container
				.findViewById(R.id.field_alias_txt);
		Spinner spinner = (Spinner) container
				.findViewById(R.id.field_value_spinner);
		fieldAlias.setText(field.getAlias());
		spinner.setPrompt(field.getAlias());

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
				this.context, android.R.layout.simple_spinner_item, values);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		// set current selection based on the value passed in
		spinner.setSelection(spinnerAdapter.getPosition(value.toString()));

		return spinner;
	}

	/**
	 * Helper method to create a date button, with appropriate onClick and
	 * onDateSet listeners to handle dates as a long (milliseconds since 1970),
	 * it uses the locale and presents a button with the date and time in short
	 * format.
	 */
	Button createDateButtonFromLongValue(View container, Field field, long date) {

		TextView fieldAlias = (TextView) container
				.findViewById(R.id.field_alias_txt);
		Button dateButton = (Button) container
				.findViewById(R.id.field_date_btn);
		fieldAlias.setText(field.getAlias());

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		dateButton.setText(formatter.format(c.getTime()));

		addListenersToDatebutton(dateButton);

		return dateButton;
	}

	/**
	 * Helper method to add the field alias and the fields value into columns of
	 * a view using standard id names. If the field has a length set, then this
	 * is used to constrain the EditText's allowable characters. No validation
	 * is applied here, it is assumed that the container has this set already
	 * (in XML).
	 */
	View createAttributeRow(View container, Field field, Object value) {

		TextView fieldAlias = (TextView) container
				.findViewById(R.id.field_alias_txt);
		EditText fieldValue = (EditText) container
				.findViewById(R.id.field_value_txt);
		fieldAlias.setText(field.getAlias());

		// set the length of the text field and its value
		if (field.getLength() > 0) {
			InputFilter.LengthFilter filter = new InputFilter.LengthFilter(
					field.getLength());
			fieldValue.setFilters(new InputFilter[] { filter });
		}

		Log.d(MyMapView.TAG, "value is null? =" + (value == null));
		Log.d(MyMapView.TAG, "value=" + value);

		if (value != null) {
			fieldValue.setText(value.toString(), BufferType.EDITABLE);
		} else {
			fieldValue.setText("", BufferType.EDITABLE);
		}

		return fieldValue;
	}

	/**
	 * Helper method to create the date button and its associated events
	 */
	void addListenersToDatebutton(Button dateButton) {

		// create new onDateSetLisetener with the button associated with it
		final ListOnDateSetListener listener = new ListOnDateSetListener(
				dateButton);

		// add a click listener to the button
		dateButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				// if its a date, get the milliseconds value
				Calendar c = Calendar.getInstance();
				formatter.setCalendar(c);

				try {

					// parse to a double
					Button button = (Button) v;
					c.setTime(formatter.parse(button.getText().toString()));

				} catch (ParseException e) {
					// do nothing as should parse
				}

				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				// show date picker with date set to the items value (hence
				// built
				// outside of onCreateDialog)
				// TODO implement time picker if required, this picker only
				// supports
				// date and therefore showing the dialog will cause a change in
				// the time
				// value for the field
				DatePickerDialog dialog = new DatePickerDialog(context,
						listener, year, month, day);
				dialog.show();
			}
		});
	}

	/**
	 * Inner class for handling date change events from the date picker dialog
	 */
	class ListOnDateSetListener implements OnDateSetListener {

		Button button;

		public ListOnDateSetListener(Button button) {

			this.button = button;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {

			Calendar c = Calendar.getInstance();
			c.set(year, month, day);

			// Update the button to show the chosen date
			button.setText(formatter.format(c.getTime()));

		}
	}
}
