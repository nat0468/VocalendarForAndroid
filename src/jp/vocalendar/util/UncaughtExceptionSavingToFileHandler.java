package jp.vocalendar.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.os.Environment;

/**
 * 捕まえられなかった例外をファイル出力するハンドラ
 */
public class UncaughtExceptionSavingToFileHandler extends UncaughtExceptionSavingHandler implements UncaughtExceptionHandler {
	static final String TAG = "UncaughtExceptionSavingToFileHandler";
	
	private static String FILE_NAME = "VocalendarForAndroidExceptionLog.txt";
	private static File EXCEPTION_REPORT_FILE = null;
	static {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		String path = sdcard + File.separator + FILE_NAME;
		EXCEPTION_REPORT_FILE = new File(path);
	}
	    
	/**
	 * コンストラクタ
	 */
	public UncaughtExceptionSavingToFileHandler(Activity activity) {
		super(activity);
	}
	
	@Override
	protected void save(String exceptionReport) throws IOException {		
		PrintWriter writer = new PrintWriter(new FileOutputStream(EXCEPTION_REPORT_FILE));
		writer.write(exceptionReport);
		writer.close();
	}
	
	public static File getExceptionReportFile() {
		return EXCEPTION_REPORT_FILE;
	}

	@Override
	protected boolean isExceptionReportExist() {
		if(EXCEPTION_REPORT_FILE.exists()) {
			return true;
		}
		return false;
	}
	
	@Override
	protected String load() throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(EXCEPTION_REPORT_FILE));
		String line = null;
		while((line = reader.readLine()) != null) {							
			sb.append(line).append('\n');
		}			
		return sb.toString();
	}
	
	/**
	 * ファイルを削除
	 */
	@Override
	public void remove() {
		EXCEPTION_REPORT_FILE.delete();
	}
}
