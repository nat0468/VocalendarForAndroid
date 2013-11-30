package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * お気に入りイベント用のEventDataBaseRow
 * 見なし開始日時のキャッシュを保有する。
 */
public class FavoriteEventDataBaseRow extends EventDataBaseRow {
	private static final long serialVersionUID = -5954922311892704741L;

	private Date deemedStartDateTime;
	
	public FavoriteEventDataBaseRow(Event event, Calendar today, TimeZone timeZone) {
		super(event, Integer.MIN_VALUE, Integer.MIN_VALUE, null, calcDayKind(event, today, timeZone));
		this.deemedStartDateTime = event.getDeemedStartDateTime(today, timeZone);
	}
	
	private static int calcDayKind(Event event, Calendar today, TimeZone timeZone) { 
		return EventDataBaseRow.calcDayKind(event.getNotNullStartDate(), timeZone);			
	}

	public Date getDeemedStartDateTime() {
		return deemedStartDateTime;
	}
}
