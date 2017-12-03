package com.openyogaland.denis.spreadsheets;

import android.content.Context;

// Google API libraries
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

// Java libraries
import java.util.Arrays;
import java.util.List;

// Class GoogleServicesHelper incapsulates all interactions with GoogleServices
class GoogleServicesHelper
{
  // TODO switch scopes and combine them using bitflags
  private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};
  
  GoogleAccountCredential googleAccountCredential;
  
  // Constructor called with explicit stringScopes[] parameter
  GoogleServicesHelper(Context context, String[] stringScopes)
  {
    // represent String[] array as List<String>
    List<String> scope        = Arrays.asList(stringScopes),
                 defaultScope = Arrays.asList(SCOPES);

    // checking if stringScopes[] contains constants from SheetsScopes
    if (scope.contains(SheetsScopes.DRIVE) ||
        scope.contains(SheetsScopes.DRIVE_FILE) ||
        scope.contains(SheetsScopes.DRIVE_READONLY) ||
        scope.contains(SheetsScopes.SPREADSHEETS) ||
        scope.contains(SheetsScopes.SPREADSHEETS_READONLY))
    {
      googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, scope);
    }
    else // use default scope SheetsScopes.SPREADSHEETS_READONLY
    {
      googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, defaultScope);
    }
    
    googleAccountCredential.setBackOff(new ExponentialBackOff());
  }
  
  // Constructor called without using default stringScopes[]
  GoogleServicesHelper(Context context)
  {
    this(context, SCOPES);
  }
}
