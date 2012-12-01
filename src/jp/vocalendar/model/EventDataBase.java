package jp.vocalendar.model;

import java.util.Date;
import java.util.List;

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
	
	/**
	 * イベント情報をDBから取得(query())するときのカラム一覧
	 */
	private static String[] QUERY_EVENT_COLUMNS = new String[] {
			EventDataBaseHelper.COLUMN_START_DATE,
			EventDataBaseHelper.COLUMN_START_DATE_TIME,
			EventDataBaseHelper.COLUMN_END_DATE,
			EventDataBaseHelper.COLUMN_END_DATE_TIME,
			EventDataBaseHelper.COLUMN_SUMMARY,
			EventDataBaseHelper.COLUMN_DESCRIPTION,
			EventDataBaseHelper.COLUMN_RECURSIVE,
			EventDataBaseHelper.COLUMN_RECURSIVE_BY,
			EventDataBaseHelper.COLUMN_BY_WEEKDAY_OCCURRENCE,
			EventDataBaseHelper.COLUMN_INDEX,
			EventDataBaseHelper.COLUMN_PREVIOUS_INDEX,
			EventDataBaseHelper.COLUMN_NEXT_INDEX
	};

	
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
	
	public void insertEvent(List<EventDataBaseRow> eventRows) {
		for(EventDataBaseRow e : eventRows) {
			insertEvent(e);
		}
	}
	
	public void insertEvent(EventDataBaseRow eventRow) {
		Event event = eventRow.getEvent();
		Log.d(TAG, "insertEvent" + event.toDateTimeSummaryString());
		ContentValues v = new ContentValues();
		v.put(EventDataBaseHelper.COLUMN_INDEX, eventRow.getIndex());		
		v.put(EventDataBaseHelper.COLUMN_PREVIOUS_INDEX, eventRow.getPreviousIndex());		
		v.put(EventDataBaseHelper.COLUMN_NEXT_INDEX, eventRow.getNextIndex());		
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
	
	public EventDataBaseRow[] getAllEvents() {
		Cursor c = database.query(
				EventDataBaseHelper.EVENT_TABLE_NAME, QUERY_EVENT_COLUMNS, 
				null, null, null, null, EventDataBaseHelper.COLUMN_INDEX + " ASC");
		EventDataBaseRow[] events = new EventDataBaseRow[c.getCount()];
		int i = 0;
		if(c.moveToFirst()) {
			do {
				events[i++] = getEvent(c);
			} while(c.moveToNext());
		}
		c.close();
		return events;
	}
	
	private EventDataBaseRow getEvent(Cursor c) {
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
		if(EventSeparator.isSeparator(e)) {
			e = new EventSeparator(e.getStartDate());
		}
		return new EventDataBaseRow(e, c.getInt(9), c.getInt(10), c.getInt(11));		
	}
	
	/**
	 * 指定されたインデックスのイベント情報を返す。
	 * @param index 
	 * @return イベント情報。指定されたインデックスが存在しない場合はnull
	 */
	public EventDataBaseRow getEventByIndex(int index) {
		Cursor c = database.query(
				EventDataBaseHelper.EVENT_TABLE_NAME, QUERY_EVENT_COLUMNS,
				EventDataBaseHelper.COLUMN_INDEX + "=?", new String[]{Integer.toString(index)},
				null, null, EventDataBaseHelper.COLUMN_INDEX + " ASC");
		EventDataBaseRow event = null;
		if(c.moveToFirst()) {
			event = getEvent(c);
		}
		c.close();
		return event;		
	}
	
	public Event[] getEventsByDate(Date date) {
		return null; // TODO
	}
}
