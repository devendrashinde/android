package com.example.dshinde.myapplication_xmlpref.helper;

import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.dshinde.myapplication_xmlpref.listners.BiometricCallbackListener;

@RequiresApi(api = Build.VERSION_CODES.P)
public class BiometricCallbackHandler extends BiometricPrompt.AuthenticationCallback {

    private BiometricCallbackListener biometricCallback;
    public BiometricCallbackHandler(BiometricCallbackListener biometricCallback) {
        this.biometricCallback = biometricCallback;
    }


    @Override
    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        biometricCallback.onAuthenticationSuccessful();
    }


    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        biometricCallback.onAuthenticationHelp(helpCode, helpString);
    }


    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        biometricCallback.onAuthenticationError(errorCode, errString);
    }


    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        biometricCallback.onAuthenticationFailed();
    }
}
