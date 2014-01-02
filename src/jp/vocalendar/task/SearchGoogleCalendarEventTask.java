package jp.vocalendar.task;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventComparatorInDate;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventFactory;
import jp.vocalendar.util.DateUtil;
import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.google.api.client.http.HttpResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;

/**
 * Googleカレンダーからイベント検索するLoadEventTask.
 */

public class SearchGoogleCalendarEventTask extends GoogleCalendarLoadEventTask {
	private static final String TAG = "SearchGoogleCalendarEventTask";  	
	
	/** 検索するカレンダーのID */
	private String[] calendarIds;
	
	/**
	 * イベント情報を読み込む開始日(この日時自体は含む)
	 */
	private DateTime startDateTime = new DateTime(System.currentTimeMillis());
	
	/**
	 * 最大イベント数
	 */
	private int maxEvents = 50;
	
	public SearchGoogleCalendarEventTask(Activity activity, TaskCallback taskCallback) {
		super(activity, taskCallback);
	}
	
	@Override
	protected List<EventDataBaseRow> doInBackground(String... query) {
		Log.d(TAG, "doInBackground(" + query + ")...");
		if(OAuthManager.getInstance().getAccount() == null) {
			initAccount(); // アカウント情報未設定の場合(まだ一回もイベント読み込みしていない場合)は初期化
		}		
		
		LinkedList<EventDataBaseRow> allEvents = new LinkedList<EventDataBaseRow>();
		
		for(int i = 0; i < calendarIds.length; i++) {
			try {
				List<Event> events = search(calendarIds[i], query[0]);				
				addEventsTo(events, query[0], allEvents);
			} catch(IOException e) {
				Log.e(TAG, "loadEvents(" + calendarIds[i] + ") fails.", e);
				this.exception = e; 
				return null;				
			}
		}
		cutPastEvents(allEvents);
		cutAfterMaxEvents(allEvents);
		addDateSeparator(allEvents);
		return allEvents; 
	}
	
	/**
	 * 最大イベント数までイベントを追加する。
	 * @param events 追加するイベント
	 * @param query 検索キーワード(Google Calendarの検索が【】★を無視するので、このメソッドで異なるものを除外)
	 * @param allEvents イベント追加先となる全イベントリスト
	 */
	private void addEventsTo(List<Event> events, String query, LinkedList<EventDataBaseRow> allEvents) {
		ListIterator<EventDataBaseRow> targetItr = allEvents.listIterator();		
		EventDataBaseRow next = null;
		if(targetItr.hasNext()) {
			next = targetItr.next();
			targetItr.previous();
		}		
		
		EventComparatorInDate c = new EventComparatorInDate(new Date(startDateTime.getValue()), timeZone);
		Iterator<Event> itr = events.iterator();		
		while(itr.hasNext()) {
			Event e = itr.next();
			if(!includeKeyword(e, query)) {
				continue; // 検索キーワードが含まれない場合は無視
			}
			moveToNext(next, targetItr, e, c);
			
			Date date = (e.getStartDateTime() != null ? e.getStartDateTime() : e.getStartDate());
			Date startDate = new Date(startDateTime.getValue());
			if(e.equalByDate(startDate, timeZone)) {
				date = startDate; // イベントがstartDateTimeを含む場合は、その日付でdayKindを計算。
			}			
			int dayKind = EventDataBaseRow.calcDayKind(date, timeZone);
			EventDataBaseRow row = new EventDataBaseRow(
					e, 0 /* 意味のない値 */, 0/* 意味のない値 */, null, dayKind);
			targetItr.add(row);
			if(targetItr.hasNext()) {
				next = targetItr.next();
				targetItr.previous();
			} else {
				next = null;
			}
		}
	}
	
	/**
	 * イベント名や説明にkeywordが含まれる場合にtrueを返す
	 * @param e
	 * @param keyword
	 * @return
	 */
	private boolean includeKeyword(Event e, String keyword) {
		keyword = keyword.toLowerCase(Locale.US);
		if(e.getSummary() != null && e.getSummary().toLowerCase(Locale.US).indexOf(keyword) != -1) {
			return true;
		}
		if(e.getDescription() != null && e.getDescription().toLowerCase(Locale.US).indexOf(keyword) != -1) {
			return true;
		}
		return false;
	}

	/**
	 * startDateの前日までのイベントを削除。
	 * @param events
	 */
	private void cutPastEvents(LinkedList<EventDataBaseRow> events) {
		java.util.Calendar startDate = java.util.Calendar.getInstance();
		startDate.setTimeInMillis(startDateTime.getValue());
		DateUtil.makeStartTimeOfDay(startDate);
		
		ListIterator<EventDataBaseRow> itr = events.listIterator();
		while(itr.hasNext()) {
			Event e = itr.next().getEvent();
			if(!e.equalOrAfterByDateWithoutRecursive(startDate, timeZone)) {
				// 検索開始日と同じまたは大きい日のイベントでない(検索開始日より小さい日)のイベントは削除
				itr.remove();
			}
		}
	}

	/**
	 * 最大個数以上のイベントを削除
	 * @param events
	 */
	private void cutAfterMaxEvents(LinkedList<EventDataBaseRow> events) {
		if(events.isEmpty()) {
			return;
		}
		
		ListIterator<EventDataBaseRow> itr = events.listIterator();
		int num = 0;
		while(itr.hasNext() && num < maxEvents) {
			itr.next();
			num++;
		}
		while(itr.hasNext()) {
			itr.next();
			itr.remove();
		}
	}
	
