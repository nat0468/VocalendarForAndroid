package jp.vocalendar.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;

import jp.vocalendar.R;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

/**
 * 日付処理のユーティリティ
 */
public class DateUtil {
	public static class ConstString {		
		// 日付表示に使う文字列
		public final String STR_FROM_TO;
		public final String STR_TO;
		public final String STR_EVERY_YEAR_FORMAT;
		public final String STR_EVERY_MONTH_FORMAT;
		public final String STR_EVERY_WEEK_FORMAT;
		public final String STR_FIRST;
		public final String STR_SECOND;
		public final String STR_THIRD;
		public final String STR_FOURTH;
		public final String STR_NTH_FORMAT;
		public final String STR_FIRST_DAY;
		public final String STR_SECOND_DAY;
		public final String STR_THIRD_DAY;
		public final String STR_NTH_DAY_FORMAT;
		public final String STR_AFTER_WEEKDAY_STRING;
		public final String STR_ALL_DAY;
		public final String STR_END;
		
		ConstString(Context context) {
			STR_FROM_TO = context.getString(R.string.from_to);
			STR_TO = context.getString(R.string.to);
			
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
			
			STR_ALL_DAY = context.getString(R.string.all_day);			
			
			STR_END = context.getString(R.string.end);
		}
	}
	
	// ConstStringのシングルトン
	private static ConstString string = null;
	
	// 繰り返しの予定種別に使う値
	public static final int RECURSIVE_NONE = 0;
	public static final int RECURSIVE_WEEKLY = 1;
	public static final int RECURSIVE_MONTHLY = 2;
	public static final int RECURSIVE_YEARLY = 3;

	private DateUtil() { }

	/**
	 * 2つの日付の年月日が一致すれば true を返す
	 * @param cal1
	 * @param cal2
	 * @return
	 */
	public static boolean equalYMD(Calendar cal1, Calendar cal2) {
		if(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&				
				cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)) {
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
	public static boolean equalYMD(Date date1, Date date2, TimeZone timeZone) {
		Calendar cal1 = Calendar.getInstance(timeZone);
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance(timeZone);
		cal2.setTime(date2);		
		return equalYMD(cal1, cal2);
	}
	
	/**
	 * 2つの日付date1, date2の年月日が date1 < date2 ならばtrueを返す。 
	 * @param date1
	 * @param date2
	 * @param timeZone
	 * @return
	 */
	public static boolean greaterYMD(Date date1, Date date2, TimeZone timeZone) {
		Calendar cal1 = Calendar.getInstance(timeZone);
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance(timeZone);
		cal2.setTime(date2);		
		return cal1.before(cal2) && !equalYMD(cal1, cal2);		
	}

	public static Date formatDateTime(Date date, Date dateTime, StringBuilder sb) {
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

	/**
	 * 月の日の文字列表現(?日, 10th, など)を返す。
	 * @param day
	 * @return
	 */
	public static String formatDayOfMonth(int day, Context context) {
		DateUtil.ConstString str = DateUtil.getString(context);
		
		switch (day) {
		case 1:
			return str.STR_FIRST_DAY;
		case 2:
			return str.STR_SECOND_DAY;
		case 3:
			return str.STR_THIRD_DAY;
		}
		return String.format(str.STR_NTH_DAY_FORMAT, day);
	}

	/**
	 * 序数の文字列表現(第1, second, など)を返す。
	 * @param n
	 * @return
	 */
	public static String formatOrdinal(int n, Context context) {
		ConstString str = getString(context);
		
		switch(n) {
		case 1:
			return str.STR_FIRST;
		case 2:
			return str.STR_SECOND;
		case 3:
			return str.STR_THIRD;
		case 4:
			return str.STR_FOURTH;
		}
		return String.format(str.STR_NTH_FORMAT, n);
	}

	public static String formatDate(Date date) {
		return formatDate("yyyy年M月d日(E)", date);
	}

	public static String formatDateTime(Date date) {
		return formatDate("yyyy年M月d日(E)k:mm", date);		
	}

	public static String formatDateWithoutYear(Date date) {
		return formatDate("M月d日(E)", date);
	}

	public static String formatDateTimeWithoutYear(Date date) {
		return formatDate("M月d日(E)k:mm", date);		
	}

	public static String formatTime(Date date) {
		return formatDate("k:mm", date);		
	}

	public static String formatMonthAndDay(Date date) {
		return formatDate("M月d日", date);
	}

	public static String formatMonthAndDayTime(Date date) {
		return formatDate("M月d日 k:mm", date);		
	}

	private static String formatDate(String pattern, Date date) {
		return DateFormat.format(pattern, date).toString();  
		// return format(pattern, date).toString();
	}

	/**
	 * 曜日文字列を出力する。
	 * @param weekday 1:日, 2:月, ... , 7:土
	 * @return
	 */
	public static String formatWeekdayString(int weekday, Context context) {
		StringBuilder sb = new StringBuilder(
				DateUtils.getDayOfWeekString(weekday, DateUtils.LENGTH_LONG));
		sb.append(getString(context).STR_AFTER_WEEKDAY_STRING);
		return sb.toString();
	}
			
	/**
	 * 日付表示に使う文字列(多言語対応)を集めたオブジェクトの初期化。
	 * @param context
	 */
	public static ConstString getString(Context context) {
		if(string == null) {
			string = new ConstString(context);
		}
		return string;
	}	
	
	/**
	 * UTCでの指定された日の始まりの時間(0時0分0秒)のミリ秒を返す(エポックタイム)。
	 * @param date
	 * @return
	 */
	public static long toUTCStartTimeOfDay(Date date, TimeZone timeZone) {
		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTime(date);

		Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		utc.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		utc.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		utc.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));		
		utc.set(Calendar.HOUR_OF_DAY, 0);
		utc.set(Calendar.MINUTE, 0);
		utc.set(Calendar.SECOND, 0);
		utc.set(Calendar.MILLISECOND, 0);
		return utc.getTimeInMillis();
	}
	
	/**
	 * 指定された年月日の始まりの時間(0時0分0秒)のCalendarを返す。
	 * @param year
	 * @param month
	 * @param date
	 * @param timeZone
	 * @return
	 */
	public static Calendar getStartTimeOfDay(int year, int month, int date, TimeZone timeZone) {
		Calendar cal = Calendar.getInstance(timeZone);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, date);		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	/**
	 * 指定された年月日(year,month,date)から指定された日数(duration)の開始日と終了日を返す。
	 * @param year
	 * @param month
	 * @param date
	 * @param timeZone
	 * @param duration
	 * @return
	 */
	public static DateTime[] makeStartAndEndDateTime(int year, int month, int date, TimeZone timeZone, int duration) {
        Calendar utcCal = Calendar.getInstance(timeZone);
        utcCal.set(Calendar.YEAR, year);
        utcCal.set(Calendar.MONTH, month);
        utcCal.set(Calendar.DATE, date);        
        utcCal.set(Calendar.HOUR_OF_DAY, 0);
        utcCal.set(Calendar.MINUTE, 0);
        utcCal.set(Calendar.SECOND, 0);
        utcCal.set(Calendar.MILLISECOND, 0);
                
        DateTime[] dates = new DateTime[2];
    	dates[0] = new DateTime(utcCal.getTime(), timeZone); //開始日時   	
    	utcCal.add(Calendar.DATE, duration);
    	dates[1] = new DateTime(utcCal.getTime(), timeZone); //終了日時
    	return dates;
	}
		
}
