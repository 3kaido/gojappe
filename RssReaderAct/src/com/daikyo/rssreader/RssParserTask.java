package com.daikyo.rssreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daikyo.rssreader.Feed.RssEntry;

public class RssParserTask extends AsyncTask<Void, String, Void> {

	private Context c;
	private ListView lv;
	private RssItemAdapter mAdapter;
	private String errorstring;
	private String address;
	private ProgressBar progressbar;

	enum TAG {
		item, title, date, pubDate, link, author
	}

	// construct1.
	public RssParserTask(Context con, RssEntry rss, ListView list,
			ProgressBar b) {
		super();
		c = con;
		lv = list;
		address = rss.getUrl();
		progressbar = b;
		mAdapter = new RssItemAdapter(c);
	}

	@Override
	protected void onPreExecute() {
		progressbar.setVisibility(View.VISIBLE);
		errorstring = "noError";// init.
	}// END PRE-EXE.

	@Override
	protected void onPostExecute(Void result) {
		if (!errorstring.equals("noError")) {
			Toast.makeText(c, errorstring, Toast.LENGTH_SHORT).show();
		}
		lv.setAdapter(mAdapter);
		progressbar.setVisibility(View.GONE);
	}// END POST-EXE.

	protected void onCancelled(Void result) {
		Log.w("task", "cancel.called");
	}

	@Override
	protected Void doInBackground(Void... entry) {

		// romereader(address); //why so slow?
		// OLD method;;
		try {
			URL url = new URL(address);
			InputStream is = url.openConnection().getInputStream();
			parseXml(is); // PARSE&ADD.
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			errorstring = e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public void romereader(String url) {
//		FeedFetcher feedFetcher = new HttpURLFeedFetcher();
//		try {
//			SyndFeed feed = feedFetcher.retrieveFeed(new URL(url));
//
//			List<SyndEntry> list = feed.getEntries();
//			for (SyndEntry ent : list) {
//				Item item = new Item();
//				item.setTitle(ent.getTitle());
//				item.setDate(ent.getPublishedDate().toString());
//				item.setURL(ent.getLink());
//				mAdapter.add(item);
//			}
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (FeedException e) {
//			e.printStackTrace();
//		} catch (FetcherException e) {
//			e.printStackTrace();
//		}
//	}

	public void parseXml(InputStream is) throws IOException,
			XmlPullParserException {

		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(is, null);

			int eventType = parser.getEventType();
			Item currentItem = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG: // -----------
					tag = parser.getName();
					try {
						switch (TAG.valueOf(tag)) { // TAG.valueof() throws
													// exception.
						case item:
							currentItem = new Item();
							break;
						case title:
							currentItem.setTitle(parser.nextText());
							break;
						case date:
						case pubDate:
							currentItem.setDate(parser.nextText());
							break;
						case link:
							currentItem.setURL(parser.nextText());
							break;
						case author:
							if (currentItem.getURL().toString()
									.contains("twitter.com")) {
								String author = parser.nextText().split("@")[0];
								String tweet = currentItem.getTitle()
										.toString();
								currentItem.setTitle(author + ": " + tweet);
							}
							break;
						}// END SWITCH STAG TYPE;
					} catch (Exception e) {
						// ignore unknown tag...
						// e.printStackTrace();
					}
					break; // END START_TAG.

				// ENDでItemを見つけた場合に、currentItemをアダプタに追加
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if (tag.equals("item")) {
						String title = currentItem.getTitle().toString();
						if (title.contains("PR:") || // AD?
								title.contains("AD:")) { // PR?
							// IGNORING...........
						} else if (!title.equals(Item.DUMMY)) {
							mAdapter.add(currentItem);
							currentItem = null;
							// Log.w("current.item",
							// currentItem.getTitle().toString());
						}
					}// END IF ITEM-END.
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			errorstring = e.getMessage();
			e.printStackTrace();
		}// END EXCEPTION.
	} // END METHOD.
}// END CLASS