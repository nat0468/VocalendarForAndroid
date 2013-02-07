package jp.vocalendar.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
	
}
