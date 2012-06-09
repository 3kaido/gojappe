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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;
import android.widget.Toast;

public class OPML {
	public final static String UPDATED = "updated";
	private static final String EMPTYSTRING = "";
	
	private static final String OPML_URL = "http://goo.gl/BzmGI";	
//	private static final String OPML_URL = "http://sites.google.com/site/3kaido/degosuke/google-reader-subscriptions.xml";
	// private static final String OPML_TEST ="http://sites.google.com/site/3kaido/degosuke/feeds/google-reader-subscriptions.xml";
		
	private static final String filename = "/sdcard/google-reader-subscriptions.xml";
	private static OPMLParser parser;

	public static void importFromFile(Context context) {
		// here calls ASYNC task.
		importFileAsync task = new importFileAsync(context);
		task.execute();
	}

	public static void importFromWeb(Context context) {
		// here calls ASYNC task.
		importWebAsync task = new importWebAsync(context);
		task.execute();
	}

	public static class importWebAsync extends AsyncTask<Void, String, Void> {
		private ProgressDialog mProgressDialog;
		private Context c;
		public boolean isFromUser = false;

		/*
		 * Constructor.
		 */
		public importWebAsync(Context con) {
			this.c = con;
		}
		public importWebAsync() {
			super();
		}
		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(c);
			mProgressDialog.setMessage(c.getString(R.string.opml_nowloading));
			WindowManager.LayoutParams dlgParams = mProgressDialog.getWindow()
					.getAttributes();
			dlgParams.dimAmount = 0.0f;
			mProgressDialog.getWindow().setAttributes(dlgParams);
			mProgressDialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			mProgressDialog.show();
		}// END PRE-EXE.

		@Override
		protected void onPostExecute(Void result) {
			// ProgressDialog Dismiss!
			try {
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			} catch (Exception e) {// ERROR IN DISMISS.
				e.printStackTrace();
			}// END EXCEption.
			/*
			 * RELOAD "READER" activity with new feeds.
			 */
			if (parser.count >0){
				Toast.makeText(c, c.getString(R.string.loadOPML_ok), Toast.LENGTH_SHORT).show();
				reloadRssReader(c);
	           	/*
	           	 * Set UPDATE-Record.
	           	 */
	           	SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(c);
	           	pf.edit().putLong(UPDATED, System.currentTimeMillis()).commit();
	           	
			}  else {
				Toast.makeText(c, c.getString(R.string.loadOPML_NG), Toast.LENGTH_SHORT).show();
			}

		}// END POST-EXE.

