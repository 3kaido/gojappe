package com.daikyo.rssreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daikyo.rssreader.Feed.RssEntry;

public class MngAdapter extends CursorAdapter {
	public static final int normal = 	Menu.FIRST;
	public static final int fav = 		Menu.FIRST +1;
	public static final int deleted = 	Menu.FIRST +2;
	public LayoutInflater mInflater;
    private	SharedPreferences pfBkmk,pfDeleteds; 
    public static class ViewHolder {
    	TextView title;
    	ImageView img;
    	String name,url,category;
    	int status;
    	}
    private Activity a;
    private OnClickListener titleclick;
    private OnClickListener imgclick;

    // CONSTRUCTOR.
    public MngAdapter(Activity act, Cursor cur, int flags) {
		super(act, cur);
		this.a = act;
        pfBkmk = a.getSharedPreferences(Pref.BOOKMARK,Context.MODE_PRIVATE);
    	pfDeleteds = a.getSharedPreferences(Pref.DELETEDS, Context.MODE_PRIVATE);
    	titleclick = new OnClickListener(){
        	public void onClick(View v){
        		View root = (View)v.getParent();
        		final ViewHolder holder = (ViewHolder)root.getTag();
        	    final AlertDialog.Builder builder = new AlertDialog.Builder(a);
        	    builder.setTitle(holder.name);
        	    builder.setItems(R.array.managermenu,  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){ // check item.
                    	if (which==0){ // Zero is "OPEN in ReaderFragment"
//                       	 	Intent i= a.getIntent();
//                       	 	i.setClass(a, RssReader2nd.class);
//                       	 	i.setAction(RssReader2nd.ACTION_NEWRSS);
//                       	    i.putExtra(Feed.BkmkNAME, holder.name);
//                       	    i.putExtra(Feed.BkmkURL, holder.url);
//                       	    a.startActivity(i);
//                       	    a.finish();
                    		RssEntry rss = RssEntry.createRssFrom(a, holder.name, holder.url);
                    		ReaderFragment rf = ReaderFragment.newInstance("preview",rss);
                    		FragmentActivity fa = (FragmentActivity)a;
                    		FragmentTransaction ft = fa.getSupportFragmentManager().beginTransaction();
                    		ft.add(R.id.managerroot, rf);
                    		ft.addToBackStack(null);
                    		ft.commit();
                    	} else { // 1 is OPEN-INTENT.
                            Uri uri = Uri.parse(holder.url);
                           	Intent i = new Intent(Intent.ACTION_VIEW,uri);
                           	a.startActivity(i);
                           	a.finish();
                    	}
                    }});
                builder.show();
        	}
        };
        imgclick = new OnClickListener() {
        	public void onClick(View v){
        		View root = (View)v.getParent();
        		final ViewHolder holder = (ViewHolder)root.getTag();
        		Integer which = (Integer) v.getTag();
            	ImageView img = (ImageView)v;
        		switch (which){
            	case fav: //-----> move to norm?
            		v.setTag(normal);
            		RssEntry.setStateNormal(a, holder.name);
            		img.setImageResource(R.drawable.ic_menu_help);
                    break;
            			
            	case deleted: // -----> move to FAV?
            		v.setTag(fav);
            		RssEntry.setStateFav(a, holder.name, holder.url);
            		img.setImageResource(R.drawable.ic_menu_star);
                    break;
            		
            	case normal: // -----> move to DEL?
            		v.setTag(deleted);
            		RssEntry.setStateDeleted(a, holder.name, holder.url);
            		img.setImageResource(R.drawable.ic_menu_close_clear_cancel);
                	break;
            		}// END SWITCH.
        		}// END ON CLICK
        	};
	}

	@Override
	public View newView(Context c, Cursor arg1, ViewGroup parent) {
		View view = ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bkmkitem, null);
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView)view.findViewById(R.id.bkmktitle);
		holder.img = (ImageView)view.findViewById(R.id.bkmkimg);
        holder.title.setOnClickListener(titleclick);
        holder.img.setOnClickListener(imgclick);// END LISTENER.
		view.setTag(holder); // attach to view.        
		return view;
	};
    
	@Override
	public void bindView(View view, final Context c, Cursor cur) {
		// RECYCLE VIEW BY 1code!
		final ViewHolder holder=(ViewHolder) view.getTag();
    	holder.name = cur.getString(cur.getColumnIndex(Feed.Columns.NAME));
    	holder.url = cur.getString(cur.getColumnIndex(Feed.Columns.URL));
    	holder.category = cur.getString(cur.getColumnIndex(Feed.Columns.CATEGORY));

        Spanned converted = Html.fromHtml(holder.category + "<font color=\"#FF6600\"> # </font>" + holder.name);
    	holder.title.setText(converted);
    	/*
    	 * CHECK FAVORITE/DELETED status.
    	 */
    	if (pfBkmk.contains(holder.name)){
    		holder.status = fav;
    		holder.img.setImageResource(R.drawable.ic_menu_star);
    	} else if (pfDeleteds.contains(holder.name)){
    		holder.status = deleted;
    		holder.img.setImageResource(R.drawable.ic_menu_close_clear_cancel);
    	} else {
    		holder.status = normal;
    		holder.img.setImageResource(R.drawable.ic_menu_help);
    	}
		holder.img.setTag(holder.status);
    }// END BIND VIEW.
}// END CLASS.