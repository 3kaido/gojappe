package com.daikyo.rssreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.daikyo.rssreader.RssCategory.CategoryPagerAdapter;

public class RssPager extends Fragment {
	public static final String ACTION_OPMLUPDATED = "reload"; // used in
															// reload-INTENT
															// from OPML.
	public static final String ACTION_NewCatSelected = "newcat"; // used to load new RSS
	
	public static final int MENU_BKMK 			= Menu.FIRST + 1;
	public static final int MENU_SETBKMKSTATE 	= Menu.FIRST + 2;
	public static final int MENU_MANAGER 		= Menu.FIRST + 3;
	public static final int MENU_HELP 			= Menu.FIRST + 4;
	public static final int MENU_UPDATE 		= Menu.FIRST + 5;
	public static final int MENU_RECOMMEND 		= Menu.FIRST + 6;
	public static final int MENU_FEEDBACK 		= Menu.FIRST + 7;
	public RssCategory category; // hold list of Rss.
    CategoryPagerAdapter mAdapter;
    ViewPager mPager;
    private int focusedPage = 0;


    private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            focusedPage = position;
        }
    }
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
        mPager = (ViewPager)v.findViewById(R.id.pager);

		/*
		 * Manager.(LongClick)
		 */
		ImageButton favorite = (ImageButton)v.findViewById(R.id.favorite);
		favorite.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v){
				Intent i = new Intent(getActivity(),Manager.class);
				startActivity(i);
				return true;
			}// END ON LCLICK.
		});// END LISTENER.
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

	/*
	 * ONCREATE...
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * Check update in every 20days.
		 */
		if (OPML.shouldUPDATE(getActivity())) {
			// Open OPML.import().....?
			OPML.promptUpdateDialog(getActivity());
		} else {
			/*
			 *  Start normally.
			 *  In case of EMPTY cat, it will prompt OPML-update;
			 */
			category = RssCategory.loadLastFromPref(getActivity());
			initCategory(category);
		}
		

	} // END OnCreate-------

	   
	private void openManager() {
		Intent goBkmk = new Intent(getActivity(), Manager.class);
		startActivity(goBkmk);
	}// END OPEN Manager

	public void selectCategoryName() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setTitle(R.string.selectcategory);
		final CharSequence[] cseq = RssCategory.getCategoryList(getActivity());
		builder.setSingleChoiceItems(cseq, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, final int which) {
						/*
						 * New Category selcted.
						 */
						String name = (String)cseq[which];
						RssCategory cat = RssCategory.saveAndReturnNewCategory(getActivity(), name);
						Intent i = new Intent(getActivity(),RssReaderAct.class);
						i.setAction(ACTION_NewCatSelected);
						startActivity(i);
						dialog.dismiss();
					}
				});
		builder.setPositiveButton(getString(R.string.rsscategory_crawlall),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						/*
						 * category = all
						 */
						String name = Feed.Category.ALL;
						RssCategory cat = RssCategory.saveAndReturnNewCategory(getActivity(), name);
						Intent i = new Intent(getActivity(),RssReaderAct.class);
						i.setAction(ACTION_NewCatSelected);
						startActivity(i);
						dialog.dismiss();
					}
				});
		builder.show();
	}
	public void initCategory(RssCategory cat){
		category = cat;
			/*
			 *  normal Pager! (from cursor)
			 */
	        mAdapter = new CategoryPagerAdapter(getFragmentManager(),category);
	        mPager.setOnPageChangeListener(new MyPageChangeListener());
	        mPager.setAdapter(mAdapter);
	}


//	/*
//	 * ON O-MENU OPEN.
//	 * 
//	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
//	 */
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(0, MENU_BKMK, 0, getString(R.string.reader_menufavlist));
//		menu.add(0, MENU_SETBKMKSTATE, 0, getString(R.string.reader_menusetaccessmethod));
//		menu.add(0, MENU_MANAGER, 0, getString(R.string.reader_menumanager));
//		menu.add(0, MENU_HELP, 0, getString(R.string.menu_help));
//		menu.add(0, MENU_FEEDBACK, 0,getString(R.string.menu_feedback));
//		menu.add(0, MENU_UPDATE, 0, getString(R.string.menu_checkupdate));
//		menu.add(0, MENU_RECOMMEND, 0, getString(R.string.menu_recommend));
//		return super.onCreateOptionsMenu(menu);
//	}// END MENU CREATE.

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

		case MENU_UPDATE: // GO TO MARKET.
			Uri uri = Uri.parse(getString(R.string.mymarket));
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			return true;

		case MENU_FEEDBACK: // GO TO FEEDBACK.
			Uri u = Uri.parse(getString(R.string.feedbackurl));
			Intent itnt = new Intent(Intent.ACTION_VIEW, u);
			startActivity(itnt);
			return true;

		case MENU_BKMK:
			openBkmk(null);
			return true;

		case MENU_MANAGER:
			openManager();
			return true;

		case MENU_SETBKMKSTATE:
			// TOAST HOWTO;;
        	Toast.makeText(getActivity(), getString(R.string.howtosetfav), Toast.LENGTH_LONG).show();
			return true;

		case MENU_RECOMMEND:
			String txt = getString(R.string.recommend);
			Intent tweet = new Intent();
			tweet.setAction(Intent.ACTION_SEND);
			tweet.setType("text/plain");
			tweet.putExtra(Intent.EXTRA_TEXT, txt);
			startActivity(tweet);
			return true;
		}// END MENU SWITCH---------
		return super.onOptionsItemSelected(item);
	}



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
    	 */
		Intent goBkmk = new Intent(getActivity(), BkmkReader.class);
		startActivity(goBkmk);
    }
    
    public void openHelp(View target) {
    	/*
    	 * called from .XML;
    	 */
		Uri url = Uri.parse(getString(R.string.myhelpurl));
		Intent i = new Intent(Intent.ACTION_VIEW, url);
		startActivity(i);
    }
    public void selectCategory(View target) {
		Log.w("selectcat", "called");
		/*
		 * called from .xml resource
		 */
		selectCategoryName();
    }
}// END CLASS FILE.