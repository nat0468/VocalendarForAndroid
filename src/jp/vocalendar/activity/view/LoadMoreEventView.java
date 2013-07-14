package jp.vocalendar.activity.view;

import jp.vocalendar.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * もっとイベントを読み込む操作のためのView。
 * タップすると読み込みのプログレスを表示する動きを想定。
 * 動作状態として「もっと読み込む」表示と、「読み込み中...」表示の２つがある。
 */
public class LoadMoreEventView extends LinearLayout {
	/** 高さ。dp単位 */
	private static int HEIGHT_DP = 48;
	
	private ProgressBar progressBar;
	private TextView textView;
	private int heightPixel;
	
	/** 読み込み中表示かどうか。読み込み中ならtrue */
	private boolean isLoading = false;
	
	public LoadMoreEventView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initChildren();
	}
	
	private void initChildren() {
		setGravity(Gravity.CENTER);
		
		heightPixel = toPixel(HEIGHT_DP);
		
		progressBar = new ProgressBar(getContext());
		progressBar.setVisibility(View.GONE);
		LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams((int)(heightPixel / 1.5), (int)(heightPixel / 1.5));
		params.rightMargin = heightPixel / 6;
		addView(progressBar, params);
		
		textView = new TextView(getContext());
		textView.setText(R.string.tap_to_load_more);
		textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load, 0, 0, 0);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, heightPixel);
		addView(textView, params);
	}

	private int toPixel(int dp) {
		return (int)(dp * getResources().getDisplayMetrics().density);
	}

	public int getHeightPixel() {
		return heightPixel;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
		if(isLoading) {
			progressBar.setVisibility(View.VISIBLE);
			textView.setText(R.string.loading_events);
			textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		} else {
			progressBar.setVisibility(View.GONE);
			textView.setText(R.string.tap_to_load_more);
			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load, 0, 0, 0);
		}
	}
	
	
}
