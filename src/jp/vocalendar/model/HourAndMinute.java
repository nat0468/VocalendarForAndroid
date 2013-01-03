package jp.vocalendar.model;

/**
 * 時間(0〜23)と分(0〜59)を表すクラス
 */
public class HourAndMinute {
	private int hour;
	private int minute;
	
	public HourAndMinute(int hour, int minute) {
		super();
		this.hour = hour;
		this.minute = minute;
	}
	
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	
	/**
	 * ソート用のインデックス値。(hour * 60 + minute)の値を返す。	 * 
	 */
	public int getIndex() {
		return hour * 60 + minute;
	}
	
	
	public String toString() {
		return String.format("%d:%02d", hour, minute);
	}
}
