package jp.vocalendar;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * ヘルプを開くクラス
 */
public class Help {
	private Help() { } // インスタンス生成禁止
	
	private static final String HELP_URL =
			"http://vocalendar.jp/apps/android/help/help.html";
	
	public static void openHelp(Context context) {
		Log.d("Help", "openHelp() " + HELP_URL);		
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(HELP_URL));
		context.startActivity(i);
	}
	
	
}
