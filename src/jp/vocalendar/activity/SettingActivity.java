package jp.vocalendar.activity;

import jp.vocalendar.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingActivity extends ListActivity{
	
	private class MenuItem {
		private int resouceId;
		private Class activity;
		
		public MenuItem(int resourceId, Class activity) {
			this.resouceId = resourceId;
			this.activity = activity;
		}
		
		public void doAction() {
			Intent intent = new Intent(SettingActivity.this, activity);
			startActivity(intent);			
		}
	}
	
	private class BackMenuItem extends MenuItem {
		public BackMenuItem() {
			super(R.string.back, null);
		}
		
		public void doAction() {
			finish();
		}
	}
	
	private MenuItem[] menuItems = {
		new MenuItem(R.string.setting_about, AboutActivity.class),
		new BackMenuItem()
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setTitle(R.string.setting_title);
    	setListAdapter(new ArrayAdapter<String>(
    			this, android.R.layout.simple_list_item_1, getSettingStrings()));
	}
	
	private String[] getSettingStrings() {
		String[] str = new String[menuItems.length];
		for(int i = 0; i < str.length; i++) {
			str[i] = getResources().getString(menuItems[i].resouceId);
		}
		return str;
	}	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		menuItems[position].doAction();
	}	
}
