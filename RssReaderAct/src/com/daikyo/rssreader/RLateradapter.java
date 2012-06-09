package com.daikyo.rssreader;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RLateradapter extends ArrayAdapter<Map.Entry<String, Long>> {
	public LayoutInflater mInflater;
	private Context c;
	private static class ViewHolder {
		TextView tv;
		Item item;
	};
	private OnClickListener click;
	private String txtcolor;
	private String bgcolor;
	
	public RLateradapter(Context con) {
		super(con, 0);
		this.c = con;
		txtcolor = Pref.getListStringColor(c);
		bgcolor = Pref.getBackgroundColor(c);
		mInflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		click = new OnClickListener() {
			public void onClick(View v) {
				ViewHolder holder = (ViewHolder) v.getTag();
				holder.item.openWeb(c);
			}// END ON CLICK
		};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View view = convertView;
		/*
		 * GENERATE/REUSE VIEW
		 */
		if (view == null) {
			// GENERATE VIEW IF NULL.
			view = mInflater.inflate(R.layout.item_row, null);
			holder = new ViewHolder();
			holder.tv = (TextView)view.findViewById(R.id.item_title);
			setBackgroundColor(holder.tv);
			view.setTag(holder); // attach to view.
			/*
			 * ON CLICK
			 */
			view.setOnClickListener(click);
		} else { // IF VIEW NOT NULL
			holder = (ViewHolder) view.getTag();
		} // END IF VIEW NULL.
		/*
		 * SET DATA;
		 */
		final Map.Entry<String, Long> entry = this.getItem(position);
		holder.item = Item.createItemFromRLATER(entry);
		String s = "<FONT COLOR=\""+txtcolor+"\">"+holder.item.getTitle()+"</>";
		Spanned spn = Html.fromHtml(s);
		holder.tv.setText(spn);
		return view;
	}// END GET VIEW.

	private void setBackgroundColor(View v) {
		if (bgcolor.equals("transparent")){
			v.setBackgroundResource(0); // transparent
		} else if (bgcolor.equals("black")){
			v.setBackgroundResource(R.drawable.solidblack);
		}
	}
}// END CLASS ADAPTER.