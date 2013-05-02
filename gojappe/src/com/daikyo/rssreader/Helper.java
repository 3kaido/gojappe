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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class Helper extends ContentProvider {
	public static boolean USE_SDCARD;
	
	public static final String FOLDER = Environment.getExternalStorageDirectory()+"/degosuke/";
	private static final String DATABASE_NAME = "gojappe.db";
	private static final char DATABASE_VERSION = 1; // DB VERSION.
	private static final byte URI_FEEDS = 1; // ID.1
	private static final byte URI_FEED = 2; 
	private static final byte URI_ENTRIES = 3;
	private static final byte URI_ENTRY= 4;
	private static final byte URI_ALLENTRIES = 5;
	private static final byte URI_ALLENTRIES_ENTRY = 6;
	private static final byte URI_FAVORITES = 7;
	private static final byte URI_FAVORITES_ENTRY = 8;
	private static final byte URI_CATEGORY = 10;
	private static final String TABLE_ENTRIES = "entries";
	private static final String ADD = " ADD ";
	private static UriMatcher URI_MATCHER;
	
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Feed.AUTHORITY, "feeds", URI_FEEDS);
		URI_MATCHER.addURI(Feed.AUTHORITY, "feeds/#", URI_FEED);
		URI_MATCHER.addURI(Feed.AUTHORITY, "feeds/#/entries", URI_ENTRIES);
		URI_MATCHER.addURI(Feed.AUTHORITY, "feeds/#/entries/#", URI_ENTRY);
		URI_MATCHER.addURI(Feed.AUTHORITY, "entries", URI_ALLENTRIES);
		URI_MATCHER.addURI(Feed.AUTHORITY, "entries/#", URI_ALLENTRIES_ENTRY);
		URI_MATCHER.addURI(Feed.AUTHORITY, "favorites", URI_FAVORITES);
		URI_MATCHER.addURI(Feed.AUTHORITY, "favorites/#", URI_FAVORITES_ENTRY);
		URI_MATCHER.addURI(Feed.AUTHORITY, "category", URI_CATEGORY); // 
	}
	
	private static class DatabaseHelper {
		private SQLiteDatabase database;
		/*
		 * CONSTRUCTOR.
		 */
		public DatabaseHelper(Context context, String name, int version) {
			File file = new File(FOLDER);
			
			if ((file.exists() && file.isDirectory() || file.mkdir()) && file.canWrite()) {
				try {
					database = SQLiteDatabase.openDatabase(FOLDER+name, null, SQLiteDatabase.OPEN_READWRITE + SQLiteDatabase.CREATE_IF_NECESSARY);
					
					if (database.getVersion() == 0) {
						onCreate(database);
					} else {
						onUpgrade(database, database.getVersion(), DATABASE_VERSION);
					}
					database.setVersion(DATABASE_VERSION);
					USE_SDCARD = true;
				} catch (SQLException sqlException) {
					database = new SQLiteOpenHelper(context, name, null, version) {
						@Override
						public void onCreate(SQLiteDatabase db) {
							DatabaseHelper.this.onCreate(db);
						}

						@Override
						public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
							DatabaseHelper.this.onUpgrade(db, oldVersion, newVersion);
						}
					}.getWritableDatabase();
					USE_SDCARD = false;
				}
			} else { // HERE IS WORKING. NO SDCARD PARMISSION.
				database = new SQLiteOpenHelper(context, name, null, version) {
					@Override
					public void onCreate(SQLiteDatabase db) {
						DatabaseHelper.this.onCreate(db);
					}

					@Override
					public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
						DatabaseHelper.this.onUpgrade(db, oldVersion, newVersion);
					}
				}.getWritableDatabase();
				USE_SDCARD = false;
			}
			// context.sendBroadcast(new Intent(Strings.ACTION_UPDATEWIDGET)); WHAT IS THIS??
		}

		public void onCreate(SQLiteDatabase database) {
		// add another for ENTRY?? database.execSQL(createTable(Feed.TABLE_NAME, Feed.Columns.COLUMNS, Feed.Columns.TYPES));
			/*
			 * creating FEED table.
			 */	
			database.execSQL(
					"CREATE TABLE IF NOT EXISTS " + Feed.TABLE_NAME +
					"(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + // Col.0
					Feed.Columns.NAME 	+ Feed.TYPE_TEXT+","+ // Col.1
					Feed.Columns.URL 	+ Feed.TYPE_TEXT +","+ // Col.2
					Feed.Columns.CATEGORY 	+Feed.TYPE_TEXT +","+ //Col.3
					Feed.Columns.CLICKCOUNT + Feed.TYPE_INT+" )" //Col.4
					);
		}
		

		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			if (oldVersion < 2) {
				// do something.
			}
		}

		public SQLiteDatabase getWritableDatabase() {
			return database;
		}
	}

	private SQLiteDatabase database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int option = URI_MATCHER.match(uri);	
		String table = null;
		StringBuilder where = new StringBuilder();
		
		switch(option) {
			case URI_FEED : {
				table = Feed.TABLE_NAME;
				final String feedId = uri.getPathSegments().get(1); // 1 is ID?
				
				new Thread() {
					public void run() {
						 delete(Feed.Columns.CONTENT_URI(feedId), null, null);
					}
				}.start();
				where.append(Feed.Columns._ID).append('=').append(feedId);
				break;
			}
			case URI_FEEDS : {
				table = Feed.TABLE_NAME;
				// delete(Feed.Columns.CONTENT_URI("0"), null, null);
				break;
			}

		}
		
		if (!TextUtils.isEmpty(selection)) {
			where.append(selection);
		}
		
		int count = database.delete(table, where.toString(), selectionArgs);
		Log.w("deleted",String.valueOf(count));
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		int option = URI_MATCHER.match(uri);
		switch(option) {
			case URI_FEEDS : return "vnd.android.cursor.dir/vnd.Feed.feed";
			case URI_FEED : return "vnd.android.cursor.item/vnd.Feed.feed";
			case URI_FAVORITES : 
			case URI_ALLENTRIES :
			case URI_ENTRIES : return "vnd.android.cursor.dir/vnd.Feed.entry";
			case URI_FAVORITES_ENTRY : 
			case URI_ALLENTRIES_ENTRY : 
			case URI_ENTRY : return "vnd.android.cursor.item/vnd.Feed.entry";
			default : throw new IllegalArgumentException("Unknown URI: "+uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long newId = -1; // LONG TO BE RETURNED AS ID.
		int option = URI_MATCHER.match(uri);
		switch (option) {
			case URI_FEEDS : {
				newId = database.insert(Feed.TABLE_NAME, null, values);
				break;
			}
			case URI_ALLENTRIES : {
				newId = database.insert(TABLE_ENTRIES, null, values);
				break;
			}
			default : throw new IllegalArgumentException("Illegal insert");
		}
		if (newId > -1) {
			getContext().getContentResolver().notifyChange(uri, null);
			return ContentUris.withAppendedId(uri, newId);
		} else { 
			throw new SQLException("Could not insert row into "+uri);
		}
	}

	
	
	
	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext(), DATABASE_NAME, DATABASE_VERSION).getWritableDatabase();
		return database != null;
	}

	/*
	 * QUERY.
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		int option = URI_MATCHER.match(uri);
		Log.w("Q.URI", uri.toString());
		switch(option) {
			case URI_FEED : {
				queryBuilder.setTables(Feed.TABLE_NAME);
				queryBuilder.appendWhere(new StringBuilder(Feed.Columns._ID).append('=').append(uri.getPathSegments().get(1)));
				break;
			}
			case URI_FEEDS : {
				queryBuilder.setTables(Feed.TABLE_NAME);
				break;
			}

			case URI_ALLENTRIES : {
				queryBuilder.setTables("entries join (select name, icon, _id as feed_id from feeds) as F on (entries.feedid = F.feed_id)");
				break;
			}
			case URI_FAVORITES_ENTRY :
				break;
			case URI_CATEGORY:
				Log.w("category", uri.toString());
				queryBuilder.setDistinct(true); // SET ROWS UNIQUE.
				queryBuilder.setTables(Feed.TABLE_NAME);
				break;

		}
		Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int option = URI_MATCHER.match(uri);
		
		String table = null;
		
		StringBuilder where = new StringBuilder();
		
		switch(option) {
			case URI_FEED : {
				table = Feed.TABLE_NAME;
				where.append(Feed.Columns._ID).append('=').append(uri.getPathSegments().get(1));
				break;
			}
			case URI_FEEDS : {
				table = Feed.TABLE_NAME;
				break;
			}

			case URI_ALLENTRIES: {
				table = TABLE_ENTRIES;
				break;
			}
			case URI_FAVORITES_ENTRY : 

		}
		
		if (!TextUtils.isEmpty(selection)) {
			if (where.length() > 0) {
			//	where.append(Strings.DB_AND).append(selection);
			} else {
				where.append(selection);
			}
		}
		
		int count = database.update(table, values, where.toString(), selectionArgs);
		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}
}