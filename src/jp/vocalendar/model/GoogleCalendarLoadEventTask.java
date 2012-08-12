package jp.vocalendar.model;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.activity.SplashScreenActivity;
import jp.vocalendar.googleapi.OAuthManager;
import android.accounts.Account;
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
	 * 読み込み中のカレンダーのID
	 */
	private String calendarId;
	
	/**
	 * 認証失敗時の残り試行回数。
	 */
	private int tryNumber = 5;
	
    /**
     * コンストラクタ。
     * @param activity イベント読み込み終了時にコールバックするSplashScreenActivity
     */
    public GoogleCalendarLoadEventTask(SplashScreenActivity activity) {
        super(activity);
    }
    
    /**
     * コンストラクタ。
     * @param activity イベント読み込み終了時にコールバックするSplashScreenActivity
     * @param tryNumner 残り試行回数
     */
    public GoogleCalendarLoadEventTask(SplashScreenActivity activity, int tryNumber) {
        this(activity);
        this.tryNumber = tryNumber;
    }
    
	@Override
	protected Void doInBackground(String... ids) {
		try {
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
			cal.set(java.util.Calendar.MINUTE, 0);
			cal.set(java.util.Calendar.SECOND, 0);
			cal.set(java.util.Calendar.MILLISECOND, 0);
			start = new DateTime(cal.getTime(), TimeZone.getDefault());
			cal.add(java.util.Calendar.DATE, +1);
			cal.add(java.util.Calendar.MILLISECOND, -1);
			end = new DateTime(cal.getTime(), TimeZone.getDefault());
			calendarId = Constants.MAIN_CALENDAR_ID;
			
			loadEvents();
		} catch(IOException e) {
			Log.e(TAG, "loadEvents() fails.", e);
		}      
		return null;
	}

	private void loadEvents() throws IOException {
		Calendar calendar = buildCalendar();
		Calendar.Events es = calendar.events();
		Calendar.Events.List list = es.list(calendarId);
		
		Events events = null;
		try {
			events = list.setTimeMin(start).setTimeMax(end).execute();
		} catch (HttpResponseException e) {
			int statusCode = e.getStatusCode();
			if (statusCode == 401 && (tryNumber - 1) > 0) {
				Log.d(TAG, "Got 401, refreshing token.");
				onRetry();
				OAuthManager.getInstance().doLogin(true, splashScreenActivity,
					new OAuthManager.AuthHandler() {
						@Override
						public void handleAuth(Account account, String authToken) {
							Log.e(TAG, (tryNumber - 1) + ": GoogleCalendarLoadEventTask.execute()");
							new GoogleCalendarLoadEventTask(splashScreenActivity, tryNumber - 1).execute(new String[0]);
						}
	             	});
			}
		}
		if(events != null) {
			handleEvents(calendarId, calendar, events);
		}
	}

	private void handleEvents(String calendarId, Calendar calendar,
			Events events) throws IOException {
		while (true) {
			Iterator<com.google.api.services.calendar.model.Event> itr = events.getItems().iterator();
			while(itr.hasNext()) {
				Event e = EventFactory.toVocalendarEvent(itr.next());
				publishProgress(e);
			}
			String pageToken = events.getNextPageToken();
			if (pageToken != null && pageToken.length() != 0) {
				events = calendar.events().list(calendarId).setPageToken(pageToken).execute();
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
