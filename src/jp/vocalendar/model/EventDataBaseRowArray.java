package jp.vocalendar.model;

import java.util.Date;

/**
 * イベントデータベースの1行を集めた配列。
 * 日付セパレータや予定なし種別を全て入れたEventDataBaseRow[]と
 * それを取り除いてイベントの入ったものだけを集めたEventDataBaseRow[]を保持する。
 */
public class EventDataBaseRowArray {
	/** 全ての種別を入れた配列 */
	private EventDataBaseRow[] allRows;
	
	/** 通常のイベントのみを集めた配列 */
	private EventDataBaseRow[] normalRows;
	
	/** イベントを読み込んだ最初の日付。イベントの有無は関係無し。空のイベントの場合はnull */
	private Date topDate;
	
	/** イベントを読み込んだ最後の日付。イベントの有無は関係無し。空のイベントの場合はnull */
	private Date lastDate;
	
	/**
	 * 空のイベントデータベースのインスタンスを作る
	 */
	public EventDataBaseRowArray() {
		this.allRows = new EventDataBaseRow[0];
		this.normalRows = new EventDataBaseRow[0];		
		this.topDate = null;
		this.lastDate = null;		
	}
	
	public EventDataBaseRowArray(EventDataBaseRow[] allRows, EventDataBaseRow[] normalRows) {
		this.allRows = allRows;
		this.normalRows = normalRows;
		updateTopDateAndLastDate(allRows);
	}

	private void updateTopDateAndLastDate(EventDataBaseRow[] allRows) {
		for(int i = 0; i < allRows.length; i++) {
			EventDataBaseRow r = allRows[i];
			if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
				if(topDate == null) {
					topDate = r.getDisplayDate();
				}
				lastDate = r.getDisplayDate();				
			}
		}
		
		// 異常時のため、念のため入れておく
		if(topDate == null) {
			topDate = new Date();
		}
		if(lastDate == null) {
			lastDate = new Date();
		}
	}

	public EventDataBaseRow[] getAllRows() {
		return allRows;
	}

	public EventDataBaseRow[] getNormalRows() {
		return normalRows;
	}

	public Date getTopDate() {
		return topDate;
	}

	public Date getLastDate() {
		return lastDate;
	}
}
