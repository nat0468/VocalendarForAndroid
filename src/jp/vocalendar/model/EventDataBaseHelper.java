package jp.vocalendar.model;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventDataBaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "EventDataBaseHelper";
	
	public static final int DATABASE_VERSION = 2;
	
	public static final String DATABASE_NAME = "EventDataBase";
	public static final String EVENT_TABLE_NAME = "events";

	public static final String COLUMN_SUMMARY = "summary";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_START_DATE = "start_date";
	public static final String COLUMN_START_DATE_TIME = "start_date_time";
	public static final String COLUMN_END_DATE = "end_date";
	public static final String COLUMN_END_DATE_TIME = "end_date_time";	
	public static final String COLUMN_START_DATE_INDEX = "start_date_index";	
	public static final String COLUMN_END_DATE_INDEX = "end_date_index";
	public static final String COLUMN_RECURSIVE = "recursive";
	public static final String COLUMN_RECURSIVE_BY = "recursive_by";
	public static final String COLUMN_BY_WEEKDAY_OCCURRENCE = "by_weekday_occurence";
	
	private static final String CREATE_TABLE_SQL =
			"CREATE TABLE " + EVENT_TABLE_NAME + " (" +
					COLUMN_SUMMARY + " text, " +
					COLUMN_DESCRIPTION + " text, " +
					COLUMN_START_DATE + " integer, " +
					COLUMN_START_DATE_TIME + " integer, " +
					COLUMN_END_DATE + " integer, " +
					COLUMN_END_DATE_TIME + " integer, " +
					COLUMN_START_DATE_INDEX + " integer, " +
					COLUMN_END_DATE_INDEX + " integer," +
					COLUMN_RECURSIVE + " integer," +
					COLUMN_RECURSIVE_BY + " integer," +
					COLUMN_BY_WEEKDAY_OCCURRENCE + " integer);";

	private static final String[] UPGRADE_TABLE_VER1_TO_VER2_SQLS = {
			"ALTER TABLE " + EVENT_TABLE_NAME + " ADD COLUMN " + COLUMN_RECURSIVE + " integer;",
			"ALTER TABLE " + EVENT_TABLE_NAME + " ADD COLUMN " + COLUMN_RECURSIVE_BY + " integer;",
			"ALTER TABLE " + EVENT_TABLE_NAME + " ADD COLUMN " + COLUMN_BY_WEEKDAY_OCCURRENCE + " integer;"
	};
	
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
		if(oldVersion == (DATABASE_VERSION - 1) && newVersion == DATABASE_VERSION) {
			try {
				for(String sql : UPGRADE_TABLE_VER1_TO_VER2_SQLS) {
					Log.d(TAG, "execSQL:" + sql);
					db.execSQL(sql);					
				}
			} catch(SQLException ex) {
				Log.e(TAG, "upgrage table failed: " + ex.getMessage());
			}
		}
	}

}
