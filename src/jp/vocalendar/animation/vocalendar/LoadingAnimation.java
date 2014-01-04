package jp.vocalendar.animation.vocalendar;

import android.content.Context;
import android.text.Spanned;
import android.view.View;
import jp.vocalendar.animation.AnimationManager;
import jp.vocalendar.animation.canvas.CanvasAnimation;

/**
 * 読み込み中画面のアニメーションのインターフェイス
 */
public interface LoadingAnimation extends CanvasAnimation {
	/**
	 * アニメーション追加などに使うAnimationManager
	 */
	public void setAnimationManager(AnimationManager<CanvasAnimation> manager);
	
	/**
	 * リソース取得などに使うContext
	 * @param context
	 */
	public void setContext(Context context);
	
	/**
	 * 作成者を表すテキスト。アニメーション表示の下に表示する。
	 */
	public Spanned getCreatorText();
	
	/**
	 * クリックされたときの処理を実装する
	 * @param v
	 */
	public void onClick(View v);
}
