package jp.vocalendar.util;

import jp.vocalendar.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 例外レポートを表示する画面
 */
public class ExceptionReportActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.exception_report);
	    
	    EditText text = (EditText)findViewById(R.id.reportText);
	    text.setText(UncaughtExceptionSavingToFileHandler.loadExceptionReportFile());
	    
	    Button b = (Button)findViewById(R.id.backButton);
	    b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UncaughtExceptionSavingToFileHandler.removeReportFile();
				finish();
			}
		});
	}

}
