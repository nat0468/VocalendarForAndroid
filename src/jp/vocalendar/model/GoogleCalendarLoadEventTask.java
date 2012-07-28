package jp.vocalendar.model;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;

import jp.vocalendar.activity.SplashScreenActivity;

public class GoogleCalendarLoadEventTask extends LoadEventTask {
	private static String TAG = "LoadEventTask";  
    private static final String API_KEY = "AIzaSyBS39cGkgQkmsMaVYfaD-TV4eAdmW3G4ss";
    private static final String CALENDAR_ID = "0mprpb041vjq02lk80vtu6ajgo@group.calendar.google.com";   

    private Calendar calendar;
	
    /**
     * コンストラクタ。
     * @param activity イベント読み込み終了時にコールバックするSplashScreenActivity
     */
    public GoogleCalendarLoadEventTask(SplashScreenActivity activity) {
        super(activity);
    }

	@Override
	protected List<Event> doInBackground(URI... arg0) {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();

		Log.i(TAG, "accountName=" + splashScreenActivity.getAccountName() +
				", authToken=" + splashScreenActivity.getAuthToken());

		Calendar.Builder builder = new Calendar.Builder(
				httpTransport, jsonFactory, new HttpRequestInitializer() {
					public void initialize(HttpRequest request) throws IOException {
						request.getHeaders().setAuthorization(GoogleHeaders.getGoogleLoginValue(
								splashScreenActivity.getAuthToken()));
					}
				});

		calendar = builder
				.setApplicationName("Google-CalendarAndroidSample/1.0")
				.setJsonHttpRequestInitializer(new GoogleKeyInitializer(API_KEY))
				.build();

		try {
			return loadEvents();
		} catch(IOException e) {
			Log.e(TAG, "loadEvents() fails.", e);
			return null;
		}      
	}

	private List<Event> loadEvents() throws IOException {
		Calendar.Events es = calendar.events();              
		  Calendar.Events.List list = es.list(CALENDAR_ID);
		  java.util.Calendar cal = java.util.Calendar.getInstance();
		  cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
          cal.set(java.util.Calendar.MINUTE, 0);
          cal.set(java.util.Calendar.SECOND, 0);
          cal.set(java.util.Calendar.MILLISECOND, 0);
          DateTime start = new DateTime(cal.getTime(), TimeZone.getDefault());
          cal.add(java.util.Calendar.DATE, +1);
          cal.add(java.util.Calendar.MILLISECOND, -1);
          DateTime end = new DateTime(cal.getTime(), TimeZone.getDefault());
		  Events events = list.setTimeMin(start).setTimeMax(end).execute();

		  while (true) {
		      Iterator<com.google.api.services.calendar.model.Event> itr = events.getItems().iterator();
		      while(itr.hasNext()) {
		    	  Event e = EventFactory.toVocalendarEvent(itr.next());
		          publishProgress(e);
		      }
		      String pageToken = events.getNextPageToken();
		      if (pageToken != null && pageToken.length() != 0) {
		          events = calendar.events().list(CALENDAR_ID).setPageToken(pageToken).execute();
		      } else {
		          break;
		      }
		  }
		  return null;
	}

}
