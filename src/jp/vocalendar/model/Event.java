package jp.vocalendar.model;


import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.text.format.DateFormat;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;

/**
 * イベントを表すクラス。
 */
public class Event implements Serializable {
	private static final long serialVersionUID = -370059715310487233L;
		
	/** 概要(タイトル) */
	private String summary;
	/** 説明 */
	private String description;
	/** 開始日(1日の予定の場合に使う)。nullの場合は通常の予定。 */
	private Date startDate;
	/** 開始日時(1日の予定でない通常の予定の場合に使う)。nullの場合は1日の予定。 */
	private Date startDateTime;	

	/** 終了日(1日の予定の場合に使う)。実際の終了日+1になる点に注意。nullの場合は通常の予定。 */
	private Date endDate;
	/** 終了日時(1日の予定でない通常の予定の場合に使う)。nullの場合は1日の予定。 */
	private Date endDateTime;	
	
	/** 繰り返しの予定種別 */
	private int recursive = RECURSIVE_NONE;
	
	// 繰り返しの予定種別に使う値
	public static final int RECURSIVE_NONE = 0;
	public static final int RECURSIVE_WEEKLY = 1;
	public static final int RECURSIVE_MONTHLY = 2;
	public static final int RECURSIVE_YEARLY = 3;
	
	/**
	 * 繰り返しの予定の場合の対象日を指定する値。
	 * RECURSIVE_MONTHLYの場合で、byWeekdayOccurrenceが0の場合は、毎月の日付。
	 * byWeekdayOccurrenceが1以上の場合は、曜日(1:日,2:月, ... , 土:7)。
	 * RECURSIVE_WEEKLYの場合は、曜日(1:日,2:月, ... , 土:7)
	 * 特に指定しない場合は0
	 */
	private int recursiveBy = 0;
	
	/**
	 * 繰り返しの場合の何番目の曜日かを指定する値。
	 * RECURSIVE_MONTHLYの場合に使われる。
	 * 指定しない場合は0を指定する。
	 */
	private int byWeekdayOccurrence = 0;
	
	/** formatDateTime()のキャッシュ */
	private String formatDateTime = null;
	
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * summaryのみ表示する文字列を返す。
	 */
	public String toString() {
		return summary;
	}
	
	/**
	 * 開始終了日時とイベント名を表示する文字列を返す。
	 * @return
	 */
	public String toDateTimeString() {
		// TODO メソッド名修正
		return formatDateTime() + " " + summary;
	}
	
	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	/**
	 * イベント日時の表示文字列を返す。
	 * @return	
	 */
	public String formatDateTime() {
		if(formatDateTime != null) {
			return formatDateTime;
		}
		StringBuilder sb = new StringBuilder();
		if(recursive == RECURSIVE_NONE) {
			formatNormalDateTime(sb);
		} else {
			formatRecursiveDateTime(sb);
		}
		return sb.toString();
	}
	
	/**
	 * 通常(繰り返しでない)イベントの日時の表示文字列。
	 * @param sb
	 */
	private void formatNormalDateTime(StringBuilder sb) {
		Date start = formatDateTime(startDate, startDateTime, sb);
		
		if(endDateTime != null) {
			sb.append(" 〜 ");
			if(equalYMD(start, endDateTime)) {
				sb.append(formatTime(endDateTime));
				return;
			}
			sb.append(formatDateTime(endDateTime));
		} else if(endDate != null) {
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(start);
			Calendar endDateCal = Calendar.getInstance();
			endDateCal.setTime(endDate);
			endDateCal.add(Calendar.DATE, -1); // endDateは実際の日付+1のため、-1日して比較			
			if(equalYMD(startCal, endDateCal)) {
				return; 
			}
			sb.append(" 〜 ");			
			sb.append(formatDate(endDate));
		} else {
			sb.append(" 〜 ");			
			sb.append("????");
		}
	}
	
	private Date formatDateTime(Date date, Date dateTime, StringBuilder sb) {
		if(dateTime != null) {
			sb.append(formatDateTime(dateTime));
			return dateTime;
		} else if(date != null) {
			sb.append(formatDate(date));
			return date;
		}
		sb.append("????");
		return null;
	}
	
