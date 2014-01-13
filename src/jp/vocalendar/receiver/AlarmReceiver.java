package jp.vocalendar.receiver;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.activity.EventListActivity;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.FavoriteEventDataBaseRow;
import jp.vocalendar.task.CheckAnnouncementTask;
import jp.vocalendar.task.SearchStarEventTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;

/**
 * 毎日のイベント通知を行うタイミングでのAlarmを受け取るReceiver
 *
 */
public class AlarmReceiver extends BroadcastReceiver
implements SearchStarEventTask.Callback, CheckAnnouncementTask.Callback {
	private static String TAG = "AlarmReceiver";
		
	public static int REQUEST_CODE_NORMAL = 100; //通常の通知
	
	/** デバッグモードのON/OFFをIntentに格納するためのキー名 */
	public static String EXTRA_DEBUG = "jp.vocalendar.receiver.AlarmReceiver.DEBUG"; 
	
	/** デバッグモードのときにtrue */
	private boolean debugMode = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		debugMode = intent.getBooleanExtra(EXTRA_DEBUG, false);
		Log.d(TAG, "onReceive: debugMode=" + debugMode);
		
		CheckAnnouncementTask t = new CheckAnnouncementTask(context, this);
		if(debugMode) {
			t.execute(Constants.ANNOUNCEMENT_URL_DEBUG);			
		} else {
			t.execute(Constants.ANNOUNCEMENT_URL);
		}		
		Log.d(TAG, "onReceive finished.");
	}
	
	/**
	 * 読み込み中画面の告知画面確認後の処理
	 */
	@Override
	public void onPostExecute(Context context, boolean result) {
		Log.d(TAG, "CheckLoadingNotificationTask finished. result=" + result);
		SearchStarEventTask t = new SearchStarEventTask(context, this);
		if(debugMode) {
			t.setDebugMode(true); //デバッグ用の場合
		}
		t.execute();		
		Log.d(TAG, "SearchStarEventTask executed...");
	}	
	
	/**
	 * ★イベント確認後の処理
	 */
	@Override
	public void onPostExecute(
			Context context, List<EventDataBaseRow> starEvents, Exception exception) {
		Log.d(TAG, "SearchStarEventTask finished.");
		if(starEvents == null) {
			//★イベント取得失敗時は★0個と見なすために、空のリストを設定
			starEvents = new LinkedList<EventDataBaseRow>();
		}
		List<EventDataBaseRow> favoriteEvents = getNotificationEvents(context);
		if(!debugMode) { //デバッグでない時は、0件は無視
			if(starEvents.size() == 0 && favoriteEvents.size() == 0) {
				Log.d(TAG, "no notification events.");			
				return;
			}
		}
		makeNotification(context, favoriteEvents, starEvents);		
	}	
	
	private void makeNotification(
			Context context,
			List<EventDataBaseRow> favoriteEvents,
			List<EventDataBaseRow> starEvents) {
		List<EventDataBaseRow> all = new LinkedList<EventDataBaseRow>();
		all.addAll(favoriteEvents);
		all.addAll(starEvents);
		EventDataBaseRow[] rows = all.toArray(new EventDataBaseRow[all.size()]);
		
		Intent intent = new Intent(context, EventListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
        		context, EventListActivity.REQUEST_CODE_OPEN_TODAY, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_vocalendar);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(makeTitle(context, rows));
        builder.setContentText(makeContent(context, rows));        		
        builder.setSmallIcon(R.drawable.ic_small_vocalendar);
        builder.setLargeIcon(largeIcon);
        builder.setTicker(makeTicker(context, rows));
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        buildInBoxStyle(builder, context, favoriteEvents, starEvents);
        
        NotificationManager manager =
        		(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll(); // 前の通知があればいったん消す。
        manager.notify(0, builder.build());
	}

	/**
	 * Android 4.1以降のためのBig text styleの通知を組み立てる。
	 * @param builder
	 * @param context
	 * @param rows
	 */
	private void buildInBoxStyle(
			Builder builder, Context context,
			List<EventDataBaseRow> favoriteEvents, List<EventDataBaseRow> starEvents) {
		NotificationCompat.InboxStyle inboxStyle =
		        new NotificationCompat.InboxStyle();
		for(EventDataBaseRow row : favoriteEvents) {		
		    inboxStyle.addLine(row.getEvent().getSummary());
		}
		for(EventDataBaseRow row : starEvents) {			
		    inboxStyle.addLine(row.getEvent().getSummary());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.favorite));
		sb.append(favoriteEvents.size());
		sb.append(context.getString(R.string.unit_of_events));
		sb.append(context.getString(R.string.comma));
		sb.append(context.getString(R.string.star_event));
		sb.append(starEvents.size());
		sb.append(context.getString(R.string.unit_of_events));		
		inboxStyle.setSummaryText(sb.toString());
		
		builder.setStyle(inboxStyle);		
	}

	protected String makeTitle(Context context, EventDataBaseRow[] rows) {
		StringBuilder title = new StringBuilder();
        title.append(context.getString(R.string.todays_events_notifications));
        title.append(context.getString(R.string.column));
        title.append(rows.length);
        title.append(context.getString(R.string.unit_of_events));
		return title.toString();
	}

	protected String makeTicker(Context context, EventDataBaseRow[] rows) {
		StringBuilder sb = new StringBuilder();
		if(rows.length == 0) {
			sb.append(context.getString(R.string.no_event));
			return sb.toString();
		}		
        String comma = context.getString(R.string.comma);
        boolean addComma = false;
        for(EventDataBaseRow row : rows) {
        	if(addComma) { sb.append(comma); }
        	sb.append(row.getEvent().getSummary());
        	addComma = true;
        }
        String message = sb.toString();
		return message;
	}

	protected String makeContent(Context context, EventDataBaseRow[] rows) {
		StringBuilder sb = new StringBuilder();
		if(rows.length == 0) {
			sb.append(context.getString(R.string.no_event));
			return sb.toString();
		}
		sb.append(rows[0].getEvent().getSummary());
		if(rows.length > 1) {
			sb.append(" ");
			sb.append(context.getString(R.string.etc));
		}
		return sb.toString();
	}

	
	private List<EventDataBaseRow> getNotificationEvents(Context context) {
		EventDataBase db = new EventDataBase(context);
		db.open();
		TimeZone timeZone = TimeZone.getDefault();
		Calendar today = Calendar.getInstance(timeZone);
		Date todayDate = today.getTime();
		List<FavoriteEventDataBaseRow> rows = 
				new LinkedList<FavoriteEventDataBaseRow>();
		db.getAllFavoriteEvents(today, timeZone, rows);
		db.close();
		
		List<EventDataBaseRow> list = new LinkedList<EventDataBaseRow>();
		for(FavoriteEventDataBaseRow row : rows) {
			if(row.getEvent().equalByDate(todayDate, timeZone)) {
				list.add(row);
			}
		}		
		return list;
	}

}
