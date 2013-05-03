package jp.vocalendar.animation.canvas;

import android.graphics.Canvas;
import jp.vocalendar.animation.AnimationSupport;

/**
 * Java2DAnimationの実装を簡単にするための抽象クラス。
 * 必要に応じてサブクラスでメソッドを実装する。
 */
public abstract class CanvasAnimationSupport
extends AnimationSupport implements CanvasAnimation {
	public void draw(Canvas canvas) {}
}
