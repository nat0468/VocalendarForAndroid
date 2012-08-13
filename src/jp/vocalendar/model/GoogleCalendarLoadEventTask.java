package jp.vocalendar.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.TimeZone;

import jp.vocalendar.Constants;
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
	 * イベント情報を読み込む開始日
	 */
	private DateTime start;
	/**
	 * イベント情報を読み込む終了日
	 */
	private DateTime end; 	
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
    
    public void setStartAndEndDate(DateTime start, DateTime end, TimeZone timeZone) {
    	this.start = start;
    	this.end = end;
    	this.timeZone = timeZone;
    }
    
	@Override
	protected Void doInBackground(String... ids) {
		for(String id : ids) {
			try {
				loadEvents(id);				
			} catch(IOException e) {
				Log.e(TAG, "loadEvents(" + id + ") fails.", e);
			}
		}	
		return null;
	}

	private void loadEvents(String id) throws IOException {
		Calendar calendar = buildCalendar();
		Calendar.Events es = calendar.events();
		Calendar.Events.List list = es.list(id);
		
		Events events = null;
		try {
			events = list.setTimeMin(start).setTimeMax(end).execute();
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
		}
		if(events != null) {
			handleEvents(id, calendar, events);
		}
	}

	private void handleEvents(String calendarId, Calendar calendar,
			Events events) throws IOException {
		if(events.getItems() == null) {
			return;
		}
		while (true) {
			Iterator<com.google.api.services.calendar.model.Event> itr = events.getItems().iterator();
			while(itr.hasNext()) {
				Event e = EventFactory.toVocalendarEvent(itr.next());
				publishProgress(e);
			}
			String pageToken = events.getNextPageToken();
			if (pageToken != null && pageToken.length() != 0) {				
				try {
					events = calendar.events().list(calendarId).setPageToken(pageToken).execute();
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
