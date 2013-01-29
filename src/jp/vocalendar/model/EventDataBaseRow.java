package jp.vocalendar.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.util.Log;

/**
 * イベントデータベースの1行を表すクラス。
 * イベント情報にインデックス属性が付加されたデータ。
 */
public class EventDataBaseRow implements Serializable {
	private static final long serialVersionUID = 1L;

	// 行の種別を表す定数
	public static final int TYPE_NORMAL_EVENT = 0; // Event
	public static final int TYPE_SEPARATOR = 1; // 日付セパレータ
	public static final int TYPE_NO_EVENT = 2; // 「予定なし」に対応する行
	
	/** 行の種別 */
	private int rowType = TYPE_NORMAL_EVENT;
	
	/** イベント情報 */
	private Event event = null;	
	
	/** このイベントを表示する日付。日時表示のソート順や表示文字列に使われる */
	private Date displayDate = null;
	
	/** インデックス。表示順をイベントデータベース挿入時と同じ順番に維持するために追加された序数 */
	private int index = Integer.MIN_VALUE;
	
	/** 
	 * イベントのインデックス。イベント情報が格納されたEventDataBaseRowの順番。
	 * イベント情報が格納されたEventDataBaseRowのみ順番に取り出すのに使う。
	 * 0始まりで、表示順に+1されていく。TYPE_NORMAL_EVENTのみ0以上の値を持つ。
	 * それ以外のTYPEのEventDataBaseRowはInteger.MIN_VALUEを持つ。
	 */
	private int eventIndex = Integer.MIN_VALUE;
	
	// 日の種類を表す定数
	public static final int DAY_KIND_NORMAL = 0; //通常日
	public static final int DAY_KIND_SATURDAY = 1; //土曜日
	public static final int DAY_KIND_HOLIDAY = 2; //休日(現在は日曜日のみ)
	
	/** 日の種類。DAY_KIND_* のいずれか */
	private int dayKind = DAY_KIND_NORMAL;	
	
	/** 付加日付情報(formatAdditionaDate())のキャッシュ */
	private String formatAdditionaDate = null;
	
	public static EventDataBaseRow makeSeparatorRow(int index, Date date) {
		EventDataBaseRow row = new EventDataBaseRow(null, index, date);
		row.setRowType(TYPE_SEPARATOR);
		return row;
	}
	
	/**
	 * 「予定なし」行を生成する
	 * @return
	 */
	public static EventDataBaseRow makeNoEventRow(int index, Date date) {
		EventDataBaseRow row =
				new EventDataBaseRow(null, index, date);
		row.setRowType(EventDataBaseRow.TYPE_NO_EVENT);
		return row;
	}
	
	/**
	 * コンストラクタ
	 * @param event
	 * @param index
	 * @param previousIndex
	 * @param nextIndex
	 * @param date
	 * @param dayKind
	 */
	public EventDataBaseRow(Event event, int index, int eventIndex, Date date, int dayKind) {
		this.event = event;
		this.index = index;
		this.eventIndex = eventIndex;
		this.displayDate = date;
		this.dayKind = dayKind;
	}
	
	/**
	 * コンストラクタ
	 * @param event
	 * @param index
	 */
	private EventDataBaseRow(Event event, int index, Date date) {
		this.event = event;
		this.index = index;
		this.displayDate = date;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isFirstEvent() {
		if(eventIndex == 0) {
			return true;
		}
		return false;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}
	
	public String toString() {
		if(event != null) {
			return event.toString();
		}
		return "event=(null)";
	}
	
	/**
	 * イベントの開始時間を表す文字列を返す。
	 * 日付イベントの場合は「終日」を返す。
	 * @return
	 */
	public String formatStartTime(TimeZone timeZone, Context context) {
		return EventUtil.getStartTime(event, displayDate, timeZone).toString(context);				
	}

	/**
	 * イベントの日付に関する付加情報を表す文字列を返す。
	 * 付加情報がない場合はnullを返す。
	 * @param timeZone
	 * @return
	 */
	public String formatAdditionalDate(TimeZone timeZone, Context context) {
		if(formatAdditionaDate == null) {
			if(rowType == TYPE_NORMAL_EVENT) {
				formatAdditionaDate = EventUtil.formatAdditionalDate(event, timeZone, context);
				// Log.d("EventDataBaseRow", event.toString() + ":" + formatAdditionaDate.length() + ":" + formatAdditionaDate);
			} else {
				formatAdditionaDate = "";
			}
		}
		if(formatAdditionaDate.length() == 0) {
			return null;
		}
		return formatAdditionaDate;
	}
	
	/**
	 * イベントの日付に関する付加情報の有無を返す。
	 * @param timeZone
	 * @return
	 */
	public boolean hasAdditionalDate(TimeZone timeZone, Context context) {
		String str = formatAdditionalDate(timeZone, context);
		if(str != null) {
			return true;
		}
		return false;
	}
	
	public Date getDisplayDate() {
		return displayDate;
	}

	public int getDayKind() {
		return dayKind;
	}

	public void setDayKind(int dayKind) {
		this.dayKind = dayKind;
	}
	
	/**
	 * 指定されたTimeZondeで、指定日の日の種類を判定する。
	 * @param date
	 * @param timeZone
	 * @return
	 */
	public static int calcDayKind(Date date, TimeZone timeZone) {
		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTime(date);
		switch(cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return DAY_KIND_HOLIDAY;
		case Calendar.SATURDAY:
			return DAY_KIND_SATURDAY;
		}
		return DAY_KIND_NORMAL;
	}

	public int getRowType() {
		return rowType;
	}

	public void setRowType(int rowType) {
		this.rowType = rowType;
	}

	public int getEventIndex() {
		return eventIndex;
	}

	public void setEventIndex(int eventIndex) {
		this.eventIndex = eventIndex;
	}
}