	private void formatRecursiveDateTime(StringBuilder sb) {
		switch(recursive) {
			case RECURSIVE_YEARLY:
				sb.append("毎年 ");
				if(startDateTime != null) {
					sb.append(formatMonthAndDayTime(startDateTime));
				} else if(startDate != null) {
					sb.append(formatMonthAndDay(startDate));
				} else {
					sb.append("????");
				}
				// TODO endDateやendDateTimeがある場合の扱い
				break;
			case RECURSIVE_MONTHLY:
				sb.append("毎月");
				if(byWeekdayOccurrence == 0) { // 日付指定の場合
					if(recursiveBy != 0) {
						sb.append(recursiveBy);
					} else {
						sb.append("?");
					}
					sb.append("日");
				} else {
					sb.append("第");
					sb.append(byWeekdayOccurrence);
					sb.append(makeWeekdayString(recursiveBy));
					sb.append("曜日");
				}
				break;
			case RECURSIVE_WEEKLY:
				sb.append("毎週");
				sb.append(makeWeekdayString(recursiveBy));
				sb.append("曜日 ");
				if(startDateTime != null) {
					sb.append(formatTime(startDateTime));
				}
				break;
			default:
				sb.append("未実装");
				break;
		}
	}
	
	/**
	 * 曜日文字列を出力する。
	 * @param weekday 1:日, 2:月, ... , 7:土
	 * @return
	 */
	private String makeWeekdayString(int weekday) {
		switch (weekday) {
		case 1:
			return "日";
		case 2:
			return "月";
		case 3:
			return "火";
		case 4:
			return "水";
		case 5:
			return "木";
		case 6:
			return "金";
		case 7:
			return "土";
		}
		return "?";
	}
	
	private static String formatDate(Date date) {
		return formatDate("yyyy年M月d日", date);
	}
	
	private static String formatDateTime(Date date) {
		return formatDate("yyyy年M月d日 k:mm", date);		
	}
	
	private static String formatTime(Date date) {
		return formatDate("k:mm", date);		
	}

	private static String formatMonthAndDay(Date date) {
		return formatDate("M月d日", date);
	}
	
	private static String formatMonthAndDayTime(Date date) {
		return formatDate("M月d日 k:mm", date);		
	}

	private static String formatDate(String pattern, Date date) {
		return DateFormat.format(pattern, date).toString();  
		// return format(pattern, date).toString();
	}
	
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

	/**
	 * ソートに使う、開始日時。
	 * 開始日指定と開始日時の指定されている方が返る。
	 */
	public long getStartDateIndex() {
		if(startDate != null) {
			return startDate.getTime();
		}
		if(startDateTime != null) {
			return startDateTime.getTime();
		}
		return 0;
	}
	
	
	/**
	 * ソートや今日以前のイベントの枝刈りに使う、終了日時。
	 * 終了日指定と終了日時の指定されている方が返る。
	 */
	public long getEndDateIndex() {
		if(endDate != null) {
			return endDate.getTime();
		}
		if(endDateTime != null) {
			return endDateTime.getTime();
		}
		return 0;
	}

	/**
	 * ある日にちと一致するイベントか。
	 * @param date
	 * @param dateのタイムゾーン(ローカルのタイムゾーンを想定)
	 * @return
	 */
	public boolean equalByDate(Date date, TimeZone timeZone) {
		if(recursive == RECURSIVE_NONE) {
			return normalDateEqualByDate(date, timeZone);
		}		
		return recursiveDateEqualByDate(date, timeZone);	
	}
	
	private boolean normalDateEqualByDate(Date date, TimeZone timeZone) {
		Date start = (startDate != null) ? startDate : startDateTime;
		Date end = (endDate != null) ? endDate : endDateTime;
		if(start.before(date) && date.before(end)) {
			return true;
		}
		
		Calendar dateCal = Calendar.getInstance(timeZone);
		dateCal.setTime(date);

		Calendar startDateCal = Calendar.getInstance(timeZone); // 開始日
		startDateCal.setTime(start);
		if(equalYMD(startDateCal, dateCal)) {
			// 開始日と年月日が一致すれば true
			return true;
		}
		
		Calendar endDateCal = Calendar.getInstance(); // 終了日
		//１日の予定の場合、開始日の次の日が終了日になるため、終了日と同じ日を一致と見なさないため、時間を1つ減らす
		//終了時間が次の日の午前0時の場合、その日を一致と見なさないため、時間を1つ減らす
		endDateCal.setTimeInMillis(end.getTime() - 1);
		if(equalYMD(dateCal, endDateCal)) {
			// 終了日と年月日が一致すれば true
			return true;
		}		
		return false;
	}

