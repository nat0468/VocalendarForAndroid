package jp.vocalendar.animation;

/**
 * Animationのインスタンスを管理するクラスが実装するインターフェイス 
 */
public interface AnimationManager<E extends Animation> {
	/**
	 * Animationを追加する。
	 * @param animation
	 */
	public void add(E animation);
	
	/**
	 * Animationを削除する。
	 * @param animation
	 */
	public void remove(E animation);
	
	/**
	 * アニメーションが実行中かどうか判定する。
	 * @return 実行中ならtrueを返す。
	 */
	public boolean isRunning();
	
	/**
	 * アニメーションの更新速度をFPSで返す。
	 * @return
	 */
	public int getFps();
	
	/**
	 * アニメーションを更新する速度をミリ秒単位で返す。
	 * @return
	 */
	public int getUpdatePeriod();	
}
