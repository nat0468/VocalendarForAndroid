package jp.vocalendar.model;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.util.Log;

/**
 * イベント配列のカーソル。
 * EventListActivityでの表示向け。
 */
public class EventArrayCursor extends AbstractCursor implements Cursor {
	public static final String[] columnNames = {
		"_id", "date", "time", "summary", "description"
	};
	
	/** カーソル対象の配列 */
	private EventDataBaseRow[] events;
	/** カーソルの現在位置 */
	private int currentPosition = 0;
	/** カーソルの現在位置のEvent */
	private EventDataBaseRow currentRow;
	
	/** 表示に使うタイムゾーン */
	private TimeZone timeZone = null;
	
	private Context context = null;
	
	public EventArrayCursor(EventDataBaseRow[] events, TimeZone timeZone, Context context) {
		this.events = events;
		this.timeZone = timeZone;
		this.context = context;
		onMove(0, 0);
	}
	
	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public int getCount() {
		return events.length;
	}

	@Override
	public double getDouble(int arg0) {
		throw new IllegalArgumentException(arg0 + " is not double");
	}

	@Override
	public float getFloat(int arg0) {
		throw new IllegalArgumentException(arg0 + " is not float");
	}

	@Override
	public int getInt(int arg0) {
		throw new IllegalArgumentException(arg0 + " is not int");
	}

	@Override
	public long getLong(int column) {
		Log.d("EventListCursor", "getLong(" + column + ")");		
		
		switch(column) {
		case 0: // _id
			return currentPosition;
		}
		throw new IllegalArgumentException(column + " is not long");
	}

	@Override
	public short getShort(int arg0) {
		throw new IllegalArgumentException(arg0 + " is not short");
	}

	@Override
	public String getString(int column) {
		//Log.d("EventListCursor", "getString(" + column + ") for " + currentRow.getEvent().toString());		
		switch(column) {
		case 0: // _id
			return Integer.toString(currentPosition); 
		case 1: // date
			return currentRow.formatAdditionalDate(timeZone, context);
		case 2: // time
			return currentRow.formatStartTime(timeZone, context);
		case 3: // summary
			return currentRow.getEvent().getSummary();
		case 4: // description
			return currentRow.getEvent().getDescription();
		}
		throw new IllegalArgumentException("invalid column: " + column);		
	}

	@Override
	public boolean isNull(int arg0) {
		return false;
	}

	@Override
	public boolean onMove(int oldPosition, int newPosition) {
		// Log.d("EventListCursor", "onMove(" + oldPosition + "," + newPosition + ")");
		try {
			currentRow = events[newPosition];
			currentPosition = newPosition;
			return true;
		} catch(IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	public Event getEvent(int position) {
		return events[position].getEvent();
	}

	public EventDataBaseRow getEventDataBaseRow(int position) {
		return events[position];
	}

	/**
	 * 末尾にイベントを追加したイベント配列を返す。
	 * このCursorの内部状態は変更されない。変更にはupdateEventDataBaseRows()を呼ぶ。
	 * @param eventsToAppend
	 * @return
	 */
	public List<EventDataBaseRow> getAppendedEventDataBaseRows(EventDataBaseRow[] eventsToAppend) {
		EventDataBaseRow[] oldEvents = events;
		List<EventDataBaseRow> result = new LinkedList<EventDataBaseRow>();
		int eventIndex = 0;
		for(int i =0; i < oldEvents.length; i++) {
			EventDataBaseRow e = new EventDataBaseRow(oldEvents[i]);
			if(e.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				e.setEventIndex(eventIndex++);
			}
			result.add(e);
		}
		for(int i = 0; i < eventsToAppend.length; i++) {
			EventDataBaseRow e = eventsToAppend[i];
			if(e.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				e.setEventIndex(eventIndex++);
			}			
			result.add(e);
		}
		return result;
	}
	
	/**
	 * 先頭にイベントを追加したイベント配列を返す。
	 * このCursorの内部状態は変更されない。変更にはupdateEventDataBaseRows()を呼ぶ。
	 * @param eventsToInsert
	 * @return
	 */
	public List<EventDataBaseRow> getInsertedEventDataBaseRows(EventDataBaseRow[] eventsToInsert) {
		EventDataBaseRow[] oldEvents = events;
		List<EventDataBaseRow> result = new LinkedList<EventDataBaseRow>();
		int i = 0;
		int eventIndex = 0;
		for(; i < eventsToInsert.length; i++) {
			EventDataBaseRow e = eventsToInsert[i];
			if(e.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				e.setEventIndex(eventIndex++);
			}
			result.add(e);
		}
		for(; i < eventsToInsert.length + oldEvents.length; i++) {
			EventDataBaseRow e = new EventDataBaseRow(oldEvents[i - eventsToInsert.length]);
			if(e.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				e.setEventIndex(eventIndex++);
			}
			result.add(e);
		}
		return result;
	}

	/**
	 * イベント配列の内容を更新する
	 * @param rows
	 */
	public void updateEventDataBaseRows(EventDataBaseRow[] rows) {
		events = rows;
		onChange(true);
	}

	/**
	 * イベント配列を返す。
	 * @return
	 */
	public EventDataBaseRow[] getEventDataBaseRows() {
		return events;
	}
}
