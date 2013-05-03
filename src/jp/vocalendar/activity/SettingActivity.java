package jp.vocalendar.activity;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.animation.vocalendar.LoadingAnimationUtil;

import android.accounts.Account;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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
	private static final String PREVIEW_LOADING_PAGE_KEY = "preview_loading_page";
	
	/** 選択可能なGoogleアカウント */
	private Account[] accounts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setTitle(R.string.setting);
    	addPreferencesFromResource(R.xml.pref);
    	
    	initNumberODatePreference();
    	initLoadingPagePreference();
    	initAccountPreference();
    	
    	Preference back = getPreferenceScreen().findPreference(BACK_KEY);
    	back.setOnPreferenceClickListener(this);    	
	}
	
	private void initNumberODatePreference() {
		ListPreference numberOfDatePref = (ListPreference)getPreferenceScreen()
    			.findPreference(Constants.NUMBER_OF_DATE_TO_GET_EVENTS_PREFERENCE_NAME);
    	numberOfDatePref.setSummary(numberOfDatePref.getEntry());
    	numberOfDatePref.setOnPreferenceChangeListener(this);
	}

	private void initLoadingPagePreference() {
		ListPreference loadingPagePref = (ListPreference)getPreferenceScreen()
				.findPreference(Constants.LOADING_PAGE_PREFERENCE_NAME);
		loadingPagePref.setSummary(loadingPagePref.getEntry().toString());		
		loadingPagePref.setOnPreferenceChangeListener(this);
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
		} else if(preference.getKey().equals(Constants.LOADING_PAGE_PREFERENCE_NAME)) {
			preference.setSummary(
					LoadingAnimationUtil.getAnimationName(newValue.toString(), this));
		} else if(preference.getKey().equals(Constants.SELECTED_ACCOUNT_PREFERENECE_NAME)) {
			preference.setSummary(newValue.toString());
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
		}
		return false;
	}
}
