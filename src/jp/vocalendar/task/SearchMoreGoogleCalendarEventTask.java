package jp.vocalendar.task;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import jp.vocalendar.model.EventDataBaseRow;

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
	 * 検索イベント数に追加する数
	 */
	private int maxEventToAdd = 0;
	
    /**
     * コンストラクタ。
     * @param activity このタスクを実行するActivity
     * @param taskCallback イベント読み込み終了時にコールバックする
     */
    public SearchMoreGoogleCalendarEventTask(
    		Activity activity, TaskCallback taskCallback, List<EventDataBaseRow> eventsToBeAppended) {
        super(activity, taskCallback);
        this.eventsToBeAppended = eventsToBeAppended;
        initStartDate();
    }

	@Override
	protected List<EventDataBaseRow> doInBackground(String... query) {
		int maxEvents = getMaxEvents();
		setMaxEvents(maxEvents + maxEventToAdd);
		List<EventDataBaseRow> eventsToAppend = super.doInBackground(query);
		if(eventsToAppend == null) { // 例外発生時
			return null;
		}
		ListIterator<EventDataBaseRow> itr = eventsToAppend.listIterator();
		EventDataBaseRow r = itr.next();
		if(r.getRowType() == EventDataBaseRow.TYPE_SEARCH_START_DATE) {
			itr.remove(); // 先頭の検索開始日は削除
		}
		if(itr.hasNext()) {
			r = itr.next();
			if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
				itr.remove(); // 先頭のセパレータは削除
			}			
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

	private void initStartDate() {
		EventDataBaseRow r = eventsToBeAppended.get(eventsToBeAppended.size() - 1);
		if(r.getRowType() == EventDataBaseRow.TYPE_SEARCH_START_DATE) { // 最後が検索開始日セパレータ、すなわち検索結果なしの場合
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(r.getDisplayDate());
			cal.add(java.util.Calendar.DATE, 1); // 1日後を検索開始日にする
			startDate = cal.getTime();
			return;
		}
		ListIterator<EventDataBaseRow> itr = eventsToBeAppended.listIterator(eventsToBeAppended.size());
		while(itr.hasPrevious()) {
			r = itr.previous();
			if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
				startDate = r.getDisplayDate(); // 一番最後の日付セパレータの日付を検索開始日にする
				return;
			}
			maxEventToAdd++;
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
			 if(sr.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
				 break; // 日付セパレータの場合は、これ以上前のイベントは重複しようがないため(検索開始日より前なため)、探索終了。
			 }
			 if(sr.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				 ListIterator<EventDataBaseRow> ti = target.listIterator();
				 while(ti.hasNext()) {
					 EventDataBaseRow tr = ti.next();
					 if(tr.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT
							 && tr.getEvent().getGid().equals(sr.getEvent().getGid())) { //重複発見
						 ti.remove(); //重複削除
						 break;
					 }
				 }
			 }
		 }
		 
	}
	
}
