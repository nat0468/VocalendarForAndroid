package jp.vocalendar.animation.canvas;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import jp.vocalendar.animation.AnimationManager;

/**
 * SurfaceViewに描画するCanvasAnimationを制御するクラス。
 * java.util.Timerを使っている。
 */
public class SurfaceViewAnimationRunner extends CanvasAnimationManagerSupport
implements AnimationManager<CanvasAnimation>, SurfaceHolder.Callback {
	private static final String TAG = "SurfaceViewAnimationRunner";
	
	/** Animationを更新するupdate()を一定間隔で呼び出すTimer */
	private Timer m_timer;
  
	private SurfaceHolder holder;
	
	public SurfaceViewAnimationRunner(SurfaceView view) {
		updateHolder(view.getHolder());
	}

	private void updateHolder(SurfaceHolder holder) {
		if(this.holder != holder) {
			Log.d(TAG, "updateHolder");			
			this.holder = holder;
			this.holder.setFormat(PixelFormat.RGBA_8888);
			this.holder.addCallback(this);
		}
	}
	
	@Override
	protected void startUpdateTimer() {
		Log.d(TAG, "startUpdateTimer");
	    TimerTask task = new TimerTask() {
	        public void run() {
	          update();
	          draw();
	        }
	      };
	      m_timer = new Timer();
	      m_timer.scheduleAtFixedRate(task, 0, getUpdatePeriod()); // update()を一定時間ごと実行    
	}

	@Override
	protected void stopUpdateTimer() {
		Log.d(TAG, "stopUpdateTimer");
		if(m_timer != null) { // startUpdateTimer()が呼ばれる前に呼ばれた場合は無視
		    m_timer.cancel();			
		}
	}
	
	@Override
	protected void setUpdateTimerPeriod(int updatePeriod) {
		stopUpdateTimer();
		startUpdateTimer();
	}
	
	protected void draw() {
		Canvas c = null;
		if(holder == null) {
			Log.d(TAG, "draw(): holder == null");		
			return;
		}
		try {
			synchronized (holder) {
				c = holder.lockCanvas();
				if(c != null) {
					draw(c);
				}
			}
		} finally {
			if(c != null) {
				holder.unlockCanvasAndPost(c);
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceDestroyed");		
		updateHolder(holder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		updateHolder(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");		
		updateHolder(holder);
	}	
}
