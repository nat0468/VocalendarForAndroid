package jp.vocalendar.task;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.ClientCredentials;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventComparatorInDate;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventFactory;
import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

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

public class GoogleCalendarLoadEventTask extends LoadEventTask {
	private static final String TAG = "GoogleCalendarLoadEventTask";  
	/**
	 * イベント情報を読み込む開始日(この日時自体は含む)
	 */
	private DateTime start;
	/**
	 * イベント情報を読み込む終了日(この日時自体は含まない)
	 */
	private DateTime end;
	
	/**
	 * イベント情報のセパレータに使う日付の一覧
	 */
	private Date[] separators;	
	/**
	 * イベント情報を読み込む開始日や終了日の指定に使うタイムゾーン
	 */
	protected TimeZone timeZone;
	
	/**
	 * 認証失敗時の残り試行回数。
	 */
	protected int tryNumber = 5;
	
	/** 残り試行回数のデフォルト値 */
	private static final int DEFAULT_TRY_NUMBER = 5;
    
    /**
     * コンストラクタ。
     * @param activity このタスクを実行するActivity
     * @param taskCallback イベント読み込み終了時にコールバックする
     */
    public GoogleCalendarLoadEventTask(Activity activity, TaskCallback taskCallback) {
        this(activity, taskCallback, DEFAULT_TRY_NUMBER);
    }
	
    /**
     * コンストラクタ。
     * @param activity イベント読み込み終了時にコールバックするSplashScreenActivity
     * @param tryNumner 残り試行回数
     */
    public GoogleCalendarLoadEventTask(Activity activity, TaskCallback taskCallback, int tryNumber) {
        super(activity, taskCallback);
        this.tryNumber = tryNumber;
    }
    
    /**
     * イベントを取得する日付等の条件を設定する。
     * @param dates イベントを取得する日付の一覧。dates[n]からdate[n+1]のイベント取得を繰り返す。
     * @param separators イベントのセパレータ。dates[n]からのイベントの前に、separators[n]を結果のイベント一覧に追加する
     * @param timeZone
     */
    public void setStartAndEndDate(
    		DateTime start, DateTime end, Date[] separators, TimeZone timeZone) {
    	this.start = start;
    	this.end = end;
    	this.separators = separators;
    	this.timeZone = timeZone;
    }
    
	@Override
	protected List<EventDataBaseRow> doInBackground(String... ids) {
		List<Event> allEventList = getAllEvents(ids);
		if(allEventList == null) {
			return null;
		}				
		List<EventDataBaseRow> eventList = separateAndSortByDate(allEventList);
		return eventList;
	}

	/**
	 * イベントを日付セパレーターを付けて並び替える
	 * @param allEventList
	 * @return
	 */
	private List<EventDataBaseRow> separateAndSortByDate(List<Event> allEventList) {
		List<EventDataBaseRow> eventList = new LinkedList<EventDataBaseRow>();
		int index = 0;
		int eventIndex = 0;
		for(Date date : separators) {
			eventList.add(
					EventDataBaseRow.makeSeparatorRow(index++, date));
			List<Event> filteredEvents = filteredBy(allEventList, date);
			if(filteredEvents.isEmpty()) {
				// 予定のない日は、予定のない日を表す行だけ追加して次へ
				eventList.add(
						EventDataBaseRow.makeNoEventRow(index++, date));
				continue;
			}			
			Collections.sort(filteredEvents,
					new EventComparatorInDate(date, timeZone));
			int dayKind = EventDataBaseRow.calcDayKind(date, timeZone);			
			for(Event e : filteredEvents) {
				EventDataBaseRow current =
						new EventDataBaseRow(e, index, eventIndex++,
								date, dayKind); 
				eventList.add(current);
				index++;
			}
		}
		return eventList;
	}
	
	private List<Event> getAllEvents(String... ids) {
		List<Event> allEventList = new LinkedList<Event>();
		for(String id : ids) {
			try {
				allEventList.addAll(loadEvents(id, start, end));		
			} catch(IOException e) {
				Log.e(TAG, "loadEvents(" + id + ") fails.", e);
				this.exception = e; 
				return null;
			}
		}	
		return allEventList;
	}

	private List<Event> loadEvents(String id, DateTime start, DateTime end)
			throws IOException {
		Calendar calendar = buildCalendar();
		Calendar.Events es = calendar.events();
		Calendar.Events.List list = es.list(id);
		
		Events events = null;
		try {
			if(isCancelled()) {
				return null;
			}
			events = list.setTimeMin(start).setTimeMax(end).setTimeZone(timeZone.getID())
							.setSingleEvents(true).execute();
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
			handleEvents(id, calendar, events, eventList);
		} else {
			Log.w(TAG, "events is null");
		}
		return eventList;
	}

	protected void handleEvents(
			String calendarId, Calendar calendar, Events events,
			List<Event> eventList) throws IOException {
		if(events.getItems() == null) {
			return;
		}
		while (true) {
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
			String pageToken = events.getNextPageToken();
			if (pageToken != null && pageToken.length() != 0) {				
				try {
					events = calendar.events().list(calendarId)
							.setPageToken(pageToken).setTimeZone(timeZone.getID()).execute();
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
			} else {
				break;
			}
		}		
	}

	/**
	 * 指定された日付に合うイベントのみ返す。
	 * @param events
	 * @param date
	 * @return
	 */
	private List<Event> filteredBy(List<Event> events, Date date) {
		List<Event> filtered = new LinkedList<Event>();
		for(Event e : events) {
			if(e.equalByDate(date, timeZone)) {
				filtered.add(e);
			}
		}
		return filtered; 
	}

	protected Calendar buildCalendar() {
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
	    JacksonFactory jsonFactory = new JacksonFactory();
	    String authToken = OAuthManager.getInstance().getAuthToken();
	    String accountName = OAuthManager.getInstance().getAccount().name;
	    
		Log.i(TAG, "accountName=" + accountName + ", authToken=" + authToken);
	
	    GoogleAccessProtectedResource accessProtectedResource =
	            new GoogleAccessProtectedResource(authToken);
	
	    return Calendar.builder(transport, jsonFactory)
	    			.setApplicationName("Vocalendar-for-Android/0.5")
	                .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
	                	@Override
	                	public void initialize(JsonHttpRequest request) {
	                		CalendarRequest calendarRequest = (CalendarRequest) request;
	                		calendarRequest.setKey(ClientCredentials.API_KEY);
	                	}
	                }).setHttpRequestInitializer(accessProtectedResource).build();
	}

	protected void initAccount() {
		OAuthManager.getInstance().doLogin(false, activity, activity, new OAuthManager.AuthHandler() {			
			@Override
			public void handleAuth(Account account, String authToken, Exception ex) {
				// TODO エラー処理
				Log.d(TAG, "Unexpected handleAuth called.");
			}
		});
	}

	public DateTime getStart() {
		return start;
	}
}
