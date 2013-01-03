package jp.vocalendar.model;

import java.util.TimeZone;

import jp.vocalendar.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * EventListActivityでイベント一覧表示に使うAdapter.
 * SeparatorEventに対するセパレータ表示を行う。
 */
public class EventArrayCursorAdapter extends SimpleCursorAdapter {
	private Context context;
	private EventArrayCursor cursor;
	private LayoutInflater inflater;
	private TimeZone timeZone;
	
	// ViewType値
	public static final int VIEW_TYPE_EVENT_WITH_DATE_TEXT= 0;
	public static final int VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT = 1;
	public static final int VIEW_TYPE_SEPARATOR = 2;
	private static final int VIEW_TYPE_COUNT = 3;
	
	/**
	 * コンストラクタ
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 */
	public EventArrayCursorAdapter(Context context, int layout, EventArrayCursor cursor,
			String[] from, int[] to, TimeZone timeZone) {
		super(context, layout, cursor, from, to);
		this.context = context;
		this.cursor = cursor;
		this.timeZone = timeZone;
		this.inflater =
				(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public boolean isEnabled(int position) {
		if(cursor.getEvent(position).isSeparator()) {
			return false;
		}
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch(getItemViewType(position)) {
		case VIEW_TYPE_SEPARATOR:
			if(convertView == null) {
				convertView = inflater.inflate(
						R.layout.event_list_separator_item, parent, false);			
			}
			Event e = cursor.getEvent(position);
			TextView tv = (TextView)convertView.findViewById(R.id.dateText);
			tv.setText(e.formatDateTime(timeZone));
			break;
		case VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT:
		case VIEW_TYPE_EVENT_WITH_DATE_TEXT:
			convertView = super.getView(position, convertView, parent); // セパレータでなければ通常処理
			LinearLayout layout = (LinearLayout)convertView.findViewById(R.id.eventLinearLayout);			
			int color = chooseColor(cursor.getEventDataBaseRow(position));
			layout.setBackgroundColor(color);
			TextView ttv = (TextView)convertView.findViewById(R.id.timeText);			
			ttv.setBackgroundColor(color);						
			TextView dtv = (TextView)convertView.findViewById(R.id.dateText);						
			if(cursor.getEventDataBaseRow(position).hasAdditionalDate(timeZone)) {
				dtv.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			} else {
				dtv.getLayoutParams().height = 0;
			}			
			break;
		}				
		return convertView;		
	}

	@Override
	public int getItemViewType(int position) {
		if(cursor.getEvent(position).isSeparator()) {
			return VIEW_TYPE_SEPARATOR;
		}
		if(cursor.getEventDataBaseRow(position).hasAdditionalDate(timeZone)) {
			return VIEW_TYPE_EVENT_WITH_DATE_TEXT;
		}
		return VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT;
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}
	
	/**
	 * 指定されたEventDataBaseRowの日の種類に応じた色を選ぶ
	 * @param row
	 * @return
	 */
	private int chooseColor(EventDataBaseRow row) {
		switch(row.getDayKind()) {
		case EventDataBaseRow.DAY_KIND_HOLIDAY:
			return context.getResources().getColor(R.color.sunday);
		case EventDataBaseRow.DAY_KIND_SATURDAY:
			return context.getResources().getColor(R.color.saturday);
		}
		return context.getResources().getColor(R.color.normalday);
	}
}
