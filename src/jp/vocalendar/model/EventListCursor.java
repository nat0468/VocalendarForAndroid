package jp.vocalendar.model;

import java.util.List;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.util.Log;

/**
 * イベントリストのカーソル。
 * EventListActivityでの表示向け。
 */
public class EventListCursor extends AbstractCursor implements Cursor {
	public static final String[] columnNames = {
		"_id", "start", "summary", "description"
	};
	
	/** カーソル対象のリスト */
	private List<Event> eventList;
	/** カーソルの現在位置 */
	private int currentPosition = 0;
	/** カーソルの現在位置のEvent */
	private Event currentEvent;
	
	public EventListCursor(List<Event> eventList) {
		this.eventList = eventList;
		onMove(0, 0);
	}
	
	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public int getCount() {
		return eventList.size();
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
		Log.d("EventListCursor", "getString(" + column + ")");		
		switch(column) {
		case 0: // _id
			return Integer.toString(currentPosition); 
		case 1: // start
			return currentEvent.formatDateTime();
		case 2: // summary
			return currentEvent.getSummary();
		case 3: // description
			return currentEvent.getDescription();
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
		Log.d("EventListCursor", "onMove(" + oldPosition + "," + newPosition + ")");
		try {
			currentEvent = eventList.get(newPosition);
			currentPosition = newPosition;
			return true;
		} catch(IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	public Event getEvent(int position) {
		return eventList.get(position);
	}

}
