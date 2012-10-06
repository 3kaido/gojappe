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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WebOpenFroyo extends FragmentActivity {

	private WebView wv;
	private WebSettings ws;
	private Item item;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webopen);
		WebHeader wh = (WebHeader)findViewById(R.id.ad);
		final ProgressBar pb = wh.getBar();
		final TextView title = wh.getTitle();

		final SharedPreferences pf = PreferenceManager
		.getDefaultSharedPreferences(this);
		
		// Create Item from Intent.
		Intent intent = getIntent();
		item = Item.createItemFromIntent(intent);

		/*
		 * WEBVIEW set.
		 */
		wv = (WebView)findViewById(R.id.webwindow); // init.
		ws = wv.getSettings(); // init.
		

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
				if (Item.shouldOpenInIntent(url)){
					Uri uri = Uri.parse(url);
					try { // NEED TRY/CATCH as it has exception.
						startActivity(new Intent(Intent.ACTION_VIEW, uri));
					} catch (ActivityNotFoundException anfe) {
						anfe.printStackTrace();
						Toast.makeText(WebOpenFroyo.this,getString(R.string.item_urlinvalid),
								Toast.LENGTH_SHORT).show();
					}
					return true;
				} else {
					view.loadUrl(url); // OPEN WITHIN ITSELF.
					return true;
				}// END CHECK URL.
			}
		});

//		/*
//		 * Plugins
//		 */
//		if (pf.getBoolean(Pref.PLUGINCONFIG, true)) {
//			ws.setPluginState(PluginState.ON_DEMAND);
//			ws.setAllowFileAccess(true);
//		} else {
//			ws.setPluginState(PluginState.OFF);
//		}
		/*
		 * Images
		 */
		if (pf.getBoolean(Pref.LOADIMAGE, true)) {
			ws.setBlockNetworkImage(false);
		} else {
			ws.setBlockNetworkImage(true);
		}

		
		ws.setDefaultFontSize(
				Integer.valueOf(pf.getString(Pref.FONT, WebOpen.FONT_DEFAULT)));
		ws.setJavaScriptEnabled(
				pf.getBoolean(Pref.JAVASCRIPT, false));
		wv.loadUrl(item.getURL().toString());
		/*
		 * page DOWN.
		 */
		ImageButton pgdn = (ImageButton) findViewById(R.id.pgdn);
		pgdn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				wv.pageDown(false);
			}// END ON CLICK.
		});// END LISTENER.
		pgdn.setOnLongClickListener(new OnLongClickListener(){
			// set "close?" button.
			public boolean onLongClick(View v) {
				// prompt "close??"
				final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setCancelable(true);
				builder.setTitle("close?");
				builder.setPositiveButton("ok, close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									final int which) {
								finish();
								dialog.dismiss();
							}// end on-click.
						});// end Listener.
				builder.show();
				return true;	
				}
		});

    	}// END ON CREATE ACTIVITY.
    
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//BACK KEY LONG = "open menu".
			openOptionsMenu();
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}
	
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
		}// END SWITCH....
		return super.onOptionsItemSelected(menu);
	}// END ITEM CLICK....

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		boolean volumescroll = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Pref.VOLUMESCROLL, true);
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
			} else { // ACTION-UP??
				return true;
			}
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (!volumescroll) break; // normal....
			if (e.getAction() == KeyEvent.ACTION_DOWN) {
				wv.pageUp(false);
				return true;
			} else { // ACTION-UP??
				return true;
			}
		} // end switch.
		return super.dispatchKeyEvent(e);
	}

    private class ImageHandler extends Handler {

        public ImageHandler() {
            super();
        }
        
        @Override
        public void handleMessage(Message msg) {
            final String url = msg.getData().getString("url");
            if (url !=null) {
//            	Log.w("url",url);
    			final AlertDialog.Builder builder = new AlertDialog.Builder(WebOpenFroyo.this);
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
    						Toast.makeText(WebOpenFroyo.this, getString(R.string.item_urlinvalid),
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
	
}// END WEBOPEN CLASS.JAVA