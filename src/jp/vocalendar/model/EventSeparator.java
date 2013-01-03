package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jp.vocalendar.util.DateUtil;

import android.text.format.DateUtils;

/**
 * イベント一覧の区切り(日付)を表すクラス。
 * startDate(日付)のみ設定され、
 * 終了日、終了日時、summary, descriptionが全てnullとなる。
 */
public class EventSeparator extends Event {
	public EventSeparator(Date date) {
		setStartDate(date);
	}

	@Override
	public String getSummary() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Date getEndDate() {
		return null;
	}

	@Override
	public Date getEndDateTime() {
		return null;
	}
	
	/**
	 * EventSeparator(区切り)のインスタンスならtrueを返す。
	 * このクラスのインスタンスは常にtrue.
	 * @return
	 */
	public boolean isSeparator() {
		return true;
	}
	
	/**
	 * 開始終了日時とイベント名を表示する文字列を返す。
	 * @return
	 */
	@Override
	public String toDateTimeSummaryString(TimeZone timeZone) {
		return formatDateTime(timeZone);
	}
	
	/**
	 * イベント日時の表示文字列を返す。
	 * @return	
	 */
	@Override	
	public String formatDateTime(TimeZone timeZone) {
		if(formatDateTime != null) {
			return formatDateTime;
		}
		StringBuilder sb = new StringBuilder();
		DateUtil.formatDateTime(getStartDate(), null, sb);
		sb.append(" ");
		Calendar cal = Calendar.getInstance();
		cal.setTime(getStartDate());
		sb.append(DateUtils.getDayOfWeekString(
				cal.get(Calendar.DAY_OF_WEEK), DateUtils.LENGTH_LONG));
		
		formatDateTime = sb.toString();
		return formatDateTime;
	}	
	
	/**
	 * 指定されたEventオブジェクトがEventSeparator(区切り)と
	 * 同等のEvent(終了日、終了日時、summary, descriptionが全てnull)ならtrueを返す。
	 * 
	 * @param event
	 * @return
	 */
	public static boolean isSeparator(Event event) {
		if(event.getEndDate() == null && event.getEndDateTime() == null &&
				event.getSummary() == null && event.getDescription() == null) {
			return true;
		}
		return false;
	}
}
