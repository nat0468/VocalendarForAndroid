package jp.vocalendar.model;

import java.util.Comparator;
import java.util.Date;

/**
 * 見なし開始日時で比較するComparator.
 */
public class DeemedStartDateTimeComparator implements Comparator<FavoriteEventDataBaseRow> {	
	@Override
	public int compare(FavoriteEventDataBaseRow row1, FavoriteEventDataBaseRow row2) {
		Date date1 = row1.getDeemedStartDateTime();
		Date date2 = row2.getDeemedStartDateTime();
		return date1.compareTo(date2);
	}
}
