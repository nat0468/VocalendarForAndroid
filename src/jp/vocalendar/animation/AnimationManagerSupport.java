package jp.vocalendar.animation;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * AnimationManagerの実装をサポートする抽象クラス。
 */
public abstract class AnimationManagerSupport<E extends Animation> 
implements AnimationManager<E>{
	/** Animation管理に使うクラス */
	public final class AnimationContainer {
		/** 管理対象のAnimation */
		private E m_animation;
		/** Animationが登録されてからの経過時間 */
		private long m_time;
		/** init()呼ばれたか */
		private boolean m_initialized = false;
		/** コンストラクタ */
		public AnimationContainer(E animation) {
			m_animation = animation;
			m_time = 0;
		}
		  
		private void init() {
			m_animation.init();
			m_time = 0;
			m_initialized = true;
		}	
		  
		public long getTime() {
			return m_time;
		}
		public void setTime(long time) {
			m_time = time;
		}
		public E getAnimation() {
			return m_animation;
		}
		public boolean isInitialized() {
			return m_initialized;
		}
		public void setInitialized(boolean initialized) {
			m_initialized = initialized;
		}
	}
	  
	/** Animation格納リスト */
	protected Collection<AnimationContainer>
	m_animationContainerList = new ConcurrentLinkedQueue<AnimationContainer>();
	  
	/**
	 * 全てのAnimationのinit()を呼ぶ。サブクラスがこのメソッドを呼ぶ。
	 */
	protected void initAnimations() {
		for(AnimationContainer ac : m_animationContainerList) {
			ac.init();
		}
	}
	
	/**
	 * 全てのAnimationのudate()を呼ぶ。サブクラスがこのメソッドを呼ぶ。
	 * @param elapsedTime 前にupdate()したときからの経過時間(ミリ秒)
	 */
	protected void updateAnimations(long elapsedTime) {
  		callUpdate(elapsedTime);
  		removeFinishedAnimations();
  	}

	private void callUpdate(long elapsedTime) {
		for(AnimationContainer ac : m_animationContainerList) {
  			ac.setTime(ac.getTime() + elapsedTime);
  			Animation.UpdateTime ut = ac.getAnimation().update(ac.getTime());
  			if(ut == Animation.UpdateTime.RESET) {
  				ac.setTime(0);
  			}
  		}
	}
	
	/**
	 * Animationの状態を確認し、終了したAnimationを削除する。
	 */
	private void removeFinishedAnimations() {
		for(AnimationContainer ac : m_animationContainerList) {
			if(ac.getAnimation().getStatus() == Animation.Status.FINISHED) {
				remove(ac);
			}
		}	  	
	}
	
  
  	/**
   * Animationを追加する。
   * @param 追加するAnimation
   */
  public void add(E animation) {
	  AnimationContainer ac = new AnimationContainer(animation);
	  if(isRunning()) {
		  ac.init();
	  }
	  m_animationContainerList.add(ac);
  }
  
  /**
   * Animationを削除する 
   */
  public void remove(E animation) {
	  for(AnimationContainer ac : m_animationContainerList) {
		  if(ac.getAnimation() == animation) {
			  remove(ac);
			  break;
		  }
	  }
  }
  
  /**
   * Animationを削除する。
   * @param ac 削除する対象を指定するAnimationContaier
   */
  protected void remove(AnimationContainer ac) {
	  m_animationContainerList.remove(ac);
	  ac.getAnimation().exit();	  
  }
  
  
  /**
   * 登録されている全てのAnimationに対応する
   * AnimationContainerを返すIterator.
   * @return
   */
  protected Iterator<AnimationContainer> getAnimationContainerIterator() {
	  return m_animationContainerList.iterator();
  }
  
}
