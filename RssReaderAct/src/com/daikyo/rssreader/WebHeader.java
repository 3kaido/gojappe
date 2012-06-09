package com.daikyo.rssreader;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebHeader extends LinearLayout {
    
    public WebHeader(Context context, AttributeSet attrs) {
        super(context,attrs);
    }
    
    public static float scaledDensity(Context c){
    	DisplayMetrics metrics = new DisplayMetrics();  
    	WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
    	wm.getDefaultDisplay().getMetrics(metrics);
//    	Log.w("test", "scaledDensity=" + metrics.scaledDensity);
    	float scaled = metrics.scaledDensity; // = density.
    	return scaled;
    }
    
    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	if (w >=  (420 * scaledDensity(getContext()))){ // IF CHANGED.
    		/*
    		 * wide enough =H.
    		 */
    		setOrientation(HORIZONTAL);
    		getTitle().setMaxLines(3);
    		
    	} else {
    		/*
    		 * not so wide =V.
    		 */
    		setOrientation(VERTICAL);
    		getTitle().setMaxLines(1);
    	}
    	super.onSizeChanged(w, h, oldw, oldh);
    }
    
	public TextView getTitle(){
		ViewGroup vg = (ViewGroup)findViewById(R.id.topbar);
		TextView title = (TextView)vg.findViewById(R.id.title);
		return title;
	}
	public ProgressBar getBar(){
		ViewGroup vg = (ViewGroup)findViewById(R.id.topbar);
		ProgressBar pb = (ProgressBar)vg.findViewById(R.id.progress);
		return pb;
	}
}
