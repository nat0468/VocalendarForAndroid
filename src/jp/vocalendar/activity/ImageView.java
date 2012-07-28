package jp.vocalendar.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ImageView extends View {
	private static final String TAG = "ImageView";
	private Bitmap bitmap;
	
	
	public ImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(bitmap != null) {
			Log.d(TAG, "onDraw");
			Rect src = new Rect(
					0, 0, 
					bitmap.getWidth(), bitmap.getHeight());
			Rect dest = new Rect(0, 0, getWidth(), getHeight());
			canvas.drawBitmap(bitmap, src, dest, null);
		}
	}
}
