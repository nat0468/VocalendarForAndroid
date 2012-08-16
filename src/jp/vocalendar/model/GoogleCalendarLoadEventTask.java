package jp.vocalendar.model;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.googleapi.OAuthManager;
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
	private static String TAG = "LoadEventTask";  
	
	/**
	 * イベント情報を読み込む日
	 */
	private DateTime[] dates;
	/**
	 * イベント情報のセパレータ
	 */
	private EventSeparator[] separators;	
	/**
	 * イベント情報を読み込む開始日や終了日の指定に使うタイムゾーン
	 */
	private TimeZone timeZone;
	
	/**
	 * 認証失敗時の残り試行回数。
	 */
	private int tryNumber = 5;
    
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
    		DateTime[] dates, EventSeparator[] separators, TimeZone timeZone) {
    	this.dates = dates;
    	this.separators = separators;
    	this.timeZone = timeZone;
    }
    
	@Override
	protected List<Event> doInBackground(String... ids) {
		List<Event> eventList = new LinkedList<Event>();
		for(int i = 0; i < (dates.length-1); i++) {
			eventList.add(separators[i]);
			List<Event> oneDayEvents = new LinkedList<Event>();
			for(String id : ids) {
				try {
					oneDayEvents.addAll(loadEvents(id, dates[i], dates[i+1]));				
				} catch(IOException e) {
					Log.e(TAG, "loadEvents(" + id + ") fails.", e);
					return null;
				}
			}	
			Collections.sort(oneDayEvents, new EventComparator());
			eventList.addAll(oneDayEvents);
		}
		return eventList;
	}

	private List<Event> loadEvents(String id, DateTime start, DateTime end)
			throws IOException {
		Calendar calendar = buildCalendar();
		Calendar.Events es = calendar.events();
		Calendar.Events.List list = es.list(id);
		
		Events events = null;
		try {
			if(canceled) {
				return null;
			}
			events = list.setTimeMin(start).setTimeMax(end).setTimeZone(timeZone.getID()).execute();
		} catch (HttpResponseException e) {
			int statusCode = e.getStatusCode();
			if (statusCode == 401 && (tryNumber - 1) > 0) {
				Log.d(TAG, "Got 401, refreshing token.");
				OAuthManager.getInstance().doLogin(true, activity,
					new OAuthManager.AuthHandler() {
						@Override
						public void handleAuth(Account account, String authToken) {
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

	private void handleEvents(
			String calendarId, Calendar calendar, Events events,
			List<Event> eventList) throws IOException {
		if(events.getItems() == null) {
			return;
		}
		while (true) {
			Iterator<com.google.api.services.calendar.model.Event> itr = events.getItems().iterator();
			while(itr.hasNext()) {
				Event e = EventFactory.toVocalendarEvent(itr.next());
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
						OAuthManager.getInstance().doLogin(true, activity,
							new OAuthManager.AuthHandler() {
								@Override
								public void handleAuth(Account account, String authToken) {
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
	
	private Calendar buildCalendar() {
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
}
