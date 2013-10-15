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
		openErrorDialog(activity, errorMessage, true);
	}

	/**
	 * エラーダイアログを表示する。このダイアログを閉じると、Activityを終了する。
	 * @param activity
	 * @param finishActivity trueの場合、ダイアログを閉じたときにActivityを閉じる
	 * @param errorMessage
	 */	
	public static void openErrorDialog(final Activity activity, String errorMessage, boolean finishActivity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(errorMessage);
		if(finishActivity) {
			builder.setPositiveButton(
			    		   activity.getText(R.string.ok),
			    		   new DialogInterface.OnClickListener() {
			    			   public void onClick(DialogInterface dialog, int id) {
			    				   dialog.cancel();
			    				   activity.finish();
			    			   }
			    		   });
		} else {
			builder.setPositiveButton(
		    		   activity.getText(R.string.ok),
		    		   new DialogInterface.OnClickListener() {
		    			   public void onClick(DialogInterface dialog, int id) {
		    				   dialog.cancel();
		    			   }
		    		   });			
		}
		AlertDialog alert = builder.create();		
		alert.show();
	}


	/**
	 * メッセージダイアログを表示する。
	 * @param activity
	 * @param errorMessage
	 * @param finishActivity trueの場合、ダイアログを閉じたときにActivityを閉じる
	 */	
	public static void openMessageDialog(final Activity activity, String errorMessage, boolean finishActivity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.message)
			   .setMessage(errorMessage);
		if(finishActivity) {
			builder.setPositiveButton(
		    		   activity.getText(R.string.ok),
		    		   new DialogInterface.OnClickListener() {
		    			   public void onClick(DialogInterface dialog, int id) {
		    				   dialog.cancel();
		    				   activity.finish();
		    			   }
		    		   });
		} else {
			builder.setPositiveButton(
		    		   activity.getText(R.string.ok),
		    		   new DialogInterface.OnClickListener() {
		    			   public void onClick(DialogInterface dialog, int id) {
		    				   dialog.cancel();
		    			   }
		    		   });			
		}
		AlertDialog alert = builder.create();
		alert.show();
	}

}
