package jp.vocalendar.activity;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.animation.vocalendar.LoadingAnimationUtil;
import jp.vocalendar.model.ColorTheme;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingActivity extends PreferenceActivity
implements OnPreferenceChangeListener, OnPreferenceClickListener	{
	private static final String BACK_KEY = "back";
	private static final String CUSTOM_COLOR_THEME = "custom_color_theme";
	private static final String PREVIEW_LOADING_PAGE_KEY = "preview_loading_page";
	
	/** 選択可能なGoogleアカウント */
	private Account[] accounts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    hideActionBar();
		}
    	super.onCreate(savedInstanceState);
    	setTitle(R.string.setting);
    	addPreferencesFromResource(R.xml.pref);
    	
    	initNumberOfDatePreference();
    	initNumberOfDateToLoadMoreEventsPreference();
    	initLoadMoreWithoutTapPreference();
    	initNumberOfEventsToSearchMorePreference();
    	initLoadingPagePreference();
    	initColorThemePreference();
    	initAccountPreference();
    	
    	Preference custom = getPreferenceScreen().findPreference(CUSTOM_COLOR_THEME);
    	if(custom != null) { // TODO
    		custom.setOnPreferenceClickListener(this);
    	}
    	Preference back = getPreferenceScreen().findPreference(BACK_KEY);
    	back.setOnPreferenceClickListener(this);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void hideActionBar() {
		getActionBar().hide();
	}
	
	private void initNumberOfDatePreference() {
		ListPreference numberOfDatePref = (ListPreference)getPreferenceScreen()
    			.findPreference(Constants.NUMBER_OF_DATE_TO_GET_EVENTS_PREFERENCE_NAME);
    	numberOfDatePref.setSummary(numberOfDatePref.getEntry());
    	numberOfDatePref.setOnPreferenceChangeListener(this);
	}

	private void initNumberOfDateToLoadMoreEventsPreference() {
		ListPreference numberOfDateToLoadMorePref = (ListPreference)getPreferenceScreen()
    			.findPreference(Constants.NUMBER_OF_DATE_TO_LOAD_MORE_EVENTS_PREFRENCE_NAME);
		numberOfDateToLoadMorePref.setSummary(numberOfDateToLoadMorePref.getEntry());
		numberOfDateToLoadMorePref.setOnPreferenceChangeListener(this);
	}

	private void initLoadMoreWithoutTapPreference() {
		CheckBoxPreference pref = (CheckBoxPreference)getPreferenceScreen()
				.findPreference(Constants.LOAD_MORE_EVENT_WITHOUT_TAP);
		pref.setOnPreferenceChangeListener(this);
	}

	private void initNumberOfEventsToSearchMorePreference() {
		ListPreference numberOfEventToSeachMorePref = (ListPreference)getPreferenceScreen()
    			.findPreference(Constants.NUMBER_OF_EVENTS_TO_SEARCH_MORE_PREFERENCE_NAME);
		numberOfEventToSeachMorePref.setSummary(numberOfEventToSeachMorePref.getEntry());
		numberOfEventToSeachMorePref.setOnPreferenceChangeListener(this);
	}

	
	private void initLoadingPagePreference() {
		ListPreference loadingPagePref = (ListPreference)getPreferenceScreen()
				.findPreference(Constants.LOADING_PAGE_PREFERENCE_NAME);
		loadingPagePref.setSummary(loadingPagePref.getEntry());		
		loadingPagePref.setOnPreferenceChangeListener(this);
	}

	private void initColorThemePreference() {		
		ListPreference colorThemePref = (ListPreference)getPreferenceScreen()
				.findPreference(Constants.COLOR_THEME_PREFERENCE_NAME);
		if(colorThemePref != null) { // TODO 
			colorThemePref.setSummary(colorThemePref.getEntry());
			colorThemePref.setOnPreferenceChangeListener(this);
		}
	}

	private void initAccountPreference() {
		ListPreference accountPref = (ListPreference)getPreferenceScreen()
    			.findPreference(Constants.SELECTED_ACCOUNT_PREFERENECE_NAME);
    	accounts = new GoogleAccountManager(this).getAccounts();
    	String[] accountEntryValues = new String[accounts.length];
    	for(int i = 0; i < accountEntryValues.length; i++) {
    		accountEntryValues[i] = accounts[i].name;
    	}
    	accountPref.setEntries(accountEntryValues);
    	accountPref.setEntryValues(accountEntryValues);
    	accountPref.setDefaultValue(accountEntryValues[0]);
    	accountPref.setOnPreferenceChangeListener(this);
    	accountPref.setSummary(accountPref.getEntry());
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(Constants.NUMBER_OF_DATE_TO_GET_EVENTS_PREFERENCE_NAME)) {
			preference.setSummary(newValue.toString());
		} else if(preference.getKey().equals(Constants.NUMBER_OF_DATE_TO_LOAD_MORE_EVENTS_PREFRENCE_NAME)) {
			preference.setSummary(newValue.toString());
		} else if(preference.getKey().equals(Constants.NUMBER_OF_EVENTS_TO_SEARCH_MORE_PREFERENCE_NAME)) {
			preference.setSummary(newValue.toString());
		} else if(preference.getKey().equals(Constants.LOADING_PAGE_PREFERENCE_NAME)) {
			preference.setSummary(
					LoadingAnimationUtil.getAnimationName(newValue.toString(), this));
		} else if(preference.getKey().equals(Constants.COLOR_THEME_PREFERENCE_NAME)) {
			preference.setSummary(ColorTheme.getColorThemeName(newValue.toString(), this));
		} else if(preference.getKey().equals(Constants.SELECTED_ACCOUNT_PREFERENECE_NAME)) {
			preference.setSummary(newValue.toString());
		} else if(preference.getKey().equals(Constants.LOAD_MORE_EVENT_WITHOUT_TAP)) {
			// なにもしない  setResult(RESULT_OK)を実行する
		} else {
			return false;
		}
		setResult(RESULT_OK);
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(BACK_KEY)) {
			finish(); // 戻る
			return true;
		} else if(preference.getKey().equals(CUSTOM_COLOR_THEME)) {
			Intent i = new Intent(this, CustomColorThemeActivity.class);
			startActivity(i);
			setResult(RESULT_OK);
			return true;
		}
		return false;
	}
}
