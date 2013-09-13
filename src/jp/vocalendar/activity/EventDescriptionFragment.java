package jp.vocalendar.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.ColorTheme;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventDescriptionFragment extends Fragment {
    public static final String ARG_EVENT_DATABASE_ROW = "EVENT_DATABASE_ROW";

    private Context context;
    private ColorTheme themeColor;
    
    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
    	Bundle args = getArguments();
    	EventDataBaseRow row =
    			(EventDataBaseRow)args.getSerializable(ARG_EVENT_DATABASE_ROW);    	
        View rootView = inflater.inflate(
                R.layout.event_description_fragment, container, false); // TODO 
        updateView(rootView, row);
        return rootView;
    }
    
    private void updateView(View view, EventDataBaseRow row) {    	
    	
		TextView date = (TextView)view.findViewById(R.id.detailDateText);
		date.setText(row.getEvent().formatDateTime(TimeZone.getDefault(), getActivity())); // デフォルトのタイムゾーン
		date.setBackgroundColor(themeColor.getDarkBackgroundColor());
		date.setTextColor(themeColor.getDarkTextColor());
		
		TextView summary = (TextView)view.findViewById(R.id.detailSummaryText);
		summary.setText(row.getEvent().getSummary());
		
		String l = row.getEvent().getLocation();
		TextView location = (TextView)view.findViewById(R.id.locationTextView);
		if(l != null) {
			if(l.startsWith("http://") || l.startsWith("https://")) { // 場所がURLの場合
				location.setText(makeAnchorHtml(l));
			} else { // 場所が地名の場合
				location.setText(makeGoogleMapAnchorHtml(l));
			}
			location.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			LinearLayout edfl = (LinearLayout)view.findViewById(R.id.eventDescriptionFragmentLayout);
			LinearLayout lll = (LinearLayout)view.findViewById(R.id.locationLinearLayout);
			edfl.removeView(lll);
		}
		
		TextView desc = (TextView)view.findViewById(R.id.detailDescriptionText);
		if(desc != null) {
			desc.setText(Html.fromHtml(changeURItoAnchorTag(row.getEvent().getDescription())));
			desc.setMovementMethod(LinkMovementMethod.getInstance());
		}

		WebView wv = (WebView)view.findViewById(R.id.detailDescriptionWebView);
		if(wv != null) {
			wv.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage cm) {
					Log.d("DescriptionWebView", cm.message() + " line:" + cm.lineNumber() + " of " + cm.sourceId());
					return true;
				}				
			});
			
			StringBuilder sb = new StringBuilder();
			sb.append(makeDescriptionHtmlHeader());
			sb.append(changeURItoAnchorTag(row.getEvent().getDescription()));
			sb.append(makeSNSShareTag());
			sb.append(makeDescriptionHtmlFooter());
			wv.loadData(sb.toString(), "text/html; charset=utf-8", "utf-8");
		}
    }
    
    private static String makeDescriptionHtmlHeader() {
    	return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body style=\"background: #f0f0f0\">";
    }
    
    private static String makeDescriptionHtmlFooter() {
    	return "</body></html>";
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
	
	private static String makeSNSShareTag() {
		return "<br/><a href=\"https://twitter.com/share\" class=\"twitter-share-button\" data-lang=\"ja\" data-count=\"none\">ツイート</a>"
				+ "<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p='http';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script>";		
	}
    
	/**
	 * 指定されたURLへのアンカーHTMLタグを作成する
	 * @param link
	 * @return
	 */
	private static Spanned makeAnchorHtml(String link) {
		StringBuilder a = new StringBuilder();
		a.append("<a href=\"");
		a.append(link);
		a.append("\">");
		a.append(link);
		a.append("</a>");
		return Html.fromHtml(a.toString());		
	}
	
	/**
	 * Google Mapを起動して、指定された地名を検索するアンカーHTMLタグを作成する。
	 * @param location
	 * @return
	 */
	private Spanned makeGoogleMapAnchorHtml(String location) {
		StringBuilder a = new StringBuilder();
		a.append(location);
		// a.append(" (<a href=\"geo:35.684263, 139.748161?q="); // 皇居の位置を中心に検索
		a.append(" (<a href=\"geo:0,0?q="); // 皇居の位置を中心に検索
		try {
			a.append(URLEncoder.encode(location, "UTF-8")); // TODO 懸案：UTF-8固定で大丈夫？
		} catch (UnsupportedEncodingException e) {
			// ここに来る事はまず無い
			e.printStackTrace();
			a.append(e.getMessage());
		}
		a.append("\">");
		a.append(getResources().getString(R.string.map));
		a.append("</a>)");
		return Html.fromHtml(a.toString());				
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = activity;
		this.themeColor = new ColorTheme(context);
	}
}
