package jp.vocalendar.model;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.util.DialogUtil;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.util.StateSet;
import android.util.Xml;

/**
 * テーマの色を格納するオブジェクトのクラス
 */
public class ColorTheme {
	private Activity context;
	
	private int darkBackgroundColor;
	private int darkTextColor;
	private int lightBackgroundColor;
	private int lightBackgroundPressedColor;
	private int lightTextColor;
	private int dividerColor;
	
	// テーマを表す定数
	public static final int THEME_DEFAULT = 0;
	public static final int THEME_MIKU = 1;
	public static final int THEME_RIN_LEN = 2;	
	public static final int THEME_LUKA = 3;
	public static final int THEME_KAITO = 4;
	public static final int THEME_MEIKO = 5;	

	public static final int THEME_GAKUPO = 6;
	public static final int THEME_GUMI = 7;

	public static final int THEME_MIKI = 8;
	public static final int THEME_YUKARI = 9;
	
	public static final int THEME_TETO = 10;

	public static final int THEME_CUSTOM = 11;
	
	/**
	 * プリファレンスの値に従ってインスタンス生成
	 * @param context
	 */
	public ColorTheme(Activity context) {
		this.context = context;
		updateColor();
	}
	
	/**
	 * 指定されたカラーテーマのコードに従ってインスタンス生成
	 * @param context
	 * @param colorThemeCode
	 */
	public ColorTheme(Activity context, int colorThemeCode) {
		this.context = context;
		try {
			initColor(colorThemeCode);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			DialogUtil.openErrorDialog(context, "failed to load color theme:" + colorThemeCode + ". " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			DialogUtil.openErrorDialog(context, "failed to load color theme:" + colorThemeCode + ". " + e.toString());
		}		
	}
	
	/**
	 * カラーテーマを現在の設定で更新する。
	 */
	public void updateColor() {
		String theme = VocalendarApplication.getColorTheme(context);
		try {
			initColor(getColorThemeCode(theme, context));
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			DialogUtil.openErrorDialog(context, "failed to load color theme:" + theme + ". " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			DialogUtil.openErrorDialog(context, "failed to load color theme:" + theme + ". " + e.toString());
		}		
	}
		
	private void initColor(int theme) throws XmlPullParserException, IOException {
		switch(theme) {
		case THEME_MIKU:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_miku);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_miku);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_miku);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_miku);
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_RIN_LEN:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_rinlen);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_rinlen);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_rinlen);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_rinlen);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_LUKA:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_luka);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_luka);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_luka);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_luka);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_KAITO:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_kaito);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_kaito);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_kaito);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_kaito);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_MEIKO:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_meiko);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_meiko);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_meiko);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_meiko);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_GAKUPO:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_gakupo);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_gakupo);	
			initLightBackgroundStateListColor(R.color.light_background_state_list_gakupo);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_gakupo);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_GUMI:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_gumi);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_gumi);	
			initLightBackgroundStateListColor(R.color.light_background_state_list_gumi);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_gumi);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_MIKI:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_miki);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_miki);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_miki);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_miki);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_YUKARI:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_yukari);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_yukari);
			initLightBackgroundStateListColor(R.color.light_background_state_list_yukari);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_yukari);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_TETO:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_teto);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_teto);
			initLightBackgroundStateListColor(R.color.light_background_state_list_teto);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_teto);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
			break;
		case THEME_CUSTOM:
			loadCustomColorTheme();
			break;			
		default:
			darkBackgroundColor = context.getResources().getColor(R.color.dark_background_default);
			darkTextColor = context.getResources().getColor(R.color.dark_text_color_default);			
			initLightBackgroundStateListColor(R.color.light_background_state_list_default);
			lightTextColor = context.getResources().getColor(R.color.light_text_color_default);			
			dividerColor = context.getResources().getColor(R.color.divider_color_default);
		}
		
		
	}

	private void loadCustomColorTheme() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		darkBackgroundColor = pref.getInt(Constants.CUSTOM_COLOR_THEME_DARK_BACKGROUND_PREF_NAME, 0x666666);
		darkTextColor = pref.getInt(Constants.CUSTOM_COLOR_THEME_DARK_TEXT_COLOR_PREF_NAME, 0xffffff);
		lightBackgroundColor = pref.getInt(Constants.CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PREF_NAME, 0xffffff);
		lightBackgroundPressedColor = pref.getInt(Constants.CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PRESSED_PREF_NAME, 0xc0c0c0);		
		lightTextColor = pref.getInt(Constants.CUSTOM_COLOR_THEME_LIGHT_TEXT_COLOR_PREF_NAME, 0x000000);
		dividerColor = pref.getInt(Constants.CUSTOM_COLOR_THEME_DIVIDER_COLOR_PREF_NAME, 0xcccccc);		
	}

	public int getDarkBackgroundColor() {
		return darkBackgroundColor;
	}

	public int getDarkTextColor() {
		return darkTextColor;
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
		} else if("THEME_LUKA".equals(key)) {
			return THEME_LUKA;
		} else if("THEME_KAITO".equals(key)) {
			return THEME_KAITO;
		} else if("THEME_MEIKO".equals(key)) {
			return THEME_MEIKO;
		} else if("THEME_GAKUPO".equals(key)) {
			return THEME_GAKUPO;
		} else if("THEME_GUMI".equals(key)) {
			return THEME_GUMI;
		} else if("THEME_MIKI".equals(key)) {
			return THEME_MIKI;
		} else if("THEME_YUKARI".equals(key)) {
			return THEME_YUKARI;
		} else if("THEME_TETO".equals(key)) {
			return THEME_TETO;
		} else if("THEME_CUSTOM".equals(key)) {
			return THEME_CUSTOM;
		}
		return THEME_DEFAULT;
	}

	public static String getColorThemeName(String key, Context context) {
		String[] names =
				context.getResources().getStringArray(R.array.color_theme_entries);
		return names[getColorThemeCode(key, context)];
	}

	public StateListDrawable makeLightBackgroundStateListDrawable() {
		StateListDrawable sld = new StateListDrawable();
		sld.addState(
				new int[] { android.R.attr.state_pressed },
				new ColorDrawable(lightBackgroundPressedColor));
		sld.addState(
				StateSet.WILD_CARD,
				new ColorDrawable(lightBackgroundColor));
		return sld;
	}

	private void initLightBackgroundStateListColor(int colorStateListResId) {
		ColorStateList csl = context.getResources().getColorStateList(colorStateListResId);
		lightBackgroundColor = csl.getDefaultColor();
		lightBackgroundPressedColor = csl.getColorForState(new int[] { android.R.attr.state_pressed }, csl.getDefaultColor()); 
	}

	public int getLightBackgroundColor() {
		return lightBackgroundColor;
	}

	public int getLightBackgroundPressedColor() {
		return lightBackgroundPressedColor;
	}

	public int getDividerColor() {
		return dividerColor;
	}
	
}
