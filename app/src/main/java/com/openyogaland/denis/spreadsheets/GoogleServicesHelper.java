package com.openyogaland.denis.spreadsheets;

import android.content.Context;

// Google API libraries
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

// Java libraries
import java.util.Arrays;

// Class GoogleServicesHelper incapsulates all interactions with GoogleServices
class GoogleServicesHelper
{
  // TODO move the scopes to XML as integers to use switch() and also use bitflags to combine scopes
  static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};
  
  GoogleAccountCredential googleAccountCredential;
  
  // If constructor is called with explicit String[] stringScopes parameter
  public GoogleServicesHelper(Context context, String[] stringScopes)
  {
    // TODO: check the SheetsScopes argument
    googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(stringScopes));
    googleAccountCredential.setBackOff(new ExponentialBackOff());
  }
  
  // if constructor is called without String[] stringScopes parameters - using default
  public GoogleServicesHelper(Context context)
  {
    this(context, SCOPES);
  }
}
