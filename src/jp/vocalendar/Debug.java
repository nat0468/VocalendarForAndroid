package jp.vocalendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * デバッグ用の処理
 */
public class Debug {
	private static String TAG = "Debug";
	private Context context;
	private boolean debugMode = true; // TODO リリース時にfalseに変更
	private boolean debugMenu = false;
	
	private Debug(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		this.context = context;
		debugMode = pref.getBoolean(Constants.DEBUG_MODE_PREF_NAME, debugMode);
		debugMenu = pref.getBoolean(Constants.DEBUG_MENU_PREF_NAME, debugMenu);
	} 
	
	private static Debug singleton = null;
	
	public static Debug getSingleton(Context context) {
		if(singleton == null) {
			singleton = new Debug(context);
		}
		return singleton;
	}
	
	public static boolean isDebugMode(Context context) {
		return getSingleton(context).isDebugMode();
	}

	public static boolean isDebugMenu(Context context) {
		return getSingleton(context).isDebugMenu();
	}

	public boolean isDebugMode() {
		Log.d(TAG, "isDebugMode: " + debugMode);
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		Log.d(TAG, "setDebugMode: " + debugMode);
		this.debugMode = debugMode;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(Constants.DEBUG_MODE_PREF_NAME, debugMode);
		editor.commit();		
	}

	public boolean isDebugMenu() {
		Log.d(TAG, "isDebugMenu: " + debugMenu);
		return debugMenu;
	}

	public void setDebugMenu(boolean debugMenu) {
		Log.d(TAG, "setDebugMenu: " + debugMenu);
		this.debugMenu = debugMenu;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(Constants.DEBUG_MENU_PREF_NAME, debugMenu);
		editor.commit();		
	}
	
	
	
	
}
