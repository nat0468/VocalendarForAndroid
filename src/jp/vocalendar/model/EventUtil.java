package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;

import jp.vocalendar.util.DateUtil;

/**
 * Eventまわりの演算や文字列作成のユーティリティを提供するクラス
 */
public class EventUtil {
	private EventUtil() { } //インスタンス生成禁止
	
	/**
	 * 指定された日付における開始時間を返す。
	 * 日をまたがらない通常イベントの場合は、イベントの開始時間を表すStartTimeを返す。
	 * 終日イベントの場合は、終日イベント扱いのStrtTimeを返す。
	 * 期間イベントの場合は、開始日であれば開始時間、終了日であれば終了時間、
	 * それ以外の日は終日イベント扱いでStartTimeを返す。
	 * @param event
	 * @param date
	 * @param timeZone
	 * @return 開始時間
	 */
	public static StartTime getStartTime(Event event, Date date, TimeZone timeZone) {
		if(isDateEventOn(event, date, timeZone)) {
			return new StartTime();
		}
		
		if(event.isPluralDatesEvent(timeZone)) {
			Calendar calEvent = Calendar.getInstance(timeZone);
			calEvent.setTime(event.getEndDateTime());
			Calendar calDate = Calendar.getInstance(timeZone);
			calDate.setTime(date);			

			if(DateUtil.equalYMD(calEvent, calDate)) {
				// 複数日イベントの場合、表示日付と終了日が同じなら終了時間を返す。
				return new StartTime(
						calEvent.get(Calendar.HOUR_OF_DAY),
						calEvent.get(Calendar.MINUTE), true);					
			}
			
		}

		// 開始時間を返す
		Calendar calEvent = Calendar.getInstance(timeZone);
		calEvent.setTime(event.getStartDateTime());
		return new StartTime(
				calEvent.get(Calendar.HOUR_OF_DAY),
				calEvent.get(Calendar.MINUTE), false);					
	}
	
	/**
	 * 開始時間を返す。
	 * 日付イベントなら「終日」を返す。
	 */
	public static StartTime getStartTime(Event event, TimeZone timeZone) {
		if(event.isDateEvent()) {
			return new StartTime(); // 日付イベントなら「終日」
		}
		// 開始時間を返す
		Calendar calEvent = Calendar.getInstance(timeZone);
		calEvent.setTime(event.getStartDateTime());
		return new StartTime(
				calEvent.get(Calendar.HOUR_OF_DAY),
				calEvent.get(Calendar.MINUTE), false);					
	}

