package jp.vocalendar.model;

import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.util.Log;

public class LoggingEventComparatorInDate extends EventComparatorInDate {
	private TimeZone timeZone;
	private Context context;
	
	public LoggingEventComparatorInDate(Date date, TimeZone timeZone, Context context) {
		super(date, timeZone);
		this.timeZone = timeZone;
		this.context = context;
	}

	@Override
	public int compare(Event e1, Event e2) {
		int result = super.compare(e1, e2);
		Log.d("LoggingEventComparator", 
				e1.formatDateTime(timeZone,context) + ":" + e1.getSummary() +
				" x " + e2.formatDateTime(timeZone,context) + ":" + e2.getSummary() +
				" = " + result);
		return result;
	}

}
