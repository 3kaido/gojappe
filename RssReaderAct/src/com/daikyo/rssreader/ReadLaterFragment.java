package com.daikyo.rssreader;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReadLaterFragment extends ListFragment {
	private static long TIMER = 2592000;// 60sec*60min*24h*30days;
	private static final Comparator<Map.Entry<String, Long>> mComparator = new Comparator<Map.Entry<String, Long>>() {
		public int compare(Map.Entry<String, Long> o1,
				Map.Entry<String, Long> o2) {
			return (int) (o2.getValue() - o1.getValue());
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.rsslistfragmentview, null);

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// load here?
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		/*
		 * Title.
		 */
		TextView cat = (TextView) view.findViewById(R.id.category);
		TextView title = (TextView) view.findViewById(R.id.title);
		cat.setText("saved");
		title.setText(getActivity().getString(R.string.readlater));
		/*
		 * Read Later List.
		 */
		LayoutInflater li = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		ListView list = getListView();
		View f1 = li.inflate(R.layout.rlaterfooter, null);
		Button forget = (Button) f1.findViewById(R.id.forgetb);
		forget.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// forget...
				SharedPreferences pflater = getActivity().getSharedPreferences(
						Item.READLATER, Context.MODE_PRIVATE);
				pflater.edit().clear().commit();
				Toast.makeText(getActivity(),
						getActivity().getString(R.string.bkmk_erasedreadlater),
						Toast.LENGTH_SHORT).show();	
			}
		});
		list.addFooterView(f1, null, false);
		View empview = view.findViewById(android.R.id.empty);
		TextView emptv = (TextView) empview.findViewById(R.id.emptxt);
		emptv.setText(R.string.rlaterfoot);
		RLateradapter adapter = new RLateradapter(getActivity());
		final SharedPreferences pflater = getActivity().getSharedPreferences(
				Item.READLATER, Context.MODE_PRIVATE);
		Iterator<?> rliterator = pflater.getAll().entrySet().iterator();
		while (rliterator.hasNext()) { // GET ALL ENTRIES.------
			Map.Entry<String, Long> item = (Map.Entry<String, Long>) rliterator
					.next();

			if ((System.currentTimeMillis() - item.getValue()) / 1000 > TIMER) {
				// item.value is before 30days.
				Log.w("Rlater", "rmove " + item.getKey());
				pflater.edit().remove(item.getKey()).commit();
			} else {
				// item.value is not before 30days.
				Log.w("Rlater", "added " + item.getKey());
				adapter.add(item);
			}// END IF AGED.
		} // END WHILE----
		adapter.sort(mComparator);
		list.setAdapter(adapter);
	}

}