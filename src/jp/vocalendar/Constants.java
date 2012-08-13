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
