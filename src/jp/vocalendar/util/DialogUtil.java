package jp.vocalendar.util;

import jp.vocalendar.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

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
}
