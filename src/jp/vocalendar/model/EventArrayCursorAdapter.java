package jp.vocalendar.model;

import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.DialogUtil;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * EventListActivityでイベント一覧表示に使うAdapter.
 * SeparatorEventに対するセパレータ表示を行う。
 */
public class EventArrayCursorAdapter extends SimpleCursorAdapter {
	/**
	 * お気に入りを切り替える処理を実装したクラスが実装するインターフェイス。
	 */
	public interface FavoriteToggler {
		/**
		 * 指定された行のイベントのお気に入りを切り替える。
		 * まだお気に入りでなければ追加。お気に入りならば削除する。
		 * @param row お気に入りを切り替える行。このメソッドから戻ったら、お気に入りの状態も更新されている。
		 */
		public void toggleFavorite(EventDataBaseRow row);
	}
	
	private static final String TAG = "EventArrayCursorAdapter";
	
	private Activity context;
	private EventArrayCursor cursor;
	private LayoutInflater inflater;
	private TimeZone timeZone;
	private ColorTheme colorTheme;
	private Bitmap favoriteBitmap, notFavoriteBitmap;
	private FavoriteToggler favoriteToggler;
	private FavoriteEventManager favoriteEventManager;
	
	// ViewType値
	public static final int VIEW_TYPE_EVENT_WITH_DATE_TEXT= 0;
	public static final int VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT = 1;
	public static final int VIEW_TYPE_SEPARATOR = 2;
	public static final int VIEW_TYPE_NO_EVENT = 3;
	public static final int VIEW_TYPE_SEARCH_START_DATE = 4;
	private static final int VIEW_TYPE_COUNT = 5;
	
	/**
	 * コンストラクタ。表示するイベント一覧にEventDataBaseRowの配列を指定する。
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 */
	public EventArrayCursorAdapter(
			Activity context, EventDataBaseRow[] events, TimeZone timeZone, ColorTheme colorTheme,
			FavoriteEventManager favoriteEventManager,
			FavoriteToggler favoriteToggler) {
		this(context, new EventArrayCursor(events, timeZone, context),
				timeZone, colorTheme, favoriteEventManager, favoriteToggler);
	}

	/**
	 * コンストラクタ。表示するイベント一覧にEventArrayCursorを指定する。
	 * @param context
	 * @param cursor
	 * @param timeZone
	 * @param colorTheme
	 * @param favoriteToggler
	 */
	public EventArrayCursorAdapter(
			Activity context, EventArrayCursor cursor, TimeZone timeZone, ColorTheme colorTheme,
			FavoriteEventManager favoriteEventManager, FavoriteToggler favoriteToggler) {
		super(context, R.layout.event_list_item_additional_date, cursor,
				new String[] { "time", "date", "summary" },
				new int[]{ R.id.timeText, R.id.dateText, R.id.summaryText });
		this.context = context;
		this.cursor = (EventArrayCursor)getCursor();
		this.timeZone = timeZone;
		this.inflater =
				(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.colorTheme = colorTheme;
		this.favoriteEventManager = favoriteEventManager;
		this.favoriteToggler = favoriteToggler;
		this.favoriteBitmap =
				BitmapFactory.decodeResource(context.getResources(), R.drawable.favorite);
		this.notFavoriteBitmap =
				BitmapFactory.decodeResource(context.getResources(), R.drawable.not_favorite);		
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
			setFavoriteStar(position, convertView);
			break;
		case VIEW_TYPE_EVENT_WITH_DATE_TEXT:
			convertView = super.getView(position, convertView, parent); // セパレータでなければ通常処理
			setColorToTimeTextView(position, convertView);
			applyThemeToListItem(convertView);
			setFavoriteStar(position, convertView);
			break;
		}				
		return convertView;		
	}
	
	private void setColorToTimeTextView(int position, View convertView) {
		EventDataBaseRow r = cursor.getEventDataBaseRow(position);
		TextView ttv = (TextView)convertView.findViewById(R.id.timeText);			
		int bg = chooseBackgroundColor(r);;
		ttv.setBackgroundColor(bg);
		int tc = chooseTextColor(r);
		ttv.setTextColor(tc);
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
		
		LinearLayout l = (LinearLayout)view.findViewById(R.id.eventListItemLayout);
		l.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
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
	 * 指定されたEventDataBaseRowの日の種類に応じた背景色を選ぶ
	 * @param row
	 * @return
	 */
	private int chooseBackgroundColor(EventDataBaseRow row) {
		switch(row.getDayKind()) {
		case EventDataBaseRow.DAY_KIND_HOLIDAY:
			return colorTheme.getSundayBackgroundColor();
		case EventDataBaseRow.DAY_KIND_SATURDAY:
			return colorTheme.getSaturdayBackgroundColor();
		}
		return colorTheme.getNormalDayBackgroundColor();
	}

	/**
	 * 指定されたEventDataBaseRowの日の種類に応じた背景色を選ぶ
	 * @param row
	 * @return
	 */
	private int chooseTextColor(EventDataBaseRow row) {
		switch(row.getDayKind()) {
		case EventDataBaseRow.DAY_KIND_HOLIDAY:
			return colorTheme.getSundayTextColor();
		case EventDataBaseRow.DAY_KIND_SATURDAY:
			return colorTheme.getSaturdayTextColor();
		}
		return colorTheme.getNormalDayTextColor();
	}

	
	public EventArrayCursor getEventArrayCursor() {
		return cursor;
	}
	
	private void setFavoriteStar(int position, View view) {
		EventDataBaseRow row = cursor.getEventDataBaseRow(position);
		ImageView iv = (ImageView)view.findViewById(R.id.favorite_image_view);
		updateFavoriteStar(row, iv);
		iv.setTag(Integer.valueOf(position));
		iv.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				int position = ((Integer)v.getTag()).intValue();
				EventDataBaseRow row = cursor.getEventDataBaseRow(position);
				favoriteToggler.toggleFavorite(row);
				updateFavoriteStar(row, (ImageView)v);
			}
		});
	}

	protected void updateFavoriteStar(EventDataBaseRow row, ImageView iv) {
		if(favoriteEventManager.isFavorite(row)) {
			iv.setImageBitmap(favoriteBitmap);
		} else {
			iv.setImageBitmap(notFavoriteBitmap);
		}
	}
	
	/**
	 * イベント情報に変更があったときに呼ぶ。イベント一覧を更新したいときに呼ぶ。
	 */
	public void notifyContentChange() {
		onContentChanged();
	}
}
