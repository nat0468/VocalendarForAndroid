package jp.vocalendar.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import jp.vocalendar.activity.SplashScreenActivity;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.CalendarParserFactory;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.net.http.AndroidHttpClient;
import android.util.Log;

/**
 * iCalendar形式でイベント読み込みをするAsyncTask
 */
public class ICalendarLoadEventTask extends LoadEventTask
implements EventContentHandler.EventHandler {
	private static String TAG = "ICalendarLoadEventTask";
	
	/** 読み込み中の URI */
	private URI loadingUri = null;
	
	/**
	 * コンストラクタ。
	 * @param activity イベント読み込み終了時にコールバックするSplashScreenActivity
	 */
	public ICalendarLoadEventTask(SplashScreenActivity activity) {
		super(activity);
	}
	
	@Override
	protected List<Event> doInBackground(URI... uris) {
		AndroidHttpClient client =
				AndroidHttpClient.newInstance("Vocalender Android Application Prototype");
		try {
			for(URI uri : uris) {
				Log.d(TAG, "getting " + uri.toString());
				loadingUri = uri;
				splashScreenActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						splashScreenActivity.onURIOpening(loadingUri);
					}
				});	
				
				HttpResponse resp = client.execute(new HttpGet(uri));
				if(resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					return null;
				}
				Log.d(TAG, "OK");
				InputStream in = resp.getEntity().getContent();
				Log.d(TAG, "getContent()");

				if(!canceled) {
					CalendarParser parser = CalendarParserFactory.getInstance().createParser();
					Reader reader = new UnfoldingReader(new InputStreamReader(in));
					Log.d(TAG, "parser.parse()...");
					parser.parse(reader, new EventContentHandler(this));
					Log.d(TAG, "parser.parse() finished.");
				} else {
					Log.d(TAG, "canceled");
				}
			}
			client.close();			
			return eventList;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			e.printStackTrace();
		}		
		return null;
	}

	@Override
	public void eventParsed(Event event) {
		eventList.add(event);
		if(splashScreenActivity != null) {
			publishProgress(event);
		}
	}
}
