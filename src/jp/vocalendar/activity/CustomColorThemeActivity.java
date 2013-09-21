package jp.vocalendar.activity;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.model.ColorTheme;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;

public class CustomColorThemeActivity extends PreferenceActivity
implements OnPreferenceChangeListener, OnPreferenceClickListener{
	private static final String IMPORT_COLOR_THEME = "import_color_theme";
	private static final String BACK_KEY = "back";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.custom_color_theme_pref);
    	
    	Preference imp = getPreferenceScreen().findPreference(IMPORT_COLOR_THEME);
    	imp.setOnPreferenceChangeListener(this);
    	
    	Preference back = getPreferenceScreen().findPreference(BACK_KEY);
    	back.setOnPreferenceClickListener(this);    	
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(IMPORT_COLOR_THEME)) {
			importColorTheme(newValue.toString());
			return true;
		}
		return false;
	}
	
	private void importColorTheme(String colorThemeName) {
		ColorTheme ct = new ColorTheme(this, ColorTheme.getColorThemeCode(colorThemeName, this));
		PreferenceScreen ps = getPreferenceScreen();
				
    	ColorPickerPreference darkBackground = 
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_DARK_BACKGROUND_PREF_NAME);
    	darkBackground.onColorChanged(ct.getDarkBackgroundColor());
    	
    	ColorPickerPreference darkText = 
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_DARK_TEXT_COLOR_PREF_NAME);
    	darkText.onColorChanged(ct.getDarkTextColor());
    	
    	ColorPickerPreference lightBackground = 
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PREF_NAME);
    	lightBackground.onColorChanged(ct.getLightBackgroundColor());
    	
    	ColorPickerPreference lightBackgroundPressed = 
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PRESSED_PREF_NAME);
    	lightBackgroundPressed.onColorChanged(ct.getLightBackgroundPressedColor());
    	
    	ColorPickerPreference lightText = 
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_LIGHT_TEXT_COLOR_PREF_NAME);
    	lightText.onColorChanged(ct.getLightTextColor());
    	
    	ColorPickerPreference divider =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_DIVIDER_COLOR_PREF_NAME);
    	divider.onColorChanged(ct.getDividerColor());
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(BACK_KEY)) {
			finish(); // 戻る
			return true;
		}
		return false;
	}

}
