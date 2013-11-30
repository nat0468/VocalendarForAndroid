package jp.vocalendar.model;

import java.util.TimeZone;

import android.content.Context;

/**
 * 常に日付を表示するイベント配列のカーソル。
 */
public class EventWithAdditionalDateCursor extends EventArrayCursor {
	public EventWithAdditionalDateCursor(EventDataBaseRow[] events, TimeZone timeZone, Context context) {
		super(events, timeZone, context);
	}
	
	@Override
	public String getString(int column) {
		if(column == 1) { // date
			return currentRow.getEvent().formatDateTime(timeZone, context);
		}
		return super.getString(column);
	}
	
}
