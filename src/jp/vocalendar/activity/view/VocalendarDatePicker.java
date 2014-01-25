package jp.vocalendar.activity.view;

import java.util.Calendar;

import jp.vocalendar.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

/**
 * 日付選択ダイアログ
 */
public class VocalendarDatePicker {
	private Activity activity;
	private OkOnClickListener okOnClickListener;
	private DatePicker datePicker = null;	
	private Dialog datePickerDialog = null;

	/**
	 * OKボタンを押したときに呼ばれるリスナ
	 */
	public static interface OkOnClickListener {
		public void onClick(int year, int month, int dayOfMonth);
	}
	
	/**
	 * コンストラクタ
	 * @param activity
	 * @param okOnClickListener ダイアログでOKボタンを押したときの処理
	 */
	public VocalendarDatePicker(
			Activity activity,
			OkOnClickListener okOnClickListener) {
		this.activity = activity;
		this.okOnClickListener = okOnClickListener;
	}
	
	/**
	 * 日付選択ダイアログを表示する。
	 * @param activity
	 * @param initialDate 初期表示する日付
	 * @param okOnClickListener
	 */
	public void show(Calendar initialDate) {
		if(datePickerDialog == null || datePicker == null) {			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			LayoutInflater inflater = activity.getLayoutInflater();
			View v = inflater.inflate(R.layout.date_picker_dialog, null);
			datePicker = (DatePicker)v.findViewById(R.id.datePicker);
			Button todayButton = (Button)v.findViewById(R.id.setTodayButton);
			todayButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					updateToToday();
				}
			});
			builder.setView(v)
				   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {					
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   okOnClickListener.onClick(
								   datePicker.getYear(),
								   datePicker.getMonth(), 
								   datePicker.getDayOfMonth());						   
					   }
				   })
				   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {				
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   dialog.cancel();
					   }
				    });
			datePickerDialog = builder.create();
		}		
		datePicker.updateDate(
				initialDate.get(Calendar.YEAR),
				initialDate.get(Calendar.MONTH),
				initialDate.get(Calendar.DATE));
		datePickerDialog.show();
	}

	private void updateToToday() {
		Calendar today = Calendar.getInstance();
		datePicker.updateDate(
			today.get(Calendar.YEAR),
			today.get(Calendar.MONTH),
			today.get(Calendar.DATE));
	}
}
