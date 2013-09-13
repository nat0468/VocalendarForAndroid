package jp.vocalendar.model;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.util.DateUtil;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;
import com.google.api.services.calendar.model.Events;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

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
				addEventsTo(events, allEvents);
			} catch(IOException e) {
				Log.e(TAG, "loadEvents(" + calendarIds[i] + ") fails.", e);
				this.exception = e; 
				return null;				
			}
		}
		cutAfterMaxEvents(allEvents);
		addDateSeparator(allEvents);
		return allEvents; 
	}
	
	/**
	 * 最大イベント数までイベントを追加する。
	 * @param events 追加するイベント
	 * @param allEvents イベント追加先となる全イベントリスト
	 */
	private void addEventsTo(List<Event> events, LinkedList<EventDataBaseRow> allEvents) {
		ListIterator<EventDataBaseRow> targetItr = allEvents.listIterator();
		EventDataBaseRow current = (targetItr.hasNext() ? targetItr.next() : null);
		Iterator<Event> itr = events.iterator();
		
		while(itr.hasNext()) {			
			Event e = itr.next();
			moveToNext(current, targetItr, e); 
			Date date = (e.getStartDateTime() != null ? e.getStartDateTime() : e.getStartDate());
			int dayKind = EventDataBaseRow.calcDayKind(date, timeZone);			
			EventDataBaseRow row = new EventDataBaseRow(
					e, 0 /* 意味のない値 */, 0/* 意味のない値 */, null, dayKind);
			targetItr.add(row);
		}
	}
	
	private void cutAfterMaxEvents(LinkedList<EventDataBaseRow> events) {
		if(events.isEmpty()) {
			return;
		}
		
		ListIterator<EventDataBaseRow> itr = events.listIterator();
		int num = 0;
		while(itr.hasNext() && num <= maxEvents) {
			itr.next();
			num++;
		}
		itr.remove();
		while(itr.hasNext()) {
			itr.next();
			itr.remove();
		}
	}
	
	private void addDateSeparator(LinkedList<EventDataBaseRow> events) {
		ListIterator<EventDataBaseRow> itr = events.listIterator();
		EventDataBaseRow lastSeparator = 
				EventDataBaseRow.makeSeparatorRow(0 /* 意味の無い値 */, new Date(startDateTime.getValue()));
		itr.add(lastSeparator);
		
		while(itr.hasNext()) {
			EventDataBaseRow r = itr.next();
			if(r.getRowType() != EventDataBaseRow.TYPE_NORMAL_EVENT) {
				continue;
			}
			Event e = r.getEvent();
			Date date = (e.getStartDateTime() != null ? e.getStartDateTime() : e.getStartDate());
			if(DateUtil.greaterYMD(lastSeparator.getDisplayDate(), date, timeZone)) {
				lastSeparator = EventDataBaseRow.makeSeparatorRow(0 /* 意味の無い値 */, date);
				itr.previous(); // 現在のイベントの前にセパレータ挿入
				itr.add(lastSeparator);
			}
		}
		
		
	}

	/**
	 * targetItrの指すリストで、eの入る場所(startDateTimeまたはstartDateの昇順)までtargetItrを進ませる。
	 * @param current targetItrの指すイベント
	 * @param targetItr 挿入先を示すイテレータ
	 * @param e 挿入するイベント
	 */
	private void moveToNext(EventDataBaseRow current, ListIterator<EventDataBaseRow> targetItr, Event e) {
		if(current == null) { // targetItrが指すイベントが空の場合
			return;
		}
		while(current.getEvent().getStartDateIndex() < e.getStartDateIndex() && targetItr.hasNext()) {
			current = targetItr.next();
		}
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
				OAuthManager.getInstance().doLogin(true, activity,
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
