package jp.vocalendar.activity.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ImageView extends View {
	private static final String TAG = "ImageView";
	private Bitmap bitmap;
	private RectF bitmapDestination;
	
	public ImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	private void makeBitmapDestination() {
		RectF imageRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF viewRect = new RectF(0, 0, getWidth(), getHeight());
		Matrix m = new Matrix();
		m.setRectToRect(imageRect, viewRect, ScaleToFit.START);
		bitmapDestination = new RectF();
		m.mapRect(bitmapDestination, imageRect);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(bitmap != null) {
			Log.d(TAG, "onDraw");
			if(bitmapDestination == null) {
				makeBitmapDestination();
			}
			canvas.drawBitmap(bitmap, null, bitmapDestination, null);
		}
	}
}
