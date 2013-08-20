package jp.vocalendar.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import jp.vocalendar.BuildConfig;
import jp.vocalendar.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

/**
 * 捕まえられなかった例外を記録するハンドラの抽象親クラス
 */
public abstract class UncaughtExceptionSavingHandler implements UncaughtExceptionHandler {
	private static final String TAG = "UncaughtExceptionSavingHandler";
	
	/** デフォルトで使用する例外ハンドラクラス */
	private static final Class<? extends UncaughtExceptionSavingHandler>
	defaultHandlerClass = UncaughtExceptionSavingToInternalStorageHandler.class;
	
	/**
	 * この例外ハンドラのシングルトン。
	 */
	protected static UncaughtExceptionSavingHandler s_handlder = null;
	
	/**
	 * 例外ハンドラの初期設定。Activity#onCreate()の最初(例外が発生しうるコードの前)で呼び出す
	 * @param activity
	 */
	public static void init(Activity activity) {
        UncaughtExceptionSavingHandler.setHandlerIfNotSet(activity);
        UncaughtExceptionSavingHandler.startExceptionReportActivityIfAvailable(activity);		
	}
	
	/**
	 * 例外ハンドラが未設定であれば、設定する。
	 * @param activity
	 * @param clazz 生成する例外ハンドラ
	 */
    public static void setHandlerIfNotSet(Activity activity) {
    	if(isDebuggable()) { 
    		UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
    		if(handler == null || !(handler instanceof UncaughtExceptionSavingHandler)) {    			
    			//未設定、もしくは異なる例外ハンドラが設定されている場合は、設定する
        		Thread.setDefaultUncaughtExceptionHandler(
        				getUncaughtExceptionSavingHandler(activity));
    		}
    	}
    }
    
    private static boolean isDebuggable() {
    	return Constants.DEBUG_MODE;
    }    
	
	public static UncaughtExceptionSavingHandler getUncaughtExceptionSavingHandler(Activity activity) {
		if(s_handlder == null) {
			try {
				s_handlder = defaultHandlerClass.getConstructor(new Class<?>[]{Activity.class}).newInstance(activity);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return s_handlder;
	}
	
	/**
	 * 例外レポートがあればレポート表示アクティビティを開く
	 * @param context
	 */
	public static void startExceptionReportActivityIfAvailable(Activity activity) {
		if(isExceptionReportExist(activity)) {
			Intent i = new Intent(activity, ExceptionReportActivity.class);
			activity.startActivity(i);
		}
	}

	private static boolean isExceptionReportExist(Activity activity) {
		UncaughtExceptionSavingHandler handler =
				getUncaughtExceptionSavingHandler(activity);
		return handler.isExceptionReportExist();
	}
	
	/**
	 * 前回実行時に出力された例外レポートの有無を返す
	 * @return
	 */
	protected abstract boolean isExceptionReportExist();
	
	protected UncaughtExceptionHandler defaultHandler;
	protected PackageInfo packageInfo;

	/**
	 * サブクラスに共通のコンストラクタ
	 * @param activity
	 */
	protected UncaughtExceptionSavingHandler(Activity activity) {
		Context context = activity.getApplicationContext();
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
			save(makeExceptionReport(ex));
		} catch (Exception e) {
			e.printStackTrace();
		}
		defaultHandler.uncaughtException(thread, ex);
	}

	protected String makeExceptionReport(Throwable ex) {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);

		w.print("time=");
		w.print(new Date().toString());
		w.print("\nversionName=");
		w.print(packageInfo.versionName);
		w.print("\nDEVICE=");
		w.print(Build.DEVICE);
		w.print("\nMODEL=");
		w.print(Build.MODEL);
		w.print("\nSDK=");
		w.print(Build.VERSION.SDK_INT);
		w.print("\nThrowable=");
		w.print(ex.toString());		
		w.println();
		
		ex.printStackTrace(w);
		String msg = sw.toString();
		w.close();
		
		return msg;
	}

	/**
	 * 例外レポートを保存する。サブクラスで実装する。
	 * @param exceptionReport
	 * @throws IOException
	 */
	protected abstract void save(String exceptionReport) throws IOException;

	/**
	 * 例外レポートを削除する。
	 */
	public static void removeReportFile(Activity activity) {
		UncaughtExceptionSavingHandler handler = getUncaughtExceptionSavingHandler(activity);
		handler.remove();
	}
	
	/**
	 * 例外レポートを削除する
	 */
	protected abstract void remove();
	
	/**
	 * 例外レポートの内容を出力する
	 * @return
	 */
	public static String loadExceptionReportFile(Activity activity) throws IOException {
		UncaughtExceptionSavingHandler handler = getUncaughtExceptionSavingHandler(activity);
		return handler.load();
	}
	
	/**
	 * 例外レポートの内容を出力する
	 * @return
	 */
	protected abstract String load() throws IOException;
}