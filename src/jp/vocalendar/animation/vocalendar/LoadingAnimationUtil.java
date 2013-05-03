package jp.vocalendar.animation.vocalendar;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import android.content.Context;

/**
 * 読み込み中アニメに関するユーティリティ
 */
public class LoadingAnimationUtil {
	private LoadingAnimationUtil() { } //インスタンス生成禁止
	
	public static String getAnimationName(String key, Context context) {
		String[] names =
				context.getResources().getStringArray(R.array.loading_page_entries);
		if(Constants.LOADING_PAGE_RANDOM.equals(key)) {
			return names[0];
		} else if(Constants.LOADING_PAGE_WALLPAPER.equals(key)) {
			return names[1];
		} else if(Constants.LOADING_PAGE_DOT_ANIMATION.equals(key)) {
			return names[2];
		} else if(Constants.LOADING_PAGE_NONE.equals(key)){
			return names[3];
		}
		return "";
	}

	public static LoadingAnimation makeLoadingAnimation(String key) {
		if(Constants.LOADING_PAGE_NONE.equals(key)) {
			return new NoneLoadingAnimation();
		} else if(Constants.LOADING_PAGE_DOT_ANIMATION.equals(key)) {
			return new DotCharacterAnimation.LinearDotCharacterAnimation();
		} else if(Constants.LOADING_PAGE_WALLPAPER.equals(key)) {
			return new WallpaperLoadingAnimation();			
		} else if(Constants.LOADING_PAGE_RANDOM.equals(key)) {
			if(Math.random() < 0.5) {
				return new DotCharacterAnimation.LinearDotCharacterAnimation();
			}
			return new WallpaperLoadingAnimation();
		}
		return new WallpaperLoadingAnimation();
	}
	
}
