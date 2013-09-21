package jp.vocalendar.model;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import android.app.Activity;

/**
 * Googleカレンダーからもっとイベントを検索して、指定されたEventリストの後ろに追加して返すタスク。
 * イベント検索画面で、もっとイベントを検索する処理に使う。
 *
 */
public class SearchMoreGoogleCalendarEventTask extends	SearchGoogleCalendarEventTask {

	/**
	 * 検索したイベントの追加先イベント一覧
	 */
	private List<EventDataBaseRow> eventsToBeAppended;
	
	/**
	 * 検索の開始日。追加先イベント一覧の一番最後で決まる
	 */
	private Date startDate;
	
    /**
     * コンストラクタ。
     * @param activity このタスクを実行するActivity
     * @param taskCallback イベント読み込み終了時にコールバックする
     */
    public SearchMoreGoogleCalendarEventTask(
    		Activity activity, TaskCallback taskCallback, List<EventDataBaseRow> eventsToBeAppended) {
        super(activity, taskCallback);
        this.eventsToBeAppended = eventsToBeAppended;
        initLastEventDataBaseRowToBeAppended();
    }

	@Override
	protected List<EventDataBaseRow> doInBackground(String... query) {
		List<EventDataBaseRow> eventsToAppend = super.doInBackground(query);
		ListIterator<EventDataBaseRow> itr = eventsToAppend.listIterator();
		EventDataBaseRow r = itr.next();
		if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
			itr.remove(); // 先頭の日付セパレータは削除
		}
		removeOverlappedEvents(eventsToBeAppended, eventsToAppend);
		eventsToBeAppended.addAll(eventsToAppend);
		return eventsToBeAppended;
	}
	
	public void setCalendarId(
			String[] calendarId, TimeZone timeZone, int maxEvents) {
		DateTime startDateTime = new DateTime(startDate, timeZone);
		setCalendarIdAndStartDateTime(calendarId, startDateTime, timeZone, maxEvents);
	}

	private void initLastEventDataBaseRowToBeAppended() {
		EventDataBaseRow r = eventsToBeAppended.get(eventsToBeAppended.size() - 1);
		if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) { // 最後が日付セパレータ、すなわち検索結果なしの場合
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(r.getDisplayDate());
			cal.add(java.util.Calendar.DATE, 1); // 1日後を検索開始日にする
			startDate = cal.getTime();
		} else {
			// 最後のイベントの日付を検索開始日にする
			Event e = r.getEvent();			
			startDate = (e.getStartDateTime() != null ? e.getStartDateTime() : e.getStartDate());			
		}		
	}
	
	/**
	 * 重複するイベントを削除する。
	 * @param src
	 * @param target
	 */
	private void removeOverlappedEvents(List<EventDataBaseRow> src, List<EventDataBaseRow> target) {
		 ListIterator<EventDataBaseRow> srcItr = src.listIterator(src.size());
		 while(srcItr.hasPrevious()) {
			 EventDataBaseRow sr = srcItr.previous();
			 if(sr.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				 ListIterator<EventDataBaseRow> ti = target.listIterator();
				 boolean srcFound = false;
				 while(ti.hasNext()) {
					 EventDataBaseRow tr = ti.next();
					 if(tr.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT
							 && tr.getEvent().getGid().equals(sr.getEvent().getGid())) { //重複発見
						 ti.remove(); //重複削除
						 srcFound = true;
						 break;
					 }
				 }
				 if(!srcFound) {
					 break; // 重複がない場合は、これ以降srcのイベントを探しても見つからないので、検索をやめる
				 }
			 }
		 }
		 
	}
	
}
