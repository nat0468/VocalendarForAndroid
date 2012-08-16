package jp.vocalendar.model;

import jp.vocalendar.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * EventListActivityでイベント一覧表示に使うAdapter.
 * SeparatorEventに対するセパレータ表示を行う。
 */
public class EventArrayCursorAdapter extends SimpleCursorAdapter {
	private EventArrayCursor cursor;
	private LayoutInflater inflater;
	
	/**
	 * コンストラクタ
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 */
	public EventArrayCursorAdapter(Context context, int layout, EventArrayCursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		this.cursor = cursor;
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
		if(isEnabled(position)) { // 選択可能なら通常のEvent(セパレータでない)
			return super.getView(position, convertView, parent); // セパレータでなければ通常処理			
		}
		if(convertView == null) {
			convertView = inflater.inflate(
					R.layout.event_list_separator_item, parent, false);			
		}
		Event e = cursor.getEvent(position);
		TextView tv = (TextView)convertView.findViewById(R.id.dateText);
		tv.setText(e.formatDateTime());
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		if(cursor.getEvent(position).isSeparator()) {
			return super.getItemViewType(position);
		}
		return super.getViewTypeCount(); // 親クラスが 0~(getViewTypeCount()-1) の範囲の値を返すので、それに+1した値を返す。
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1; // このクラスがセパレータ用に返すTypeだけ増やす。
	}
	
}
