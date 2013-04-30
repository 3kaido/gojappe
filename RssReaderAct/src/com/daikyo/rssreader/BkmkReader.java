package com.daikyo.rssreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.daikyo.rssreader.Feed.RssEntry;

public class BkmkReader extends FragmentActivity {
	public static final String ACTION_OPMLUPDATED = "reload"; // used in
															// reload-INTENT
															// from OPML.
	public static final String ACTION_NEWRSS = "newrss"; // used to load new RSS
															// from Bkmk.
	public static final String ACTION_NewCatSelected = "newcat"; // used to load new RSS
	
	public static final int MENU_HELP 			= Menu.FIRST + 4;
	
    BkmkPagerAdapter mAdapter;
    ViewPager mPager;
    private int focusedPage = 0;
    
    public static class BkmkPagerAdapter extends FragmentPagerAdapter {	
        private ArrayList<RssEntry> bkmkArray;
    	/*
    	 * Adapter for "normal" Pager (from cursor)
    	 */
    	
        public BkmkPagerAdapter(Context c,FragmentManager fm) {
            super(fm);
    		/*
    		 * load BKMK from Pref.
    		 */
            
    		bkmkArray = new ArrayList<RssEntry>();
    		SharedPreferences pfBkmk = c.getSharedPreferences(Pref.BOOKMARK, MODE_PRIVATE);
    		Iterator<?> mpGetter = pfBkmk.getAll().entrySet().iterator();
    		
    		while (mpGetter.hasNext()) { // GET ALL ENTRIES.------
    			Map.Entry<String, String> item = (Map.Entry<String, String>) mpGetter
    					.next();
    			String name = item.getKey();
    			String url = item.getValue();
    			RssEntry rss= Feed.RssEntry.createRssFrom(c, name, url);
    			bkmkArray.add(rss);
    		}
    		Collections.shuffle(bkmkArray);// RANDOMIZER....
        }

        @Override
        public int getCount() {
//        	Log.w("gcount","int= " + category.getCount());
        	return bkmkArray.size()+1;
        }

        
        
        @Override
        public Fragment getItem(int position) {
        	if (position == 0){
        		/*
        		 * load ReadLater
        		 */
        		return new ReadLaterFragment();
        	} else {
            	RssEntry rss = bkmkArray.get(position-1);
//            	return RssHolder.newInstance(position, "Fav", rss);
                return ReaderFragment.newInstance("Fav",rss);
        	}
        }
    }

    
    private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
    	
        @Override
        public void onPageSelected(int position) {
            focusedPage = position;
        }
    }
    
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//BACK KEY LONG = "open menu".
			openOptionsMenu();
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}
    
	/*
	 * ONCREATE...
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singlepane);
		//background;
		setBackgroundColor();
		/*
		 * Load Bkmk. Set to Adapter.
		 */
        mAdapter = new BkmkPagerAdapter(this,getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setOnPageChangeListener(new MyPageChangeListener());
        mPager.setPageMargin(8);
        mPager.setPageMarginDrawable(new ColorDrawable(color.black));
        mPager.setAdapter(mAdapter);
	} // END OnCreate-------

	private void setBackgroundColor() {
		View v = findViewById(R.id.parent);
		String color = Pref.getBackgroundColor(this);
		if (color.equals("transparent")){
			v.setBackgroundResource(0);
		} else if (color.equals("black")){
			v.setBackgroundResource(R.drawable.solidblack);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.w("resume", "called");
	}
	   

	private void openManager() {
		Intent goBkmk = new Intent(this, Manager.class);
		startActivity(goBkmk);
	}// END OPEN Manager


	/*
	 * ON O-MENU OPEN.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_HELP, 0, getString(R.string.menu_help));
		return super.onCreateOptionsMenu(menu);
	}// END MENU CREATE.

	/*
	 * ON O-MENU SELECT.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case MENU_HELP:
			openHelp(null);
			return true;

		}// END MENU SWITCH---------
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		// Log.w("disp.key",String.valueOf(e.getKeyCode()));
		if (e.getAction() == KeyEvent.ACTION_DOWN) {
			int which = e.getKeyCode();
			switch (which) {
			case KeyEvent.KEYCODE_SEARCH:
				openManager(); // open Manager.
				return true;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				View v = this.getCurrentFocus();
				Log.w("Dpad-center", String.valueOf(v.toString()));
				v.performClick();
				return true;
//		case KeyEvent.KEYCODE_VOLUME_DOWN:
//		case KeyEvent.KEYCODE_VOLUME_UP:
//
//			final ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.switcher);
//			int now = vs.getDisplayedChild();
//			ListView lv = (ListView) vs.getChildAt(now).findViewById(
//					R.id.list);
//			int last = lv.getLastVisiblePosition();
//			if (which == KeyEvent.KEYCODE_VOLUME_DOWN) {
//				lv.setSelection(last);
//			} else {
//				int first = lv.getFirstVisiblePosition();
//				int diff = last - first;
//				int target = first - diff;
//				if (target < 0) {
//					target = 0;
//				}
//				lv.setSelection(target);
//			}
//			return true;
			}
		}

		return super.dispatchKeyEvent(e);
	}// END DISPATCH KEY.




    public void flipToNext(View target) {
		Log.w("flipnext", "called");
		/*
		 * called from .xml resource;
		 * should flip the pager;
		 */
		int nextpage = focusedPage +1;
		mPager.setCurrentItem(nextpage);
    }
    
    public void openBkmk(View target) {
    	/*
    	 * called from .xml  
    	 * do nothing on BkmkReader;
    	 */
    }
    
    
    public void openHelp(View target) {
    	/*
    	 * called from .XML;
    	 */
		Uri url = Uri.parse(getString(R.string.myhelpurl));
		Intent i = new Intent(Intent.ACTION_VIEW, url);
		startActivity(i);
    }

}// END CLASS FILE.