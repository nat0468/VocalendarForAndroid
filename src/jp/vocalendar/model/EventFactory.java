package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * イベントオブジェクトの生成メソッドを提供するクラス。
 */
public class EventFactory {
	public static Event toVocalendarEvent(com.google.api.services.calendar.model.Event ge) {
		Event e = new Event();
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
}
