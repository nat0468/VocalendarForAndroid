package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.vocalendar.util.DateUtil;

import android.util.Log;


/**
 * イベントオブジェクトの生成メソッドを提供するクラス。
 */
public class EventFactory {
	private static final String TAG = "EventFactory";
	
	public static Event toVocalendarEvent(
			com.google.api.services.calendar.model.Event ge,
			TimeZone timeZone) {
		Event e = new Event();
		e.setId(ge.getId());
		e.setSummary(ge.getSummary());
		e.setDescription(ge.getDescription());
		if(ge.getStart() != null) {
			if(ge.getStart().getDate() != null) {
				e.setStartDate(parseDate(ge.getStart().getDate()));
			} else if(ge.getStart().getDateTime() != null) {
				e.setStartDateTime(new Date(ge.getStart().getDateTime().getValue()));		
			}
		}
		if(ge.getEnd() != null) {
			if(ge.getEnd().getDate() != null) {
				e.setEndDate(parseDate(ge.getEnd().getDate()));				
			} else if(ge.getEnd().getDateTime() != null) {
				e.setEndDateTime(new Date(ge.getEnd().getDateTime().getValue()));
			}
		}
		if(ge.getRecurrence() != null) {
			String rrule = ge.getRecurrence().get(0);
			operateRrule(e, rrule, timeZone);
		}
		return e;
	}
	
	//日付文字列(yyyy-mm-dd)の正規表現パターン
	private static final Pattern dateVauePattern =
			Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");

	private static Date parseDate(String date) {
		Matcher m = dateVauePattern.matcher(date);
		if(m.matches() && m.groupCount() == 3) {
			String year = m.group(1);
			String month = m.group(2);
			String day = m.group(3);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo")); // TODO 決め打ちなのでVOCALENDAR専用になっている
			cal.set(Calendar.YEAR, Integer.parseInt(year));
			cal.set(Calendar.MONTH, Calendar.JANUARY + (Integer.parseInt(month)-1));
			cal.set(Calendar.DATE, Integer.parseInt(day));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);           
			return cal.getTime();
		}
		return null;
	}	
	
	//Recurrence Rule(RRULEコンポーネントのパラメータ)の正規表現パターン
	private static final Pattern recurPattern =
			Pattern.compile("([A-Za-z]+)=([A-Za-z0-9]+)");
	
	private static final String[] WEEKDAY_LIST = new String[] {
		"SU", "MO", "TU", "WE", "TH", "FR", "SA"		
	};
	
	/**
	 * RRULEの値をEventに反映する。
	 * @param e
	 * @param rrule
	 */
	private static void operateRrule(Event e, String rrule, TimeZone timeZone) {
		Log.d(TAG, "operateRrule: " + rrule);
		Matcher m = recurPattern.matcher(rrule);
		while(m.find()) {
			String param = m.group(1);
			String value = m.group(2);
			
			if("FREQ".equals(param)) {
				if("YEARLY".equals(value)) {
					e.setRecursive(DateUtil.RECURSIVE_YEARLY);					
				} else if ("MONTHLY".equals(value)) {
					e.setRecursive(DateUtil.RECURSIVE_MONTHLY);			
					if(e.getRecursiveBy() == 0) {
						// BYMONTHDAY(毎月？日)が未指定の場合、開始日の日を使う (Google Calendarの仕様？)
						Calendar cal = Calendar.getInstance(timeZone);
						cal.setTime(e.getNotNullStartDate());
						e.setRecursiveBy(cal.get(Calendar.DAY_OF_MONTH));
					}
				} else if ("WEEKLY".equals(value)) {
					e.setRecursive(DateUtil.RECURSIVE_WEEKLY);					
				} else { // 未対応のFREQは無視
					Log.w(TAG, "Not implemented FREQ:" + value);
					e.setRecursive(DateUtil.RECURSIVE_NONE);
				}				
			} else if("BYMONTHDAY".equals(param)) {
				e.setRecursiveBy(Integer.parseInt(value));
			} else if("BYDAY".equals(param)) {
				char[] c = new char[1];
				value.getChars(0, 1, c, 0);
				if(Character.isDigit(c[0])) {
					e.setByWeekdayOccurrence(Integer.parseInt(new String(c)));
					value = value.substring(1);
				}
				for(int i = 0; i < WEEKDAY_LIST.length; i++) {
					if(WEEKDAY_LIST[i].equals(value)) {
						e.setRecursiveBy(i+1);
					}
				}
			} else {
				Log.w(TAG, "Not implemented param: " + param + "=" + value);
			}
		}
	}
	
}
