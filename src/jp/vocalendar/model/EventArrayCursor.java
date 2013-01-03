package jp.vocalendar.model;

import java.util.TimeZone;

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
	
	public EventArrayCursor(EventDataBaseRow[] events, TimeZone timeZone) {
		this.events = events;
		this.timeZone = timeZone;
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
		Log.d("EventListCursor", "getString(" + column + ") for " + currentRow.getEvent().toString());		
		switch(column) {
		case 0: // _id
			return Integer.toString(currentPosition); 
		case 1: // date
			return currentRow.formatAdditionalDate(timeZone);
		case 2: // time
			return currentRow.formatStartTime(timeZone);
		case 3: // summary
			return currentRow.getEvent().getSummary();
		case 4: // description
			return currentRow.getEvent().getDescription();
		}
		throw new IllegalArgumentException("invalid column: " + column);		
	}

	@Override
	public boolean isNull(int arg0) {
		// TODO Auto-generated method stub
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

}
