package com.daikyo.rssreader;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WebOpen extends FragmentActivity {

	public static final String FONT_DEFAULT = "16";
	private WebView wv;
	private WebSettings ws;
	private Item item;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webopen);
		final ProgressBar pb = (ProgressBar)findViewById(R.id.progress);
		final TextView title = (TextView)findViewById(R.id.title);

		// Create Item from Intent.
		Intent intent = getIntent();
		item = Item.createItemFromIntent(intent);
		/*
		 * WEBVIEW set.
		 */
		wv = (WebView) findViewById(R.id.webwindow);
		ws = wv.getSettings();
		wv.setWebChromeClient(new WebChromeClient()); // MAGIC to set as
															// DefaultBrowser?
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				pb.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (view.getTitle() != null)
					title.setText(view.getTitle());
				pb.setVisibility(View.GONE);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Item.shouldOpenInIntent(url)) { // IF yes, open-in-intent.
					Uri uri = Uri.parse(url);
					try { // NEED TRY/CATCH as it has exception.
						startActivity(new Intent(Intent.ACTION_VIEW, uri));
					} catch (ActivityNotFoundException anfe) {
						anfe.printStackTrace();
						Toast.makeText(WebOpen.this, getString(R.string.item_urlinvalid),
								Toast.LENGTH_SHORT).show();
					}
					return true;
				} else {
					view.loadUrl(url); // OPEN WITHIN ITSELF.
					return true;
				}// END CHECK URL.
			}
		});
		final SharedPreferences pf = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Log.w("ver",String.valueOf(Build.VERSION.SDK_INT));

		ws.setDefaultFontSize(
				Integer.valueOf(pf.getString(Pref.FONT, FONT_DEFAULT)));
		/*
		 * Images
		 */
		if (pf.getBoolean(Pref.LOADIMAGE, true)) {
			ws.setBlockNetworkImage(false);
		} else {
			ws.setBlockNetworkImage(true);
		}
//		String ua = pf.getString(Pref.UA, "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; SPH-D700 Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
//		ws.setUserAgentString(ua);
		ws.setJavaScriptEnabled(
				pf.getBoolean(Pref.JAVASCRIPT, true));
		wv.loadUrl(item.getURL().toString());

		ImageButton pgdn = (ImageButton) findViewById(R.id.pgdn);
		pgdn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				wv.pageDown(false);
			}// END ON CLICK.
		});// END LISTENER.
	}// END ON CREATE ACTIVITY.


	@Override
	protected void onStart() {
		final ImageHandler handler = new ImageHandler();
		wv.setOnLongClickListener(new OnLongClickListener(){
			public boolean onLongClick(View v) {
				Log.w("lclick",v.toString());
				Message msg = new Message();
				msg.setTarget(handler);
				wv.requestImageRef(msg);
				return true;
			}// END ON LCLICK.
		});// END LISTENER.	
		super.onStart();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, RssReaderAct.MENU_ITEM_BROWSER, 0, getString(R.string.webopen_openexternalbrowser));
		menu.add(0, RssReaderAct.MENU_SETTING, 0, getString(R.string.webopen_menuconfig));
		menu.add(0, RssReaderAct.MENU_ITEM_TWITTER, 0, getString(R.string.menu_tweet));
        menu.add(0, RssReaderAct.MENU_ITEM_SHARE, 0, getString(R.string.menu_share));
		return result;
	}// END CREATE MENU.

	@Override
	public boolean onOptionsItemSelected(MenuItem menu) {

		switch (menu.getItemId()) {
		case RssReaderAct.MENU_ITEM_BROWSER:
			item.openIntent(this);
			return true;
			
		case RssReaderAct.MENU_SETTING:
			Intent intnt = new Intent(this, Pref.class);
			startActivity(intnt);
			return true;
			
		case RssReaderAct.MENU_ITEM_TWITTER:
			item.selectAA(this);		
			return true;
			
		case RssReaderAct.MENU_ITEM_SHARE:
		    item.share(this);
		    return true;
			
		}// END SWITCH....
		return super.onOptionsItemSelected(menu);
	}// END ITEM CLICK....

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		boolean volumescroll = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Pref.VOLUMESCROLL, false);
		switch (e.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			if (e.getAction() == KeyEvent.ACTION_DOWN && wv.canGoBack()) {
				wv.goBack();
				return true;
			} else {
				break;
			}
		case KeyEvent.KEYCODE_SEARCH:
			if (e.getAction() == KeyEvent.ACTION_DOWN) {
				wv.pageDown(false);
				return true;
			}
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (!volumescroll) break; // normal...
			if (e.getAction() == KeyEvent.ACTION_DOWN) {
				wv.pageDown(false);
				return true;
			}
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (!volumescroll) break; // normal....
			if (e.getAction() == KeyEvent.ACTION_DOWN) {
				wv.pageUp(false);
				return true;
			}
		} // end switch.
		return super.dispatchKeyEvent(e);
	}

	@Override
	public void onPause() { // FOCUS GONE. CHECK FLASH.
		super.onPause();
		if (ws.getPluginsEnabled()) {
			// test stop time??
			// wv.pauseTimers();
			wv.reload();
		}
	}// END onPause.

	@Override
	protected void onResume() {
		super.onResume();
		if (ws.getPluginsEnabled())
			wv.resumeTimers();
	}// END ON-RESUME

    private class ImageHandler extends Handler {

        public ImageHandler() {
            super();
        }
        
        @Override
        public void handleMessage(Message msg) {
            final String url = msg.getData().getString("url");
            if (url !=null) {
//            	Log.w("url",url);
    			final AlertDialog.Builder builder = new AlertDialog.Builder(WebOpen.this);
    			builder.setCancelable(true);
    			builder.setTitle("Open IMAGE?");
    			builder.setMessage(url);
    			builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    			        Uri uri = Uri.parse(url);
    					try { // NEED TRY/CATCH as it has exception.
    						startActivity(new Intent(Intent.ACTION_VIEW, uri));
    					} catch (ActivityNotFoundException e) {
    						e.printStackTrace();
    						Toast.makeText(WebOpen.this, getString(R.string.item_urlinvalid),
    									Toast.LENGTH_SHORT).show();
    					}
    					dialog.dismiss();
    				}});
    			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}});
    			builder.show();
            }
        }
    }
	
	
}// END WebOpen CLASS.JAVA