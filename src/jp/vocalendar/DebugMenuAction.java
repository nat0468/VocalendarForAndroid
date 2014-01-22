package jp.vocalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBar;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.FavoriteEventDataBaseRow;
import jp.vocalendar.model.FavoriteEventManager;

/**
 * デバッグメニューの処理
 */
public class DebugMenuAction {
	/**
	 * 今日のお気に入りイベントをダミーデータで更新する。
	 * @param context
	 */
	public static void changeFavoriteEventByDummyData(Activity context) {
		VocalendarApplication app = (VocalendarApplication)context.getApplication();
		FavoriteEventManager manager = app.getFavoriteEventManager();
		LinkedList<FavoriteEventDataBaseRow> list = new LinkedList<FavoriteEventDataBaseRow>();
        EventDataBase db = new EventDataBase(context);
		db.open();
        int todayIndex = db.getAllFavoriteEvents(Calendar.getInstance(), TimeZone.getDefault(), list);
        db.close();
		FavoriteEventDataBaseRow row = list.get(todayIndex);
		Event e = row.getEvent();
		if(e.getStartDateTime() != null) {
			e.getStartDateTime().setDate(2);
		} else if(e.getStartDate() != null){
			e.getStartDate().setDate(2);
		}
		if(e.getEndDateTime() != null) {
			e.getEndDateTime().setDate(3);
		} else if(e.getEndDate() != null) {
			e.getEndDate().setDate(3);
		}
		e.setSummary(e.getSummary() + " dummy");
		e.setDescription("dummy\n" + e.getDescription());
		manager.updateFavorite(context, e);
	}
}
