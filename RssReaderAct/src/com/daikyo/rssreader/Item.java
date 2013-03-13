package com.daikyo.rssreader;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class Item {
	public static final String READLATER = "readlater"; // used for rLater.
    public static final String SEPARATOR = "sep!,";
    public static final String DUMMY = "dummmmy";
    private static final String xNAME = "xName";
    private static final String xURL = "xURL";
    private CharSequence mName;
    private String mDate;
    private CharSequence mURL;
    
    	//コンストラクタ。　値を初期化
    public Item() {
        mName = DUMMY;
        mDate = "";
        mURL = "";
    }

    //Dateのメソッド, get and set.
    public String getDate() {
        return "　"+mDate;
        }
    public void setDate(String date) {
        mDate = date;}
    
    //Titleのメソッド
    public CharSequence getTitle() {
        return mName;
        }

    public void setTitle(CharSequence title) {
        mName = title;
        }
    
    public CharSequence getURL() {
    	String tourl = mURL.toString();
    	return tourl;
    	}
    public void setURL(CharSequence url) {
    	mURL = url;
    	}
    
    public void readThisLater(Context c){
        SharedPreferences pflater = c.getSharedPreferences(Item.READLATER, Activity.MODE_PRIVATE);
        pflater.edit().putLong(
        		mName + SEPARATOR + mURL,
        		System.currentTimeMillis()).commit();
        Toast.makeText(c, mName+c.getString(R.string.item_willreadlater), Toast.LENGTH_SHORT).show();
    }
    public void tweet(Context c,String aa){
        Intent tweet = new Intent();
        tweet.setAction(Intent.ACTION_SEND);
        tweet.setType("text/plain");
        tweet.putExtra(Intent.EXTRA_TEXT, aa + " → " +mName+ " "+ mURL + c.getString(R.string.app_hashtag));
        c.startActivity(tweet);
    }
    public void share(final Context c){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mURL);
        intent.putExtra(Intent.EXTRA_SUBJECT, mName);
        c.startActivity(intent);
    }
    public void selectAA(final Context c){
		final AlertDialog.Builder builder = new AlertDialog.Builder(c);
		final String title = c.getText(R.string.selectaa).toString() + mName;
		builder.setCancelable(true);
		builder.setTitle(title);
		builder.setItems (R.array.aa, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int which){ // check item.
				String[] array = c.getResources().getStringArray(R.array.aa);
				String aa = array[which];
				tweet(c,aa);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(c.getString(R.string.item_tweetwithoutaa), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int which){ // check item.
				tweet(c,"");
			}
		});	
		builder.show();
    }
    
    public void openIntent(Context c){
        Uri uri = Uri.parse(mURL.toString());
		try { // NEED TRY/CATCH as it has exception.
			c.startActivity(new Intent(Intent.ACTION_VIEW, uri));
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(c,c.getString(R.string.item_urlinvalid),
						Toast.LENGTH_SHORT).show();
		}
    }
    
    public void openWeb(Context c){
    	// record lastItem;
    	Pref.putLastItemName(c, mName.toString());
    	if (shouldOpenInIntent(mURL.toString())){
    		/*
    		 * open in Intent.
    		 */
    		openIntent(c);
 
    	} else {
    		/*
    		 * open in Gojappe-Browser.
    		 */
    		Intent web = new Intent();
            if (Build.VERSION.SDK_INT>=8){
                web.setClass(c,WebOpenFroyo.class);
            } else {
                web.setClass(c, WebOpen.class);
            }
            web.putExtra(Item.xURL,  mURL);
            web.putExtra(Item.xNAME, mName);
            c.startActivity(web);
    	}
    }
    public static boolean shouldOpenInIntent(String url){
    	/*
    	 * Return true if OPEN-IN-INTENT.
    	 */
		if (url.contains("market://")
				|| url.contains("play.google.")
				|| url.contains("comment") //comment
				|| url.contains("=form") //comment
				|| url.contains(".gif")
				|| url.contains(".png")
				|| url.contains(".jpg")
				|| url.contains(".jpeg")
				|| url.contains(".nicovideo.")
			 	|| url.contains("youtube.com")
				|| url.contains(".2ch.net")
				|| url.contains("twitter.com")) {
	    	return true; // Open-in Intent.
		}
		return false; // OPEN IN Gojappe-browser.
    }
    
    public static Item createItemFromIntent(Intent i){
    	String name = i.getStringExtra(Item.xNAME);
    	String url = i.getStringExtra(Item.xURL);
    	Item item = new Item();
    	item.setTitle(name);
    	item.setURL(url);
    	return item;
    }
    public static Item createItemFromRLATER(Map.Entry<String, Long> entry){
    	String[] tempboth = entry.getKey().split(Item.SEPARATOR);
    	final String name = tempboth[0]; // get name.
    	final String url = tempboth[1]; // get URL.
    	Item item = new Item();
    	item.setTitle(name);
    	item.setURL(url);
    	return item;
    }
    public static Item createItemFromBundle(Bundle args){
		String name = args.getString("name");
		String url = args.getString("url");
    	Item item = new Item();
    	item.setTitle(name);
    	item.setURL(url);
    	return item;
    }
}