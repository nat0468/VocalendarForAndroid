package jp.vocalendar.model;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventDataBaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "EventDataBaseHelper";
	
	public static final int DATABASE_VERSION = 10;
	
	public static final String DATABASE_NAME = "EventDataBase";
	public static final String EVENT_TABLE_NAME = "events";

	public static final String COLUMN_INDEX = "idx"; // 表示順をinsert時と同じ順番に維持するためのインデックス 0開始
	public static final String COLUMN_EVENT_INDEX = "next_idx"; // イベントのインデックス。0開始
	public static final String COLUMN_GID = "gid"; // Google Calendarのevent ID
	public static final String COLUMN_GCALENDAR_ID = "gcalendar_id"; // Google Calendarのcalendar ID
	public static final String COLUMN_SUMMARY = "summary";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_START_DATE = "start_date";
	public static final String COLUMN_START_DATE_TIME = "start_date_time";
	public static final String COLUMN_END_DATE = "end_date";
	public static final String COLUMN_END_DATE_TIME = "end_date_time";	
	public static final String COLUMN_RECURSIVE = "recursive";
	public static final String COLUMN_RECURSIVE_BY = "recursive_by";
	public static final String COLUMN_BY_WEEKDAY_OCCURRENCE = "by_weekday_occurence";
	public static final String COLUMN_DISPLAY_DATE = "display_date";
	public static final String COLUMN_ROW_TYPE = "row_type";
	public static final String COLUMN_DAY_KIND = "day_kind";
	
	private static final String CREATE_TABLE_SQL =
			"CREATE TABLE " + EVENT_TABLE_NAME + " (" +
					COLUMN_INDEX + " integer, " +
					COLUMN_EVENT_INDEX + " integer, " +
					COLUMN_GID + " text, " +
					COLUMN_GCALENDAR_ID + " text, " +
					COLUMN_SUMMARY + " text, " +
					COLUMN_DESCRIPTION + " text, " +
					COLUMN_START_DATE + " integer, " +
					COLUMN_START_DATE_TIME + " integer, " +
					COLUMN_END_DATE + " integer, " +
					COLUMN_END_DATE_TIME + " integer, " +
					COLUMN_RECURSIVE + " integer," +
					COLUMN_RECURSIVE_BY + " integer," +
					COLUMN_BY_WEEKDAY_OCCURRENCE + " integer," +
					COLUMN_DISPLAY_DATE + " integer," +
					COLUMN_ROW_TYPE + " integer," +
					COLUMN_DAY_KIND + " integer);";

	private static final String DROP_TABLE_SQL = 
			"DROP TABLE " + EVENT_TABLE_NAME + ";";
	
	/**
	 * コンストラクタ
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public EventDataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate");
		try {
			db.execSQL(CREATE_TABLE_SQL);
		} catch(SQLException ex) {
			Log.e(TAG, "creating table failed: " + ex.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade(oldVer=" + oldVersion + ",newVer=" + newVersion + ")");
		try {
			db.execSQL(DROP_TABLE_SQL);
			db.execSQL(CREATE_TABLE_SQL);
		} catch(SQLException ex) {
			Log.e(TAG, "upgrage table failed: " + ex.getMessage());
		}
	}
}
