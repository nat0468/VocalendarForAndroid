package jp.vocalendar.activity.view;

import jp.vocalendar.animation.canvas.SurfaceViewAnimationRunner;
import jp.vocalendar.animation.vocalendar.LoadingAnimation;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * アニメーション表示用のSurfaceView
 */
public class AnimationSurfaceView extends SurfaceView
implements SurfaceHolder.Callback, View.OnClickListener {
	private static final String TAG = "AnimatinSurfaeView";
	
	private SurfaceViewAnimationRunner runner;
	private LoadingAnimation loadingAnimation;
	
	public AnimationSurfaceView(Context context) {
		super(context);
		init(context);		
	}
	
	public AnimationSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);		
	}

	public AnimationSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);		
	}

	private void init(Context context) {
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		runner = new SurfaceViewAnimationRunner(this);		
		setOnClickListener(this);
	}

	public void setLoadingAnimation(LoadingAnimation laodingAnimation) {
		this.loadingAnimation = laodingAnimation;
		laodingAnimation.setAnimationManager(runner);
		laodingAnimation.setContext(getContext());
		runner.add(laodingAnimation);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		runner.runAsynchronous();
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");		
		runner.pause();
	}
	
	public void pause() {
		Log.d(TAG, "pause");
		runner.pause();
	}
	
	public void resume() {
		Log.d(TAG, "resume");		
		runner.resume();
	}
	
	public void destroy() {
		Log.d(TAG, "destroy");
		runner.destroy();		
	}

	@Override
	public void onClick(View v) {
		if(loadingAnimation != null) {
			loadingAnimation.onClick(v);
		}
	}
}
