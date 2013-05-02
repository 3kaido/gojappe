package com.daikyo.rssreader;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

import com.daikyo.rssreader.Feed.RssEntry;

/**
 * @author root
 *
 */
public class RssCategory {
	private Context c;
	public String name = "";// name. this is MUST.
	private ArrayList<Integer> randomizedIdArray; // Shuffled ID from the Category.
	public History history;
	public class History {
		int getter;
		public ArrayList<Feed.RssEntry> array;
	}
	
    
    public static class CategoryPagerAdapter extends FragmentPagerAdapter {	
    	/*
    	 * Adapter for "normal" Pager (from cursor)
    	 */
    	RssCategory category;
    	
        public CategoryPagerAdapter(FragmentManager fm,RssCategory cat) {
            super(fm);
            this.category = cat;
        }

        @Override
        public int getCount() {
//        	Log.w("gcount","int= " + category.getCount());
        	return category.getCount();
        }
        
        

        @Override
		public CharSequence getPageTitle(int position) {
        	Feed.RssEntry rss = category.getRss(position);
        	String title = rss.getName();
			return title;
		}

		@Override
        public Fragment getItem(int position) {
        	Feed.RssEntry rss = category.getRss(position);
//        	return RssHolder.newInstance(position, category.getName(), rss);
            return ReaderFragment.newInstance(category.getName(),rss);
        }
    }
	
	
	// CONSTRUCTOR
	public RssCategory(Context con, String name) {
		this.c = con;
		this.name = name;
		randomizedIdArray = this.initFeedArray();
		history = new History();
		history.array = new ArrayList<Feed.RssEntry>();
		history.getter = -1;// INIT AGAIN.
	}

	public String getName() {
		return name;
	}

	public Integer getCount(){
		return randomizedIdArray.size();
	}
		
	public static String[] getCategoryList(Context c){
		String[] chars;
		final String[] proj = {Feed.Columns.CATEGORY}; // PROJECTION= only Category
		Cursor cur = c.getContentResolver().query(Feed.Columns.CONTENT_CATEGORY, 
				// THIS Q  is Distinct.(ALL ROW UNIQUE)
				proj, // proj = only category column.
				null, // where = any
				null, // where.aug = any
				null);// sort. =no sort.
		if (cur.moveToFirst()){
			int colindex = cur.getColumnIndex(Feed.Columns.CATEGORY);
			chars = new String[cur.getCount()];
			for (int i =0; i <= cur.getCount()-1; i++) {
				String name = cur.getString(colindex);
				chars[i] = new String(name);
				cur.moveToNext();
			}
			return chars;
		}
		cur.close();
		return null; // THIS DOESNT INCLUEDE 'all'. 
	}
	
	public static  RssCategory loadLastFromPref(final Context c) {
		SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(c);
		String name = pf.getString(Pref.LASTCATEGORY, Feed.Category.ALL); // LOAD LAST
		RssCategory category = new RssCategory(c, name);
		return category;
	}
	
	public static void saveLastToPref(Context c,String name) {
		/*
		 * v1 method....depreceated;
		 */
		SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(c);
		pf.edit().putString(Pref.LASTCATEGORY, name).commit(); // Save.
	}

	public static RssCategory saveAndReturnNewCategory(Context c,String name) {
	SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(c);
	pf.edit().putString(Pref.LASTCATEGORY, name).commit(); // Save.
	/*
	 * create new from selected;
	 */
	RssCategory category = new RssCategory(c, name);
	return category;
}
	
	
//	public void showHistory(){
//		final String [] hist = history.array.toArray(new String[history.array.size()]);
//		final AlertDialog.Builder builder = new AlertDialog.Builder(c);
//		builder.setCancelable(true);
//		builder.setTitle("HISTORY");
//		builder.setItems(hist, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, final int which) {
//				String where = Feed.Columns.NAME+"=?";
//				String[] aug= {hist[which]};	        	
//				Cursor cur = c.getContentResolver().query(Feed.Columns.CONTENT_URI,
//					null, 	// proj. null is all.
//					where, 	// where
//					aug, 	// where.aug
//					null);	// sort.
//				cur.moveToFirst();
//				String name = cur.getString(1); // 1 is Name
//				String url = cur.getString(2);	// 2 is URL
//				cur.close();
//				Feed.RssEntry rss = RssEntry.createRssFrom(c, name, url);
//				rss.openReader(c);
//			}
//		});
//		builder.show();
//	}

	public Feed.RssEntry getRss(int position){
		RssEntry rss;
		try {
			/*
			 * previously loaded.
			 */
			rss = history.array.get(position);
			return rss;
		} catch (IndexOutOfBoundsException e){
//			e.printStackTrace(); // For TRACE.
			/*
			 * get new.
			 */
			final String where = Feed.Columns._ID + "=?";
			int loopCheck =0; // Init Before Loop....
			do { // GET next from shuffled feed! WITH DELETEDS CHECK.
				history.getter++;
				if (history.getter >= randomizedIdArray.size()-1){
					/*
					 * Checking if Getter is over the size of randomizedIdArray....
					 */
					history.getter = 0; // if yes, init.again!
				}
				int id = randomizedIdArray.get(history.getter);

				final String[] aug = {String.valueOf(id)};
				Cursor cur = c.getContentResolver().
				query(Feed.Columns.CONTENT_URI,
					null, 	// proj. null is all.
					where, 	// where
					aug, 	// where.aug
					null);	// sort.
				cur.moveToFirst();
				String name = cur.getString(1); // 1 is Name
				String url = cur.getString(2);	// 2 is URL
				rss = RssEntry.createRssFrom(c, name, url);
				cur.close();
				if (loopCheck >= randomizedIdArray.size()){
					/*
					 * LoopCheck.
					 * this loop happens if the user disable all the feeds in the category.
					 */
		        	Toast.makeText(c, c.getString(R.string.rsscategory_alldeletederror), Toast.LENGTH_LONG).show();
					break; // this break return 1st RssEntry.
				}
				loopCheck++;
			} while (rss.state == Feed.State.DISABLED);
			history.array.add(rss);
			return rss;
		}
	}

		
	// WHERE used in SQL query.
	public String where(){
		if (name.equals(Feed.Category.ALL)){
			return null; // WHERE must be NULL.
		} else {
			String where = Feed.Columns.CATEGORY + "=?";
			return where;
		}
	}
	// AUG used in SQL query.
	public String[] whereaug(){
		if (name.equals(Feed.Category.ALL)){
			return null; // Where.AUG must be NULL.
		} else {
			String[] aug = {name};
			return aug;
		}
	}
	
	public ArrayList<Integer> initFeedArray(){
		ArrayList<Integer> array = new ArrayList<Integer>();
		Cursor cur = c.getContentResolver().
			query(Feed.Columns.CONTENT_URI,
				null, // proj. null is all.
				this.where(), // where
				this.whereaug(), // where.aug
				null);// sort.
		if (cur.moveToFirst()){ // CURSOR has some returned ROWs
			do {
				int id = cur.getInt(0); // 0 is for _ID
				array.add(id);
				cur.moveToNext();
			} while (!cur.isLast());
			Collections.shuffle(array);// SHUFFLE. DONE.
		}else {
			/*
			 * HERE must have a "LOAD FROM WEB" method
			 */
			OPML.promptUpdateDialog(c);
		}
		cur.close();
		return array;
	}
	public ArrayList<Integer> initBkmkArray(){
		
		
		return null;
	}
}// end Class