package com.openyogaland.denis.spreadsheets;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

// class MainActivity represents UI work logic and user interactions
public class MainActivity extends AppCompatActivity implements OnClickListener
{
  // Constants
  private static final int REQUEST_ACCOUNT_PICKER          = 1000;
  private static final int REQUEST_AUTHORIZATION           = 1001;
  private static final int REQUEST_GOOGLE_PLAY_SERVICES    = 1002;
  private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
  private static final String PREF_ACCOUNT_NAME            = "accountName";
  
  // GoogleServicesHelper class incapsulates interactions with GoogleServices
  GoogleServicesHelper    googleServicesHelper  = null;
  
  // User Interface fields
  ProgressDialog          mProgress             = null;
  TextView                mOutputText           = null;
  Button                  mCallApiButton        = null;
  
  // First method to call then Activity is created
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_layout);

    mCallApiButton = findViewById(R.id.mCallApiButton);
    mCallApiButton.setOnClickListener(this);
    
    mOutputText = findViewById(R.id.mOutputText);
    mOutputText.setMovementMethod(new ScrollingMovementMethod());
    
    mProgress = new ProgressDialog(this);
    mProgress.setMessage(getString(R.string.calling_spreadsheets));
  
    // Initialize GoogleServicesHelper
     googleServicesHelper = new GoogleServicesHelper(getApplicationContext());
  }
  
  // Implementing method onClick(View) from OnClickListener interface
  public void onClick(View view)
  {
    // Chtcking view id
    switch (view.getId())
    {
      // in case of mCallApiButton
      case R.id.mCallApiButton:
        mCallApiButton.setEnabled(false);
        mOutputText.setText("");
        getResultsFromApi();
        mCallApiButton.setEnabled(true);
        break;
    }
  }
  
  // Verify that Google Play Services are installed, an account was selected and the device
  // currently has online access. Else the app will prompt the user.
  // After that attempt to call the API
  // TODO check if Google Play Services are available
  void getResultsFromApi()
  {
    if(!isGooglePlayServicesAvailable())
    {
      acquireGooglePlayServices();
    }
    else if(googleServicesHelper.googleAccountCredential.getSelectedAccountName() == null)
    {
      chooseAccount();
    }
    else if(!isDeviceOnline())
    {
      mOutputText.setText("No network connection available.");
    }
    else
    {
      new MakeRequestTask(googleServicesHelper.googleAccountCredential, mOutputText, mProgress)
          .execute();
    }
  }
  
  // Check that Google Play services APK is installed and up to date.
  // Return true if Google Play Services is available and up to date on this device
  private boolean isGooglePlayServicesAvailable()
  {
    GoogleApiAvailability apiAvailability      = GoogleApiAvailability.getInstance();
    final int             connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
    return connectionStatusCode == ConnectionResult.SUCCESS;
  }
  
  // Attempt to resolve a missing, out-of-date, invalid or disabled Google Play Services
  // installation via a user dialog
  private void acquireGooglePlayServices()
  {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
    if(apiAvailability.isUserResolvableError(connectionStatusCode))
    {
      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }
  }
  
  // TODO remove method if never used
   /* Respond to requests for permissions at runtime for API 23 and above.
   *
   * @param requestCode
   *     The request code passed in
   *     requestPermissions(android.app.Activity, String, int, String[])
   * @param permissions
   *     The requested permissions. Never null.
   * @param grantResults
   *     The grant results for the corresponding permissions
   *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
      grantResults)
  {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
  
  /**
   * Attempts to set the account used with the API credentials. If an account
   * name was previously saved it will use that one; otherwise an account
   * picker dialog will be shown to the user. Note that the setting the
   * account to use with the credentials object requires the app to have the
   * GET_ACCOUNTS permission
   */
  private void chooseAccount()
  {
    String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
    if(accountName != null)
    {
      googleServicesHelper.googleAccountCredential.setSelectedAccountName(accountName);
      getResultsFromApi();
    }
    else
    {
      // Start a dialog from which the user can choose an account
      startActivityForResult(googleServicesHelper.googleAccountCredential.newChooseAccountIntent(),
          REQUEST_ACCOUNT_PICKER);
    }
  }
  
  /**
   * Called when an activity launched here (specifically, AccountPicker
   * and authorization) exits, giving you the requestCode you started it with,
   * the resultCode it returned, and any additional data from it.
   *
   * @param requestCode
   *     code indicating which activity result is incoming.
   * @param resultCode
   *     code indicating the result of the incoming
   *     activity result.
   * @param data
   *     Intent (containing result data) returned by incoming
   *     activity result.
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    switch(requestCode)
    {
      case REQUEST_GOOGLE_PLAY_SERVICES:
        if(resultCode != RESULT_OK)
        {
          mOutputText.setText("This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.");
        }
        else
        {
          getResultsFromApi();
        }
        break;
      case REQUEST_ACCOUNT_PICKER:
        if(resultCode == RESULT_OK && data != null && data.getExtras() != null)
        {
          String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          if(accountName != null)
          {
            SharedPreferences        settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor   = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, accountName);
            editor.apply();
            googleServicesHelper.googleAccountCredential.setSelectedAccountName(accountName);
            getResultsFromApi();
          }
        }
        break;
      case REQUEST_AUTHORIZATION:
        if(resultCode == RESULT_OK)
        {
          getResultsFromApi();
        }
        break;
    }
  }
  
  /**
   * Checks whether the device currently has a network connection.
   *
   * @return true if the device has a network connection, false otherwise.
   */
  private boolean isDeviceOnline()
  {
    ConnectivityManager connMgr     = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo         networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }
  
  /**
   * Display an error dialog showing that Google Play Services is missing
   * or out of date.
   *
   * @param connectionStatusCode
   *     code describing the presence (or lack of)
   *     Google Play Services on this device.
   */
  private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode)
  {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    Dialog dialog = apiAvailability.getErrorDialog(MainActivity.this, connectionStatusCode,
        REQUEST_GOOGLE_PLAY_SERVICES);
    dialog.show();
  }
}
