package jp.vocalendar.model;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jp.vocalendar.R;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

/**
 * イベントを表すクラス。
 */
public class Event implements Serializable {
	private static final long serialVersionUID = -370059715310487233L;
		
	// 日付表示に使う文字列
	private static String STR_FROM_TO;
	private static String STR_EVERY_YEAR_FORMAT;
	private static String STR_EVERY_MONTH_FORMAT;
	private static String STR_EVERY_WEEK_FORMAT;
	private static String STR_FIRST;
	private static String STR_SECOND;
	private static String STR_THIRD;
	private static String STR_FOURTH;	
	private static String STR_NTH_FORMAT;	
	private static String STR_FIRST_DAY;
	private static String STR_SECOND_DAY;
	private static String STR_THIRD_DAY;
	private static String STR_NTH_DAY_FORMAT;
	private static String STR_AFTER_WEEKDAY_STRING;
	
	/**
	 * 日付表示に使う文字列(多言語対応)の初期化。システムの言語設定に応じて設定
	 * @param context
	 */
	public static void initString(Context context) {
		STR_FROM_TO = context.getString(R.string.from_to);
		
		STR_EVERY_YEAR_FORMAT = context.getString(R.string.every_year_format);
		STR_EVERY_MONTH_FORMAT = context.getString(R.string.every_month_format);
		STR_EVERY_WEEK_FORMAT = context.getString(R.string.every_week_format);		

		STR_FIRST = context.getString(R.string.first);		
		STR_SECOND = context.getString(R.string.second);		
		STR_THIRD = context.getString(R.string.third);		
		STR_FOURTH = context.getString(R.string.fourth);
		STR_NTH_FORMAT = context.getString(R.string.nth_format);
		
		STR_FIRST_DAY = context.getString(R.string.first_day);		
		STR_SECOND_DAY = context.getString(R.string.second_day);		
		STR_THIRD_DAY = context.getString(R.string.third_day);		
		STR_NTH_DAY_FORMAT = context.getString(R.string.nth_day_format);
		
		STR_AFTER_WEEKDAY_STRING = context.getString(R.string.after_weekday_string);
	}
	
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
	protected String formatDateTime = null;
	
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
	public String toDateTimeSummaryString() {
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
	 * 日付イベントの場合にtrueを返す。
	 * @return
	 */
	public boolean isDateEvent() {
		return startDate != null;
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
		formatDateTime = sb.toString();
		return formatDateTime;
	}
	
	/**
	 * 通常(繰り返しでない)イベントの日時の表示文字列。
	 * @param sb
	 */
	private void formatNormalDateTime(StringBuilder sb) {
		Date start = formatDateTime(startDate, startDateTime, sb);
		
		if(endDateTime != null) {
			sb.append(STR_FROM_TO);
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
			sb.append(STR_FROM_TO);			
			sb.append(formatDate(endDate));
		} else {
			sb.append(STR_FROM_TO);			
			sb.append("????");
		}
	}
	
	protected static Date formatDateTime(Date date, Date dateTime, StringBuilder sb) {
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
				if(startDateTime != null) {
					sb.append(String.format(STR_EVERY_YEAR_FORMAT, formatMonthAndDayTime(startDateTime)));
				} else if(startDate != null) {
					sb.append(String.format(STR_EVERY_YEAR_FORMAT, formatMonthAndDay(startDate)));
				} else {
					sb.append(String.format(STR_EVERY_YEAR_FORMAT, "????"));
				}
				// TODO endDateやendDateTimeがある場合の扱い
				break;
			case RECURSIVE_MONTHLY:
				if(byWeekdayOccurrence == 0) { // 日付指定の場合
					sb.append(String.format(STR_EVERY_MONTH_FORMAT, formatDayOfMonth(recursiveBy)));
				} else {
					String dayOfMonth = 
							formatOrdinal(byWeekdayOccurrence) + formatWeekdayString(recursiveBy);
					sb.append(String.format(STR_EVERY_MONTH_FORMAT, dayOfMonth));
				}
				break;
			case RECURSIVE_WEEKLY:
				sb.append(String.format(STR_EVERY_WEEK_FORMAT, formatWeekdayString(recursiveBy)));
				if(startDateTime != null) {
					sb.append(" ");
					sb.append(formatTime(startDateTime));
				}
				break;
			default:
				sb.append("not implemented");
				break;
		}
	}
	
