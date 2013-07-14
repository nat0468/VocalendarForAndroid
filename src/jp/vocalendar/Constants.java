package jp.vocalendar;

/**
 * Vocalendarアプリで使う定数を集めたクラス。
 */
public class Constants {
	  /**
	   * API操作に使うアカウント種別
	   */
	  public static final String ACCOUNT_TYPE = "com.google";

	  /**
	   * 認証範囲。
	   */
	  public static final String OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/calendar.readonly";	  
	  
	  /**
	   * ユーザーが選択したアカウントを格納するプリファレンス名
	   */
	  public static final String SELECTED_ACCOUNT_PREFERENECE_NAME = "selected_account_preference";
	  
	  /**
	   * 最後にイベント情報を取得している時間を格納するプリファレンス名
	   */
	  public static final String LAST_UPDATED_PREFERENCE_NAME = "last_updated_preference";
	  
	  /**
	   * イベント情報を取得する日数を格納するプリファレンス名
	   */
	  public static final String NUMBER_OF_DATE_TO_GET_EVENTS_PREFERENCE_NAME = "number_of_date_to_get_events_preference";
	  
	  /**
	   * イベント情報の読み込み画面の設定を格納するプリファレンス名
	   */
	  public static final String LOADING_PAGE_PREFERENCE_NAME = "loading_page_preference";
	  
	  /**
	   * イベント情報の読み込み画面の設定値：壁紙
	   */
	  public static final String LOADING_PAGE_WALLPAPER = "LOADING_PAGE_WALLPAPER";

	  /**
	   * イベント情報の読み込み画面の設定値：ドット絵アニメーション
	   */
	  public static final String LOADING_PAGE_DOT_ANIMATION = "LOADING_PAGE_DOT_ANIMATION";

	  /**
	   * イベント情報の読み込み画面の設定値：ランダム
	   */
	  public static final String LOADING_PAGE_RANDOM = "LOADING_PAGE_RANDOM";

	  /**
	   * イベント情報の読み込み画面の設定値：表示なし
	   */
	  public static final String LOADING_PAGE_NONE = "LOADING_PAGE_NONE";
	  
	  /**
	   * イベント一覧画面でもっと読み込む時に取得するイベント数を格納するプリファレンス名
	   */
	  public static final String NUMBER_OF_DATE_TO_LOAD_MORE_EVENTS_PREFRENCE_NAME = "number_of_date_to_load_more_events_preference";

	  /**
	   * タップ無しで自動でもっとイベントを読み込むかどうかを格納するプリファレンス名
	   */
	  public static final String LOAD_MORE_EVENT_WITHOUT_TAP = "load_more_event_without_tap";
	  
	  
	  /**
	   * onActivityResult request codes:
	   */
	  public static final int GET_LOGIN = 0;
	  //public static final int AUTHENTICATED = 1;
	  //public static final int CREATE_EVENT = 2;
	  
	  /**
	   * VOCALENDAR メイン のID
	   */
	  public static final String MAIN_CALENDAR_ID = "0mprpb041vjq02lk80vtu6ajgo@group.calendar.google.com";   
	  
	  /**
	   * VOCALENAR 放送系 のID
	   */
	  public static final String BROADCAST_CALENDAR_ID = "5fsoru1dfaga56mcleu5mp76kk@group.calendar.google.com";
	  
}
