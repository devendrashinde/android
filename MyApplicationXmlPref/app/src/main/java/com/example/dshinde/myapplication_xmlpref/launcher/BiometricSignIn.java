package com.example.dshinde.myapplication_xmlpref.launcher;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Button;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.recyclerviewbased.MainActivityRecyclerView;

import java.util.concurrent.Executor;

public class BiometricSignIn extends BaseActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_sign_in);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                showInShortToast("Authentication error: " + errString);
                switch (errorCode){
                    case BiometricPrompt.ERROR_CANCELED:
                    case BiometricPrompt.ERROR_NO_BIOMETRICS:
                    case BiometricPrompt.ERROR_USER_CANCELED:
                    case BiometricPrompt.ERROR_HW_NOT_PRESENT:
                    case BiometricPrompt.ERROR_HW_UNAVAILABLE:
                        startMainActivity();
                        break;
                    default:
                }
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                showInShortToast("Authentication succeeded!");
                startSignInActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                showInShortToast("Authentication failed");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for MyNotes")
                .setSubtitle("Log in using your biometric credential")
                .setDeviceCredentialAllowed(true)
                .build();

        Button biometricLoginButton = findViewById(R.id.biometric_login);
        biometricLoginButton.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });
        biometricPrompt.authenticate(promptInfo);
    }

    private void startSignInActivity() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivityRecyclerView.class);
        String userId = null;
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private Boolean checkBiometricSupport() {

        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        PackageManager packageManager = this.getPackageManager();

        if (!keyguardManager.isKeyguardSecure()) {
            showInShortToast("Lock screen security not enabled in Settings");
            return false;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_BIOMETRIC) !=
                PackageManager.PERMISSION_GRANTED) {

            showInShortToast("Fingerprint authentication permission not enabled");
            return false;
        }

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT))
        {
            return true;
        }

        return true;
    }

}
