package jp.vocalendar.receiver;

import java.util.Calendar;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * イベントを通知するためのAlarmReceiverを設定する処理を実装したクラス。
 * AlarmReceiverを設定するべきイベント(OS起動、日付変更など)が発生したときに
 * AlarmReceiverを設定するReceiverでもある。
 */
public class AlarmReceiverSetter extends BroadcastReceiver {
	private static String TAG = "AlarmReceiverSetter";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		int notificationTime = VocalendarApplication.getNotificationTime(context);
	    setAlarmReceiverToAlarmManager(context, notificationTime);
		Log.d(TAG, "onReceive finished.");
	}
	
	public static void setAlarmReceiverToAlarmManager(Context context, int notificationTime) {
		Log.d(TAG, "setAlarmReceiverToAlarmManager");
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(
				context, AlarmReceiver.REQUEST_CODE_NORMAL, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if(notificationTime == Constants.NOT_NOTIFY_PREFERENCE_VALUE) {
			alarmManager.cancel(sender);
			Log.d(TAG, "not notify event");
			return;
		}
		
		// 通知時間を設定
		Calendar calSet = Calendar.getInstance();
		calSet.setTimeInMillis(System.currentTimeMillis());
		calSet.setTimeZone(TimeZone.getDefault());
		calSet.set(Calendar.HOUR_OF_DAY, notificationTime);
		calSet.set(Calendar.MINUTE, 0);
		calSet.set(Calendar.SECOND, 0);
		calSet.set(Calendar.MILLISECOND, 0);
		if(calSet.getTimeInMillis() < System.currentTimeMillis()) {
			// 過去の日時になった場合は、次の日にする
			calSet.add(Calendar.DATE, 1);
		}

		alarmManager.setRepeating(
				AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);

		Log.d(TAG, "setInexactRepeating() finished.");		
	}
	
	/**
	 * (デバッグ用)3秒後にイベント通知を実行する。
	 * @param context
	 */
	public static void setAlarmReceiverToAlarmManagerSoonDebug(Context context) {
		Log.d(TAG, "setAlarmReceiverToAlarmManagerSoon");		
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(AlarmReceiver.EXTRA_DEBUG, true);

		PendingIntent sender = PendingIntent.getBroadcast(
				context, AlarmReceiver.REQUEST_CODE_NORMAL, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 1秒後に通知を表示させる
		Calendar calSet = Calendar.getInstance();
		calSet.setTimeInMillis(System.currentTimeMillis());
		calSet.setTimeZone(TimeZone.getDefault());
		calSet.add(Calendar.SECOND, 1);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(
				AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), sender);
		Log.d(TAG, "setAlarmReceiverToAlarmManagerSoon() finished.");		
	}

	/**
	 * 通知時間の値に対応する文字列を返す。
	 * @param entryValue
	 * @param context
	 * @return
	 */
	public static String toEntry(String entryValue, Context context) {
		String[] entryValues =
				context.getResources().getStringArray(R.array.notification_time_entry_values);
		for(int i = 0; i < entryValues.length; i++) {
			if(entryValues[i].equals(entryValue)) {
				return context.getResources().getStringArray(R.array.notification_time_entries)[i];
			}
		}
		return null;
	}

}
