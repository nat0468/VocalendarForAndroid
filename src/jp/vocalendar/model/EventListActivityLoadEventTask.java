package jp.vocalendar.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import jp.vocalendar.activity.EventListActivity;
import jp.vocalendar.googleapi.OAuthManager;

/**
 * EventListActivity中にイベントを読み込むときに使うTask
 */
public class EventListActivityLoadEventTask extends GoogleCalendarLoadEventTask {
	private static final String TAG = "EventListActivityLoadEventTask";
	
	/** イベント読み込み結果を格納する */
	private EventDataBaseRowArray eventDataBaseRowArray = null;
	
	/** 表示中のイベントを持つカーソル */
	private EventArrayCursor eventArrayCursor = null;
	
	/** もっと読み込む方向を示す。trueなら前のイベント、falseなら後のイベントを読み込む。 */
	private boolean loadMorePreviousEvent = true;
	
    /**
     * コンストラクタ。
     * @param activity このタスクを実行するActivity
     * @param taskCallback イベント読み込み終了時にコールバックする
     * @param loadMorePreviousEvent もっと読み込む方向を示す。trueなら前のイベント、falseなら後のイベントを読み込む
     */
    public EventListActivityLoadEventTask(
    		EventListActivity activity, TaskCallback taskCallback, boolean loadMorePreviousEvent) {
        super(activity, taskCallback);
        this.eventArrayCursor = activity.getEventArrayCursor();
        this.loadMorePreviousEvent = loadMorePreviousEvent;
    }

	@Override
	/**
	 * 追加で読み込んだイベントを返す。全イベントはeventDataBaseRowArrayに格納する。
	 */
	protected List<EventDataBaseRow> doInBackground(String... ids) {
		if(OAuthManager.getInstance().getAccount() == null) {
			initAccount(); // アカウント情報未設定の場合(まだ一回もイベント読み込みしていない場合)は初期化
		}		
		List<EventDataBaseRow> result = super.doInBackground(ids);
		if(result == null) {
			return null; // 読み込みに失敗したので、何もせずに返る
		}
		
		List<EventDataBaseRow> allRows = null;
		if(loadMorePreviousEvent) {
			allRows = eventArrayCursor.getInsertedEventDataBaseRows(
						result.toArray(new EventDataBaseRow[result.size()]));
		} else {
			allRows = eventArrayCursor.getAppendedEventDataBaseRows(
						result.toArray(new EventDataBaseRow[result.size()]));
		}
				
		List<EventDataBaseRow> normalRows = new LinkedList<EventDataBaseRow>();
		Iterator<EventDataBaseRow> itr = allRows.iterator();
		while(itr.hasNext()) {
			EventDataBaseRow e = itr.next();
			if(e.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
				normalRows.add(e);
			}
		}
		eventDataBaseRowArray = new EventDataBaseRowArray(
				allRows.toArray(new EventDataBaseRow[allRows.size()]), 
				normalRows.toArray(new EventDataBaseRow[normalRows.size()]));
				
		return result;
	}
    
	public EventDataBaseRowArray getEventDataBaseRowArray() {
		return eventDataBaseRowArray;
	}
	
}
