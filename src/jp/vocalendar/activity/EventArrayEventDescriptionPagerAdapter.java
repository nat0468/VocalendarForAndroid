package jp.vocalendar.activity;

import jp.vocalendar.model.EventDataBaseRow;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * EventDataBaseRowの配列を内部に持つPagerAdapter
 */
public class EventArrayEventDescriptionPagerAdapter extends
		FragmentStatePagerAdapter {
	private EventDataBaseRow[] rows;
	
	public EventArrayEventDescriptionPagerAdapter(FragmentManager fm, EventDataBaseRow[] rows) {
		super(fm);
		this.rows = rows;
	}
	
	@Override
	public Fragment getItem(int i) {
        Fragment fragment = new EventDescriptionFragment();
        Bundle args = new Bundle();
        args.putSerializable(
        		EventDescriptionFragment.ARG_EVENT_DATABASE_ROW, rows[i]);
        fragment.setArguments(args);
        return fragment;
	}

	@Override
	public int getCount() {
		return rows.length;
	}

}
