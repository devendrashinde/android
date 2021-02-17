package com.example.dshinde.myapplication_xmlpref.listners;

public interface BiometricCallbackListener {

    void onAuthenticationSuccessful();

    void onAuthenticationHelp(int helpCode, CharSequence helpString);

    void onAuthenticationError(int errorCode, CharSequence errString);

    void onAuthenticationFailed();
}
