package jp.vocalendar.animation.vocalendar;

import android.content.Context;
import jp.vocalendar.animation.AnimationManager;
import jp.vocalendar.animation.canvas.CanvasAnimation;
import jp.vocalendar.animation.canvas.CanvasAnimationSupport;

public abstract class LoadingAnimationSupport extends CanvasAnimationSupport
implements LoadingAnimation {
	protected AnimationManager<CanvasAnimation> manager;
	protected Context context;
		
	@Override
	public void setAnimationManager(AnimationManager<CanvasAnimation> manager) {
		this.manager = manager;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
}
