package jp.vocalendar.animation;

/**
 * 描画環境に依存するグラフィック描画以外のメソッドを定義した
 * Animationのスーパーインターフェイス。
 */
public interface Animation {

	/**
	 * 初期化処理を行う。
	 * update()が呼ばれる前に呼ばれる。
	 */
	public abstract void init();

	/**
	 * 一定時間経過毎に呼ばれる。
	 * 典型的には、その時間での表示位置計算など、表示処理の事前準備を行う。
	 * @param time 経過時間
	 */
	public abstract UpdateTime update(long time);

	/**
	 * Sceneのremove()メソッドで自分をSceneから削除されたときに呼ばれる。
	 * 必要であれば使用リソースの開放を行う。
	 */
	public abstract void exit();

	/**
	 * Animationの状態を返す。
	 * @return
	 */
	public abstract Status getStatus();

	/**
	 * update()の引数に渡す時間を制御する定数。
	 * update()の戻り値に使う。
	 */
	public enum UpdateTime {
		KEEP,		//経過時間はそのままにして続行。init()からの経過時間の合計を渡してもらうときに使う
		RESET		//経過時間を0に戻す。前回のupdate()からの経過時間を渡してもらうときに使う	
	}
	  
	/**
	 * Animationの状態を表す定数。
	 * getStatus()の戻り値に使う。
	 */
	enum Status {
		RUNNING,	//実行中
		FINISHED //終了(AnimationManagerの管理から外れ、exit()される対象となる)	
	}	
}