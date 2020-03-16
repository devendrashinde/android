/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dshinde.myapplication_xmlpref.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dshinde.myapplication_xmlpref.activities.listviewbased.MainActivity;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.recyclerviewbased.MainActivityRecyclerView;
import com.example.dshinde.myapplication_xmlpref.auth.GoogleSignInActivity;

public class SignInActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Button listeners
        findViewById(R.id.signInButton).setOnClickListener(this);
        findViewById(R.id.continueButton).setOnClickListener(this);

    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivityRecyclerView.class);
        String userId = null;
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void startGoogleSignInActivity(){
        Intent intent = new Intent(this, GoogleSignInActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signInButton) {
            startGoogleSignInActivity();
        } else if (i == R.id.continueButton) {
            startMainActivity();
        }

    }
}
