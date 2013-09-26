package jp.vocalendar.model;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jp.vocalendar.util.DateUtil;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * イベントを表すクラス。
 */
public class Event implements Serializable {
	private static final long serialVersionUID = -370059715310487233L;
	
	/** Calendar ID(GoogleのCalendar ID) */
	private String gCalendarId = null;

	/** イベントのID(Google Calendarのevent ID) */
	private String gid;
	
	/** 概要(タイトル) */
	private String summary;
	/** 説明 */
	private String description;
	/** 場所 */
	private String location;
	/** 開始日(1日の予定の場合に使う)。nullの場合は通常の予定。 */
	private Date startDate;
	/** 開始日時(1日の予定でない通常の予定の場合に使う)。nullの場合は1日の予定。 */
	private Date startDateTime;	

	/** 終了日(1日の予定の場合に使う)。実際の終了日+1になる点に注意。nullの場合は通常の予定。 */
	private Date endDate;
	/** 終了日時(1日の予定でない通常の予定の場合に使う)。nullの場合は1日の予定。 */
	private Date endDateTime;	
	
	/** 繰り返しの予定種別 */
	private int recursive = DateUtil.RECURSIVE_NONE;
	
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
	public String toDateTimeSummaryString(TimeZone timeZone, Context context) {
		// TODO メソッド名修正
		return formatDateTime(timeZone, context) + " " + summary;
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
	 * 指定された日付において日付イベントと見なせる場合にtrueを返す。
	 * 期間イベントの場合に、開始日時と終了日時の間にある日付の場合に、
	 * 日付イベントと見なす。
	 * @param date
	 * @return
	 */
	public boolean isDateEventOn(Date date, TimeZone timeZone) {
		return EventUtil.isDateEventOn(this, date, timeZone);		
	}

	/**
	 * 2日以上(日付をまたがる)イベントの場合にtrueを返す。
	 * @return
	 */
	public boolean isPluralDatesEvent(TimeZone timeZone) {
		if(isRecursive()) {
			return false;
		}
		if(startDate != null) {			
			//１日の予定の場合、開始日の次の日が終了日になるため、異なる日付(2日以上)と見なさないため、時間を1つ減らす
			Date d = new Date(endDate.getTime()-1);
			if(!DateUtil.equalYMD(startDate, d, timeZone)) {
				return true;
			}
			return false;
		}
		if(!DateUtil.equalYMD(startDateTime, endDateTime, timeZone)) {
			return true;
		}
		return false;
	}	
	
	/**
	 * イベント日時の表示文字列を返す。
	 * @return	
	 */
	public String formatDateTime(TimeZone timeZone, Context context) {
		if(formatDateTime != null) {
			return formatDateTime;
		}
		formatDateTime = EventUtil.formatDateTime(this, timeZone, context);
		return formatDateTime;
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
	 * 開始日または開始日時の値が指定されている方を返す。
	 * @return
	 */
	public Date getNotNullStartDate() {
		if(startDate != null) {
			return startDate;
		}
		return startDateTime; //両方ともnullの場合はないと想定		
	}
	
	/**
	 * 終了日または終了日時の値が指定されている方を返す。
	 * @return
	 */
	public Date getNotNullEndDate() {
		if(endDate != null) {
			return endDate;
		}
		return endDateTime; //両方ともnullの場合はないと想定		
	}

	/**
	 * ソートに使う、開始日時。
	 * 開始日指定と開始日時の指定されている方が返る。
	 */
	public long getStartDateIndex() {
		return getNotNullStartDate().getTime();
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
		Log.d("Event", toString() + ":getEndDateIndex() returns 0!!");
		return 0;
	}

	/**
	 * ある日にちと一致するイベントか。
	 * @param date
	 * @param dateのタイムゾーン(ローカルのタイムゾーンを想定)
	 * @return
	 */
	public boolean equalByDate(Date date, TimeZone timeZone) {
		if(recursive == DateUtil.RECURSIVE_NONE) {
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
		if(DateUtil.equalYMD(startDateCal, dateCal)) {
			// 開始日と年月日が一致すれば true
			return true;
		}
		
		Calendar endDateCal = Calendar.getInstance(); // 終了日
		//１日の予定の場合、開始日の次の日が終了日になるため、終了日と同じ日を一致と見なさないため、時間を1つ減らす
		//終了時間が次の日の午前0時の場合、その日を一致と見なさないため、時間を1つ減らす
		endDateCal.setTimeInMillis(end.getTime() - 1);
		if(DateUtil.equalYMD(dateCal, endDateCal)) {
			// 終了日と年月日が一致すれば true
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
		case DateUtil.RECURSIVE_YEARLY:
			if((startDateCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)) &&
					(startDateCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH))) {
				return true;
			} else if((endDateCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)) &&
					(endDateCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH))) {
				return true;
			}
			break;
		case DateUtil.RECURSIVE_MONTHLY:
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
		case DateUtil.RECURSIVE_WEEKLY:
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
	 * ある日にちと一致、または日にちより過去のイベントならtrueを返す。
	 * ただし繰り返し日付は考慮しない。検索結果の日付比較に使う。
	 * @param date
	 * @return
	 */
	public boolean equalOrAfterByDateWithoutRecursive(Calendar dateCal, TimeZone timeZone) {
		Date date = dateCal.getTime();
		
		Date start = (startDate != null) ? startDate : startDateTime;
		Date end = (endDate != null) ? endDate : endDateTime;
		if(start.before(date) && date.before(end)) {
			return true;
		}
		
		Calendar startDateCal = Calendar.getInstance(timeZone); // 開始日
		startDateCal.setTime(start);
		if(DateUtil.equalYMD(startDateCal, dateCal)) {
			// 開始日と年月日が一致すれば true
			return true;
		}
		
		Calendar endDateCal = Calendar.getInstance(); // 終了日
		//１日の予定の場合、開始日の次の日が終了日になるため、終了日と同じ日を一致と見なさないため、時間を1つ減らす
		//終了時間が次の日の午前0時の場合、その日を一致と見なさないため、時間を1つ減らす
		endDateCal.setTimeInMillis(end.getTime() - 1);
		if(DateUtil.equalYMD(dateCal, endDateCal)) {
			// 終了日と年月日が一致すれば true
			return true;
		}		
		return startDateCal.after(dateCal); // 一致しない場合は、指定日より開始日が後ならtrue
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
		return recursive != DateUtil.RECURSIVE_NONE;
	}
	
	public String getGid() {
		return gid;
	}
	public void setGid(String id) {
		this.gid = id;
	}
	public String getGCalendarId() {
		return gCalendarId;
	}
	public void setGCalendarId(String gCalendarId) {
		this.gCalendarId = gCalendarId;
	}
	
	/** 詳細情報画面のURLリンク */
	public String getDetailUrl() {
		return "http://vocalendar.jp/detail/?feedurl=https://www.google.com/calendar/feeds/" + 
				Uri.encode(gCalendarId) + "/public/full/" + gid;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