	    @Override
	    protected void onProgressUpdate(String... progress) {
	        mProgressDialog.setMessage(progress[0]);
	    }
		
		
		@Override
		protected Void doInBackground(Void... params) {
			parser = new OPMLParser(false); // FALSE IS FOR VARIABLE CATEGORY (from web).
			parser.context = c;
			/*
			 * Delete old table, EXCLUDING "user" feeds.
			 */
			String[] aug = new String[]{Feed.Category.USER};
			c.getContentResolver().delete(
					Feed.Columns.CONTENT_URI,
					Feed.Columns.CATEGORY + " != ?", // DELETE ROW is not "user"
					aug);
			/*
			 * LOAD Input Stream from WEB.
			 */
			try {
				URL url = new URL(OPML_URL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				publishProgress(c.getString(R.string.opml_nowparsing));
				Xml.parse(new InputStreamReader(is), parser); // PARSE AND
																// INSERT TO DB.
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static class importFileAsync extends importWebAsync {
		private ProgressDialog mProgressDialog;
		private Context c;
		private boolean fileNotFound = false;
		/*
		 * Constructor.
		 */
		public importFileAsync(Context con) {
			this.c = con;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(c);
			mProgressDialog.setMessage(c.getString(R.string.opml_nowloading));
			WindowManager.LayoutParams dlgParams = mProgressDialog.getWindow()
					.getAttributes();
			dlgParams.dimAmount = 0.0f;
			mProgressDialog.getWindow().setAttributes(dlgParams);
			mProgressDialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			mProgressDialog.show();
		}// END PRE-EXE.
		@Override
		protected void onPostExecute(Void result) {
			// ProgressDialog Dismiss!
			try {
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			} catch (Exception e) {// ERROR IN DISMISS.
				e.printStackTrace();
			}// END EXCEption.
			/*
			 * RELOAD "READER" activity with new feeds.
			 */
			if (fileNotFound){
				Toast.makeText(c, c.getString(R.string.filenotfound), Toast.LENGTH_LONG).show();
			}
			if (parser.count >0){
				Toast.makeText(c, c.getString(R.string.loadOPML_ok), Toast.LENGTH_SHORT).show();
				reloadRssReader(c);
			}
		}// END POST-EXE.

	    @Override
	    protected void onProgressUpdate(String... progress) {
	        mProgressDialog.setMessage(progress[0]);
	    }
	    
		@Override
		protected Void doInBackground(Void... params) {
			parser = new OPMLParser(true); // TRUE is for USER import.
			parser.context = c;
			
			/* before Adding.
			 * Delete old table "USER" category.
			 */
			deleteUserFeeds(c);
			
		    File file = new File(filename);
			try {
				FileInputStream fin = new FileInputStream(file);
				publishProgress(c.getString(R.string.opml_nowparsing));
				Xml.parse(new InputStreamReader(fin), parser);
			} catch (FileNotFoundException e) {
				fileNotFound = true; // FILE NOT FOUND.....
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	/*
	 * Delete old table "USER" category.
	 */
	public static int deleteUserFeeds(Context c){
		String[] aug = new String[]{Feed.Category.USER}; // user.
		int i = c.getContentResolver().delete(Feed.Columns.CONTENT_URI,
				Feed.Columns.CATEGORY+" = ?",aug);
		Log.w("deleted.user",String.valueOf(i));
		if (i>0){
			reloadRssReader(c);
		}
		return i;
	}
	
	public static void reloadRssReader(final Context c){
		Intent intent = new Intent(c,RssReaderAct.class);
		intent.setAction(RssReaderAct.ACTION_OPMLUPDATED);
		c.startActivity(intent);// NEW INTENT CAUSES CATEGORY-RELOAD.
	}
	
	public static boolean shouldUPDATE(final Context c){
       	SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(c);
       	Long lastupdate = pf.getLong(UPDATED, 0); // last update.
		Long current = System.currentTimeMillis(); // now.
		if (current - lastupdate > 20 * 24 * 60 * 60 * 1000){ // 20Days. 1000
			return true;
			
		} else {
			return false;
		}
	}
	public static void promptUpdateDialog(final Context c){
   	   final AlertDialog dialog = new AlertDialog.Builder(c)
	      .setMessage(R.string.updateOPML)
	      .setPositiveButton("読み込む", new DialogInterface.OnClickListener() {
	      	 public void onClick(DialogInterface dialog, int which) {
	      		 OPML.importFromWeb(c);
	      	  }})
	      .setCancelable(true)// SHOULD NOT BE CANCELED.
	      .create();
	      dialog.show();
	}
	
	private static class OPMLParser extends DefaultHandler {
		private static final String TAG_BODY = "body";
		private static final String TAG_OUTLINE = "outline";
		private static final String ATTRIBUTE_TITLE = "title";
		private static final String ATTRIBUTE_XMLURL = "xmlUrl";
		private boolean bodyTagEntered;
		private String category;
		private Context context;
		private boolean fromuser; // TRUE when USER-IMPORT.
		public int count; // Increment as DB.insert();

		// CUSTOM CONSTRUCTOR;
		public OPMLParser(boolean isfromuser){
			super();
			count = 0; // INIT as Zero.
			fromuser = isfromuser;
			if (fromuser){
				category = Feed.Category.USER; // FIX CATEGORY NAME AS 'user'.
			}
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (!bodyTagEntered) {
				if (TAG_BODY.equals(localName)) {
					bodyTagEntered = true;
				}

			} else if (TAG_OUTLINE.equals(localName)) {
				String url = attributes.getValue(EMPTYSTRING, ATTRIBUTE_XMLURL);

				if (url == null) {
					/*
					 * NO URL, THIS 'outline' is for CATEGORY. UPDATE THE MEMBER
					 * WITH THE CAT-NAME.
					 */
					if (fromuser){
						this.category = Feed.Category.USER; // CATEGORY IS ALWAYS USER!
					} else {
						this.category = attributes.getValue(EMPTYSTRING,
								ATTRIBUTE_TITLE); // CATEGORY IS FROM OPML.
					}
				} else {
					/*
					 * URL IS AVALEBLE == FEED ITEM. PUTTING VALUES TO DB.
					 */
					String title = attributes.getValue(EMPTYSTRING,
							ATTRIBUTE_TITLE);// EMPTY for "no namespace"

					ContentValues values = new ContentValues();
					values.put(Feed.Columns.CATEGORY, category);
					values.put(Feed.Columns.URL, url);
					Log.w("url", category + url);
					values.put(Feed.Columns.NAME, title != null
							&& title.length() > 0 ? title : null);

//					Cursor cursor = context.getContentResolver().query(
//							Feed.Columns.CONTENT_URI,
//							null,
//							new StringBuilder(Feed.Columns.URL).append(WHEREQ)
//									.toString(), new String[] { url }, null);
//
//					/*
//					 * INSERT OR UPDATE?
//					 */
//					if (!cursor.moveToFirst()) {
						context.getContentResolver().insert(
								Feed.Columns.CONTENT_URI, values);
						count++;// Increment as record.
						
//					} else {// THERE IS EXISTING, OVERRAP entry;
//						// TODO update row?
//					}
//					cursor.close();
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (bodyTagEntered && TAG_BODY.equals(localName)) {
				bodyTagEntered = false;
			}

		}

	}
}