package jp.vocalendar.animation.vocalendar;

import android.content.Context;

/**
 * キャラクターを配置する座標を表すクラス。
 * キャラクターを配置した座標を格納する機能も提供する。
 */
public class CharacterCordinate {
	/** 横方向のマスの数 */
	private int width;
	/** 縦方向のマスの数 */
	private int height;
	/** 1マスのピクセルサイズ */
	private float squarePixelSize;
	
	/** マスに格納しているキャラクターの番号を格納する配列 */
	private OneDotCharacterAnimation squares[][];
	
	/** 一番左上のマスのX座標オフセット */
	private int offsetX;
	/** 一番左上のマスのY座標オフセット */
	private int offsetY;

	
	
	/**
	 * 座標空間を初期化する
	 * @param pixelWidth
	 * @param piexelHeight
	 * @param characterSize
	 */
	public void init(int pixelWidth, int pixelHeight, int characterSize, Context context) {
		squarePixelSize = characterSize * context.getResources().getDisplayMetrics().density; // 1マスの大きさ
		width = (int)(pixelWidth / squarePixelSize);
		offsetX = (int) (pixelWidth - width * squarePixelSize) / 2;
		height = (int)(pixelHeight / squarePixelSize);
		offsetY = (int)(pixelHeight - height * squarePixelSize) / 2;		
		squares = new OneDotCharacterAnimation[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				squares[x][y] = null;
			}
		}
	}
	
	public Position put(OneDotCharacterAnimation character, int x, int y) {
		squares[x][y] = character;
		return new Position(
				x, y,
				(int)(offsetX + squarePixelSize * x), 
				(int)(offsetY + squarePixelSize * y),
				(int)(offsetX + squarePixelSize * (x+1)),
				(int)(offsetY + squarePixelSize * (y+1)));
	}
	
	public OneDotCharacterAnimation get(int x, int y) {
		return squares[x][y];
	}
	
	public boolean isEmpty(int x, int y) {
		return (squares[x][y] == null);
	}
	
	/** 座標中の位置を表すクラス */
	public static class Position {
		/** 座標中のX座標 */
		private int x;
		/** 座標中のY座標 */
		private int y;
		
		/** ピクセル換算の上端のY座標 */
		private int pixelTop;
		/** ピクセル換算の下端のY座標 */
		private int pixelBottom;
		/** ピクセル換算の左端のX座標 */
		private int pixelLeft;
		/** ピクセル換算の右端のX座標 */		
		private int piexelRight;
		
		Position(int x, int y, int pixelLeft, int pixelTop, int pixelRight, int pixelBottom) {
			this.x = x;
			this.y = y;
			this.pixelLeft = pixelLeft;
			this.pixelTop = pixelTop;
			this.piexelRight = pixelRight;
			this.pixelBottom = pixelBottom;
		}

		public int getX() {
			return x;
		}


		public int getY() {
			return y;
		}

		public int getPixelTop() {
			return pixelTop;
		}

		public int getPixelBottom() {
			return pixelBottom;
		}

		public int getPixelLeft() {
			return pixelLeft;
		}

		public int getPiexelRight() {
			return piexelRight;
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
