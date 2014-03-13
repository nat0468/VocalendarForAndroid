package jp.vocalendar.task;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.ClientCredentials;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventFactory;
import jp.vocalendar.util.DateUtil;
import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;
import com.google.api.services.calendar.model.Events;

/**
 * 今日の★イベントを検索するタスク
 */
public class SearchStarEventTask extends AsyncTask<String, Event, List<EventDataBaseRow>> {
	private static String TAG = "SearchStarEventTask";
	
	/**
	 * 検索終了時にコールバックするインスタンスのインターフェイス
	 */
	public static interface Callback {
		public void onPostExecute(Context context, List<EventDataBaseRow> rows, Exception exception);
	}

	private Context context;
	private TimeZone timeZone;	
	private Callback callback;
	private Date todayDate; //今日の日付
	
	/** デバッグモード。trueの場合、今月の★イベント検索になる */
	private boolean debugMode = false;
	
	/**
	 * 検索処理中に発生した例外
	 */
	private Exception exception; 
	
	public SearchStarEventTask(Context context, Callback callback) {
		this.context = context;
		this.callback = callback;
		this.timeZone = TimeZone.getDefault();		
	}

	@Override
	protected List<EventDataBaseRow> doInBackground(String... params) {
		Log.d(TAG, "doInBackground()");
		
		OAuthManager.getInstance().doLogin(false, null, context, new OAuthManager.AuthHandler() {			
			@Override
			public void handleAuth(Account account, String authToken,
					Exception exception) {
				if(account == null) {
					Log.d(TAG, "handleAuth(): account=null,authToken=" + authToken);					
				} else {
					Log.d(TAG, "handleAuth(): account=" + account.name + ",authToken=" + authToken);										
				}
				if(exception != null) { //Google認証失敗
					SearchStarEventTask.this.exception = exception;
					Log.e(TAG, "handleAuth(): exception occured", exception);
				}
			}
		});		
		if(exception != null) { //Google認証失敗時は何もしない
			return null;
		}
		
		LinkedList<EventDataBaseRow> allEvents = new LinkedList<EventDataBaseRow>();

		java.util.Calendar cal = DateUtil.makeStartTimeOfToday(timeZone);
		this.todayDate = cal.getTime();
		DateTime startTime = new DateTime(todayDate, timeZone);
		cal.add(java.util.Calendar.DATE, 1);
		if(debugMode) {
			//cal.add(java.util.Calendar.MONTH, 1); //デバッグ時には1ヶ月後まで対象とする			
			//Log.d(TAG, "debug mode on. Expand search range...: " + cal.toString());
		}
		Date tommorowDate = cal.getTime();
		DateTime endTime = new DateTime(tommorowDate, timeZone);		
		
		for(int i = 0; i < Constants.CALENDER_IDS.length; i++) {
			try {
				List<Event> events = search(Constants.CALENDER_IDS[i], startTime, endTime);
				makeEventDataBaseRowAndAdd(events, allEvents, todayDate);
			} catch(IOException e) {
				Log.e(TAG, "loadEvents(" + Constants.CALENDER_IDS[i] + ") fails.", e);
				this.exception = e; 
				return null;				
			}
		}
		return allEvents;
	}

	/**
	 * EventDataBaseRowを生成してallEventsに追加する。
	 * @param events
	 * @param allEvents
	 * @param dayKind EventDataBaseRow生成に使うdayKind
	 */
	private void makeEventDataBaseRowAndAdd(List<Event> events,
			LinkedList<EventDataBaseRow> allEvents, Date today) {
		int dayKind = EventDataBaseRow.calcDayKind(today, timeZone);			
		for(Event event : events) {
			EventDataBaseRow row = new EventDataBaseRow(
					event, 0 /* 意味のない値 */, 0/* 意味のない値 */, today, dayKind);
			allEvents.add(row);
		}
	}

	private List<Event> search(String calendarId, DateTime startTime, DateTime endTime) throws IOException {
		Log.d(TAG, "search(" + calendarId + ")...");

		Calendar calendar = buildCalendar();
		Calendar.Events es = calendar.events();
		Calendar.Events.List list = es.list(calendarId);
		
		Events events = null;
/*		events = list.setTimeMin(startTime).setTimeMax(endTime)
						.setTimeZone(timeZone.getID()).setOrderBy("startTime")
						.setSingleEvents(true).setQ(Constants.STAR_EVENT_CHARACTER).execute(); */
		events = list.setTimeMin(startTime).setTimeMax(endTime)
				.setTimeZone(timeZone.getID()).setOrderBy("startTime")
				.setSingleEvents(true).execute(); // queryに「★」を指定しても、検索がうまく行かないので全件取得
		List<Event> eventList = new LinkedList<Event>();
		if(events != null) {
			Log.d(TAG, "handleEvents");
			handleEvents(calendarId, calendar, events, eventList);
		} else {
			Log.w(TAG, "events is null");
		}
		return eventList;
	}

	private void handleEvents(
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
			if(!ge.getSummary().startsWith(Constants.STAR_EVENT_CHARACTER)) {
				continue; // ★イベントでないイベント(★で始まらない)は無視
			}			
			Event e = EventFactory.toVocalendarEvent(
							calendarId, ge, timeZone, context);
			Log.d(TAG, e.toDateTimeSummaryString(timeZone, context));
			if(!e.equalByDate(todayDate, timeZone)) {
				Log.d(TAG, "skip " + e.toDateTimeSummaryString(timeZone, context));
				continue; // 今日と一致しないイベントは無視
			}			
			eventList.add(e);
			publishProgress(e);
		}
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
	    			.setApplicationName(Constants.APPLICATION_NAME_FOR_GOOGLE)
	                .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
	                	@Override
	                	public void initialize(JsonHttpRequest request) {
	                		CalendarRequest calendarRequest = (CalendarRequest) request;
	                		calendarRequest.setKey(ClientCredentials.API_KEY);
	                	}
	                }).setHttpRequestInitializer(accessProtectedResource).build();
	}

	@Override
	protected void onPostExecute(List<EventDataBaseRow> rows) {
		callback.onPostExecute(context, rows, exception);
	}

	public Context getContext() {
		return context;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	

}
