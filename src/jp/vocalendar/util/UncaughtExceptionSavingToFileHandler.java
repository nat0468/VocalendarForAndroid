package jp.vocalendar.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import jp.vocalendar.BuildConfig;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/**
 * 捕まえられなかった例外をファイル出力するハンドラ
 */
public class UncaughtExceptionSavingToFileHandler implements UncaughtExceptionHandler {
	private static final String TAG = "UncaughtExceptionSavingToFileHandler";
	
	private static String FILE_NAME = "VocalendarForAndroidExceptionLog.txt";
	private static File EXCEPTION_REPORT_FILE = null;
	static {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		String path = sdcard + File.separator + FILE_NAME;
		EXCEPTION_REPORT_FILE = new File(path);
	}
	
	/**
	 * この例外ハンドラのシングルトン。
	 */
	private static UncaughtExceptionSavingToFileHandler s_handlder = null;
	
	private UncaughtExceptionHandler defaultHandler;
	private PackageInfo packageInfo;
	
	/**
	 * この例外ハンドラが未設定であれば、設定する。
	 * @context getApplicationContext()を格納する
	 */
    public static void setHandlerIfNotSet(Context context) {
    	if(isDebuggable()) {
    		UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
    		if(handler != getUncaughtExceptionSavingToFileHandler(context)) {
    			//未設定のため、設定する
        		Thread.setDefaultUncaughtExceptionHandler(
        				getUncaughtExceptionSavingToFileHandler(context));
    		}
    	}
    }
    
    private static boolean isDebuggable() {
    	return BuildConfig.DEBUG;
    }
    
	public static UncaughtExceptionSavingToFileHandler
	getUncaughtExceptionSavingToFileHandler(Context context) {
		if(s_handlder == null) {
			s_handlder = new UncaughtExceptionSavingToFileHandler(context);
		}
		return s_handlder;
	}
	
	private UncaughtExceptionSavingToFileHandler(Context context) {
		try {
			packageInfo =
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch(NameNotFoundException e) {
			e.printStackTrace();
		}
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.d(TAG, "uncaughtException! " + ex.toString());
		try {
			save(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		defaultHandler.uncaughtException(thread, ex);
	}

	private void save(Throwable ex) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new FileOutputStream(EXCEPTION_REPORT_FILE));		
		String msg = makeMessage(ex);
		writer.println(msg);
		ex.printStackTrace(writer);
		writer.close();
	}
	
	private String makeMessage(Throwable ex) {
		StringBuilder sb = new StringBuilder();
		sb.append("time=");
		sb.append(new Date().toString());
		sb.append("\nversionName=");
		sb.append(packageInfo.versionName);
		sb.append("\nDEVICE=");
		sb.append(Build.DEVICE);
		sb.append("\nMODEL=");
		sb.append(Build.MODEL);
		sb.append("\nSDK=");
		sb.append(Build.VERSION.SDK_INT);
		sb.append("\nThrowable=");
		sb.append(ex.toString());
		return sb.toString();
	}
	
	public static File getExceptionReportFile() {
		return EXCEPTION_REPORT_FILE;
	}

	/**
	 * 例外レポートがあればレポート表示アクティビティを開く
	 * @param context
	 */
	public static void startExceptionReportActivityIfAvailable(Context context) {
		if(!EXCEPTION_REPORT_FILE.exists()) {
			return;
		}
		Intent i = new Intent(context, ExceptionReportActivity.class);
		context.startActivity(i);
	}
	
	public static String loadExceptionReportFile() {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(EXCEPTION_REPORT_FILE));
			String line = null;
			while((line = reader.readLine()) != null) {							
				sb.append(line).append('\n');
			}			
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}
	
	public static void removeReportFile() {
		EXCEPTION_REPORT_FILE.delete();
	}
}
