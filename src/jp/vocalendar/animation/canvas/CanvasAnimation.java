package jp.vocalendar.animation.canvas;

import jp.vocalendar.animation.Animation;
import android.graphics.Canvas;

/**
 * キャラクターや画面上のコンポーネントなど、
 * 何らかの動作や表示など、アニメーションを行うクラスが実装する
 * インターフェイス。
 * 描画処理はCanvasを使う。
 */
public interface CanvasAnimation extends Animation {  
  /**
   * 表示処理を行う。
   * @param graphics 描画用のGraphics2D
   */
  public void draw(Canvas canvas);
}
