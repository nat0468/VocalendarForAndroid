package jp.vocalendar.task;

import java.util.Date;
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
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;

/**
 * メモリ(VocalendarApplication)に読み込んだイベントから検索するLoadEventTask.
 */
public class SearchLocalEventTask extends LoadEventTask {
	/** 検索開始日 */
	private Date startDate;	
	
	public SearchLocalEventTask(Activity activity, TaskCallback taskCallback, Date startDate) {
		super(activity, taskCallback);
		this.startDate = startDate;
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
        
        List<EventDataBaseRow> found = new LinkedList<EventDataBaseRow>();
        
		// 検索開始日を先頭に追加。
		EventDataBaseRow searchStartDate =
				EventDataBaseRow.makeSearchStartDateRow(0 /* 意味の無い値 */, startDate);
		found.add(searchStartDate);
        
        String q = query.toLowerCase(Locale.US);
        for (int i = 0; i < rows.length; i++) {
        	EventDataBaseRow r = rows[i];
        	if(r.getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
        			separator = rows[i]; // いったん追加候補に入れる
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