	private void addDateSeparator(LinkedList<EventDataBaseRow> events) {
		ListIterator<EventDataBaseRow> itr = events.listIterator();
		
		EventDataBaseRow lastSeparator = null;
		if(!events.isEmpty()) {
			java.util.Calendar startDateTimeCal = java.util.Calendar.getInstance();
			startDateTimeCal.setTimeInMillis(startDateTime.getValue());
			
			EventDataBaseRow r = events.getFirst();
			Date date = getDisplayDate(r);
			if(date.after(new Date(startDateTime.getValue()))) { // 検索開始日と同じまたは後の期間イベントの場合				
				lastSeparator = EventDataBaseRow.makeSeparatorRow(0 /* 意味の無い値 */, getDisplayDate(r)); 				
			} else { // 検索開始日より前の場合
				lastSeparator = EventDataBaseRow.makeSeparatorRow(0 /* 意味の無い値 */, new Date(startDateTime.getValue()));  // 検索開始日を入れる
			}
			itr.add(lastSeparator);
		}				
		
		while(itr.hasNext()) {
			EventDataBaseRow r = itr.next();
			if(r.getRowType() != EventDataBaseRow.TYPE_NORMAL_EVENT) {
				continue;
			}
			Date date = getDisplayDate(r);
			if(DateUtil.greaterYMD(lastSeparator.getDisplayDate(), date, timeZone)) {
				lastSeparator = EventDataBaseRow.makeSeparatorRow(0 /* 意味の無い値 */, date);
				itr.previous(); // 現在のイベントの前にセパレータ挿入
				itr.add(lastSeparator);
			}			
		}
		
		// 検索開始日を先頭に追加。
		EventDataBaseRow searchStartDate =
				EventDataBaseRow.makeSearchStartDateRow(0 /* 意味の無い値 */, new Date(startDateTime.getValue()));
		events.addFirst(searchStartDate);				
	}
	
	private Date getDisplayDate(EventDataBaseRow row) {
		Event e = row.getEvent();
		Date date = (e.getStartDateTime() != null ? e.getStartDateTime() : e.getStartDate());
		return date;
	}

	/**
	 * targetItrの指すリストで、eの入る場所(startDateTimeまたはstartDateの昇順)までtargetItrを進ませる。
	 * @param next targetItrの指すnextイベント
	 * @param targetItr 挿入先を示すイテレータ
	 * @param e 挿入するイベント
	 */
	private void moveToNext(
			EventDataBaseRow next, ListIterator<EventDataBaseRow> targetItr, Event e, EventComparatorInDate c) {
		if(next == null) { // targetItrが指すイベントが空の場合
			return;
		}
		if(!(next.getEvent().getStartDateIndex() < e.getStartDateIndex())) { //既にtargetItrが挿入位置
			return;
		}
		do {
			next = targetItr.next();
			if(!targetItr.hasNext()) { // 末尾が挿入位置
				return;
			}
		} while(next.getEvent().getStartDateIndex() < e.getStartDateIndex());
		targetItr.previous();
	}

	private List<Event> search(String calendarId, String query) throws IOException {
		Log.d(TAG, "search(" + calendarId + ", " + query + ")...");

		Calendar calendar = buildCalendar();
		Calendar.Events es = calendar.events();
		Calendar.Events.List list = es.list(calendarId);
		
		Events events = null;
		try {
			if(isCancelled()) {
				return null;
			}
			events = list.setTimeMin(startDateTime).setTimeZone(timeZone.getID()).setOrderBy("startTime")
							.setSingleEvents(true).setMaxResults(maxEvents).setQ(query).execute();
		} catch (HttpResponseException e) {
			int statusCode = e.getStatusCode();
			if (statusCode == 401 && (tryNumber - 1) > 0) {
				Log.d(TAG, "Got 401, refreshing token.");
				OAuthManager.getInstance().doLogin(true, activity, activity,
					new OAuthManager.AuthHandler() {
						@Override
						public void handleAuth(Account account, String authToken, Exception ex) {
							tryNumber--;
							Log.e(TAG, "doRetry: " + tryNumber);
							doRetry(tryNumber);
						}
	             	});
			}
			throw e;
		}
		List<Event> eventList = new LinkedList<Event>();
		if(events != null) {
			Log.d(TAG, "handleEvents");
			handleEvents(calendarId, calendar, events, eventList);
		} else {
			Log.w(TAG, "events is null");
		}
		return eventList;
	}
	
	@Override
	protected void handleEvents(
			String calendarId, Calendar calendar, Events events,
			List<Event> eventList) throws IOException {
		if(events.getItems() == null) {
			return;
		}
		Iterator<com.google.api.services.calendar.model.Event> itr = events.getItems().iterator();
		while(itr.hasNext()) {
			com.google.api.services.calendar.model.Event ge = itr.next();
			if(ge.getStatus() != null && !"confirmed".equals(ge.getStatus())) {
				continue; // confirmed 以外のイベントは無視
			}						
			Event e = EventFactory.toVocalendarEvent(
							calendarId, ge, timeZone, activity);
			eventList.add(e);
			publishProgress(e);
		}
	}
	
	
	public String[] getCalendarIds() {
		return calendarIds;
	}

	public void setCalendarIds(String[] calendarIds) {
		this.calendarIds = calendarIds;
	}

	public DateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public int getMaxEvents() {
		return maxEvents;
	}

	public void setMaxEvents(int maxEvents) {
		this.maxEvents = maxEvents;
	}

	public void setCalendarIdAndStartDateTime(
			String[] calendarId, DateTime startDateTime, TimeZone timeZone, int maxEvents) {
		this.calendarIds = calendarId;
		this.startDateTime = startDateTime;
		this.timeZone = timeZone;
		this.maxEvents = maxEvents;
	}
}
