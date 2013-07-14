package jp.vocalendar.animation.canvas;

import java.util.Iterator;

import jp.vocalendar.animation.Animation;
import jp.vocalendar.animation.AnimationManagerSupport;
import jp.vocalendar.animation.Animation.Status;
import jp.vocalendar.animation.AnimationManagerSupport.AnimationContainer;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Canvas用のAnimationManagerの実装をサポートするクラス。 
 */
public abstract class CanvasAnimationManagerSupport 
extends AnimationManagerSupport<CanvasAnimation> {
  	/** Animationを表示する頻度(ミリ秒) */
	private int m_updatePeriod = 1000 / 20;
	/** 前にupdate()したときの時刻 */
	private long m_previousTime;
	/** 前にupdate()したときからの経過時間(ミリ秒) */
	private long m_elapsedTime;
	/**
	 * Animation実行中(run()やrunAsynchronous()が呼ばれた後)
	 * ならtrue.それ以外はfalse
	 */
	private boolean m_running = false;
	
	private SurfaceHolder holder = null;
		
	/**
  	 * 指定されたCanvasにアニメーションを描画する。
  	 * @param canvas
  	 */
  	public void draw(Canvas canvas) {
  		Iterator<AnimationContainer> itr = getAnimationContainerIterator();
  		while(itr.hasNext()) {
  			AnimationContainer ac = itr.next();
  			if(ac.isInitialized() && 
  					ac.getAnimation().getStatus() == Animation.Status.RUNNING) {
  	  					ac.getAnimation().draw(canvas);
  	  		}
		}
  	}

	public synchronized void runAsynchronous() {
		m_running = true;
		
		beforeInit();
		initAnimations();
		afterInit();
	    	
	  	m_previousTime = System.currentTimeMillis();
	  	update();
	    
	  	startUpdateTimer();
	}

	public void run() {
		runAsynchronous();
		
		//Animationが終了するまで、このメソッドは返らない。    
		while(isRunning()) {
			try {
				synchronized(this) {
					// cancel()からnotify()が呼ばれるまで待つ
					wait();
		        }
			} catch(InterruptedException e) {
				// do nothing
			}
		}
		stopUpdateTimer();
	}

	public synchronized void pause() {
		m_running = false;
		stopUpdateTimer();
	}	
	
	public synchronized void resume() {
		if(!m_running) {
			runAsynchronous();
		}
	}
	
	public synchronized void cancel() {
		m_running = false;
		stopUpdateTimer();
		for(AnimationContainer ac : m_animationContainerList) {
			remove(ac);
		}
		synchronized(this) {
			notifyAll();
		}
	}

	/**
	 * 一定時間ごとに呼ばれ、
	 * Animationの更新を行う。
	 */
	protected synchronized void update() {
		if(!isRunning()) {
			return;
		}
	
		updateElapsedTime();
		updateAnimations(m_elapsedTime);
	}

	/** 経過時間を現在時刻で更新 */
	private void updateElapsedTime() {
		long currentTime = System.currentTimeMillis();
		m_elapsedTime = currentTime - m_previousTime; // 経過時間の更新
		m_previousTime = currentTime; // 時刻の更新
	}

	/**
	 * Animationを更新する頻度(ミリ秒)を設定する。
	 */
	public void setUpdatePeriod(int updatePeriod) {
		m_updatePeriod = updatePeriod;
		setUpdateTimerPeriod(updatePeriod);
	}

	public int getUpdatePeriod() {
		return m_updatePeriod;
	}

	public int getFps() {
		return 1000 / m_updatePeriod;
	}

	/**
	   * 実行中かどうかを判定する。
	   * @return 実行中であればtrue。それ以外はfalse。
	   */
	public boolean isRunning() {
	    return m_running;
	  }

	/**
	 * update()を定期的に呼び出すタイマーを
	 * 開始するメソッド。サブクラスが実装する。
	 * run/runAsynchronous()の中から呼ばれる。
	 */
	protected abstract void startUpdateTimer();

	/**
	 * update()を定期的に呼び出すタイマーを
	 * 終了するメソッド。サブクラスが実装する。
	 */
	protected abstract void stopUpdateTimer();

	/**
	 * update()を定期的に呼び出すタイマーの
	 * 頻度を更新する。
	 * @param updatePeriod
	 */
	protected abstract void setUpdateTimerPeriod(int updatePeriod);

	/**
	 * run()が呼ばれた後、
	 * Animationのinit()が呼ばれる前に呼ばれるメソッド。
	 * サブクラスが上書きする。
	 */
	protected void beforeInit() {}

	/**
	 * run()が呼ばれた後、
	 * Animationのinit()が呼ばれた後に呼ばれるメソッド。
	 * サブクラスが上書きする。
	 */
	protected void afterInit() {}	
}
