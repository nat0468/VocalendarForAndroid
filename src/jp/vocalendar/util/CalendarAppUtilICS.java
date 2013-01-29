package jp.vocalendar.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBaseRow;

/**
 * ICS(Android 4.0以降、API Level 14以降)用のカレンダーアプリ連携ユーティリティ
 */
public class CalendarAppUtilICS {
	private CalendarAppUtilICS() { } // インスタンス生成抑止
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void importEvent(EventDataBaseRow row, Context context) {
		Event event = row.getEvent();
        Intent intent = new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI);
        intent.putExtra(Events.TITLE, event.getSummary());
        intent.putExtra(Events.DESCRIPTION, event.getDescription());        
        if(event.isRecursive()) {
        	importRecursiveEvent(row, intent);
        } else {
            importNormalEvent(event, intent);
        }        
        context.startActivity(intent);		
	}

	private static void importNormalEvent(Event event, Intent intent) {
		if(event.isDateEvent()) {
			TimeZone timeZone = TimeZone.getDefault();
		    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
		    		DateUtil.toUTCStartTimeOfDay(event.getStartDate(), timeZone));
			intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true); // 終日
		} else {
		    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
		    		event.getNotNullStartDate().getTime());
		    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
		       		event.getNotNullEndDate().getTime());            	
		}
	}

	private static void importRecursiveEvent(EventDataBaseRow row, Intent intent) {
		// 繰り返しイベントならば、指定された日のみの予定を追加
		Event event = row.getEvent();
		if(event.isDateEvent()) {
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
					row.getDisplayDate().getTime());
			intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true); // 終日
		} else {
			Calendar date = Calendar.getInstance();
			date.setTime(row.getDisplayDate());
			
			Calendar start = Calendar.getInstance();
			start.setTime(event.getStartDateTime());
			setYMD(start, date);
			intent.putExtra(
					CalendarContract.EXTRA_EVENT_BEGIN_TIME,
					start.getTimeInMillis());

			Calendar end = Calendar.getInstance();
			end.setTime(event.getEndDateTime());
			setYMD(end, date);
			intent.putExtra(
					CalendarContract.EXTRA_EVENT_END_TIME,
					end.getTimeInMillis());            	
		}
	}

	/**
	 * 1番目のCalendarに2番目のCalendarの年月日を指定
	 * @param target
	 * @param date
	 */
	private static void setYMD(Calendar target, Calendar date) {
		target.set(Calendar.YEAR, date.get(Calendar.YEAR));
		target.set(Calendar.MONTH, date.get(Calendar.MONTH));
		target.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
	}
}
