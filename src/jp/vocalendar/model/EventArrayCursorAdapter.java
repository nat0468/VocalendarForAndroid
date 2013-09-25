package jp.vocalendar.model;

import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.util.DateUtil;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
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
	private static final String TAG = "EventArrayCursorAdapter";
	
	private Context context;
	private EventArrayCursor cursor;
	private LayoutInflater inflater;
	private TimeZone timeZone;
	private ColorTheme colorTheme;
	
	// ViewType値
	public static final int VIEW_TYPE_EVENT_WITH_DATE_TEXT= 0;
	public static final int VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT = 1;
	public static final int VIEW_TYPE_SEPARATOR = 2;
	public static final int VIEW_TYPE_NO_EVENT = 3;
	public static final int VIEW_TYPE_SEARCH_START_DATE = 4;
	private static final int VIEW_TYPE_COUNT = 5;
	
	/**
	 * コンストラクタ
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 */
	public EventArrayCursorAdapter(
			Context context, EventDataBaseRow[] events, TimeZone timeZone, ColorTheme colorTheme) {
		super(context, R.layout.event_list_item_additional_date,
				new EventArrayCursor(events, timeZone, context),
				new String[] { "time", "date", "summary" },
				new int[]{ R.id.timeText, R.id.dateText, R.id.summaryText });
		this.context = context;
		this.cursor = (EventArrayCursor)getCursor();
		this.timeZone = timeZone;
		this.inflater =
				(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.colorTheme = colorTheme;
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
			tv.setBackgroundColor(colorTheme.getDarkBackgroundColor());
			tv.setTextColor(colorTheme.getDarkTextColor());
			break;
		case VIEW_TYPE_SEARCH_START_DATE:
			if(convertView == null) {
				convertView = inflater.inflate(
						R.layout.event_list_separator_item, parent, false);			
			}
			row = cursor.getEventDataBaseRow(position);
			tv = (TextView)convertView.findViewById(R.id.dateText);
			StringBuilder sb = new StringBuilder();
			sb.append(context.getResources().getString(R.string.search_start_date));
			sb.append(context.getResources().getString(R.string.column));
			sb.append(DateUtil.formatDate(row.getDisplayDate()));
			tv.setText(sb.toString());
			tv.setBackgroundColor(colorTheme.getDarkBackgroundColor());
			tv.setTextColor(colorTheme.getDarkTextColor());
			break;
		case VIEW_TYPE_NO_EVENT:
			if(convertView == null) {
				convertView = inflater.inflate(
						R.layout.event_list_no_event_item, parent, false);
			}
			break;
		case VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT:
			if(convertView == null) {
				convertView = inflater.inflate(
						R.layout.event_list_item, parent, false);
			}
			setViewValue(position, convertView);
			setColorToTimeTextView(position, convertView);
			applyThemeToListItem(convertView);
			break;
		case VIEW_TYPE_EVENT_WITH_DATE_TEXT:
			convertView = super.getView(position, convertView, parent); // セパレータでなければ通常処理
			setColorToTimeTextView(position, convertView);
			applyThemeToListItem(convertView);
			break;
		}				
		return convertView;		
	}
	
	private void setColorToTimeTextView(int position, View convertView) {
		int color = chooseColor(cursor.getEventDataBaseRow(position));
		TextView ttv = (TextView)convertView.findViewById(R.id.timeText);			
		ttv.setBackgroundColor(color);
	}

	private void setViewValue(int position, View view) {
		EventDataBaseRow row = cursor.getEventDataBaseRow(position);

		TextView tt = (TextView)view.findViewById(R.id.timeText);
		tt.setText(row.formatStartTime(timeZone, context));
		
		TextView st = (TextView)view.findViewById(R.id.summaryText);
		st.setText(row.getEvent().getSummary()); 
	}		
	
	private void applyThemeToListItem(View view) {
		TextView dt = (TextView)view.findViewById(R.id.dateText);
		if(dt != null) {
			dt.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
			dt.setTextColor(colorTheme.getLightTextColor());			
		}
		
		TextView st = (TextView)view.findViewById(R.id.summaryText);
		st.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
		st.setTextColor(colorTheme.getLightTextColor());					
	}
	
	@Override
	public int getItemViewType(int position) {
		EventDataBaseRow row = cursor.getEventDataBaseRow(position);
		switch(row.getRowType()) {
		case EventDataBaseRow.TYPE_NO_EVENT:
			return VIEW_TYPE_NO_EVENT;
		case EventDataBaseRow.TYPE_SEPARATOR:
			return VIEW_TYPE_SEPARATOR;
		case EventDataBaseRow.TYPE_SEARCH_START_DATE:
			return VIEW_TYPE_SEARCH_START_DATE;
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

	public EventArrayCursor getEventArrayCursor() {
		return cursor;
	}
}
