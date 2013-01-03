package jp.vocalendar.model;

import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

public class LoggingEventComparatorInDate extends EventComparatorInDate {

	public LoggingEventComparatorInDate(Date date, TimeZone timeZone) {
		super(date, timeZone);
	}

	@Override
	public int compare(Event e1, Event e2) {
		int result = super.compare(e1, e2);
		TimeZone tz = TimeZone.getDefault();
		Log.d("LoggingEventComparator", 
				e1.formatDateTime(tz) + ":" + e1.getSummary() +
				" x " + e2.formatDateTime(tz) + ":" + e2.getSummary() + " = " + result);
		return result;
	}

}
