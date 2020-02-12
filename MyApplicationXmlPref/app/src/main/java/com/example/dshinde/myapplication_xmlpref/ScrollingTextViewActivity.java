package com.example.dshinde.myapplication_xmlpref;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScrollingTextViewActivity extends AppCompatActivity {

    TextView textView;
    float defaultTextSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling_text_view);

        textView = (TextView) findViewById(R.id.textView);
        Bundle bundle = getIntent().getExtras();
        setTitle(bundle.getString("subject"));
        textView.setText(bundle.getString("text"));
        defaultTextSize = textView.getTextSize();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    defaultTextSize += 10;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize );
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    defaultTextSize -= 10;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize );
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    public void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String textToShare = this.getTitle() + System.getProperty(System.lineSeparator()) + textView.getText().toString();
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

}
