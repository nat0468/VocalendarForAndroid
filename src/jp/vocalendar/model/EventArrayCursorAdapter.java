package jp.vocalendar.model;

import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.util.DateUtil;
import android.content.Context;
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
	public static final int VIEW_TYPE_NO_EVENT = 3;
	private static final int VIEW_TYPE_COUNT = 4;
	
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
		if(cursor.getEventDataBaseRow(position).getRowType() 
				== EventDataBaseRow.TYPE_NORMAL_EVENT) {			
			return true;
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch(getItemViewType(position)) {
		case VIEW_TYPE_SEPARATOR:
			if(convertView == null) {
				convertView = inflater.inflate(
						R.layout.event_list_separator_item, parent, false);			
			}
			EventDataBaseRow row = cursor.getEventDataBaseRow(position);
			TextView tv = (TextView)convertView.findViewById(R.id.dateText);
			tv.setText(DateUtil.formatDate(row.getDisplayDate()));
			break;
		case VIEW_TYPE_NO_EVENT:
			if(convertView == null) {
				convertView = inflater.inflate(
						R.layout.event_list_no_event_item, parent, false);
			}
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
			if(cursor.getEventDataBaseRow(position).hasAdditionalDate(timeZone, context)) {
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
		EventDataBaseRow row = cursor.getEventDataBaseRow(position);
		switch(row.getRowType()) {
		case EventDataBaseRow.TYPE_NO_EVENT:
			return VIEW_TYPE_NO_EVENT;
		case EventDataBaseRow.TYPE_SEPARATOR:
			return VIEW_TYPE_SEPARATOR;
		}
		
		// EventDataBaseRow.TYPE_NORMAL_EVENT の場合
		if(row.hasAdditionalDate(timeZone, context)) {
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
