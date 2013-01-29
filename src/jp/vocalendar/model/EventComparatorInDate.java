package jp.vocalendar.model;

import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Eventオブジェクトを、指定された日付内での開始時間に基づいて、昇順に並べ替えるクラス。
 * 期間イベントの開始日時が、指定された日付より前の場合は、その期間イベントは全日イベントと見なしたりする。
 */
public class EventComparatorInDate implements Comparator<Event> {
	public static final String TAGS = "EventComparator"; 
	
	/**
	 * 比較に使う日付
	 */
	private Date date;
	
	/**
	 * 比較に使うタイムゾーン
	 */
	private TimeZone timeZone;
		
	/**
	 * コンストラクタ
	 * @param date 並び替えをする日付
	 * @param timeZone 比較に使うタイムゾーン
	 */
	public EventComparatorInDate(Date date, TimeZone timeZone) {
		this.date = date;
		this.timeZone = timeZone;
	}
	
	/**
	 * 以下のルールで、Eventを比較する。
	 * <ul>
	 *   <li>日付イベントの方が、日時イベントより小さい
	 *   <li>繰り返しイベントの方が、繰り返しでないイベントより小さい
	 *   <li>繰り返しイベント同士の場合、summaryが小さい方が小さい。
	 *   <li>日付イベントまたは日時イベントの場合、開始日または開始時間が早い方が小さい。
	 *   <li>日付イベントまたは日時イベントで、開始日または開始時間が同じ場合、終了日または終了時間が早い方が小さい。
	 *   <li>日付イベントまたは日時イベントで、開始日または開始時間が同じ、かつ終了日または終了時間が同じ場合は、summaryが小さい方が小さい。
	 *   
	 * </ul>
	 */
	@Override
	public int compare(Event e1, Event e2) {
		if(e1 == e2) {
			return 0;
		}
		
		StartTime st1 = EventUtil.getStartTime(e1, date, timeZone);
		StartTime st2 = EventUtil.getStartTime(e2, date, timeZone);		
		
		// 一方が終日イベントの場合は、終日イベントの方が小さい
		if(st1.isDateEvent() && !st2.isDateEvent()) {
			return -1;
		} else if(!st1.isDateEvent() && st2.isDateEvent()) {
			return 1;
		}
		
		// 両方とも終日イベントの場合
		if(st1.isDateEvent() && st2.isDateEvent()) {
			// イベント名で比較
			return e1.getSummary().compareTo(e2.getSummary());
		}
		
		if(st1.getIndex() == st2.getIndex()) { // 両方とも同じ開始時間の場合			
			// 繰り返しイベントとそれ以外では、繰り返しイベントの方が小さい。
			if(e1.isRecursive() && !e2.isRecursive()) {
				return -1;
			} else if(!e1.isRecursive() && e2.isRecursive()) {
				return 1;
			} 
			// イベント名で比較
			return e1.getSummary().compareTo(e2.getSummary());
		}
		return st1.getIndex() - st2.getIndex();		
	}
}
