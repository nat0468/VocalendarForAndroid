package jp.vocalendar.activity;

import jp.vocalendar.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setTitle(getString(R.string.about_vocalendar_app));
		TextView tv = (TextView)findViewById(R.id.aboutTextView);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		CharSequence html = Html.fromHtml(getString(R.string.about_message));
		tv.setText(html);
	}	
}
