/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package jp.vocalendar.googleapi;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import jp.vocalendar.Constants;
import jp.vocalendar.R;

/**
 * OAuthManager let's the entire application retrieve an OAuth 2.0 access token
 * from the AccountManager.
 */
public class OAuthManager {
	private static final String TAG = "OAuthManager"; 
	
	
  /**
   * An interface for receiving updates once the user has selected the account.
   */
  public interface AuthHandler {
    /**
     * Handle the account being selected.
     * 
     * @param account The selected account or null if none could be found
     * @param authToken The authorization token or null if access has been
     *        denied.
     * @param exception 例外発生時の例外を指定。処理成功時はnullを指定。
     * OperationCanceledException, IOException, AuthenticatorException,
     * NoAccountException のいずれかが入る。
     */
    public void handleAuth(Account account, String authToken, Exception exception);
  }

  /**
   * 利用可能なアカウントがない時に使われる例外。
   * AuthHandlerのhandleAuthメソッドの引数exceptionに指定することを想定。
   */
  public class NoAccountException extends Exception {
	  
  }
  
  /** The chosen account. */
  private Account account;

  /** The most recently fetched auth token or null if none is available. */
  private String authToken;

  /** Singleton instance. */
  private static OAuthManager instance;

  /**
   * Private constructor.
   */
  private OAuthManager() {
  }

  /**
   * Get the singleton instance of AccountChooser.
   * 
   * @return The instance of AccountChooser.
   */
  public static OAuthManager getInstance() {
    if (instance == null) {
      instance = new OAuthManager();
    }
    return instance;
  }

  /**
   * Returns the current account.
   * 
   * @return The current account or null if no account has been chosen.
   */
  public Account getAccount() {
    return account;
  }

  /**
   * Returns the current auth token. Response may be null if no valid auth token
   * has been fetched.
   * 
   * @return The current auth token or null if no auth token has been fetched
   */
  public String getAuthToken() {
    return authToken;
  }

