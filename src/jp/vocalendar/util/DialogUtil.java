package jp.vocalendar.util;

import jp.vocalendar.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.drm.DrmStore.Action;

public class DialogUtil {
	public static void openNotImplementedDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getText(R.string.not_implemented) )
		       .setPositiveButton(
		    		   context.getText(R.string.ok),
		    		   new DialogInterface.OnClickListener() {
		    			   public void onClick(DialogInterface dialog, int id) {
		    				   dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();		
		alert.show();
	}
	
	/**
	 * エラーダイアログを表示する。このダイアログを閉じると、Activityを終了する。
	 * @param activity
	 * @param errorMessage
	 */
	
	public static void openErrorDialog(final Activity activity, String errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(errorMessage)
		       .setPositiveButton(
		    		   activity.getText(R.string.ok),
		    		   new DialogInterface.OnClickListener() {
		    			   public void onClick(DialogInterface dialog, int id) {
		    				   dialog.cancel();
		    				   activity.finish();
		    			   }
		    		   });
		AlertDialog alert = builder.create();		
		alert.show();
	}
}
