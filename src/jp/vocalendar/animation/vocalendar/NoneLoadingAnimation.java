package jp.vocalendar.animation.vocalendar;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import jp.vocalendar.animation.AnimationManager;
import jp.vocalendar.animation.canvas.CanvasAnimation;
import jp.vocalendar.animation.canvas.CanvasBackground;

/**
 * 何も表示しない読み込み中画面の設定
 */
public class NoneLoadingAnimation extends CanvasBackground implements LoadingAnimation {
	@Override
	public void init() {
		super.init();		
		setColor(Color.rgb(240, 240, 240));
	}

	@Override
	public void setAnimationManager(AnimationManager<CanvasAnimation> manager) {
		// 何もしない
	}

	@Override
	public void setContext(Context context) {
		// 何もしない
	}

	@Override
	public Spanned getCreatorText() {
		return new SpannableString("");
	}
}
