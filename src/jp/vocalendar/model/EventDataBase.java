package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

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
			EventDataBaseHelper.COLUMN_START_DATE, // 0
			EventDataBaseHelper.COLUMN_START_DATE_TIME,
			EventDataBaseHelper.COLUMN_END_DATE,
			EventDataBaseHelper.COLUMN_END_DATE_TIME,
			EventDataBaseHelper.COLUMN_GID,			
			EventDataBaseHelper.COLUMN_GCALENDAR_ID, // 5
			EventDataBaseHelper.COLUMN_SUMMARY,
			EventDataBaseHelper.COLUMN_DESCRIPTION,
			EventDataBaseHelper.COLUMN_RECURSIVE,
			EventDataBaseHelper.COLUMN_RECURSIVE_BY,
			EventDataBaseHelper.COLUMN_BY_WEEKDAY_OCCURRENCE, // 10
			EventDataBaseHelper.COLUMN_INDEX,
			EventDataBaseHelper.COLUMN_EVENT_INDEX,
			EventDataBaseHelper.COLUMN_DISPLAY_DATE,
			EventDataBaseHelper.COLUMN_ROW_TYPE,
			EventDataBaseHelper.COLUMN_DAY_KIND, // 15
			EventDataBaseHelper.COLUMN_LOCATION,			
	};
	
	/**
	 * お気に入りイベント情報をDBから取得(query())するときのカラム一覧
	 */
	private static String[] QUERY_FAVORITE_EVENT_COLUMNS = new String[] {
			EventDataBaseHelper.COLUMN_START_DATE_INDEX, // 0
			EventDataBaseHelper.COLUMN_GID,			
			EventDataBaseHelper.COLUMN_GCALENDAR_ID,
			EventDataBaseHelper.COLUMN_SUMMARY,
			EventDataBaseHelper.COLUMN_DESCRIPTION,
			EventDataBaseHelper.COLUMN_START_DATE, // 5
			EventDataBaseHelper.COLUMN_START_DATE_TIME,
			EventDataBaseHelper.COLUMN_END_DATE,
			EventDataBaseHelper.COLUMN_END_DATE_TIME,
			EventDataBaseHelper.COLUMN_RECURSIVE,
			EventDataBaseHelper.COLUMN_RECURSIVE_BY, // 10
			EventDataBaseHelper.COLUMN_BY_WEEKDAY_OCCURRENCE,
			EventDataBaseHelper.COLUMN_LOCATION, // 12	
	};

	private EventDataBaseHelper helper;
	private SQLiteDatabase database;
	
	public EventDataBase(Context context) {
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
		// Log.d(TAG, "insertEvent" + event.toDateTimeSummaryString(TimeZone.getDefault()));
		ContentValues v = new ContentValues();
		v.put(EventDataBaseHelper.COLUMN_INDEX, eventRow.getIndex());		
		v.put(EventDataBaseHelper.COLUMN_EVENT_INDEX, eventRow.getEventIndex());		
		v.put(EventDataBaseHelper.COLUMN_DISPLAY_DATE, eventRow.getDisplayDate().getTime());
		v.put(EventDataBaseHelper.COLUMN_ROW_TYPE, eventRow.getRowType());
		v.put(EventDataBaseHelper.COLUMN_DAY_KIND, eventRow.getDayKind());
		if(eventRow.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
			Event event = eventRow.getEvent();
			v.put(EventDataBaseHelper.COLUMN_GID, event.getGid());		
			v.put(EventDataBaseHelper.COLUMN_GCALENDAR_ID, event.getGCalendarId());
			v.put(EventDataBaseHelper.COLUMN_SUMMARY, event.getSummary());
			v.put(EventDataBaseHelper.COLUMN_LOCATION, event.getLocation());
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
		}	
		try {
			database.insertOrThrow(EventDataBaseHelper.EVENTS_TABLE_NAME, null, v);
		} catch(SQLiteException e) {
			Log.e(TAG, "insertEventFailed. " + e.getMessage());
		}
	}
	
	public void deleteAllEvent() {
		Log.d(TAG, "deleteAllEvent");
		database.execSQL("delete from " + EventDataBaseHelper.EVENTS_TABLE_NAME + ";");
	}
	
	public EventDataBaseRow[] getAllEvents() {
		Cursor c = database.query(
				EventDataBaseHelper.EVENTS_TABLE_NAME, QUERY_EVENT_COLUMNS, 
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
	
	public EventDataBaseRowArray getEventDataBaseRowArray() {
		EventDataBaseRow[] events = getAllEvents();
		LinkedList<EventDataBaseRow> normalEvents = new LinkedList<EventDataBaseRow>();
		for (int i = 0; i < events.length; i++) {
			if(events[i].getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				normalEvents.add(events[i]);
			}
		}
		return new EventDataBaseRowArray(
				events,
				normalEvents.toArray(new EventDataBaseRow[normalEvents.size()]));
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
		e.setGid(c.getString(4));
		e.setGCalendarId(c.getString(5));
		e.setSummary(c.getString(6));		
		e.setDescription(c.getString(7));
		e.setRecursive(c.getInt(8));
		e.setRecursiveBy(c.getInt(9));
		e.setByWeekdayOccurrence(c.getInt(10));
		
		Date dispDate = new Date(c.getLong(13)); 
		int rowType = c.getInt(14);
		int index = c.getInt(11);					
		
		switch(rowType) {
		case EventDataBaseRow.TYPE_SEPARATOR:
			return EventDataBaseRow.makeSeparatorRow(index, dispDate);
		case EventDataBaseRow.TYPE_NO_EVENT:
			return EventDataBaseRow.makeNoEventRow(index, dispDate);
		}
		
		e.setLocation(c.getString(16));
		
		EventDataBaseRow row = new EventDataBaseRow(
				e, index, c.getInt(12), dispDate, c.getInt(15));
		return row;
	}
	
	/**
	 * 指定されたインデックスのイベント情報を返す。
	 * @param index 
	 * @return イベント情報。指定されたインデックスが存在しない場合はnull
	 */
	public EventDataBaseRow getEventByIndex(int index) {
		Cursor c = database.query(
				EventDataBaseHelper.EVENTS_TABLE_NAME, QUERY_EVENT_COLUMNS,
				EventDataBaseHelper.COLUMN_INDEX + "=?", new String[]{Integer.toString(index)},
				null, null, EventDataBaseHelper.COLUMN_INDEX + " ASC");
		EventDataBaseRow event = null;
		if(c.moveToFirst()) {
			event = getEvent(c);
		}
		c.close();
		return event;		
	}
	
	/**
	 * 指定されたイベントインデックスのイベント情報を返す。
	 * @param eventIndex 
	 * @return イベント情報。指定されたインデックスが存在しない場合はnull
	 */
	public EventDataBaseRow getEventByEventIndex(int eventIndex) {
		Cursor c = database.query(
				EventDataBaseHelper.EVENTS_TABLE_NAME, QUERY_EVENT_COLUMNS,
				EventDataBaseHelper.COLUMN_EVENT_INDEX + "=?", new String[]{Integer.toString(eventIndex)},
				null, null, EventDataBaseHelper.COLUMN_INDEX + " ASC");
		EventDataBaseRow event = null;
		if(c.moveToFirst()) {
			event = getEvent(c);
		}
		c.close();
		return event;		
	}
	
	public int countEvent() {
		Cursor c = database.rawQuery(
				"SELECT COUNT(" + EventDataBaseHelper.COLUMN_GCALENDAR_ID + ") FROM " +
						EventDataBaseHelper.EVENTS_TABLE_NAME + " WHERE " +
						EventDataBaseHelper.COLUMN_ROW_TYPE + "=?",
						new String[]{ Integer.toString(EventDataBaseRow.TYPE_NORMAL_EVENT) });
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}
	
	/**
	 * 指定されたイベントがお気に入りかどうか判定する。
	 * @param e
	 * @return お気に入りならば true を返す。
	 */
	public boolean isFavorite(Event e) {
		Cursor c = database.rawQuery(
				"SELECT COUNT(*) FROM " + EventDataBaseHelper.FAVORITES_TABLE_NAME +
				" WHERE " + EventDataBaseHelper.COLUMN_GCALENDAR_ID + "=? AND " +
				EventDataBaseHelper.COLUMN_GID + "=?;",
				new String[] { e.getGCalendarId(), e.getGid() });
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count != 0;					
	}
	
	/**
	 * 指定したイベントをお気に入りに登録する
	 * @param e
	 * @return 登録に成功したらtrue。既に登録済みで登録しなかった場合はfalse 
	 */
	public boolean addFavorite(Event event) {
		if(isFavorite(event)) {
			return false;
		}
		ContentValues v = new ContentValues();
		long index = (event.isRecursive() ? Integer.MAX_VALUE : event.getStartDateIndex());
		v.put(EventDataBaseHelper.COLUMN_START_DATE_INDEX, index);
		v.put(EventDataBaseHelper.COLUMN_GID, event.getGid());
		v.put(EventDataBaseHelper.COLUMN_GCALENDAR_ID, event.getGCalendarId());
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
		v.put(EventDataBaseHelper.COLUMN_LOCATION, event.getLocation());

		try {
			database.insertOrThrow(EventDataBaseHelper.FAVORITES_TABLE_NAME, null, v);
			return true;
		} catch(SQLiteException e) {
			Log.e(TAG, "addFavorite. " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * お気に入りイベントを削除する。
	 * @param event
	 * @return 削除に成功したらtrueを返す。それ以外はfalaseを返す。
	 */
	public boolean removeFavorite(Event event) {
		int c = database.delete(
				EventDataBaseHelper.FAVORITES_TABLE_NAME,
				EventDataBaseHelper.COLUMN_GCALENDAR_ID + "=? AND " +
						EventDataBaseHelper.COLUMN_GID + "=?",
				new String[] { event.getGCalendarId(), event.getGid() });
		return c > 0;
	}
	
	/**
	 * お気に入りイベントを取得する。
	 * @param today お気に入り一覧を取得する起点日時(典型的には現在日時)
	 * @param timeZone 
	 * @param rows 取得したお気に入りイベントを格納するリスト
	 * @return rowsの中の起点日時の位置(インデックス)
	 */
	public int getAllFavoriteEvents(Calendar today, TimeZone timeZone, List<FavoriteEventDataBaseRow> rows) {
		Cursor c = database.query(
				EventDataBaseHelper.FAVORITES_TABLE_NAME, QUERY_FAVORITE_EVENT_COLUMNS, 
				null, null, null, null, EventDataBaseHelper.COLUMN_START_DATE_INDEX + " ASC");
		if(c.moveToFirst()) {
			do {
				rows.add(getFavoriteEvent(c, today, timeZone));				
			} while(c.moveToNext());
		}
		c.close();
		sortByDeemedStartDateTime(rows, today, timeZone);
		setEventIndex(rows);
		return findToday(rows, today);
	}
	
	private void setEventIndex(List<FavoriteEventDataBaseRow> rows) {
		int i = 0;
		for(EventDataBaseRow row : rows) {
			row.setEventIndex(i++);
		}
	}

	private FavoriteEventDataBaseRow getFavoriteEvent(Cursor c, Calendar today, TimeZone timeZone) {			
		Event e = new Event();
		e.setGid(c.getString(1));
		e.setGCalendarId(c.getString(2));
		e.setSummary(c.getString(3));		
		e.setDescription(c.getString(4));

		if(!c.isNull(5)) {
			e.setStartDate(new Date(c.getLong(5)));
		}
		if(!c.isNull(6)) {
			e.setStartDateTime(new Date(c.getLong(6)));
		}
		if(!c.isNull(7)) {
			e.setEndDate(new Date(c.getLong(7)));
		}
		if(!c.isNull(8)) {
			e.setEndDateTime(new Date(c.getLong(8)));
		}
		e.setRecursive(c.getInt(9));
		e.setRecursiveBy(c.getInt(10));
		e.setByWeekdayOccurrence(c.getInt(11));
		e.setLocation(c.getString(12));
		
		FavoriteEventDataBaseRow row = new FavoriteEventDataBaseRow(e, today, timeZone);
		return row;
	}
	
	private void sortByDeemedStartDateTime(
			List<FavoriteEventDataBaseRow> rows, Calendar today, TimeZone timeZone) {
		Collections.sort(rows, new DeemedStartDateTimeComparator());
	}
	
	private int findToday(List<FavoriteEventDataBaseRow> rows, Calendar today) {
		Date d = today.getTime();
		int i = 0;
		for(FavoriteEventDataBaseRow r : rows) {
			if(d.compareTo(r.getDeemedStartDateTime()) <= 0) {
				return i;
			}
			i++;
		}
		return 0;
	}

	
}
