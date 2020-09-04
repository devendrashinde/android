package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;

public class ScrollingTextViewActivity extends BaseActivity implements View.OnTouchListener{

    public static final int SIZE = 10;
    TextView textView;
    float defaultTextSize;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling_text_view);

        textView = (TextView) findViewById(R.id.textView);
        Bundle bundle = getIntent().getExtras();
        setTitle(bundle.getString("subject"));
        String data = JsonHelper.formatAsString(bundle.getString("text"),true);
        parseText(data);
        defaultTextSize = textView.getTextSize();
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    mScaleFactor++;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize + (mScaleFactor * SIZE));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mScaleFactor--;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize  + (mScaleFactor * SIZE));
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void parseText(String text){
        new Thread() {
            @Override
            public void run() {
                displayText(Html.fromHtml(JsonHelper.formatAsString(text,true)));


            }
        }.start();

    }

    private void displayText(Spanned text){
        runOnUiThread(()->textView.setText(text));
    }

    public void share() {
        share(textView.getText().toString());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize + (mScaleFactor * SIZE)  );
            return true;
        }
    }
}
