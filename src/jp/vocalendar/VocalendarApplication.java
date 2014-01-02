package jp.vocalendar;

import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.FavoriteEventManager;
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
	
	/**
	 * イベント一覧画面で読み込んだイベント情報
	 */
	private EventDataBaseRowArray eventDataBaseRowArray;
	
	/**
	 * お気に入りイベント情報。一覧画面と詳細画面で共有するために使う。
	 */
	private FavoriteEventManager favoriteEventManager = new FavoriteEventManager();

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
	
	/**
	 * もっと検索する時の取得イベント数を返す。
	 * @param context
	 * @return
	 */
	public static int getNumberOfEventToSearchMore(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String value = pref.getString(Constants.NUMBER_OF_EVENTS_TO_SEARCH_MORE_PREFERENCE_NAME, "20");
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			Log.e(TAG, "Invalid number of date to get events: " + value);
		}
		return 20;
	}
	
	/**
	 * カラーテーマを返す
	 * @param context
	 * @return
	 */
	public static String getColorTheme(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(Constants.COLOR_THEME_PREFERENCE_NAME, "THEME_DEFAULT");		
	}

	public static int getNotificationTime(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String time = pref.getString(
				Constants.NOTIFICATION_TIME_PREFERENCE_NAME,
				Constants.DEFAULT_NOTIFICATION_TIME_PREFERENCE_VALUE);
		return Integer.parseInt(time);
	}
	
	public FavoriteEventManager getFavoriteEventManager() {
		return favoriteEventManager;
	}

	public void setFavoriteEventManager(FavoriteEventManager favoriteEventManager) {
		this.favoriteEventManager = favoriteEventManager;
	}
}
