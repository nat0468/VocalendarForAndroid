package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class EventDescriptionActivity extends Activity {
	/** Intentに表示するEventを格納するときに使うキー */
	public static final String KEY_EVENT = "event";
	
	/** 表示するイベント */
	private Event event = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_description);
		setTitle(R.string.vocalendar);
		
		event = (Event)getIntent().getSerializableExtra(KEY_EVENT);
		//setTitle(event.getSummary());
		TextView tv = (TextView)findViewById(R.id.descriptionView);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		StringBuilder sb = new StringBuilder();

		sb.append(event.formatDateTime());
		sb.append("<h3>"); sb.append(event.getSummary()); sb.append("</h3>");
		sb.append(changeURItoAnchorTag(event.getDescription()));
		CharSequence html = Html.fromHtml(sb.toString());
		tv.setText(html);
	}
	
	private static String changeURItoAnchorTag(String src) {
		String dest = src.replaceAll("[\n|\r|\f]", "<br/>");
		dest = dest.replaceAll("\\\\n", "<br/>");
		dest =  dest.replaceAll("(https?://[-_.!~*'\\(\\)a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)",
								"<a href=\"$1\">$1</a>");
		return dest;
	}

}
