package jp.vocalendar.animation.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 背景表示を表すクラス
 */
public class CanvasBackground extends CanvasAnimationSupport {
	private int color = Color.WHITE;
	private Bitmap bitmap;
	
	public CanvasBackground() {
	}
	
	public void draw(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(color);
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
		if(bitmap != null) {
			canvas.drawBitmap(
					bitmap,
					new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
					calcBitmapDest(canvas),
					p);
		}
	}

	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	
	/**
	 * 画像を描画する位置の計算
	 * @return
	 */
    private Rect calcBitmapDest(Canvas canvas) {
    	// 画像とバッファのサイズ
    	double orgWidth = bitmap.getWidth();
    	double orgHeight = bitmap.getHeight();
    	double destWidth = canvas.getWidth();
    	double destHeight = canvas.getHeight();
    	
    	// 画像の縦横比
        double orgRatio = (double)orgWidth / orgHeight;
        double destRatio = (double)destWidth / destHeight;
        
        Rect rect = new Rect(); //描画位置
        
        if(orgRatio < destRatio) { // 描画先の方が幅が大きい
            double scale = (double)destHeight / orgHeight; // 描画先の高さに合わせる
            double imageWidth = orgWidth * scale;
            
            rect.set(
            	(int)(destWidth - imageWidth) / 2, 0,
            	(int)(destWidth + imageWidth) / 2, (int)destHeight);
        } else { // 描画先の方が高さが大きい
            double scale = (double)destWidth / orgWidth; // 描画先の幅に合わせる
            double imageHeight = orgHeight * scale;
            rect.set(
            	0, (int)(destHeight - imageHeight) / 2,
            	(int)destWidth, (int)destHeight - (int)((destHeight - imageHeight)/2));
        }      
        return rect;
    }
}
