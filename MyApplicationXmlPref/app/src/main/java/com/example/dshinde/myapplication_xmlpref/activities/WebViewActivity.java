package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.dshinde.myapplication_xmlpref.R;

public class WebViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_linear_layout);

        WebView webView=findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

    }
}
