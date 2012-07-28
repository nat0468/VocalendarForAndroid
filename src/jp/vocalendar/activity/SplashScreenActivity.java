package jp.vocalendar.activity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

import edu.emory.mathcs.backport.java.util.LinkedList;

import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.GoogleCalendarLoadEventTask;
import jp.vocalendar.model.ICalendarLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * スプラッシュ画面を表示するActivity。
 * バックグランド処理でイベントを読み込む。
 */
public class SplashScreenActivity extends Activity {
	private static String TAG = "SplashScreenActivity";
	
	/** VOCALENDARのICSファイルのURL */
	private static String ICS_MAIN_URL = "https://www.google.com/calendar/ical/0mprpb041vjq02lk80vtu6ajgo%40group.calendar.google.com/public/basic.ics"; //メイン
	private static String ICS_BROADCAST_URL = "https://www.google.com/calendar/ical/5fsoru1dfaga56mcleu5mp76kk%40group.calendar.google.com/public/basic.ics"; //放送系 

	/** preferences 登録用キー */
	private static final String PREF_ACCOUNT_NAME = "accountName";
	private static final String PREF_AUTH_TOKEN = "authToken";	
	
	private static final String AUTH_TOKEN_TYPE = "cl";
	// private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/calendar.readonly";
	private static final int REQUEST_AUTHENTICATE = 0;

	private SharedPreferences settings;
	
	private LoadEventTask task = null;	
	private TextView loadingItemView = null;
	
	private Date today, tommorow;
	private TimeZone timeZone;
	private List<Event> foundEvents = new LinkedList();
	
	private GoogleAccountManager accountManager;
	private String accountName;
	private String authToken;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        setTitle(R.string.vocalendar);
        
        Button cancel = (Button)findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				task.cancel(false);
				transitToEventListActivity();
			}
		});
		loadingItemView = (TextView)findViewById(R.id.loadingItemView);
                
        initAccount();        
	}

	private void startLoadEventTask() {
        timeZone = TimeZone.getDefault();
		Calendar cal = Calendar.getInstance(timeZone);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime();
		
		cal.add(Calendar.DATE, +1);
		tommorow = cal.getTime();
		
    	try {
    		//task = new ICalendarLoadEventTask(this);
    		task = new GoogleCalendarLoadEventTask(this);
			task.execute(new URI(ICS_MAIN_URL), new URI(ICS_BROADCAST_URL));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void transitToEventListActivity() {
		Intent i = new Intent(this, EventListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);		
		finish();
	}

	public void onURIOpening(URI uri) {
		loadingItemView.setText("開いています: " + uri);		
	}
	
	public void onProgressUpdate(Event event) {
		String str = event.toDateTimeString();
		Log.d("SplashScreenActivity", str);
		loadingItemView.setText(str);
		if(event.equalByDate(today, timeZone)) {
			foundEvents.add(event);
		}
	}
	
	public void onPostExecute(List<Event> eventList) {		
		loadingItemView.setText("表示準備中");
		
		EventDataBase db = new EventDataBase(this);
		db.open();
		db.deleteAllEvent();
		Iterator<Event> itr = foundEvents.iterator();
		while(itr.hasNext()) {
			db.insertEvent(itr.next());
		}
		db.close();		
		
		transitToEventListActivity();
	}
	
	private void initAccount() {
	    settings = getPreferences(MODE_PRIVATE);
	    accountName = settings.getString(PREF_ACCOUNT_NAME, null);
	    authToken = settings.getString(PREF_AUTH_TOKEN, null);
	    accountManager = new GoogleAccountManager(this);

	    Account account = accountManager.getAccountByName(accountName);
	    if (account == null) {
	    	chooseAccount();
	    	return;
	    }
	    if (authToken != null) {	
	    	onAuthToken();
	        return;
	    }
	    accountManager.getAccountManager().getAuthToken(account, AUTH_TOKEN_TYPE, true,
	    		new AccountManagerCallback<Bundle>() {	    	
	              public void run(AccountManagerFuture<Bundle> future) {
	                try {
	                  Bundle bundle = future.getResult();
	                  if (bundle.containsKey(AccountManager.KEY_INTENT)) {
	                    Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
	                    intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
	                    startActivityForResult(intent, REQUEST_AUTHENTICATE);
	                  } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
	                    setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
	                    onAuthToken();
	                  }
	                } catch (Exception e) {
	                  Log.e(TAG, e.getMessage(), e);
	                }
	              }
	            }, null);	  	    
	}
	
	private void chooseAccount() {
		accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
				AUTH_TOKEN_TYPE, null, this, null, null,
				new AccountManagerCallback<Bundle>() {

			public void run(AccountManagerFuture<Bundle> future) {
				Bundle bundle;
				try {
					bundle = future.getResult();
					setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
					setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
					onAuthToken();
				} catch (OperationCanceledException e) {
					// user canceled
				} catch (AuthenticatorException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}, null);
	}

	  void setAccountName(String accountName) {
		  SharedPreferences.Editor editor = settings.edit();
		  editor.putString(PREF_ACCOUNT_NAME, accountName);
		  editor.commit();
		  this.accountName = accountName;
	  }

	  void setAuthToken(String authToken) {
		  SharedPreferences.Editor editor = settings.edit();
		  editor.putString(PREF_AUTH_TOKEN, authToken);
		  editor.commit();
		  this.authToken = authToken;
	  }
	  
	  private void onAuthToken() {
		  Log.i(TAG, "onAuthToken: " + accountName + "," + authToken);
	      startLoadEventTask();        		  
	  }
	  
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    switch (requestCode) {
	      case REQUEST_AUTHENTICATE:
	        if (resultCode == RESULT_OK) {
	          initAccount();
	        } else {
	          chooseAccount();
	        }
	        break;
	    }
	  }

	public String getAccountName() {
		return accountName;
	}

	public String getAuthToken() {
		return authToken;
	}	  
}
