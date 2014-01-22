package jp.vocalendar.task;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.FavoriteEventListActivity;
import jp.vocalendar.activity.LoadingActivity;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.ClientCredentials;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventFactory;
import jp.vocalendar.model.FavoriteEventManager;
import jp.vocalendar.util.DialogUtil;
import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * お気に入りイベントの情報を更新するタスク
 */
public class UpdateFavoriteEventTask extends
		AsyncTask<FavoriteEventManager.GCalendarIdAndGid[], Event, UpdateFavoriteEventTask.Result> {
	/** タスククラス名。LoadingActivity指定用 */
	public static final String TASK_CLASS_NAME = "UpdateFavoriteEventTask"; 
	
	/**
	 * イベント情報が見つからなかったときに投げる例外
	 */
	final class EventNotFoundException extends IOException {		
	}
	
	private static String TAG = "UpdateFavoriteEventTask"; 
	
	public enum Result {
		OK, SOME_EVENTS_NOT_FOUND, ERROR
	}
	
	private Activity context;
	private LoadingActivity.TaskCallback taskCallback;
	
	private List<Event> notFoundEvents = new LinkedList<Event>();
	
	/**
	 * 処理中に発生した例外
	 */
	private Exception exception;
	
	public UpdateFavoriteEventTask(Activity context, LoadingActivity.TaskCallback taskCallback) {
		this.context = context;
		this.taskCallback = taskCallback;
		taskCallback.onInit(context.getString(R.string.update_favorite_events_dialog_message));
	}
	
	@Override
	protected UpdateFavoriteEventTask.Result doInBackground(FavoriteEventManager.GCalendarIdAndGid[]... params) {
		FavoriteEventManager.GCalendarIdAndGid[] rows = params[0];
		VocalendarApplication app = (VocalendarApplication)context.getApplication();
		FavoriteEventManager favoriteEventManager = app.getFavoriteEventManager();

		try {
			doLogin();			
		} catch (Exception e) {
			Log.d(TAG, "doLogin() failed.", e);
			return Result.ERROR;
		}
		Calendar calendar = buildCalendar();
		Calendar.Events events = calendar.events();
		for(int i = 0; i < rows.length; i++) {
			if(isCancelled()) {
				break;
			}
			Event event = null;
			try {
				event = update(context, events, rows[i]);
				Log.d(TAG, rows[i].toString());
			} catch(EventNotFoundException e) {
				Log.d(TAG, "update() failed because event not found:" + rows[i].toString());
				notFoundEvents.add(event);
			} catch (IOException e) {
				Log.d(TAG, "update() failed.", e);
				return Result.ERROR;
			}
			favoriteEventManager.updateFavorite(context, event);
			publishProgress(event);
		}
		if(notFoundEvents.size() != 0) {
			return Result.SOME_EVENTS_NOT_FOUND;
		}
		return Result.OK;
	}

	/**
	 * 進捗表示
	 */
	@Override
	protected void onProgressUpdate(Event... values) {
		taskCallback.onProgressUpdate(values[0].toString());
	}

	@Override
	protected void onPostExecute(Result result) {
		Log.d(TAG, "onPostExecute.");
		if(result == Result.ERROR) {
			showErrorDialog(exception);			
		}
		if(result == Result.SOME_EVENTS_NOT_FOUND) {
			showSomeEventsNotFoundDialog();
		}		
		taskCallback.onPostExecute();
	}

	@Override
	protected void onCancelled(Result result) {
		Log.d(TAG, "onCancelled.");
		// TODO キャンセル処理
	}
		
	/**
	 * Googleアカウントにログイン
	 */
	private void doLogin() throws Exception {
		OAuthManager.getInstance().doLogin(false, context, context, new OAuthManager.AuthHandler() {			
			@Override
			public void handleAuth(Account account, String authToken,
					Exception exception) {
				if(account == null) {
					Log.d(TAG, "handleAuth(): account=null,authToken=" + authToken);					
				} else {
					Log.d(TAG, "handleAuth(): account=" + account.name + ",authToken=" + authToken);										
				}
				if(exception != null) { //Google認証失敗
					Log.e(TAG, "handleAuth(): exception occured", exception);
					UpdateFavoriteEventTask.this.exception = exception;
				}
			}
		});		
		if(this.exception != null) {
			throw exception;
		}
	}
	
	private Calendar buildCalendar() {
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
	    JacksonFactory jsonFactory = new JacksonFactory();
	    String authToken = OAuthManager.getInstance().getAuthToken();
	    String accountName = OAuthManager.getInstance().getAccount().name;
	    
		Log.i(TAG, "buildCalendar(): accountName=" + accountName + ", authToken=" + authToken);
	
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
	
	private Event update(Context context, Calendar.Events events, FavoriteEventManager.GCalendarIdAndGid id) 
	throws EventNotFoundException, IOException {
		TimeZone tz = TimeZone.getDefault();
		try {
			com.google.api.services.calendar.model.Event ge =
					events.get(id.getGCalendarId(), id.getGid()).execute();
			Event event = EventFactory.toVocalendarEvent(id.getGCalendarId(), ge, tz, context);
			EventFactory.updateVocalendarEvent(
					event.getGCalendarId(), ge, tz, context, event);
			return event;
		} catch(GoogleJsonResponseException e) {
			if(e.getStatusCode() == 404) {
				Log.d(TAG, "get(" + id.getGCalendarId() + "," + id.getGid() + ") failed for no event.", e);
				throw new EventNotFoundException();
			}		
			Log.d(TAG, "get(" + id.getGCalendarId() + "," + id.getGid() + ") failed.", e);
			throw e;
		} catch(IOException e) {			
			Log.d(TAG, "get(" + id.getGCalendarId() + "," + id.getGid() + ") failed.", e);
			throw e;
		}
	}
	
	private void showErrorDialog(Exception exception) {
		String msg = null;
		if(exception instanceof IOException) { // 通信エラー
			msg = context.getResources().getString(R.string.fail_to_connect_server);					
			DialogUtil.openMessageDialog(context, msg, false);
		} else { // 予期しないエラー
			msg = context.getResources().getString(R.string.unexpected_error);
			if(exception.getMessage() != null) {
				msg = msg + ": " + exception.getMessage();
			}
			DialogUtil.openErrorDialog(context, msg);
		}
	}
	
	private void showSomeEventsNotFoundDialog() {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.some_favorite_events_can_not_be_found_on_server1));
		sb.append('\n');
		for(Event e : notFoundEvents) {
			sb.append(e.toString());
			sb.append('\n');
		}
		sb.append('\n');
		sb.append(context.getString(R.string.some_favorite_events_can_not_be_found_on_server2));
		DialogUtil.openErrorDialog(context, sb.toString(), false);
	}
}
