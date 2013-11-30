package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.FavoriteEventManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	private FavoriteEventManager favoriteEventManager;
	private Bitmap favoriteBitmap, notFavoriteBitmap;
	
	public EventArrayEventDescriptionPagerAdapter(Context context, FragmentManager fm,
			EventDataBaseRow[] rows, FavoriteEventManager favoriteEventManager) {
		super(fm);
		this.rows = rows;
		this.favoriteEventManager = favoriteEventManager;
		this.favoriteBitmap =
				BitmapFactory.decodeResource(context.getResources(), R.drawable.favorite);
		this.notFavoriteBitmap =
				BitmapFactory.decodeResource(context.getResources(), R.drawable.not_favorite);		
	}
	
	@Override
	public Fragment getItem(int i) {
		EventDescriptionFragment fragment = new EventDescriptionFragment();
        fragment.setFavoriteAndNotFavoriteBitmap(favoriteBitmap, notFavoriteBitmap);
        fragment.setFavoriteEventManager(favoriteEventManager);
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

	public Bitmap getFavoriteBitmap() {
		return favoriteBitmap;
	}

	public Bitmap getNotFavoriteBitmap() {
		return notFavoriteBitmap;
	}

}
