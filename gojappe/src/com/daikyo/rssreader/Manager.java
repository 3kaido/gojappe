package com.daikyo.rssreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class Manager extends FragmentActivity {
	public static final byte MENU_WEBOPML = Menu.FIRST;
	public static final byte MENU_USEROPML = Menu.FIRST + 1;
	public static final byte MENU_FEEDBACK = Menu.FIRST + 2;
	public static final byte MENU_DELUSEROPML = Menu.FIRST + 3;
	private Cursor cur;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.managerview);
		
		cur = getContentResolver().query(Feed.Columns.CONTENT_URI, // all
																			// row
																			// in
																			// table.
				null, // proj
				null, // where =all
				null, // where.aug =all
				null);// sort. =no sort.
		MngAdapter adapter = new MngAdapter(this, cur, 0); // GET ADAPTER.
		ListView lv = ((ListView)findViewById(android.R.id.list));
		View empview = findViewById(android.R.id.empty);
		lv.setEmptyView(empview);
		lv.setAdapter(adapter);
    	/*
    	 * HINT
    	 */
    	Button backb = (Button)findViewById(R.id.backb);
    	backb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { // go to webOpen.
				finish();
			}
		});
	}// END ONCREATE


	@Override
	public void onPause() { // FOCUS GONE. KILLED...
		super.onPause();
	}// END onPause.
	
    @Override
    protected void onDestroy() {
    	cur.close(); // CLOSE!
        super.onDestroy();
        Log.w("onDESTROY","called");
    }
	/*
	 * ON O-MENU OPEN.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_WEBOPML, 0, getString(R.string.manager_menuupdatefeed));
		menu.add(0, MENU_USEROPML, 0, getString(R.string.manager_adduserfeed));
		menu.add(0, MENU_FEEDBACK, 0, getString(R.string.menu_feedback));
		menu.add(0, MENU_DELUSEROPML, 0, getString(R.string.manager_menuclearuserfeed));
		return result;
	}// END MENU CREATE.
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
	 * ON O-MENU SELECT.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case MENU_WEBOPML: // LOAD OPML FROM WEB.
			final AlertDialog dialog = new AlertDialog.Builder(Manager.this)
					.setMessage(R.string.loadOPML).setPositiveButton(getString(R.string.manager_menuupdatefeed),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									OPML.importFromWeb(Manager.this);
								}
							}).setCancelable(true)// CANCEL OKAY.
					.create();
			dialog.show();
			return true;

		case MENU_USEROPML: // LOAD OPML FROM WEB.
			final AlertDialog dlog = new AlertDialog.Builder(Manager.this)
					.setMessage(R.string.userOPML).setPositiveButton(getString(R.string.manager_adduserfeed),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									OPML.importFromFile(Manager.this);
								}
							}).setCancelable(true)// CANCEL OKAY.
					.create();
			dlog.show();
			return true;

		case MENU_FEEDBACK: // REDIRECT TO FEEDBACK.
			Uri u = Uri.parse(getString(R.string.feedbackurl));
			Intent itnt = new Intent(Intent.ACTION_VIEW, u);
			startActivity(itnt);
			return true;
		case MENU_DELUSEROPML: // DELETE USER FEEDS.
			final AlertDialog dialogue = new AlertDialog.Builder(Manager.this)
			.setMessage(R.string.clearUserOPML).setPositiveButton(getString(R.string.manager_menuclearuserfeed),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							OPML.deleteUserFeeds(Manager.this);
						}
					}).setCancelable(true)// CANCEL OKAY.
			.create();
			dialogue.show();
			return true;
		}
		return false;
	}	
	
}// END CLASS.JAVA