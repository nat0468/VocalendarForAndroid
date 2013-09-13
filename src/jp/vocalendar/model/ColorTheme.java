package jp.vocalendar.model;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import android.content.Context;

/**
 * テーマの色を格納するオブジェクトのクラス
 */
public class ColorTheme {
	private Context context;
	
	private int darkBackgroundColor;
	private int darkTextColor;
	private int lightBackgroundStateList;
	private int lightTextColor;
	
	// テーマを表す定数
	private static final int THEME_DEFAULT = 0;
	private static final int THEME_MIKU = 1;
	private static final int THEME_RIN_LEN = 2;	
	private static final int THEME_MIKI = 3;
	
	private static final int THEME_LUKA = 0;
	private static final int THEME_KAITO = 0;
	private static final int THEME_MEIKO = 0;	
	private static final int THEME_GACPO = 0;
	private static final int THEME_GUMI = 0;
	private static final int THEME_YUKARI = 0;

	public ColorTheme(Context context) {
		this.context = context;
		updateColor();
	}
	
	/**
	 * カラーテーマを現在の設定で更新する。
	 */
	public void updateColor() {
		initColor(getColorThemeCode(VocalendarApplication.getColorTheme(context), context));		
	}
		
	private void initColor(int theme) {
		switch(theme) {
		case THEME_MIKU:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_miku);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_miku);			
			lightBackgroundStateList = R.color.light_background_state_list_miku;
			lightTextColor = context.getResources().getColor(R.color.light_text_color_miku);			
			break;
		case THEME_RIN_LEN:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_rinlen);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_rinlen);			
			lightBackgroundStateList = R.color.light_background_state_list_rinlen;
			lightTextColor = context.getResources().getColor(R.color.light_text_color_rinlen);			
			break;
		case THEME_MIKI:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_miki);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_miki);			
			lightBackgroundStateList = R.color.light_background_state_list_miki;
			lightTextColor = context.getResources().getColor(R.color.light_text_color_miki);			
			break;
		default:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_default);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_default);			
			lightBackgroundStateList = R.color.light_background_state_list_default;
			lightTextColor = context.getResources().getColor(R.color.light_text_color_default);			
		}
		
		
	}

	public int getDarkBackgroundColor() {
		return darkBackgroundColor;
	}

	public int getDarkTextColor() {
		return darkTextColor;
	}

	public int getLightBackgroundStateList() {
		return lightBackgroundStateList;
	}

	public int getLightTextColor() {
		return lightTextColor;
	}
	
	public static int getColorThemeCode(String key, Context context) {
		if("THEME_DEFAULT".equals(key)) {
			return THEME_DEFAULT;
		} else if("THEME_MIKU".equals(key)) {
			return THEME_MIKU;
		} else if("THEME_RIN_LEN".equals(key)) {
			return THEME_RIN_LEN;
		} else if("THEME_MIKI".equals(key)) {
			return THEME_MIKI;
		}
		return THEME_DEFAULT;
	}

	public static String getColorThemeName(String key, Context context) {
		String[] names =
				context.getResources().getStringArray(R.array.color_theme_entries);
		return names[getColorThemeCode(key, context)];
	}		
}
