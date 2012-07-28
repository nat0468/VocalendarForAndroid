package jp.vocalendar.model;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * イベント情報を格納するデータベース
 */
public class EventDataBase {
	private static final String TAG = "EventDataBase";
	
	private Context context;
	private EventDataBaseHelper helper;
	private SQLiteDatabase database;
	
	public EventDataBase(Context context) {
		this.context = context;
		this.helper = new EventDataBaseHelper(context);
	}
	
	public void open() {
		try {
			database = helper.getWritableDatabase();
		} catch(SQLiteException e) {
			Log.e(TAG, "open() failed. " + e.getMessage());
		}
	}
	
	public void close() {
		if(database != null) {
			database.close();
		}
		database = null;
	}
	
	public void insertEvent(Event event) {
		Log.d(TAG, "insertEvent" + event.toDateTimeString());
		ContentValues v = new ContentValues();
		v.put(EventDataBaseHelper.COLUMN_SUMMARY, event.getSummary());
		v.put(EventDataBaseHelper.COLUMN_DESCRIPTION, event.getDescription());
		if(event.getStartDate() != null) {
			v.put(EventDataBaseHelper.COLUMN_START_DATE, event.getStartDate().getTime());
		}
		if(event.getStartDateTime() != null) {
			v.put(EventDataBaseHelper.COLUMN_START_DATE_TIME, event.getStartDateTime().getTime());
		}
		if(event.getEndDate() != null) {
			v.put(EventDataBaseHelper.COLUMN_END_DATE, event.getEndDate().getTime());
		}
		if(event.getEndDateTime() != null) {
			v.put(EventDataBaseHelper.COLUMN_END_DATE_TIME, event.getEndDateTime().getTime());
		}
		v.put(EventDataBaseHelper.COLUMN_START_DATE_INDEX, event.getStartDateIndex());
		v.put(EventDataBaseHelper.COLUMN_END_DATE_INDEX, event.getEndDateIndex());
		v.put(EventDataBaseHelper.COLUMN_RECURSIVE, event.getRecursive());
		v.put(EventDataBaseHelper.COLUMN_RECURSIVE_BY, event.getRecursiveBy());
		v.put(EventDataBaseHelper.COLUMN_BY_WEEKDAY_OCCURRENCE, event.getByWeekdayOccurrence());
		
		try {
			database.insertOrThrow(EventDataBaseHelper.EVENT_TABLE_NAME, null, v);
		} catch(SQLiteException e) {
			Log.e(TAG, "insertEventFailed. " + e.getMessage());
		}
	}
	
	public void deleteAllEvent() {
		Log.d(TAG, "deleteAllEvent");
		database.execSQL("delete from " + EventDataBaseHelper.EVENT_TABLE_NAME + ";");
	}
	
	public Event[] getAllEvents() {
		Cursor c = database.query(
				EventDataBaseHelper.EVENT_TABLE_NAME,
				new String[] {
						EventDataBaseHelper.COLUMN_START_DATE,
						EventDataBaseHelper.COLUMN_START_DATE_TIME,
						EventDataBaseHelper.COLUMN_END_DATE,
						EventDataBaseHelper.COLUMN_END_DATE_TIME,
						EventDataBaseHelper.COLUMN_SUMMARY,
						EventDataBaseHelper.COLUMN_DESCRIPTION,
						EventDataBaseHelper.COLUMN_RECURSIVE,
						EventDataBaseHelper.COLUMN_RECURSIVE_BY,
						EventDataBaseHelper.COLUMN_BY_WEEKDAY_OCCURRENCE						
				},
				null, null, null, null, EventDataBaseHelper.COLUMN_END_DATE_INDEX + " ASC");
		Event[] events = new Event[c.getCount()];
		int i = 0;
		if(c.moveToFirst()) {
			do {
				events[i++] = getEvent(c);
			} while(c.moveToNext());
		}
		return events;
	}
	
	private Event getEvent(Cursor c) {
		Event e = new Event();
		if(!c.isNull(0)) {
			e.setStartDate(new Date(c.getLong(0)));
		}
		if(!c.isNull(1)) {
			e.setStartDateTime(new Date(c.getLong(1)));
		}
		if(!c.isNull(2)) {
			e.setEndDate(new Date(c.getLong(2)));
		}
		if(!c.isNull(3)) {
			e.setEndDateTime(new Date(c.getLong(3)));
		}
		e.setSummary(c.getString(4));
		e.setDescription(c.getString(5));
		e.setRecursive(c.getInt(6));
		e.setRecursiveBy(c.getInt(7));
		e.setByWeekdayOccurrence(c.getInt(8));
		return e;
	}
	
	
	public Event[] getEventsByDate(Date date) {
		return null; // TODO
	}
}
