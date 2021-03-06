package com.daikyo.rssreader;

import java.util.Random;

import net.nend.android.NendAdView;
import mediba.ad.sdk.android.openx.MasAdView;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.google.ads.*;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MyAdFragment extends Fragment {

	public final int ADMOB	  = 0;
	public final int MEDIBA  = 1;
	public final int NEND 	= 2;
//	public final int ADLANTIS = 2;
//	public final int AdVision = 1;
	private AdView admob;
	private int state;
	private MasAdView mediba;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		state = -1; // init.
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Context c = getActivity();
		/*
		 * ad
		 */
		int state = new Random().nextInt(3);
		//Log.w("adNum",String.valueOf(state));
		
		switch (0) {
		case MEDIBA: // MEDIBA
//			mediba = new MasAdView(c);
//			mediba.setAuid(c.getString(R.string.mediba));
//			mediba.start();
//			return mediba;
			
		case ADMOB:
			AdRequest request = new AdRequest();
			String keyword = Pref.getLastItemName(c);
			request.addKeyword(keyword);
			admob = new AdView(getActivity(), admobSize(c),
					c.getString(R.string.admob_id));
			admob.loadAd(request);
			return admob;
		case NEND:
			Log.w("adNum","nend");
			NendAdView nend;
			nend = (NendAdView) inflater.inflate(R.layout.nend, null);
			
//			/*
//			* NendAdView(Context context, int [Spotid], String [Apikey])
//			*/
//			NendAdView nend = new NendAdView(getActivity(),9828, "3b656bdd5480e13360e08616cf3ce08870c070ec" );
			return nend;
		}
		return null;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		if (state == ADMOB && admob != null) {
			admob.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public static float scaledDensity(Context c) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		Log.w("test", "scaledDensity=" + metrics.scaledDensity);
		float scaled = metrics.scaledDensity; // = density.
		// Log.w("xdpi",String.valueOf(metrics.xdpi));
		// Log.w("xpx",String.valueOf(metrics.widthPixels));
		// Log.w("dense",String.valueOf(metrics.density));
		return scaled;
	}

	public static AdSize admobSize(Context c) {
		/*
		 * Align Size with Device Window size. so this works only with
		 * Full-screen-width.
		 */
//		AdSize size = new AdSize(AdSize.FULL_WIDTH,AdSize.AUTO_HEIGHT); // Init.
//		AdSize size = AdSize.SMART_BANNER; // Init.
		AdSize size = AdSize.BANNER; // Init.
//		DisplayMetrics metrics = new DisplayMetrics();
//		WindowManager wm = (WindowManager) c
//				.getSystemService(Context.WINDOW_SERVICE);
//		wm.getDefaultDisplay().getMetrics(metrics);
		// Log.w("dense", "scaledDensity=" + metrics.scaledDensity);
		// Log.w("xdpi",String.valueOf(metrics.xdpi));
		// Log.w("xpx",String.valueOf(metrics.widthPixels));
		// Log.w("dense",String.valueOf(metrics.density));
		//
		// if (metrics.widthPixels >= 728 * metrics.scaledDensity) {
		// size = AdSize.IAB_LEADERBOARD;
		// Log.w("size", "iab leaderb");
		// } else if (metrics.widthPixels >= 468 * metrics.scaledDensity) {
		// size = AdSize.IAB_BANNER;
		// Log.w("size", "iab banner");
		// }
		return size;
	}
}
