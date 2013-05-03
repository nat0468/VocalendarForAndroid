package jp.vocalendar.animation.vocalendar;

import jp.vocalendar.R;
import jp.vocalendar.animation.AnimationManager;
import jp.vocalendar.animation.canvas.CanvasAnimation;
import jp.vocalendar.animation.canvas.CanvasBackground;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;

public class WallpaperLoadingAnimation extends CanvasBackground
implements LoadingAnimation {
	private AnimationManager<CanvasAnimation> manager;
	private Context context;
		
	@Override
	public void setAnimationManager(AnimationManager<CanvasAnimation> manager) {
		this.manager = manager;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	public void init() {
		super.init();
		
		setColor(Color.rgb(0xf0, 0xf0, 0xf0));
		setBitmap(
				BitmapFactory.decodeResource(context.getResources(), R.drawable.wallpaper));		
	}

	@Override
	public Spanned getCreatorText() {
        String str = context.getResources().getText(R.string.illustration_by).toString()
				+ " <a href=\"http://www.elrowa.com/\">ELrowa</a>.";
        return Html.fromHtml(str);
	}
	
}
