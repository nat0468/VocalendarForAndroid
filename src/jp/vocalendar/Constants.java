package jp.vocalendar;

/**
 * Vocalendarアプリで使う定数を集めたクラス。
 */
public class Constants {
	/**
	 * デバッグモードで実行する場合はtrue(例外レポート機能がオンになる)になるプリファレンス名
	 */
	public static final String DEBUG_MODE_PREF_NAME = "debug_mode";	
	/**
	 * デバッグモードをONにするアクション名
	 */
	public static final String ACTION_DEBUG_MODE_ON = "jp.vocalendar.intent.action.DEBUG_MODE_ON";
	
	/**
	 * デバッグメニューを表示する場合はtrueになるプリファンレンス名
	 */
	public static final String DEBUG_MENU_PREF_NAME = "debug_menu";
	/**
	 * デバッグメニューをONにするアクション名
	 */
	public static final String ACTION_DEBUG_MENU_ON = "jp.vocalendar.intent.action.DEBUG_MENU_ON";
	
	
	
	
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
	   * 検索画面でもっと検索する時に取得するイベント数を格納するプリファレンス名
	   */
	  public static final String NUMBER_OF_EVENTS_TO_SEARCH_MORE_PREFERENCE_NAME = "number_of_events_to_search_more_preference";
	  
	  /**
	   * 画面の色テーマを格納するプリファレンス名
	   */
	  public static final String COLOR_THEME_PREFERENCE_NAME = "color_theme_preference";

	  // カスタムカラーのプリファレンス名
	  public static final String CUSTOM_COLOR_THEME_NAME_PREF_NAME = "custom_color_theme_name_pref";
	  public static final String CUSTOM_COLOR_THEME_DARK_BACKGROUND_PREF_NAME = "custom_color_theme_dark_background_pref";
	  public static final String CUSTOM_COLOR_THEME_DARK_TEXT_COLOR_PREF_NAME = "custom_color_theme_dark_text_color_pref";
	  public static final String CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PREF_NAME = "custom_color_theme_light_background_pref";
	  public static final String CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PRESSED_PREF_NAME = "custom_color_theme_light_background_pressed_pref";
	  public static final String CUSTOM_COLOR_THEME_LIGHT_TEXT_COLOR_PREF_NAME = "custom_color_theme_light_text_color_pref";
	  public static final String CUSTOM_COLOR_THEME_DIVIDER_COLOR_PREF_NAME = "custom_color_theme_divider_color_pref";
	  
	  public static final String CUSTOM_COLOR_THEME_NORMALDAY_BACKGROUND_COLOR_PREF_NAME = "custom_color_theme_normalday_background_color_pref";
	  public static final String CUSTOM_COLOR_THEME_NORMALDAY_TEXT_COLOR_PREF_NAME = "custom_color_theme_normalday_text_color_pref";
	  public static final String CUSTOM_COLOR_THEME_SATURDAY_BACKGROUND_COLOR_PREF_NAME = "custom_color_theme_saturday_background_color_pref";
	  public static final String CUSTOM_COLOR_THEME_SATURDAY_TEXT_COLOR_PREF_NAME = "custom_color_theme_saturday_text_color_pref";
	  public static final String CUSTOM_COLOR_THEME_SUNDAY_BACKGROUND_COLOR_PREF_NAME = "custom_color_theme_sunday_background_color_pref";
	  public static final String CUSTOM_COLOR_THEME_SUNDAY_TEXT_COLOR_PREF_NAME = "custom_color_theme_sunday_text_color_pref";
	  
	  public static final String DEFAULT_THEME_NAME = "THEME_DEFAULT";
	  
	  /** イベント通知時間のプリファレンス名 */
	  public static final String NOTIFICATION_TIME_PREFERENCE_NAME = "notification_time_pref";
	  /** イベント通知時間のデフォルトプリファレンス値 */
	  public static final String DEFAULT_NOTIFICATION_TIME_PREFERENCE_VALUE = "8";
	  public static final int NOT_NOTIFY_PREFERENCE_VALUE = -1;
	  
	  /** 読み込み中画面のお知らせをクリックしたときに開くURLを格納するプリファレンス値。この値がnullなら告知無しを示す。 */
	  public static final String ANNOUNCEMENT_URL_PREFERENCE_NAME = "announcement_url_pref";
	  /** お知らせ表示は一回までの設定を格納するプリファレンス値。この値がtrueならお知らせは一回まで表示 */
	  public static final String ANNOUNCEMENT_ONCE_ONLY ="announcement_once_only";
	  /** お知らせを表示したかどうかを格納するプリファレンス値。この値がfalseなら見表示。trueなら表示済み */
	  public static final String ANNOUNCEMENT_DISPLAYED ="announcement_displayed";	  
	  
	  /** イベント通知機能で★イベントを通知するかどうかを格納するプリファレンス値 */
	  public static final String NOTIFICATE_STAR_EVENT = "notificate_star_event";
	  
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
	  
	  public static final String[] CALENDER_IDS = new String[] { MAIN_CALENDAR_ID, BROADCAST_CALENDAR_ID };
	  
	  /**
	   * ★イベントの「★」の文字
	   */
	  public static final String STAR_EVENT_CHARACTER = "★";
	  
	  /** お知らせのファイルがあるURLパス */
	  public static final String ANNOUNCEMENT_URL = "http://vocalendar.jp/announcement/";
	  
	  /** お知らせのファイルがあるURLパス(デバッグ用) */
	  public static final String ANNOUNCEMENT_URL_DEBUG = "http://vocalendar.jp/announcement/debug_";
	  
	  /** Google Calendar API呼び出し時に指定するアプリケーション名 */
	  public static final String APPLICATION_NAME_FOR_GOOGLE = "Vocalendar-for-Android/1.0";
	  
	  public static final String NUMBER_OF_FAVORITE_EVENT_TO_DISPLAY = "NUMBER_OF_FAVORITE_EVENT_TO_DISPLAU";
}
