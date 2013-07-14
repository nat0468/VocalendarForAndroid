package jp.vocalendar;

import jp.vocalendar.model.EventDataBaseRowArray;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * VOCALENDAR for AndroidのApplicationクラス。
 * 読み込んだイベント情報EventDataBaseRowArrayの共有に使う。
 */
public class VocalendarApplication extends Application {
	private static final String TAG = "VocalendarApplication";
	
	private EventDataBaseRowArray eventDataBaseRowArray;

	public EventDataBaseRowArray getEventDataBaseRowArray() {
		return eventDataBaseRowArray;
	}

	public void setEventDataBaseRowArray(EventDataBaseRowArray eventDataBaseRowArray) {
		this.eventDataBaseRowArray = eventDataBaseRowArray;
	}
	
	/**
	 * イベント取得日数を返す。
	 * @return
	 */
	public static int getNumberOfDateToGetEvent(Context context) {		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String value = pref.getString(Constants.NUMBER_OF_DATE_TO_GET_EVENTS_PREFERENCE_NAME, "3");
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			Log.e(TAG, "Invalid number of date to get events: " + value);
		}
		return 3;
	}	
	
	/**
	 * タップ無しで自動でイベントを読み込む設定を返す。
	 * @param context
	 * @return
	 */
	public static boolean getLoadMoreEventWithoutTap(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean value = pref.getBoolean(Constants.LOAD_MORE_EVENT_WITHOUT_TAP, false);
		return value;
	}
}
