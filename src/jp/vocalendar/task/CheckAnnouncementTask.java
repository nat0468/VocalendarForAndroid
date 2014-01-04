package jp.vocalendar.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.model.EventDataBaseRow;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 読み込み中画面に表示するお知らせの有無をチェックするタスク
 */
public class CheckAnnouncementTask extends
		AsyncTask<String, Void, Boolean> {
	/**
	 * 確認終了後にコールバックするインスタンスのインターフェイス
	 */
	public static interface Callback {
		public void onPostExecute(Context context, boolean result);
	}
	
	private static final String TAG = "CheckAnnouncementTask";

	private static final String IMAGE_FILE_NAME = "image.jpg";
	private static final String URL_FILE_NAME ="url.txt";
	
	private Context context;
	private Callback callback;
	private String previousUrl;
	
	public CheckAnnouncementTask(Context context, Callback callback) {
		this.context = context;
		this.callback = callback;
		this.previousUrl = VocalendarApplication.getAnnouncementURL(context);  
	}
	
	/**
	 * @param path お知らせファイルを置く場所へのパス('/'で終わる)
	 */
	@Override
	protected Boolean doInBackground(String... path) {				
		URL imageFile;
		URL urlFile;
		try {
			imageFile = new URL(path[0] + IMAGE_FILE_NAME);
			urlFile = new URL(path[0] + URL_FILE_NAME);
		} catch (MalformedURLException e) {
			Log.d(TAG, "invalid url", e);
			return false;
		}
		
		if(!checkExist(imageFile)) {
			unsetURL();
			VocalendarApplication.setAnnouncementDisplayed(context, false);
			return Boolean.FALSE;
		}
		String url;
		try {
			saveImageFile(load(imageFile));
			url = saveURL(load(urlFile));
		} catch(IOException e) {
			Log.d(TAG, "failed to save ImageFile and URL.", e);
			return Boolean.FALSE;
		}
		if(!url.equals(previousUrl)) { //URL変更(お知らせ変更時)
			VocalendarApplication.setAnnouncementDisplayed(context, false); //このお知らせは未表示			
		}
		return Boolean.TRUE;
	}
	
	private boolean checkExist(URL url) {
		Log.d(TAG, "checkExist:" + url.toString());
		HttpURLConnection con = null;

		try {
			con = (HttpURLConnection)url.openConnection();			
			con.setAllowUserInteraction(false);
		    con.setInstanceFollowRedirects(true);
		    con.setRequestMethod("HEAD");
		    con.connect();
		    int response = con.getResponseCode();
		    if(response == HttpURLConnection.HTTP_OK){
		    	return true;
		    }
		    return false;
		} catch(IOException e) {
			Log.d(TAG, "checkExist failed. " + e.toString());
			return false;
		} finally {
			if(con != null) {
				con.disconnect();
			}
		}		
	}

	private InputStream load(URL url) throws IOException {
		Log.d(TAG, "load:" + url.toString());
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		try {		
			con.setAllowUserInteraction(false);
		    con.setInstanceFollowRedirects(true);
		    con.setRequestMethod("GET");
		    con.connect();
		    int response = con.getResponseCode();
		    if(response != HttpURLConnection.HTTP_OK){
		    	Log.d(TAG, "load failed. response code:" + response);
		    	throw new IOException("Http reponse code is not 200. code:" + response);
		    }
		    return con.getInputStream();
		} catch(IOException e) {
			con.disconnect();
			throw e;
		}
	}
	
	private void saveImageFile(InputStream inputStream) throws IOException {
		Log.d(TAG, "saveImageFile...");
		BufferedInputStream in = new BufferedInputStream(inputStream);
		BufferedOutputStream out =
				new BufferedOutputStream(context.openFileOutput(IMAGE_FILE_NAME, Context.MODE_PRIVATE));
		byte[] buffer = new byte[1024];
		int size = in.read(buffer);
		while(size != -1) {
			out.write(buffer, 0, size);
			size = in.read(buffer);
		}
		out.close();
		in.close();
		Log.d(TAG, "saveImageFile finished.");
	}
	
	/**
	 * URLをInputStreamから読み込んでプリファレンスに保存する
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private String saveURL(InputStream inputStream) throws IOException {
		Log.d(TAG, "saveURL...");
		BufferedInputStream in = new BufferedInputStream(inputStream);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c = in.read();
		while(c != -1) {
			out.write(c);
			c = in.read();
		}
		in.close();
		String url = out.toString().trim();
		VocalendarApplication.setLoadingNotificationURL(context, url);
		Log.d(TAG, "saveURL finished: " + url);
		return url;
	}
	
	private void unsetURL() {
		Log.d(TAG, "unsetURL");
		VocalendarApplication.setLoadingNotificationURL(context, null);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(callback != null) {
			callback.onPostExecute(context, result);
		}
	}
	
	public static InputStream openImageFile(Context context) {
		try {
			return context.openFileInput(IMAGE_FILE_NAME);
		} catch (FileNotFoundException e) {
			Log.d(TAG, IMAGE_FILE_NAME, e); // ここに来ることはありえない
		}		
		return null;
	}
}
