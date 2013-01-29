package jp.vocalendar.activity;

import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.model.EventDataBaseRow;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EventDescriptionFragment extends Fragment {
    public static final String ARG_EVENT_DATABASE_ROW = "EVENT_DATABASE_ROW";

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
    	Bundle args = getArguments();
    	EventDataBaseRow row =
    			(EventDataBaseRow)args.getSerializable(ARG_EVENT_DATABASE_ROW);    	
        View rootView = inflater.inflate(
                R.layout.event_description_fragment, container, false);
        updateView(rootView, row);
        return rootView;
    }
    
    private void updateView(View view, EventDataBaseRow row) {
		TextView date = (TextView)view.findViewById(R.id.detailDateText);
		date.setText(row.getEvent().formatDateTime(TimeZone.getDefault(), getActivity())); // デフォルトのタイムゾーン
		
		TextView summary = (TextView)view.findViewById(R.id.detailSummaryText);
		summary.setText(row.getEvent().getSummary());
		
		TextView desc = (TextView)view.findViewById(R.id.detailDescriptionText);
		desc.setText(Html.fromHtml(changeURItoAnchorTag(row.getEvent().getDescription())));
		desc.setMovementMethod(LinkMovementMethod.getInstance());    	
    }
    
	private static String changeURItoAnchorTag(String src) {
		if(src == null) {
			return "";
		}
		String dest = src.replaceAll("[\n|\r|\f]", "<br/>");
		dest = dest.replaceAll("\\\\n", "<br/>");
		dest =  dest.replaceAll("(https?://[-_.!~*'\\(\\)a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)",
								"<a href=\"$1\">$1</a>");
		return dest;
	}
	
    
}
