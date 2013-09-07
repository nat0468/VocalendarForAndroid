package jp.vocalendar.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;

import jp.vocalendar.VocalendarApplication;

/**
 * メモリ(VocalendarApplication)に読み込んだイベントから検索するLoadEventTask.
 */
public class SearchLocalEventTask extends LoadEventTask {
	
	public SearchLocalEventTask(Activity activity, TaskCallback taskCallback) {
		super(activity, taskCallback);
	}
	
	@Override
	protected List<EventDataBaseRow> doInBackground(String... query) {
		return searchBySummaryWithSeparator(query[0]);
	}

	private List<EventDataBaseRow> searchBySummaryWithSeparator(String query) {
        VocalendarApplication app = (VocalendarApplication)activity.getApplication();
        EventDataBaseRowArray eventDataBaseRowArray = app.getEventDataBaseRowArray();
        EventDataBaseRow[] rows = eventDataBaseRowArray.getAllRows();
        
        List<EventDataBaseRow> found = new LinkedList<EventDataBaseRow>();
        String q = query.toLowerCase(Locale.US);
        for (int i = 0; i < rows.length; i++) {
        	EventDataBaseRow r = rows[i];
        	if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
        		found.add(rows[i]);
        	} else if(r.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
        		if(contains(r.getEvent().getSummary(), q) 
        				|| contains(r.getEvent().getDescription(), q)) { // タイトルまたは説明にqueryを含む
        			found.add(rows[i]);
        		}
        	}
        }
        return found;
	}
	
	/**
	 * strにqueryが含まれるか判定する。含まれるならtrueを返す。strは小文字に変換して比較する。
	 * strはnullも可。nullの場合は必ずfalseを返す。
	 * @param str
	 * @param query
	 * @return
	 */
	private boolean contains(String str, String query) {
		if(str == null) {
			return false;
		}
		return str.toLowerCase(Locale.US).contains(query);
	}

}