	/**
	 * 2つの日付の年月日が一致すれば true を返す
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean equalYMD(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);		
		return equalYMD(cal1, cal2);
	}
	
	/**
	 * 2つの日付の年月日が一致すれば true を返す
	 * @param cal1
	 * @param cal2
	 * @return
	 */
	private boolean equalYMD(Calendar cal1, Calendar cal2) {
		if(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&				
				cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)) {
			return true;
		}
		return false;
	}
	
	private boolean recursiveDateEqualByDate(Date date, TimeZone timeZone) {
		Calendar startDateCal = Calendar.getInstance(timeZone);
		startDateCal.setTime((startDate != null) ? startDate : startDateTime);
		Calendar endDateCal = Calendar.getInstance(timeZone);
		endDateCal.setTimeInMillis(((endDate != null) ? endDate.getTime()-1 : endDateTime.getTime()-1)); // 終了日時と一致は一致しないと見なすため
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);
		
		switch(recursive) {
		case RECURSIVE_YEARLY:
			if((startDateCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)) &&
					(startDateCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH))) {
				return true;
			} else if((endDateCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)) &&
					(endDateCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH))) {
				return true;
			}
			break;
		case RECURSIVE_MONTHLY:
			if(byWeekdayOccurrence == 0) {
				if(startDateCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH)) {
					return true;
				} else if(endDateCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH)) {
					return true;
				}
			} else if((dateCal.get(Calendar.DAY_OF_WEEK_IN_MONTH) == byWeekdayOccurrence) &&
				matchWithRecursiveBy(dateCal)) {
				return true;
			}
			break;
		case RECURSIVE_WEEKLY:
			return matchWithRecursiveBy(dateCal);	
		}
		return false;
	}

	/**
	 * recursiveByで指定された曜日と、calの曜日が一致するか判定する。
	 * @param date
	 * @return
	 */
	private boolean matchWithRecursiveBy(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return recursiveBy == 1;
		case Calendar.MONDAY:
			return recursiveBy == 2;
		case Calendar.TUESDAY:
			return recursiveBy == 3;
		case Calendar.WEDNESDAY:
			return recursiveBy == 4;
		case Calendar.THURSDAY:
			return recursiveBy == 5;
		case Calendar.FRIDAY:
			return recursiveBy == 6;
		case Calendar.SATURDAY:
			return recursiveBy == 7;
		}
		return false;
	}
	
	/**
	 * 時分秒を0に設定する。
	 * @param d
	 */
	private Date normalizeDate(Date d) {
		Date ret = (Date)d.clone();
		ret.setHours(0);
		ret.setMinutes(0);
		ret.setSeconds(0);
		return ret;
	}

	/**
	 * iCal4JのComponentからEventを作成する。
	 * VEVENTでないComponentの場合はnullを返す。
	 * @param component
	 * @return
	 */
	public static Event toEvent(Component component) throws ParseException {
		if(!Component.VEVENT.equals(component.getName())) {
			return null;
		}
		
		Event e = new Event();
		
		Property dtstart = component.getProperty(Property.DTSTART);
		Parameter valueParam = dtstart.getParameter(Parameter.VALUE);
		if(valueParam != null && "DATE".equals(valueParam.getValue())) { // 開始日指定の場合
			e.setStartDate(new net.fortuna.ical4j.model.Date(dtstart.getValue())); 			
		} else { // 開始日時指定の場合
			e.setStartDateTime(new DateTime(dtstart.getValue()));
		}
		
		Property dtend = component.getProperty(Property.DTEND);
		valueParam = dtend.getParameter(Parameter.VALUE);
		if(valueParam != null && "DATE".equals(valueParam.getValue())) { // 終了日指定の場合
			e.setEndDate(new net.fortuna.ical4j.model.Date(dtend.getValue())); 			
		} else { // 終了日時指定の場合
			e.setEndDateTime(new DateTime(dtend.getValue()));			
		}
		
		e.setSummary(component.getProperty(Property.SUMMARY).getValue());
		e.setDescription(component.getProperty(Property.DESCRIPTION).getValue());
		
		return e;
	}
	public int getRecursive() {
		return recursive;
	}
	public void setRecursive(int recursive) {
		this.recursive = recursive;
	}
	public int getRecursiveBy() {
		return recursiveBy;
	}
	public void setRecursiveBy(int recursiveBy) {
		this.recursiveBy = recursiveBy;
	}
	public int getByWeekdayOccurrence() {
		return byWeekdayOccurrence;
	}
	public void setByWeekdayOccurrence(int byWeekdayOccurrence) {
		this.byWeekdayOccurrence = byWeekdayOccurrence;
	}	
}
