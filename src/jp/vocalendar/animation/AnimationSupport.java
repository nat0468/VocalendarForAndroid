package jp.vocalendar.animation;

/**
 * Animationの実装を簡単にするための抽象クラス。
 * 必要に応じてサブクラスでメソッドを実装する。
 */
public abstract class AnimationSupport implements Animation {
	protected Status m_status = Status.RUNNING;
	
	public void init() {}
	public UpdateTime update(long time) { return UpdateTime.KEEP; }
	public void exit() {}
	public Status getStatus() { return m_status; }
	public void setStatus(Status status) { m_status = status; }
}
