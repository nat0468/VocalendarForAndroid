package jp.vocalendar.model;

import java.util.TimeZone;

import jp.vocalendar.model.EventArrayCursorAdapter.FavoriteToggler;
import android.app.Activity;

/**
 * 常に日付もじれるを表示するイベント一覧表示に使うAdapter
 */
public class EventWithAdditionalDateArrayCursorAdapter extends
		EventArrayCursorAdapter {

	/**
	 * コンストラクタ
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 */
	public EventWithAdditionalDateArrayCursorAdapter(
			Activity context, EventDataBaseRow[] events, TimeZone timeZone, ColorTheme colorTheme,
			FavoriteEventManager favoriteEventManager,
			FavoriteToggler favoriteToggler) {
		super(context, new EventWithAdditionalDateCursor(events, timeZone, context),
				timeZone, colorTheme, favoriteEventManager, favoriteToggler);
	}

	@Override
	public int getItemViewType(int position) {
		int type = super.getItemViewType(position);
		if(type == VIEW_TYPE_EVENT_WITHOUT_DATE_TEXT) {
			return VIEW_TYPE_EVENT_WITH_DATE_TEXT; // 必ず日付文字列を表示する。
		}
		return type;
	}
	
	
}
