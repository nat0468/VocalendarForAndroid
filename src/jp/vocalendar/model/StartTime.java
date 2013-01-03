package jp.vocalendar.model;

import jp.vocalendar.util.DateUtil;

/**
 * イベント開始時間を表すクラス
 */
public class StartTime {
	/**
	 * 開始時間。
	 * 終日イベントの場合はnull。
	 * また前日からの継続イベントの場合は、終了時間が入る。
	 */
	private HourAndMinute hourAndMinute = null;
	
	/**
	 * 前日からの継続イベントの場合にtrue。
	 */
	private boolean continuesFromYesterday = false;

	/**
	 * コンストラクタ
	 * @param hourAndMinute
	 * @param continuesFromYesterday
	 */
	public StartTime(HourAndMinute hourAndMinute, boolean continuesFromYesterday) {
		this.hourAndMinute = hourAndMinute;
		this.continuesFromYesterday = continuesFromYesterday;
	}

	/**
	 * コンストラクタ
	 * @param hour
	 * @param minute
	 * @param continuesFromYesterday
	 */
	public StartTime(int hour, int minute, boolean continuesFromYesterday) {
		this.hourAndMinute =  new HourAndMinute(hour, minute);
		this.continuesFromYesterday = continuesFromYesterday;
	}


	/**
	 * デフォルトコンストラクタ。
	 * 終日イベントを表すインスタンスを生成。
	 * 
	 */
	public StartTime() { }		
	
	public HourAndMinute getHourAndMinute() {
		return hourAndMinute;
	}

	public void setHourAndMinute(HourAndMinute hourAndMinute) {
		this.hourAndMinute = hourAndMinute;
	}

	public boolean isContinuesFromYesterday() {
		return continuesFromYesterday;
	}

	public void setContinuesFromYesterday(boolean continuesFromYesterday) {
		this.continuesFromYesterday = continuesFromYesterday;
	}
	
	/**
	 * 終日イベントの場合にtrueを返す。
	 * @return
	 */
	public boolean isDateEvent() {
		if(hourAndMinute == null) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(continuesFromYesterday) {
			sb.append(DateUtil.STR_TO);
		}
		if(hourAndMinute == null) {
			sb.append(DateUtil.STR_ALL_DAY);
		} else {
			sb.append(hourAndMinute.toString());
		}
		return sb.toString();
	}
	
	/**
	 * ソート用のインデックス値を返す。
	 * 終日 < 前日からの継続 < 通常イベント の順になるように値を返す。
	 * @return
	 */
	public int getIndex() {
		if(isDateEvent()) {
			return 0;
		}
		if(continuesFromYesterday) {
			return hourAndMinute.getIndex();
		}
		return 60 * 24 + hourAndMinute.getIndex();
	}
}
