package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.FavoriteEventManager;
import jp.vocalendar.util.DialogUtil;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * イベント一覧に共通の処理を実装したクラス。
 */
public abstract class AbstractEventListActivity extends ActionBarActivity
implements EventArrayCursorAdapter.FavoriteToggler {

	private ListView listView;

	/** イベント一覧表示用のアダプタ */
	protected EventArrayCursorAdapter eventArrayCursorAdapter;
	
	protected FavoriteEventManager favoriteEventManager;
	
	
	/**
	 * 指定された行のイベントのお気に入りを切り替える。
	 * まだお気に入りでなければ追加。お気に入りならば削除する。
	 * @param row お気に入りを切り替える行。このメソッドから戻ったら、お気に入りの状態も更新されている。
	 */
	@Override
	public void toggleFavorite(EventDataBaseRow row) {
		favoriteEventManager.toggleFavorite(row, this);
        eventArrayCursorAdapter.notifyDataSetChanged(); // お気に入り変更の反映       
	}

	@Override
	protected void onResume() {
		super.onResume();
		//イベント詳細画面でお気に入りが変更される場合があるので、その反映
		if(eventArrayCursorAdapter != null) {
			eventArrayCursorAdapter.notifyDataSetChanged();
		}
	}	
	
	public void setSelection(int position) {
		getListView().setSelection(position);
	}

	public ListView getListView() {
		if(listView == null) {
			listView = (ListView)findViewById(R.id.eventList);
		}
		return listView;
	}

	public void setListAdapter(ListAdapter adapter) {
		getListView().setAdapter(adapter);
	}

	public FavoriteEventManager getFavoriteEventManager() {
		return favoriteEventManager;
	}

	public EventArrayCursorAdapter getEventArrayCursorAdapter() {
		return eventArrayCursorAdapter;
	}	
}
