package com.openyogaland.denis.spreadsheets;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Google Sheets API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
// TODO remove UI interactions and use GoogleServicesHelper
  public class MakeRequestTask extends AsyncTask<Void, Void, List<String>>
  {
    // Variables
    Sheets         mService    = null;
    Exception      mLastError  = null;
    TextView       mOutputText = null;
    ProgressDialog mProgress   = null;
    
    // Constructor
    MakeRequestTask(GoogleAccountCredential credential, TextView mOutputText, ProgressDialog
        mProgress)
    {
      HttpTransport transport   = AndroidHttp.newCompatibleTransport();
      JsonFactory   jsonFactory = JacksonFactory.getDefaultInstance();
      
      mService = new Sheets.Builder(transport, jsonFactory, credential).setApplicationName("Spreadsheets").build();
      
      this.mOutputText = mOutputText;
      this.mProgress   = mProgress;
    }
    
    /**
     * Background task to call Google Sheets API.
     *
     * @param params
     *     no parameters needed for this task.
     */
    @Override protected List<String> doInBackground(Void... params)
    {
      try
      {
        return getDataFromApi();
      }
      catch(Exception e)
      {
        mLastError = e;
        cancel(true);
        return null;
      }
    }
    
    /**
     * Fetch a list of names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    private List<String> getDataFromApi() throws IOException
    {
      String       spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
      String       range         = "Class Data!A2:F";
      List<String> results       = new ArrayList<String>();
      ValueRange response = this.mService.spreadsheets().values().get(spreadsheetId, range).execute();
      List<List<Object>> values = response.getValues();
      if(values != null)
      {
        results.add("Name, Gender, Activity");
        for(List row : values)
        {
          results.add(row.get(0) + ", " + row.get(1) + ", " + row.get(5));
        }
      }
      return results;
    }
    
    
    @Override
    protected void onPreExecute()
    {
      mOutputText.setText("");
      mProgress.show();
    }
    
    @Override
    protected void onPostExecute(List<String> output)
    {
      mProgress.hide();
      if(output == null || output.size() == 0)
      {
        mOutputText.setText("No results returned.");
      }
      else
      {
        output.add(0, "Data retrieved using the Google Sheets API:");
        mOutputText.setText(TextUtils.join("\n", output));
      }
    }
    
    // TODO uncomment method onCancelled() and move some part to MainActivity UI class
    /**
    @Override
    protected void onCancelled()
    {
      mProgress.hide();
      if(mLastError != null)
      {
        if(mLastError instanceof GooglePlayServicesAvailabilityIOException)
        {
          showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
        }
        else if(mLastError instanceof UserRecoverableAuthIOException)
        {
          startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(),
              MainActivity.REQUEST_AUTHORIZATION);
        }
        else
        {
          mOutputText.setText(getString(R.string.ErrorOccurred) + mLastError.getMessage());
        }
      }
      else
      {
        mOutputText.setText(R.string.RequestCancelled);
      }
    }
    **/
  }