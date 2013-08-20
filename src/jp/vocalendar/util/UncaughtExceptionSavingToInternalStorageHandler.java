package jp.vocalendar.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import android.app.Activity;
import android.content.Context;

/**
 * 内部ストレージに例外レポートを格納するハンドラ
 */
public class UncaughtExceptionSavingToInternalStorageHandler extends
		UncaughtExceptionSavingHandler {
	private static final String TAG = "UncaughtExceptionSavingToInternalStorageHandler";	
	private static String FILE_NAME = "ExceptionLog.txt";

	private Context context = null; 
	
	public UncaughtExceptionSavingToInternalStorageHandler(Activity activity) {
		super(activity);
		this.context = activity;
	}
	
	@Override
	protected boolean isExceptionReportExist() {
		try {
			FileInputStream in = context.openFileInput(FILE_NAME);
			in.close();
		} catch(FileNotFoundException e) {
			return false;
		} catch (IOException e) { // ここに来る事はない
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected void save(String exceptionReport) throws IOException {
		FileOutputStream fout = 
				context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
		PrintStream pout = new PrintStream(new BufferedOutputStream(fout));
		pout.print(exceptionReport);
		pout.close();
	}

	@Override
	protected void remove() {
		context.deleteFile(FILE_NAME);
	}

	@Override
	protected String load() throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(context.openFileInput(FILE_NAME)));
		String line = null;
		while((line = reader.readLine()) != null) {							
			sb.append(line).append('\n');
		}
		return sb.toString();
	}
}
