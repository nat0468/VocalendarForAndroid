package jp.vocalendar.model;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import jp.vocalendar.util.DialogUtil;

/**
 * お気に入りイベントの管理クラス。
 * 内部にお気に入りイベントのgcalendarIdとgidの全一覧保持し、それと比較して、お気に入りイベントを判定する。
 */
public class FavoriteEventManager {
	
	/**
	 * GClalendarIdとgidのホルダ。
	 */
	private class GCalendarIdAndGid {
		public GCalendarIdAndGid(EventDataBaseRow r) {
			this(r.getEvent());
		}
		
		public GCalendarIdAndGid(Event e) { 
			this(e.getGCalendarId(), e.getGid());
		}
		
		public GCalendarIdAndGid(String gCalendarId, String gid) {
			this.gCalendarId = gCalendarId;
			this.gid = gid;
		}
		
		public String gCalendarId;
		public String gid;

		@Override
		public int hashCode() {
			return (gCalendarId + ":::" + gid).hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if(o == null) {
				return false;
			}
			if(this == o){
				return true;
			}
			if(o instanceof GCalendarIdAndGid) {
				GCalendarIdAndGid g = (GCalendarIdAndGid)o;
				if(this.gCalendarId.equals(g.gCalendarId) && this.gid.equals(g.gid)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	/**
	 * お気に入りイベントのGCalendarIdAndGidを保持するマップ
	 */
	private Map<GCalendarIdAndGid, GCalendarIdAndGid> favoriteMap =
			new HashMap<FavoriteEventManager.GCalendarIdAndGid, FavoriteEventManager.GCalendarIdAndGid>();

	/**
	 * コンストラクタ
	 */
	public FavoriteEventManager() { }
	
	/**
	 * コンストラクタ。
	 * 読み込んだお気に入り一覧を指定する。
	 * @param favorited
	 */
	public FavoriteEventManager(EventDataBaseRow[] favorites) {
		loadFavoriteEvent(favorites);
	}
	
	/**
	 * 内部に保持するお気に入りイベントを追加する。
	 * @param facorites
	 */
	public void loadFavoriteEvent(EventDataBaseRow[] favorites) {
		for(EventDataBaseRow f : favorites) {
			putFavorite(f.getEvent());
		}
	}
	
	/**
	 * 指定されたイベントの範囲で、対応するお気に入りイベントを内部に保持する(DBから読み込む)
	 * @param rows
	 */
	public void loadFavoriteEventFor(EventDataBaseRow[] rows, EventDataBase db) {
		for(EventDataBaseRow r : rows) {
			Event e = r.getEvent();			
			if(e != null && db.isFavorite(e)) {
				putFavorite(e);
			}
		}
	}
	
	private void putFavorite(Event e) {
		GCalendarIdAndGid g = new GCalendarIdAndGid(
				e.getGCalendarId(), e.getGid());
		favoriteMap.put(g, g);		
	}
	
	/**
	 * 指定されたイベントがお気に入りかどうかを返す。
	 * @param row
	 * @return
	 */
	public boolean isFavorite(EventDataBaseRow row) {
		GCalendarIdAndGid g = new GCalendarIdAndGid(row);
		return favoriteMap.containsKey(g);
	}
	
	/**
	 * 指定された行のイベントのお気に入りを切り替える。
	 * まだお気に入りでなければ追加。お気に入りならば削除する。
	 * @param row お気に入りを切り替える行。
	 */
	public void toggleFavorite(EventDataBaseRow row, Activity context) {
		EventDataBase db = new EventDataBase(context);
		db.open();
		if(isFavorite(row)) {
			if(!removeFavorite(db, row.getEvent())) {
				DialogUtil.openErrorDialog(context, "remove favorite failed", false); // TODO エラー処理
			}
		} else {
			if(!addFavorite(db, row.getEvent())) {
				DialogUtil.openErrorDialog(context, "add favorite failed", false); // TODO エラー処理
			}
		}
		db.close();		
	}	
	
	/**
	 * 指定したイベントをお気に入りに登録する
	 * @param e
	 * @return 登録に成功したらtrue。既に登録済みで登録しなかった場合はfalse 
	 */
	public boolean addFavorite(EventDataBase db, Event event) {
		boolean result = db.addFavorite(event);
		if(result) {
			GCalendarIdAndGid g = new GCalendarIdAndGid(event.getGCalendarId(), event.getGid());
			favoriteMap.put(g, g);
		}
		return result;
	}
	
	public boolean removeFavorite(EventDataBase db, Event event) {
		boolean result = db.removeFavorite(event);
		if(result) {
			GCalendarIdAndGid g = new GCalendarIdAndGid(event.getGCalendarId(), event.getGid());
			favoriteMap.remove(g);
		}
		return result;
	}
}
