package jp.vocalendar.activity;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.activity.view.AnimationSurfaceView;
import jp.vocalendar.animation.vocalendar.LoadingAnimation;
import jp.vocalendar.animation.vocalendar.LoadingAnimationUtil;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PreviewLoadingPageActivity extends ActionBarActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        getSupportActionBar().hide();
        
        Button cancel = (Button)findViewById(R.id.cancelButton);
        cancel.setText(getResources().getString(R.string.back));
        cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        initAnimation();
	}

	protected void initAnimation() {
		AnimationSurfaceView view = getAnimationSurfaceView();
		LoadingAnimation anim = makeLoadingAnimation();
		view.addAnimation(anim);
		TextView tv = (TextView)findViewById(R.id.loadingImageCreatorText);
		tv.setMovementMethod(LinkMovementMethod.getInstance());		
		tv.setText(anim.getCreatorText());
	}

	private AnimationSurfaceView getAnimationSurfaceView() {
		AnimationSurfaceView view =
				(AnimationSurfaceView)findViewById(R.id.loadingAnimationSurfaceView);
		return view;
	}
	
	private LoadingAnimation makeLoadingAnimation() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String value = pref.getString(
				Constants.LOADING_PAGE_PREFERENCE_NAME, Constants.LOADING_PAGE_RANDOM);
		return LoadingAnimationUtil.makeLoadingAnimation(value);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getAnimationSurfaceView().pause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		getAnimationSurfaceView().resume();
	}
	
}
