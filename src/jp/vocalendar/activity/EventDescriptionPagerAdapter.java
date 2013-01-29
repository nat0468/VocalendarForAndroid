package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.Button;

public class EventDescriptionPagerAdapter extends FragmentStatePagerAdapter {
	private Activity activity;
	
	private int numberOfEvents = -1;
	
	public EventDescriptionPagerAdapter(FragmentManager fm, Activity activity) {
		super(fm);
		this.activity = activity;
	}
	 
	@Override
	public Fragment getItem(int i) {
		EventDataBase db = new EventDataBase(activity);
		db.open();	
        EventDataBaseRow row = db.getEventByEventIndex(i);
        db.close();

        Fragment fragment = new EventDescriptionFragment();
        Bundle args = new Bundle();
        args.putSerializable(
        		EventDescriptionFragment.ARG_EVENT_DATABASE_ROW, row);
        fragment.setArguments(args);
        return fragment;
    }

	@Override
	public int getCount() {
		if(numberOfEvents == -1) {
			EventDataBase db = new EventDataBase(activity);
			db.open();	
	        numberOfEvents = db.countEvent();
	        db.close();					
		}
        return numberOfEvents;
	}
}