  /**
   * Log-in using the preferred account, prompting the user to choose if no
   * preferred account is found.
   * 
   * @param invalidate Whether or not to invalidate the token.
   * @param activity The activity to use to display the prompt, get the
   *        AcountManager instance and SharedPreference instance.
   * @param callback The callback to call when an account and token has been
   *        found.
   */
  public void doLogin(boolean invalidate, Activity activity, Context context, AuthHandler callback) {
    if (account != null) {
      doLogin(account.name, invalidate, activity, context, callback);
    } else {
      SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);

      doLogin(preference.getString(Constants.SELECTED_ACCOUNT_PREFERENECE_NAME, ""),
    		  invalidate, activity, context, callback);
    }
  }


  /**
   * Log-in using the specified account name. If an empty account name is
   * provided, a prompt is shown to the user to choose the account to use and
   * saved in the preferences.
   * 
   * @param accountName The account name to use.
   * @param invalidate Whether or not to invalidate the token.
   * @param activity The activity to use to display the prompt, get the
   *        AcountManager instance and SharedPreference instance.
   * @param callback The callback to call when an account and token has been
   *        found.
   */
  public void doLogin(String accountName, boolean invalidate, 
		  Activity activity, Context context,  AuthHandler callback) {
	Log.d(TAG, "doLogin:" + accountName + "," + invalidate + "," + accountName + "," + callback);
    if (account != null && accountName.equals(account.name)) {
      if (!invalidate && authToken != null) {
        callback.handleAuth(account, authToken, null);
      } else {
        if (authToken != null && invalidate) {
          final AccountManager accountManager = AccountManager.get(context);
          accountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authToken);
          invalidate = false;
        }
        authorize(account, invalidate, activity, context, callback);
      }
    } else {
      chooseAccount(accountName, invalidate, activity, context, callback);
    }
  }

  /**
   * Request authorization to call the Calendar API on behalf of the specified
   * account.
   * 
   * @param account The account to request authorization for.
   * @param invalidate Whether or not to invalidate the token.
   * @param context The context to use to retrieve the AccountManager.
   * @param callback The callback to call when a token as been retrieved.
   */
  private void authorize(final Account account, final boolean invalidate,
		  final Activity activity, final Context context, final AuthHandler callback) {
    final AccountManager accountManager = AccountManager.get(context);

    accountManager.getAuthToken(account, Constants.OAUTH_SCOPE, true,
        new AccountManagerCallback<Bundle>() {
          @Override
          public void run(AccountManagerFuture<Bundle> future) {
            try {
              Bundle result = future.getResult();

              // AccountManager needs user to grant permission
              if (result.containsKey(AccountManager.KEY_INTENT)) {
            	  if(activity == null) {
            		  // Activityが指定されていない場合(Receiverからの呼び出し)は、処理キャンセル扱い
                      Log.e(TAG, "No Activity. Operation Canceled");
                      callback.handleAuth(null, null, new OperationCanceledException());            		  
            		  return;
            	  }
            	  Intent intent = (Intent) result.getParcelable(AccountManager.KEY_INTENT);
            	  intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
            	  activity.startActivityForResult(intent, Constants.GET_LOGIN);
            	  return;
              } else if (result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                Log.e(TAG, "Got auth token: " + invalidate);
                authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                if (invalidate) {
                  // Invalidate the current token and request a new one.
                  accountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authToken);
                  authorize(account, false, activity, context, callback);
                } else {
                  // Return the token to the callback.
                  callback.handleAuth(account, authToken, null);
                }
              }
            } catch (OperationCanceledException e) {
              Log.e(TAG, "Operation Canceled", e);
              callback.handleAuth(null, null, e);
            } catch (IOException e) {
              Log.e(TAG, "IOException", e);
              callback.handleAuth(null, null, e); 
            } catch (AuthenticatorException e) {
              Log.e(TAG, "Authentication Failed", e);
              callback.handleAuth(null, null, e);
            }
          }
        }, null /* handler */);
  }

  /**
   * Prompt the user to choose an account if more than one account is found and
   * none is matching the provided account name.
   * 
   * @param accountName Account name to look for.
   * @param invalidate Whether or not to invalidate the autToken.
   * @param activity The activity to use to display the prompt.
   * @param callback The callback to call when an account and token has been
   *        found.
   */
  private void chooseAccount(String accountName, final boolean invalidate,
		  final Activity activity, final Context context, final AuthHandler callback) {
    final Account[] accounts = new GoogleAccountManager(context).getAccounts();

    if (accounts.length < 1) {
      callback.handleAuth(null, null, new NoAccountException());
    } else if (accounts.length == 1) {
      gotAccount(accounts[0], invalidate, activity, context, callback);
    } else if (accountName != null && accountName.length() > 0) {
      for (Account account : accounts) {
        if (account.name.equals(accountName)) {
          gotAccount(account, invalidate, activity, context, callback);
          return;
        }
      }
    } else {
      // Let the user choose.
      Log.e(TAG, "Multiple matching accounts found.");

      // Build dialog.
      String[] choices = new String[accounts.length];
      for (int i = 0; i < accounts.length; i++) {
        choices[i] = accounts[i].name;
      }

      final AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(R.string.choose_account_title);
      builder.setItems(choices, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          gotAccount(accounts[which], invalidate, activity, context, callback);
        }
      });
      builder.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          callback.handleAuth(null, null, new OperationCanceledException());
        }
      });
      try {
    	  builder.show();
      } catch(RuntimeException e) {
    	  // UIスレッドと結びついていないContextで呼ばれた場合(Receiverからの呼び出し)
    	  callback.handleAuth(null, null, e);
      }
    }
  }

  /**
   * Save chosen account into the instance and shared preferences.
   * 
   * @param account Chosen account.
   * @param invalidate Whether or not to invalidate the auth token.
   * @param activity Activity to run thread on.
   * @param callback Method to call when auth token has been retrieved.
   */
  private void gotAccount(Account account, boolean invalidate,
		  Activity activity, Context context, AuthHandler callback) {
    SharedPreferences.Editor editor =
        PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("selected_account_preference", account.name);
    editor.commit();

    this.account = account;

    authorize(account, invalidate, activity, context, callback);
  }
}
