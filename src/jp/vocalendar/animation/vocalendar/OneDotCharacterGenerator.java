package jp.vocalendar.animation.vocalendar;

/**
 * ドットキャラクターを生成するクラスの抽象クラス
 */
public abstract class OneDotCharacterGenerator {
	/** 次のキャラクターを出力するタイミングであればtrueを返す */
	public abstract boolean toAddNextOneDocCharacter(long time);
	
	/** 次のキャラクターの座標を設定する */
	public abstract void setPosition(OneDotCharacterAnimation a, CharacterCordinate cordinate);
	
	/**
	 * ランダムにキャラクターを配置する
	 */
	public static class Random extends OneDotCharacterGenerator {
		private int intervalMilliSecondToAddCharacter;
		protected int maxCharacter;
		protected int numberOfAddedCharacter = 1;
		
		public Random(int intervalMilliSecondToAddCharacter, int maxCharacter) {
			this.intervalMilliSecondToAddCharacter = intervalMilliSecondToAddCharacter;
			this.maxCharacter = maxCharacter;
		}
		
		public boolean toAddNextOneDocCharacter(long time) {		
			if(time >= intervalMilliSecondToAddCharacter && numberOfAddedCharacter < maxCharacter) {
				numberOfAddedCharacter++;
				return true;
			}
			return false;
		}
		
		public void setPosition(OneDotCharacterAnimation a, CharacterCordinate cordinate) {
			int x = 0;
			int y = 0;
			int w = cordinate.getWidth();
			int h = cordinate.getHeight();
			for(int i = 0; i < w * h; i++) { // 最大マス目の数だけ繰り返し
				x = (int)(Math.random() * w);
				y = (int)(Math.random() * h);
				if(cordinate.isEmpty(x, y)) {
					break;
				}
			}
			a.setPosition(cordinate.put(a, x, y));
		}
	}
	
	/** 横一列にキャラクターを並べる */
	public static class Linear extends Random {
		public Linear(int intervalMilliSecondToAddCharacter, int maxCharacter) {
			super(intervalMilliSecondToAddCharacter, maxCharacter);
		}

		public void setPosition(OneDotCharacterAnimation a, CharacterCordinate cordinate) {
			int offset = numberOfAddedCharacter / 2;
			int x = cordinate.getWidth() / 2;
			int y = cordinate.getHeight() / 2;
			if(numberOfAddedCharacter % 2 == 1) {
				x += offset;
			} else {
				x -= offset;
			}
			a.setPosition(cordinate.put(a, x, y));
			if(numberOfAddedCharacter == cordinate.getWidth()) {
				maxCharacter = 0; // 横一列に並んだらこれ以上キャラクターは追加しない
			}
		}
	}
}
