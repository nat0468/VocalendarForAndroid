package jp.vocalendar.animation.vocalendar;

import jp.vocalendar.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;

/**
 * ドットアニメーションの全体を管理するアニメーション。
 * 背景の描画も担当する。個々のキャラクター描画はOneDotCharacterAnimationが担当。
 * アニメーションの動きはランダム。
 */
public class DotCharacterAnimation extends LoadingAnimationSupport
implements LoadingAnimation {
	/** ドットアニメーション用drawable */
	private static int[][] drawables = {
			{ R.drawable.gacpo1, R.drawable.gacpo2, R.drawable.gacpo3, R.drawable.gacpo2  },
			{ R.drawable.gumi1, R.drawable.gumi2, R.drawable.gumi3, R.drawable.gumi2  },
			{ R.drawable.kaito1, R.drawable.kaito2, R.drawable.kaito3, R.drawable.kaito2  },
			{ R.drawable.len1, R.drawable.len2, R.drawable.len3, R.drawable.len2  },
			{ R.drawable.luka1, R.drawable.luka2, R.drawable.luka3, R.drawable.luka2  },
			{ R.drawable.meiko1, R.drawable.meiko2, R.drawable.meiko3, R.drawable.meiko2  },
			{ R.drawable.miki1, R.drawable.miki2, R.drawable.miki3, R.drawable.miki2  },
			{ R.drawable.miku1, R.drawable.miku2, R.drawable.miku3, R.drawable.miku2  },
			{ R.drawable.rin1, R.drawable.rin2, R.drawable.rin3, R.drawable.rin2  },
			{ R.drawable.teto1, R.drawable.teto2, R.drawable.teto3, R.drawable.teto2  },
			{ R.drawable.yukari1, R.drawable.yukari2, R.drawable.yukari3, R.drawable.yukari2  },
	};
	
	private static final int DRAWABLES_INDEX_SHUFFLE_COUNTER = drawables.length;
	
	/** ドットアニメーション用drawableの表示順インデックス */
	protected int[] drawablesIndex = new int[drawables.length];
	
	/** 次に表示するドットアニメーション用drawableインデックス */
	private int nextDrawablesIndex = 0;
	
	private int color;
	private int defaultIntervalMilliSecond = 250;
	
	protected int intervalMilliSecondToAddCharacter = defaultIntervalMilliSecond * 4;
		
	/** キャラクターを配置する座標 */
	private CharacterCordinate cordinate = null;
	
	/** 初期化待ちの座標。初期化されたらcordianteに格納する。 */
	private CharacterCordinate cordinateToInit = null;
	
	private OneDotCharacterGenerator generator;
			
	
	/** 描画エリア(Canvas)の幅 */
	private int width = Integer.MIN_VALUE;
	/** 描画エリア(Canvas)の高さ */
	private int height = Integer.MIN_VALUE;
	
	@Override
	public Spanned getCreatorText() {
		return new SpannableString("Illustaration by イディ");
	}
	
	@Override
	public void init() {
		super.init();	
		color = Color.rgb(240, 240, 240);
		initDrawablesIndex();
		generator = makeOneDotCharacterGenerator();
	}
	
	protected OneDotCharacterGenerator makeOneDotCharacterGenerator() {
		int i = (int)Math.random() * 2;
		if(i == 0) {
			return new OneDotCharacterGenerator.Random(intervalMilliSecondToAddCharacter, drawablesIndex.length);
		}
		return new OneDotCharacterGenerator.Linear(intervalMilliSecondToAddCharacter, drawablesIndex.length);
	}
	
	private void initDrawablesIndex() {
		for(int i = 0; i < drawablesIndex.length; i++) {
			drawablesIndex[i] = i;
		}
		for(int i = 0; i < DRAWABLES_INDEX_SHUFFLE_COUNTER; i++) {
			swapDrawablesIndex(
					(int)(Math.random() * drawablesIndex.length),
					(int)(Math.random() * drawablesIndex.length));
		}
	}
	
	private void swapDrawablesIndex(int a, int b) {
		int av = drawablesIndex[a];
		drawablesIndex[a] = drawablesIndex[b];
		drawablesIndex[b] = av;		
	}
	
	protected OneDotCharacterAnimation getNextOneDotCharacterAnimation() {
		OneDotCharacterAnimation a = new OneDotCharacterAnimation();
		a.setBitmaps(makeBitmaps(drawables[drawablesIndex[nextDrawablesIndex++]]));
		a.setIntervalMilliSecond(defaultIntervalMilliSecond);		
		return a;
	}
	
	Bitmap[] makeBitmaps(int[] drawables) {
		Bitmap[] bitmaps = new Bitmap[drawables.length];
		for(int i = 0; i < drawables.length; i++) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inScaled = false;
			bitmaps[i] = BitmapFactory.decodeResource(context.getResources(), drawables[i], opt);		
		}
		return bitmaps;
	}

	
	@Override
	public void draw(Canvas canvas) {
		if(cordinate == null && cordinateToInit == null) {
			cordinateToInit = new CharacterCordinate(); // 次のupdate()時に初期化する
			width = canvas.getWidth();
			height = canvas.getHeight();
		}
		
		Paint p = new Paint();
		p.setColor(color);
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
	}

	@Override
	public UpdateTime update(long time) {
		if(cordinateToInit != null) {
			cordinate = cordinateToInit;
			cordinateToInit = null;
			OneDotCharacterAnimation a = getNextOneDotCharacterAnimation();
			int size = a.getBitmaps()[0].getWidth() * 2;
			cordinate.init(width, height, size, context);
			generator.setPosition(a, cordinate);
			manager.add(a);
		}
		
		if(generator.toAddNextOneDocCharacter(time)) {
			OneDotCharacterAnimation a = getNextOneDotCharacterAnimation();
			generator.setPosition(a, cordinate);
			manager.add(a);
			return UpdateTime.RESET;
		}
		return UpdateTime.KEEP;
	}
	
	/** 
	 * アニメーションの動きがランダム位置に表示固定。
	 */
	public static class RandomDotCharacterAnimation extends DotCharacterAnimation {
		protected OneDotCharacterGenerator makeOneDotCharacterGenerator() {
			return new OneDotCharacterGenerator.Random(intervalMilliSecondToAddCharacter, drawablesIndex.length);
		}		
	}

	/**
	 * アニメーションの動きが横一列表示固定
	 */
	public static class LinearDotCharacterAnimation extends DotCharacterAnimation {
		protected OneDotCharacterGenerator makeOneDotCharacterGenerator() {
			return new OneDotCharacterGenerator.Linear(intervalMilliSecondToAddCharacter, drawablesIndex.length);
		}		
	}
}
