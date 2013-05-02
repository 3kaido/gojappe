package com.daikyo.rssreader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RssItemAdapter extends ArrayAdapter<Item> {
	final public static String dozo = "<font color=\"#FF6600\">（　・∀・）つ旦  </font>";
	private static class ViewHolder {
		TextView mTitle;
		Item item;
	}
	private Context c;
	public LayoutInflater mInflater;
	private OnClickListener onclick;
	private OnLongClickListener onlongclick;
	private OnFocusChangeListener onfocus;
	private String color;
	
	public RssItemAdapter(Context con) {
		super(con, 0);
		c = con;
		color = Pref.getListStringColor(c);
		mInflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		onclick = new OnClickListener() {
			public void onClick(View v) { // go to webOpen.
				final ViewHolder h = (ViewHolder) v.getTag();
				Item item = h.item;
				item.openWeb(c);
			}
		};
		onlongclick = new OnLongClickListener() {
			public boolean onLongClick(View v) {
				final ViewHolder h = (ViewHolder) v.getTag();
				final Item item = h.item;
				final String title = item.getTitle().toString(); // Title
				final AlertDialog.Builder builder = new AlertDialog.Builder(c);
				builder.setTitle(title);
				builder.setItems(R.array.rssitemmenu,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) { // check item.
								if (which == 0) { // Zero is "READ LATER"
									item.readThisLater(c);
								} else if (which == 1) { // 1 is
									// "TWEET/SHARE"
									item.selectAA(c);
								} else { // 2 is "OPEN INTENT"
									item.openIntent(c);
								}
							}// end on-click.
						});// end Listener.
				builder.show();
				return true;
			}
		};
		onfocus = new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				final ViewHolder h = (ViewHolder) v.getTag();
				final Item item = h.item;
				String source;
				if (hasFocus) {
					source = dozo + item.getDate();
				} else {
					source = item.getDate();
				}
//				h.mDate.setText(Html.fromHtml(source));
			}
		};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View view = convertView;
		final Item originalItem = getItem(position);
		/*
		 * Recycle View
		 */
		if (convertView == null) {
			// GENERATE VIEW IF NULL.
			view = mInflater.inflate(R.layout.item_row, null);
			holder = new ViewHolder();
			holder.mTitle = (TextView) view.findViewById(R.id.item_title);
			view.setTag(holder); // attach to view.
			/*
			 * set Actions;
			 */
			view.setOnClickListener(onclick);
			view.setOnFocusChangeListener(onfocus);
			view.setOnLongClickListener(onlongclick);
		} else { // IF VIEW NOT NULL
			holder = (ViewHolder) view.getTag();//recycle.
		} // END IF VIEW NULL.

		/*
		 * Set View data.
		 */
		holder.item = originalItem;// remember for reuse;
		String s = "<FONT COLOR=\""+color+"\">"+holder.item.getTitle()+"</><BR><FONT COLOR=\"#777777\"><Small>"+holder.item.getDate()+"</></>";
		Spanned spn = Html.fromHtml(s);
		holder.mTitle.setText(spn);
		return view;
	}

}