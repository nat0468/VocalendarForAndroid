package jp.vocalendar.model;

import java.util.Comparator;

import android.util.Log;

/**
 * Eventオブジェクトを、昇順に並べ替えるクラス。
 */
public class EventComparator implements Comparator<Event> {
	public static final String TAGS = "EventComparator"; 
	
	/**
	 * 以下のルールで、Eventを比較する。
	 * <ul>
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
			log(e1, e2, 0);
			return 0;
		}
		
		// 繰り返しイベントの場合
		if(e1.isRecursive() && e2.isRecursive()) {
			log(e1, e2, compareRecursive(e1, e2));
			return compareRecursive(e1, e2);
		} else if(e1.isRecursive() && !e2.isRecursive()) {
			log(e1, e2, -1);
			return -1;
		} else if(!e1.isRecursive() && e2.isRecursive()) {
			log(e1, e2, 1);
			return 1;
		}
		
		// 日付または日時イベントの場合
		if(e1.getStartDateIndex() == e2.getStartDateIndex()) {
			if(e1.getEndDateIndex() == e2.getEndDateIndex()) {
				log(e1, e2, e1.getSummary().compareTo(e2.getSummary()));
				return e1.getSummary().compareTo(e2.getSummary());
			} 
			log(e1, e2, (int)(e1.getEndDateIndex() - e2.getEndDateIndex()));
			return (int)(e1.getEndDateIndex() - e2.getEndDateIndex());
		}
		log(e1, e2, (int)(e1.getStartDateIndex() - e2.getStartDateIndex()));
		return (int)(e1.getStartDateIndex() - e2.getStartDateIndex());
	}
	
	private int compareRecursive(Event e1, Event e2) {
		return e1.getSummary().compareTo(e2.getSummary());
	}
	
	private int compareDateEvent(Event e1, Event e2) {
		if(e1.getStartDate().equals(e2.getStartDate())) {
			if(e1.getEndDate().equals(e2.getEndDate())) {
				return e1.getSummary().compareTo(e2.getSummary());
			}
			return e1.getEndDate().compareTo(e2.getEndDate());
		}
		return e1.getStartDate().compareTo(e2.getStartDate());
	}
	
	private int compareDateTimeEvent(Event e1, Event e2) {		
		if(e1.getStartDateTime().equals(e2.getStartDateTime())) {
			if(e1.getEndDateTime().equals(e2.getEndDateTime())) {
				return e1.getSummary().compareTo(e2.getSummary());
			}
			return e1.getEndDateTime().compareTo(e2.getEndDateTime());
		}
		return e1.getStartDateTime().compareTo(e2.getStartDateTime());
	}
	
	private void log(Event e1, Event e2, int returnValue) {
		Log.d(TAGS, e1.toDateTimeSummaryString() + "x" + e2.toDateTimeSummaryString() + ":" + returnValue);		
	}
}
