package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
	public static String formatAdditionalDate(Event event, TimeZone timeZone) {
		StringBuilder sb = new StringBuilder();
		if(event.getRecursive() == DateUtil.RECURSIVE_NONE) {
			if(event.isPluralDatesEvent(timeZone)) {
				formatAdditionalDate(event, timeZone, sb);
			}
		} else {
			formatAdditionalRecursiveDate(event, timeZone, sb);
		}
		return sb.toString();
	}
		
	/**
	 * 通常(繰り返しでない)イベントの日時の表示文字列。
	 * @param sb
	 */
	private static void formatAdditionalDate(
			Event event, TimeZone timeZone, StringBuilder sb) {
		Date start = event.getNotNullStartDate();
		sb.append(DateUtil.formatMonthAndDay(start));

		Date end = event.getNotNullEndDate();
		sb.append(DateUtil.STR_FROM_TO);
		sb.append(DateUtil.formatMonthAndDay(end));		
	}
	
	private static void formatAdditionalRecursiveDate(
			Event event, TimeZone timeZone, StringBuilder sb) {
		Date start = event.getNotNullStartDate();
		switch(event.getRecursive()) {
			case DateUtil.RECURSIVE_YEARLY:
				sb.append(String.format(DateUtil.STR_EVERY_YEAR_FORMAT, DateUtil.formatMonthAndDay(start)));
				break;
			case DateUtil.RECURSIVE_MONTHLY:
				if(event.getByWeekdayOccurrence() == 0) { // 日付指定の場合
					sb.append(
							String.format(DateUtil.STR_EVERY_MONTH_FORMAT,
									DateUtil.formatDayOfMonth(event.getRecursiveBy())));
				} else {
					String dayOfMonth = 
							DateUtil.formatOrdinal(event.getByWeekdayOccurrence())
							+ DateUtil.formatWeekdayString(event.getRecursiveBy());
					sb.append(String.format(DateUtil.STR_EVERY_MONTH_FORMAT, dayOfMonth));
				}
				break;
			case DateUtil.RECURSIVE_WEEKLY:
				sb.append(
						String.format(DateUtil.STR_EVERY_WEEK_FORMAT, 
								DateUtil.formatWeekdayString(event.getRecursiveBy())));
				break;
			default:
				sb.append("not implemented");
				break;
		}
	}
	
	
}
