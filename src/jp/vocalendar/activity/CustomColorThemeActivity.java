package jp.vocalendar.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.util.DialogUtil;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class CustomColorThemeActivity extends PreferenceActivity
implements OnPreferenceChangeListener, OnPreferenceClickListener{
	private static final String TAG = "CustomColorThemActivity";
	private static final String IMPORT_COLOR_THEME = "import_color_theme";
	private static final String EXPORT_IMPORT_SETTINGS = "export_import_settings";
	private static final String PREVIEW = "preview";
	private static final String BACK_KEY = "back";
	
	private static String[] PREF_NAMES = {
			Constants.CUSTOM_COLOR_THEME_DARK_BACKGROUND_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_DARK_TEXT_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_LIGHT_BACKGROUND_PRESSED_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_LIGHT_TEXT_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_DIVIDER_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_NORMALDAY_BACKGROUND_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_NORMALDAY_TEXT_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_SATURDAY_BACKGROUND_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_SATURDAY_TEXT_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_SUNDAY_BACKGROUND_COLOR_PREF_NAME,
			Constants.CUSTOM_COLOR_THEME_SUNDAY_TEXT_COLOR_PREF_NAME,
	};
	
	private Dialog exportImportSettingsDialog = null;
	private EditText exportImportSettingDialogCodeText = null;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			hideActionBar();
		}
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.custom_color_theme_pref);
    	
    	Preference imp = getPreferenceScreen().findPreference(IMPORT_COLOR_THEME);
    	imp.setOnPreferenceChangeListener(this);
    	
    	Preference prev = getPreferenceScreen().findPreference(PREVIEW);
    	prev.setOnPreferenceClickListener(this);
    	
    	Preference expimp = getPreferenceScreen().findPreference(EXPORT_IMPORT_SETTINGS);
    	expimp.setOnPreferenceClickListener(this);
    	
    	Preference back = getPreferenceScreen().findPreference(BACK_KEY);
    	back.setOnPreferenceClickListener(this);   
    	
    	if(checkNotImported()) {
    		importColorTheme(Constants.DEFAULT_THEME_NAME);
    	}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void hideActionBar() {
		getActionBar().hide();
	}

	/**
	 * まだカスタムテーマの色が設定されていない場合にtrueを返す。
	 * @return
	 */
	private boolean checkNotImported() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		return !pref.contains(Constants.CUSTOM_COLOR_THEME_DARK_BACKGROUND_PREF_NAME); // 背景色の設定有無を、カスタムテーマの色設定とする		
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
    	
    	ColorPickerPreference normalDayBackground =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_NORMALDAY_BACKGROUND_COLOR_PREF_NAME);
    	normalDayBackground.onColorChanged(ct.getNormalDayBackgroundColor());

    	ColorPickerPreference normalDayText =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_NORMALDAY_TEXT_COLOR_PREF_NAME);
    	normalDayText.onColorChanged(ct.getNormalDayTextColor());
    	
    	ColorPickerPreference satudayBg =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_SATURDAY_BACKGROUND_COLOR_PREF_NAME);
    	satudayBg.onColorChanged(ct.getSaturdayBackgroundColor());

    	ColorPickerPreference satudayText =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_SATURDAY_TEXT_COLOR_PREF_NAME);
    	satudayText.onColorChanged(ct.getSaturdayTextColor());

    	ColorPickerPreference sundayBg =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_SUNDAY_BACKGROUND_COLOR_PREF_NAME);
    	sundayBg.onColorChanged(ct.getSundayBackgroundColor());

    	ColorPickerPreference sundayText =
    			(ColorPickerPreference)ps.findPreference(Constants.CUSTOM_COLOR_THEME_SUNDAY_TEXT_COLOR_PREF_NAME);
    	sundayText.onColorChanged(ct.getSundayTextColor());
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(PREVIEW)) {
			openPreview();
			return true;
		}
		if(preference.getKey().equals(EXPORT_IMPORT_SETTINGS)) {
			openExportImportSettingsDialog();
			return true;
		}
		if(preference.getKey().equals(BACK_KEY)) {
			finish(); // 戻る
			return true;
		}		
		return false;
	}

	protected void openPreview() {
		Intent i = new Intent(this, PreviewEventListActivity.class);
		i.putExtra(PreviewEventListActivity.EXTRA_PREVIEW_COLOR_THEME_CODE, ColorTheme.THEME_CUSTOM);
		startActivity(i);
	}

	private void openExportImportSettingsDialog() {
		if(exportImportSettingsDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);			
			LayoutInflater inflater = getLayoutInflater();
			View v = inflater.inflate(R.layout.custom_color_theme_import_export_dialog, null);
			builder.setView(v)
				   .setPositiveButton(R.string.import_button, new DialogInterface.OnClickListener() {				
					   @Override
					   public void onClick(DialogInterface dialog, int which) {						   
						   importColorThemeCode(
								   exportImportSettingDialogCodeText.getText().toString());
					   }
				   })
				   .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {				
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   dialog.cancel();
					   }
				   })
				   .setTitle(R.string.export_import);
				
			exportImportSettingsDialog = builder.create();
			exportImportSettingDialogCodeText =
					(EditText)v.findViewById(R.id.customeColortThemeCodeText);
		}		
		exportImportSettingDialogCodeText.setText(makeColorThemeCode());
		exportImportSettingsDialog.show();
	}
	
	private void importColorThemeCode(String code) {
		int[] colors = parseCode(code); 		
		if(colors == null || colors.length != PREF_NAMES.length) {
			DialogUtil.openErrorDialog(this, getString(R.string.invalid_color_theme_code), false);
			return;
		}
		PreferenceScreen ps = getPreferenceScreen();
		for (int i = 0; i < PREF_NAMES.length; i++) {
			ColorPickerPreference cpp = (ColorPickerPreference)ps.findPreference(PREF_NAMES[i]);
			cpp.onColorChanged(colors[i]);
		}
	}

	/**
	 * カスタム・カラー・テーマのコードを読み込む。
	 * @param code
	 * @return 読み込んだ色の配列。読み込みエラー時はnullを返す。
	 */
	private int[] parseCode(String code) {
		BufferedReader reader = new BufferedReader(new StringReader(code));
		LinkedList<Integer> colorList = new LinkedList<Integer>();
		
		boolean error = false;
		try {
			String line = reader.readLine();
			while(line != null) {
				int c = ColorPickerPreference.convertToColorInt(line);
				colorList.add(c);
				line = reader.readLine();
			}
		} catch(IOException e) {
			e.printStackTrace();
			error = true;
		} catch(NumberFormatException e) {
			e.printStackTrace();
			error = true;
		}
		
		if(error) {
			return null;
		}
		int[] colors = new int[colorList.size()];
		Iterator<Integer> itr = colorList.iterator();
		int i = 0;
		while(itr.hasNext()) {
			colors[i++] = itr.next();
		}
		return colors;
	}
	
	
	private String makeColorThemeCode() {
		PreferenceScreen ps = getPreferenceScreen();
		int[] colors = new int[PREF_NAMES.length];
		for(int i = 0; i < PREF_NAMES.length; i++) {
	    	ColorPickerPreference cpp =
	    			(ColorPickerPreference)ps.findPreference(PREF_NAMES[i]);			
			colors[i] = cpp.getValue();			
		}
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < colors.length; i++) {
    		if(i != 0) {
    			sb.append('\n');
    		}
    		sb.append(makeColorCode(colors[i]));    		
    	}
		return sb.toString();
	}
	
	private String makeColorCode(int color) {
		return ColorPickerPreference.convertToARGB(color);
	}
}
