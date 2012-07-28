package jp.vocalendar.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.parameter.Value;


public class EventContentHandler implements ContentHandler {
	private static final String TAG = "EventContentHandler";
	
	
	/** １つのイベントをパース完了時にコールバックするメソッドを実装したインターフェイス */
	public interface EventHandler {
		public void eventParsed(Event event);
	}
	
	/**
	 * １つのイベントをパース完了時にコールバックするEventHandler
	 */
	private EventHandler eventHandler;
	
	
	/** パース中のプロパティ名 */
	private String currentPropertyName = null;

	/** パース中のイベント */
	private Event currentEvent = null;
	/** 日付指定のイベント(DTSTARTまたはDTENDのプロパティで、パラメータVALUEにDATEが指定されている)場合にtrue */
	private boolean isDateSpecified = false;
	/** パース中のプロパティの値 */
	private String currentPropertyValue = null;
	
	private int numberOfEvent = 0;
	
	/**
	 * コンストラクタ
	 * @param eventHandler
	 */
	public EventContentHandler(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}
	
	@Override
	public void startComponent(String name) {
		if(Component.VEVENT.equals(name)) {
			currentEvent = new Event();
		}
	}

	@Override
	public void endComponent(String name) {
		if(Component.VEVENT.equals(name)) {
			if(eventHandler != null) {
				numberOfEvent++;
				eventHandler.eventParsed(currentEvent);
			}
			currentEvent = null;
		}
	}

	@Override
	public void startProperty(String name) {
		currentPropertyName = name;
		isDateSpecified = false;
		currentPropertyValue = null;		
	}		
	
	@Override
	public void propertyValue(String value) throws URISyntaxException,
			ParseException, IOException {
		currentPropertyValue = value;
	}

	@Override
	public void parameter(String name, String value)
			throws URISyntaxException {
		if(Property.DTSTART.equals(currentPropertyName) ||
				Property.DTEND.equals(currentPropertyName)) {
			if(Value.DATE.getName().equals(name) && Value.DATE.getValue().equals(value)) {
				isDateSpecified = true;
			}
		}
		
	}	
	
	@Override
	public void endProperty(String name) {
		if(currentEvent == null) {
			currentPropertyName = null;			
			return;
		}
		
		if(Property.SUMMARY.equals(name)) {
			currentEvent.setSummary(currentPropertyValue);
		} else if(Property.DESCRIPTION.equals(name)) {
			currentEvent.setDescription(currentPropertyValue);			
		} else if(Property.DTSTART.equals(name)) {
			operateDtstart();
		} else if(Property.DTEND.equals(name)) {
			operateDtend();
		} else if(Property.RRULE.equals(name)) {
			operateRrule();
		}
		currentPropertyName = null;
		currentPropertyValue = null;
	}

	private void operateDtend() {
		try {
			if(isDateSpecified) {
				currentEvent.setEndDate(
						parseDateValue(currentPropertyValue));
			} else {
				currentEvent.setEndDateTime(
						new net.fortuna.ical4j.model.DateTime(currentPropertyValue));						
			}
		} catch(ParseException ex) {
			Log.e(TAG, "Parse DTEND property failed. currentProprtyValue="
							+ currentPropertyValue + ", ex=" + ex.getMessage());				
		}
	}

	private void operateDtstart() {
		try {
			if(isDateSpecified) {
				currentEvent.setStartDate(
						parseDateValue(currentPropertyValue));
			} else {
				currentEvent.setStartDateTime(
						new net.fortuna.ical4j.model.DateTime(currentPropertyValue));						
			}
		} catch(ParseException ex) {
			Log.e(TAG, "Parse DTSTART property failed. currentProprtyValue="
							+ currentPropertyValue + ", ex=" + ex.getMessage());				
		}
	}

	//iCalendarの日付文字列の正規表現パターン
	private static final Pattern dateVauePattern =
			Pattern.compile("([0-9]{4})([0-9]{2})([0-9]{2})");
	
	/**
	 * iCalendarの日付文字列をパースする。
 	 * date               = date-value 
     * date-value         = date-fullyear date-month date-mday
     * date-fullyear      = 4DIGIT
     * date-month         = 2DIGIT        ;01-12
     * date-mday          = 2DIGIT        ;01-28, 01-29, 01-30, 01-31
     *                                    ;based on month/year	 * 
	 * @param dateValue
	 * @return
	 */
	private Date parseDateValue(String dateValue) {
		Matcher m = dateVauePattern.matcher(dateValue);
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
	private Pattern recurPattern =
			Pattern.compile("([A-Za-z]+)=([A-Za-z0-9]+)");
	
	private static final String[] WEEKDAY_LIST = new String[] {
		"SU", "MO", "TU", "WE", "TH", "FR", "SA"		
	};
	
	private void operateRrule() {
		Matcher m = recurPattern.matcher(currentPropertyValue);
		while(m.find()) {
			String param = m.group(1);
			String value = m.group(2);
			
			if("FREQ".equals(param)) {
				if("YEARLY".equals(value)) {
					currentEvent.setRecursive(Event.RECURSIVE_YEARLY);					
				} else if ("MONTHLY".equals(value)) {
					currentEvent.setRecursive(Event.RECURSIVE_MONTHLY);					
				} else if ("WEEKLY".equals(value)) {
					currentEvent.setRecursive(Event.RECURSIVE_WEEKLY);					
				} else { // 未対応のFREQは無視
					Log.w(TAG, "Not implemented FREQ:" + value);
					currentEvent.setRecursive(Event.RECURSIVE_NONE);
				}				
			} else if("BYMONTHDAY".equals(param)) {
				currentEvent.setRecursiveBy(Integer.parseInt(value));
			} else if("BYDAY".equals(param)) {
				char[] c = new char[1];
				value.getChars(0, 1, c, 0);
				if(Character.isDigit(c[0])) {
					currentEvent.setByWeekdayOccurrence(Integer.parseInt(new String(c)));
					value = value.substring(1);
				}
				for(int i = 0; i < WEEKDAY_LIST.length; i++) {
					if(WEEKDAY_LIST[i].equals(value)) {
						currentEvent.setRecursiveBy(i+1);
					}
				}
			} else {
				Log.w(TAG, "Not implemented param: " + param + "=" + value);
			}
		}
	}
	
	@Override
	public void startCalendar() {
		// TODO Auto-generated method stub
	}

	@Override
	public void endCalendar() {
		System.out.println("All events are loaded: " + numberOfEvent);
	}

	/*
	// 非Android環境での実行用
	private static class Log {
		public static void e(String tag, String msg) {
			System.out.println(tag + " " + msg);
		}
		public static void w(String tag, String msg) {
			System.out.println(tag + " " + msg);
		}
	}
	*/
}