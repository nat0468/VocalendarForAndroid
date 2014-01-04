package jp.vocalendar.animation.vocalendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.animation.AnimationManager;
import jp.vocalendar.animation.canvas.CanvasAnimation;
import jp.vocalendar.animation.canvas.CanvasBackground;
import jp.vocalendar.task.CheckAnnouncementTask;

/**
 * お知らせを表示するAnimation
 */
public class AnnouncementAnimation extends CanvasBackground implements LoadingAnimation{
	private AnimationManager<CanvasAnimation> manager;
	private Context context;
	private String urlToOpen;
		
	@Override
	public void setContext(Context context) {
		this.context = context;
		urlToOpen = VocalendarApplication.getAnnouncementURL(context);
	}
	
	@Override
	public void init() {
		super.init();		
		setColor(Color.rgb(0xf0, 0xf0, 0xf0));
		Bitmap b = BitmapFactory.decodeStream(CheckAnnouncementTask.openImageFile(context));
		setBitmap(b);
	}

	@Override
	public Spanned getCreatorText() {
		String html = "<a href=\"" + urlToOpen + "\">" + urlToOpen + "</a>";
		return Html.fromHtml(html);
	}

	@Override
	public void setAnimationManager(AnimationManager<CanvasAnimation> manager) {
		this.manager = manager;
	}

	@Override
	public void onClick(View v) {		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
		context.startActivity(intent);		
	}
}
