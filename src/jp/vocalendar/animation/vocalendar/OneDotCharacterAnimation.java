package jp.vocalendar.animation.vocalendar;

import jp.vocalendar.animation.canvas.CanvasAnimation;
import jp.vocalendar.animation.canvas.CanvasAnimationSupport;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class OneDotCharacterAnimation extends CanvasAnimationSupport
implements CanvasAnimation {
	private int left, top, right, bottom;
	private int bitmapsIndex = 0;
	private Bitmap[] bitmaps = null;
	private int intervalMilliSecond = 250;
	
	@Override
	public void init() {
		super.init();		
	}

	@Override
	public UpdateTime update(long time) {
		if(bitmaps != null) {
			bitmapsIndex =
					(int)(time / intervalMilliSecond) % bitmaps.length;
		}
		return UpdateTime.KEEP;
	}
	
	@Override
	public void draw(Canvas canvas) {		
		Bitmap b = bitmaps[bitmapsIndex];
		Paint p = new Paint();
		p.setAntiAlias(false);
		p.setFilterBitmap(false);
		Rect dst = new Rect(left, top, right, bottom);
		canvas.drawBitmap(b, null, dst, p);
	}

	public int getIntervalMilliSecond() {
		return intervalMilliSecond;
	}

	public void setIntervalMilliSecond(int intervalMilliSecond) {
		this.intervalMilliSecond = intervalMilliSecond;
	}

	public Bitmap[] getBitmaps() {
		return bitmaps;
	}

	public void setBitmaps(Bitmap[] bitmaps) {
		this.bitmaps = bitmaps;
	}
	
	/**
	 * キャラクター表示位置。
	 * @param 
	 * @param y
	 */
	public void setPosition(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public void setPosition(CharacterCordinate.Position p) {
		setPosition(
				p.getPixelLeft(), p.getPixelTop(), p.getPiexelRight(), p.getPixelBottom());
	}
}