	/**
	 * 月の日の文字列表現(?日, 10th, など)を返す。
	 * @param day
	 * @return
	 */
	private static String formatDayOfMonth(int day) {
		switch (day) {
		case 1:
			return STR_FIRST_DAY;
		case 2:
			return STR_SECOND_DAY;
		case 3:
			return STR_THIRD_DAY;
		}
		return String.format(STR_NTH_DAY_FORMAT, day);
	}
	
	/**
	 * 序数の文字列表現(第1, second, など)を返す。
	 * @param n
	 * @return
	 */
	private static String formatOrdinal(int n) {
		switch(n) {
		case 1:
			return STR_FIRST;
		case 2:
			return STR_SECOND;
		case 3:
			return STR_THIRD;
		case 4:
			return STR_FOURTH;
		}
		return String.format(STR_NTH_FORMAT, n);
	}
	
	/**
	 * 曜日文字列を出力する。
	 * @param weekday 1:日, 2:月, ... , 7:土
	 * @return
	 */
	private String formatWeekdayString(int weekday) {
		StringBuilder sb = new StringBuilder(
				DateUtils.getDayOfWeekString(weekday, DateUtils.LENGTH_LONG));
		sb.append(STR_AFTER_WEEKDAY_STRING);
		return sb.toString();
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
	 * 繰り返し種別を指定する。 
	 * @return RECURSIVE_* 定数を指定する。
	 */
	public int getRecursive() {
		return recursive;
	}
	public void setRecursive(int recursive) {
		this.recursive = recursive;
	}	

	/**
	 * 繰り返しの予定の場合の対象日を指定する値を取得。
	 * RECURSIVE_MONTHLYの場合で、byWeekdayOccurrenceが0の場合は、毎月の日付。
	 * byWeekdayOccurrenceが1以上の場合は、曜日(1:日,2:月, ... , 土:7)。
	 * RECURSIVE_WEEKLYの場合は、曜日(1:日,2:月, ... , 土:7)
	 * 特に指定しない場合は0
	 */		
	public int getRecursiveBy() {
		return recursiveBy;
	}
	
	/**
	 * 繰り返しの予定の場合の対象日を指定する値を設定。
	 * RECURSIVE_MONTHLYの場合で、byWeekdayOccurrenceが0の場合は、毎月の日付。
	 * byWeekdayOccurrenceが1以上の場合は、曜日(1:日,2:月, ... , 土:7)。
	 * RECURSIVE_WEEKLYの場合は、曜日(1:日,2:月, ... , 土:7)
	 * 特に指定しない場合は0
	 */	
	public void setRecursiveBy(int recursiveBy) {
		this.recursiveBy = recursiveBy;
	}
	
	/**
	 * 繰り返しの場合の何番目の曜日かを指定する値。
	 * RECURSIVE_MONTHLYの場合に使われる。
	 * 指定がない場合は0が返る。
	 */	
	public int getByWeekdayOccurrence() {
		return byWeekdayOccurrence;
	}
	
	/**
	 * 繰り返しの場合の何番目の曜日かを指定する値を指定する。
	 * RECURSIVE_MONTHLYの場合に使われる。
	 * 指定しない場合は0を指定する。
	 */	
	public void setByWeekdayOccurrence(int byWeekdayOccurrence) {
		this.byWeekdayOccurrence = byWeekdayOccurrence;
	}	
	public boolean isRecursive() {
		return recursive != RECURSIVE_NONE;
	}
	
	/**
	 * EventSeparator(区切り)のインスタンスならtrueを返す。
	 * このクラスのインスタンスは常にfalse.
	 * @return
	 */
	public boolean isSeparator() {
		return false;
	}
}
