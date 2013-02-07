package jp.vocalendar.activity;

import jp.vocalendar.R;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setTitle(getString(R.string.about_vocalendar_app));
		TextView tv = (TextView)findViewById(R.id.aboutTextView);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		
		String version = "?.?";
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
			Log.e("AboutActivity", "getPackageInfo() failed.");
			e.printStackTrace();
		}
		String htmlSrc = String.format(
				getString(R.string.about_message), version);
		CharSequence html = Html.fromHtml(htmlSrc);
		tv.setText(html);

		TextView tv2 = (TextView)findViewById(R.id.aboutTextView2); 
		tv2.setMovementMethod(LinkMovementMethod.getInstance());
		tv2.setText(Html.fromHtml(getString(R.string.about_message2)));
	}
}
