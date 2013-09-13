package jp.vocalendar.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;

import android.app.Activity;
import android.util.Log;

import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.googleapi.OAuthManager;

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
        EventDataBaseRow separator = null; //追加候補の日付セパレータ
        boolean separatorNotAdded = true;
        
        List<EventDataBaseRow> found = new LinkedList<EventDataBaseRow>();
        String q = query.toLowerCase(Locale.US);
        for (int i = 0; i < rows.length; i++) {
        	EventDataBaseRow r = rows[i];
        	if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
        		if(separatorNotAdded) {
        			found.add(rows[i]); // 最初の日付セパレータは検索開始日の印として入れる
        			separatorNotAdded = false;
        		} else {
        			separator = rows[i]; // いったん追加候補に入れる
        		}
        	} else if(r.getRowType() == EventDataBaseRow.TYPE_NORMAL_EVENT) {
        		if(contains(r.getEvent().getSummary(), q) 
        				|| contains(r.getEvent().getDescription(), q)) { // タイトルまたは説明にqueryを含む
        			if(separator != null) {
        				found.add(separator); // 追加候補の日付セパレータを入れてから、イベントを入れる(対応するイベントのない日付セパレータは入れない)
        				separator = null;
        			}
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
