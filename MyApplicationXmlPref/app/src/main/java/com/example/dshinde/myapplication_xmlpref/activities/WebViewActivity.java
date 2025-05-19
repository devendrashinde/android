package com.example.dshinde.myapplication_xmlpref.activities;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;

public class WebViewActivity extends BaseActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_linear_layout);

        webView=findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(false);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String uri = bundle.getString(Constants.PARAM_URL);
        assert uri != null;
        webView.loadUrl(uri);
    }
}