	/**
	 * 指定された日付において日付イベントと見なせる場合にtrueを返す。
	 * 期間イベントの場合に、開始日時と終了日時の間にある日付の場合に、
	 * 日付イベントと見なす。
	 * @param date
	 * @return
	 */
	public static boolean isDateEventOn(Event event, Date date, TimeZone timeZone) {
		if(event.isDateEvent()) {
			return true;
		}
		if(!event.isRecursive()) {
			// 繰り返しイベントでない(期間イベントまたは通常イベント)の場合、
			// 開始日時と終了日時の間にある日付の場合に、日付イベントと見なす
			if(!DateUtil.equalYMD(event.getNotNullStartDate(), date, timeZone)
					&& !DateUtil.equalYMD(event.getNotNullEndDate(), date, timeZone)) {
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * イベントの日付に関する付加情報を表す文字列を返す。
	 * @param timeZone
	 * @return
	 */
	public static String formatAdditionalDate(Event event, TimeZone timeZone, Context context) {
		StringBuilder sb = new StringBuilder();
		if(event.getRecursive() == DateUtil.RECURSIVE_NONE) {
			if(event.isPluralDatesEvent(timeZone)) {
				formatAdditionalDate(event, timeZone, sb, context);
			}
		} else {
			formatAdditionalRecursiveDate(event, timeZone, sb, context);
		}
		return sb.toString();
	}
		
	/**
	 * 通常(繰り返しでない)イベントの日時の表示文字列。
	 * @param sb
	 */
	private static void formatAdditionalDate(
			Event event, TimeZone timeZone, StringBuilder sb, Context context) {
		DateUtil.ConstString str = DateUtil.getString(context);
		Date start = event.getNotNullStartDate();
		sb.append(DateUtil.formatMonthAndDay(start));

		Date end = new Date(event.getNotNullEndDate().getTime() - 1); // 終了日は終了日の次の日の時間のため、1つ減らす
		sb.append(str.STR_FROM_TO);
		sb.append(DateUtil.formatMonthAndDay(end));		
	}
	
	private static void formatAdditionalRecursiveDate(
			Event event, TimeZone timeZone, StringBuilder sb, Context context) {
		DateUtil.ConstString str = DateUtil.getString(context);
		Date start = event.getNotNullStartDate();
		switch(event.getRecursive()) {
			case DateUtil.RECURSIVE_YEARLY:
				sb.append(String.format(str.STR_EVERY_YEAR_FORMAT, DateUtil.formatMonthAndDay(start)));
				break;
			case DateUtil.RECURSIVE_MONTHLY:
				if(event.getByWeekdayOccurrence() == 0) { // 日付指定の場合
					sb.append(
							String.format(str.STR_EVERY_MONTH_FORMAT,
									DateUtil.formatDayOfMonth(event.getRecursiveBy(), context)));
				} else {
					String dayOfMonth = 
							DateUtil.formatOrdinal(event.getByWeekdayOccurrence(), context)
							+ DateUtil.formatWeekdayString(event.getRecursiveBy(), context);
					sb.append(String.format(str.STR_EVERY_MONTH_FORMAT, dayOfMonth));
				}
				break;
			case DateUtil.RECURSIVE_WEEKLY:
				sb.append(
						String.format(str.STR_EVERY_WEEK_FORMAT, 
								DateUtil.formatWeekdayString(event.getRecursiveBy(), context)));
				break;
			default:
				sb.append("not implemented");
				break;
		}
	}
	
	/**
	 * イベント日時の表示文字列を返す。
	 * @return	
	 */
	public static String formatDateTime(Event event, TimeZone timeZone, Context context) {
		StringBuilder sb = new StringBuilder();
		if(event.getRecursive() == DateUtil.RECURSIVE_NONE) {
			formatNormalDateTime(event, sb, timeZone, context);
		} else {
			formatRecursiveDateTime(event, sb, context);
		}
		return sb.toString();
	}
	
	/**
	 * 通常(繰り返しでない)イベントの日時の表示文字列。
	 * @param sb
	 */
	private static void formatNormalDateTime(
			Event event, StringBuilder sb, TimeZone timeZone, Context context) {
		DateUtil.ConstString str = DateUtil.getString(context);		
		Date start = DateUtil.formatDateTime(
				event.getStartDate(), event.getStartDateTime(), sb);
		
		if(event.getEndDateTime() != null) {
			sb.append(str.STR_FROM_TO);
			if(DateUtil.equalYMD(start, event.getEndDateTime(), timeZone)) {
				sb.append(DateUtil.formatTime(event.getEndDateTime()));
				return;
			}
			sb.append(DateUtil.formatDateTimeWithoutYear(event.getEndDateTime()));
		} else if(event.getEndDate() != null) {
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(start);
			Calendar endDateCal = Calendar.getInstance();
			endDateCal.setTime(event.getEndDate());
			endDateCal.add(Calendar.DATE, -1); // endDateは実際の日付+1のため、-1日して比較			
			if(DateUtil.equalYMD(startCal, endDateCal)) {
				return; 
			}
			sb.append(str.STR_FROM_TO);
			Date endDate = new Date(event.getEndDate().getTime() - 1);
			sb.append(DateUtil.formatDateWithoutYear(endDate)); // endDateは実際の日付+1のため、-1して出力	
		} else {
			sb.append(str.STR_FROM_TO);			
			sb.append("????");
		}
	}
	
	private static void formatRecursiveDateTime(
			Event event, StringBuilder sb, Context context) {
		DateUtil.ConstString str = DateUtil.getString(context);		
		switch(event.getRecursive()) {
			case DateUtil.RECURSIVE_YEARLY:
				if(event.getStartDateTime() != null) {
					sb.append(String.format(
							str.STR_EVERY_YEAR_FORMAT,
							DateUtil.formatMonthAndDayTime(event.getStartDateTime())));
				} else if(event.getStartDate() != null) {
					sb.append(String.format(
							str.STR_EVERY_YEAR_FORMAT, 
							DateUtil.formatMonthAndDay(event.getStartDate())));
				} else {
					sb.append(String.format(str.STR_EVERY_YEAR_FORMAT, "????"));
				}
				// TODO endDateやendDateTimeがある場合の扱い
				break;
			case DateUtil.RECURSIVE_MONTHLY:
				if(event.getByWeekdayOccurrence() == 0) { // 日付指定の場合
					sb.append(String.format(str.STR_EVERY_MONTH_FORMAT,
							DateUtil.formatDayOfMonth(event.getRecursiveBy(), context)));
				} else {
					String dayOfMonth = 
							DateUtil.formatOrdinal(event.getByWeekdayOccurrence(), context)
							+ DateUtil.formatWeekdayString(event.getRecursiveBy(), context);
					sb.append(String.format(str.STR_EVERY_MONTH_FORMAT, dayOfMonth));
				}
				break;
			case DateUtil.RECURSIVE_WEEKLY:
				sb.append(String.format(str.STR_EVERY_WEEK_FORMAT,
						DateUtil.formatWeekdayString(event.getRecursiveBy(), context)));
				if(event.getStartDateTime() != null) {
					sb.append(" ");
					sb.append(DateUtil.formatTime(event.getStartDateTime()));
				}
				break;
			default:
				sb.append("not implemented");
				break;
		}
	}

	/**
	 * ダミーのイベント情報を作成する。
	 * @param eventSummary
	 * @return
	 */
	public static Event generateDummyEvent(String eventSummary) {
		Event e = new Event();
		e.setSummary(eventSummary);
		e.setStartDateTime(new Date());
		e.setEndDateTime(new Date(System.currentTimeMillis() + 1000 * 60));
		return e;
	}	
	
}
