package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.ColorTheme;
import android.content.Intent;
import android.widget.Toast;

/**
 * イベント一覧のプレビュー(カラーテーマ設定の)用Activity
 */
public class PreviewEventListActivity extends EventListActivity {
	/** プレビュー用に設定するカラー・テーマのコードをIntentに格納するエクストラキー */
	public static final String EXTRA_PREVIEW_COLOR_THEME_CODE =
			PreviewEventListActivity.class.getPackage().getName() + "PreviewColorThemeCode";
	
	private int colorThemeCode = ColorTheme.THEME_DEFAULT;
	
	@Override
	protected ColorTheme initColorTheme() {
		colorThemeCode = getIntent().getIntExtra(EXTRA_PREVIEW_COLOR_THEME_CODE, ColorTheme.THEME_DEFAULT);
		return new ColorTheme(this, colorThemeCode);
	}	
	
	@Override
	protected boolean isUpdateRequired() {
		return false; // 明示的に更新操作されない限り更新しない。
	}

	@Override
	protected void onResume() {
		super.onResume();
		showToastMessage();
	}
	
	private void showToastMessage() {
		Toast.makeText(this, R.string.preview_custom_color_theme, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void beforeOpenEventDescriptionActivity(Intent i) {		
		i.putExtra(EXTRA_PREVIEW_COLOR_THEME_CODE, colorThemeCode);
	}

	@Override
	protected void beforeOpenSeatch(Intent i) {
		i.putExtra(EXTRA_PREVIEW_COLOR_THEME_CODE, colorThemeCode);
	}

}
