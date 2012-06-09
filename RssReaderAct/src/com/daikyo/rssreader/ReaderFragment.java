package com.daikyo.rssreader;


import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daikyo.rssreader.Feed.RssEntry;

public class ReaderFragment extends ListFragment {
	/*
	 * Loads Rss Items in foreground/background!
	 */
	private String catName;//category name;
	private Feed.RssEntry rss;
	private ListView lv;
	public ProgressBar pg;
	/*
     * calling new();
     */
    static ReaderFragment newInstance(String catName,RssEntry r) {
        ReaderFragment f = new ReaderFragment();
        // Supply rss  as an argument.
        Bundle args = new Bundle();
        args.putString("category", catName);
        args.putString("name", r.getName());
        args.putString("url", r.getUrl());
        f.setArguments(args);
        return f;
    }
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * get Data from bundle.
		 */
		Bundle args = getArguments();
		String name = args.getString("name");
		String url = args.getString("url");
		catName = args.getString("category");
		rss = RssEntry.createRssFrom(getActivity(), name, url);
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.rsslistfragmentview, null);

		pg = (ProgressBar)v.findViewById(R.id.progress);
		TextView title = (TextView)v.findViewById(R.id.title);
		TextView catv = (TextView)v.findViewById(R.id.category);
		title.setText(rss.getSpannedTitle());
		catv.setText(catName);
		lv = (ListView)v.findViewById(android.R.id.list);
		View footer = inflater.inflate(R.layout.footer, null);
		lv.addFooterView(footer);
		title.setOnLongClickListener(new OnLongClickListener() {
			/*
			 * title rss fav/delete;
			 */
			public boolean onLongClick(View v) {
				rss.changeBookmarkState((TextView) v);
				return true;
			}
		});	
		return v;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		/*
		 * load Rss here.
		 */
		//lv = getListView();// should be called after createV();
		loadRss();
	}

	public void loadRss(){
		RssParserTask task = new RssParserTask(getActivity(), rss, lv,pg);
		task.execute(); // do TASK.
	}    
}