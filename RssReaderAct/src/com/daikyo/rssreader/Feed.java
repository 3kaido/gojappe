/**
 * Sparse rss
 * 
 * Copyright (c) 2010, 2011 Stefan Handschuh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.daikyo.rssreader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;
import android.widget.Toast;

public class Feed {
	public static final String CONTENT = "content://";
	public static final String AUTHORITY = "com.daikyo.rssreader.Feed";
	public static final String TABLE_NAME = "feeds";
	public static final String WHEREQ = "=?";
	private static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
	public static final String TYPE_TEXT = " TEXT";
	public static final String TYPE_DATETIME = "DATETIME";
	public static final String TYPE_INT = " INT";
	public static final String TYPE_BOOLEAN = "INTEGER(1)";
	public static final String BkmkNAME = "com.daikyo.rssreader.bkmkname";
	public static final String BkmkURL = "com.daikyo.rssreader.bkmkurl";

	public static class State {
		public static final byte NORMAL	    = 0;
		public static final byte FAV 		= 1;
		public static final byte DISABLED 	= 2;
	}
	
	public static class Columns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse(new StringBuilder(CONTENT).append(AUTHORITY).append("/feeds").toString());
		public static final Uri CONTENT_CATEGORY = Uri.parse(new StringBuilder(CONTENT).append(AUTHORITY).append("/category").toString());
		public static final String URL = "url"; // Col.1
		public static final String NAME = "name"; // Col.2
		public static final String CATEGORY = "category"; // Col.3
		public static final String CLICKCOUNT = "clicks"; // Col.4
		public static final String[] COLUMNS = new String[] {_ID, URL, NAME, CATEGORY,CLICKCOUNT};
		public static final String[] TYPES = new String[] {TYPE_PRIMARY_KEY, "TEXT UNIQUE", TYPE_TEXT, TYPE_DATETIME, "BLOB", TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_DATETIME};
		public static final Uri CONTENT_URI(String feedId) {
			return Uri.parse(new StringBuilder(CONTENT).append(AUTHORITY).append("/feeds/").append(feedId).toString());
		}
	}
	public static class Category {
		public static final String ALL = "all";// used for Category AUG and WHERE.
		public static final String USER = "user"; // used for USER_OPML.
	}
	public static class RssEntry {
		public static final String SLASH = 	"<font color=\"#FF6600\"># </font>"; //normal
		public static final String STAR = 	"<font color=\"#FF6600\">★ </font>"; // for Fav?
		private String Url; 	// Col.1
		private String Name; 	// Col.2
		private String Category;// Col.3
		public byte state;
		private Context c; // used for various Action.
		
		private byte getState() {
		    final SharedPreferences bkmk = c.getSharedPreferences(Pref.BOOKMARK, Context.MODE_PRIVATE);
		    final SharedPreferences deleted = c.getSharedPreferences(Pref.DELETEDS, Context.MODE_PRIVATE);
		    if (bkmk.contains(Name)){
				return state = Feed.State.FAV;			
		    } else if (deleted.contains(Name)){
				return state = Feed.State.DISABLED;
		    } else {
				return state = Feed.State.NORMAL;
			}
		}

		public String getUrl() {
			return Url;
		}
		public void setUrl(String url) {
			Url = url;
		}
		public String getName() {
			return Name;
		}
		public void setName(String name) {
			Name = name;
		}
		public String getCategory() {
			return Category;
		}
		public void setCategory(String category) {
			Category = category;
		}
		public static RssEntry createRssFrom(Context context,String name,String url){
			RssEntry item = new RssEntry();
			item.c = context;
			item.setName(name);
			item.setUrl(url);
			item.state = item.getState();
			return item;
		}
//		public void openReader(Context c){
//       	 	Intent i= new Intent(c,RssReader2nd.class);
//       	 	i.setAction(RssReader2nd.ACTION_NEWRSS);
//       	    i.putExtra(Feed.BkmkNAME, Name);
//       	    i.putExtra(Feed.BkmkURL, Url);
//       	    c.startActivity(i);
//		}

		public Spanned getSpannedTitle(){
			String title = SLASH + Name;
	        if (state == Feed.State.FAV){ 
	        	//IF ALREADY BKMK--
	    		title = STAR + Name;
	        }
			return Html.fromHtml(title);
		}


//		public byte changeBookmarkState(){
//			final AlertDialog.Builder builder = new AlertDialog.Builder(c);
//			builder.setCancelable(true);
//			builder.setTitle(Name);
//			builder.setItems(R.array.bookmarkoption, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, final int which){ // check item.
//					state = (byte) which;
//					if (state == Feed.State.NORMAL){ // Zero is "READ LATER"
//						// normal.
//						setStateNormal(c,Name);
//
//					}else if (state == Feed.State.FAV) { // 1 is "TWEET/SHARE"
//						// fav.
//						setStateFav(c,Name,Url);
//
//					}else if (state == Feed.State.DISABLED) { 
//						// disabled.
//						setStateDeleted(c,Name,Url);
//						
//					}
//					// CHECK title TextView.
//					RssHolder rh = ((RssReader2nd)c).getCurrentFragment();
//					rh.setTitleBookmarked();
//				}//end on-click.
//			});// end Listener.
//			builder.show();
//			return state;
//		}
		
		public byte changeBookmarkState(final TextView tv){
			final AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setCancelable(true);
			builder.setTitle(Name);
			builder.setItems(R.array.bookmarkoption, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, final int which){ // check item.
					state = (byte) which;
					if (state == Feed.State.NORMAL){ // Zero is "READ LATER"
						// normal.
						setStateNormal(c,Name);

					}else if (state == Feed.State.FAV) { // 1 is "TWEET/SHARE"
						// fav.
						setStateFav(c,Name,Url);

					}else if (state == Feed.State.DISABLED) { 
						// disabled.
						setStateDeleted(c,Name,Url);
						
					}
					// CHECK title TextView.
					tv.setText(getSpannedTitle());
				}//end on-click.
			});// end Listener.
			builder.show();
			return state;
		}
		
		public static void setStateNormal(Context c,String name){
		    final SharedPreferences.Editor bkmk = c.getSharedPreferences(Pref.BOOKMARK, Context.MODE_PRIVATE).edit();
		    final SharedPreferences.Editor deleted = c.getSharedPreferences(Pref.DELETEDS, Context.MODE_PRIVATE).edit();
			bkmk.remove(name).commit();
			deleted.remove(name).commit();
	        Toast.makeText(c, name + c.getString(R.string.rss_willaccessrandom), Toast.LENGTH_SHORT).show();
		}
		
		public static void setStateFav(Context c,String name,String url){
		    final SharedPreferences.Editor bkmk = c.getSharedPreferences(Pref.BOOKMARK, Context.MODE_PRIVATE).edit();
		    final SharedPreferences.Editor deleted = c.getSharedPreferences(Pref.DELETEDS, Context.MODE_PRIVATE).edit();
	    	bkmk.putString(name, url).commit();
	    	deleted.remove(name).commit();
	        Toast.makeText(c, name + c.getString(R.string.rss_addedtofav), Toast.LENGTH_SHORT).show();
		}
		
		public static void setStateDeleted(Context c,String name,String url){
		    final SharedPreferences.Editor bkmk = c.getSharedPreferences(Pref.BOOKMARK, Context.MODE_PRIVATE).edit();
		    final SharedPreferences.Editor deleted = c.getSharedPreferences(Pref.DELETEDS, Context.MODE_PRIVATE).edit();
			deleted.putString(name, url).commit();
			bkmk.remove(name).commit();
	    	Toast.makeText(c, name + c.getString(R.string.rss_setdeleted), Toast.LENGTH_SHORT).show();
		}
	}
}// END CLASS