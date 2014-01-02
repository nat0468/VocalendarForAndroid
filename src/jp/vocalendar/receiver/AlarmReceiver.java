package jp.vocalendar.receiver;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.activity.EventListActivity;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.FavoriteEventDataBaseRow;
import jp.vocalendar.util.DateUtil;
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
public class AlarmReceiver extends BroadcastReceiver {
	private static String TAG = "AlarmReceiver";
		
	public static int REQUEST_CODE_NORMAL = 100; //通常の通知
	public static int REQUEST_CODE_DEBUG = 110; //デバッグ用の通知
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		EventDataBaseRow[] rows = getNotificationEvents(context);
		if(getResultCode() == REQUEST_CODE_NORMAL) {
			if(rows.length == 0) {
				Log.d(TAG, "no notification events.");				
			}
		}
		makeNotification(context, rows);
		Log.d(TAG, "onReceive finished.");
	}
	
	private void makeNotification(Context context, EventDataBaseRow[] rows) {
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
        buildInBoxStyle(builder, context, rows);
        
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
			Builder builder, Context context, EventDataBaseRow[] rows) {
		NotificationCompat.InboxStyle inboxStyle =
		        new NotificationCompat.InboxStyle();
		for(EventDataBaseRow row : rows) {			
		    inboxStyle.addLine(row.getEvent().getSummary());
			Log.d(TAG, "buildBigTextStyle:addLine() " + row.getEvent().getSummary());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.favorite));
		sb.append(rows.length);
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

	
	private EventDataBaseRow[] getNotificationEvents(Context context) {
		EventDataBase db = new EventDataBase(context);
		db.open();
		TimeZone timeZone = TimeZone.getDefault();
		Calendar today = Calendar.getInstance(timeZone);
		Date todayDate = today.getTime();
		List<FavoriteEventDataBaseRow> rows = 
				new LinkedList<FavoriteEventDataBaseRow>();
		db.getAllFavoriteEvents(today, timeZone, rows);
		db.close();
		
		List<FavoriteEventDataBaseRow> todayRows = new LinkedList<FavoriteEventDataBaseRow>();
		for(FavoriteEventDataBaseRow row : rows) {
			if(row.getEvent().equalByDate(todayDate, timeZone)) {
				todayRows.add(row);
			}
		}
		
		return todayRows.toArray(new EventDataBaseRow[todayRows.size()]);
	}
}
