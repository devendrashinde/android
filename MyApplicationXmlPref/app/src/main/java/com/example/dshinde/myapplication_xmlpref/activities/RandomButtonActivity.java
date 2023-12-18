package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.ControlType;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RandomButtonActivity extends BaseActivity {

    private static final String TAG = RandomButtonActivity.class.getSimpleName();
    private static final int REMOVE = 1;
    private static final int STAY = 2;
    final DisplayMetrics displaymetrics = new DisplayMetrics();
    private Map<Integer, ScreenControl> controls = new HashMap<>();
    private LinearLayout linearLayout;
    private int nextButtonId = 0;
    TextView textView;
    RadioGroup rgMode;
    RadioGroup rgTheme;
    int mode = REMOVE;
    float defaultTextSize = 60;
    int counter=0;
    public static final int SIZE = 10;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_linear_layout);
        linearLayout = findViewById(R.id.linear_layout);

        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Bundle bundle = getIntent().getExtras();
        String text = bundle.getString(Constants.NOTE_TEXT);
        text = text.replaceAll("\n"," ");
        text = text.replaceAll("\r","");
        text = text.replaceAll("  ","");
        setTitle(text);

        buildScreenControls(text);
        setButtonTextSize(defaultTextSize);
        addStaticControls();
        addControlsToLayoutRandomly();
        setTheme(Constants.NIGHT_MODE);
    }

    private ArrayList<Integer> getRandomList(){
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i=0; i<controls.size(); i++) {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);
        return list;
    }

    private void addControlsToLayoutRandomly() {
        ArrayList<Integer> list = getRandomList();

        for (Integer i : list) {
            addRandomButton((Button) controls.get(i).getValueControl());
        }
        nextButtonId = 0;
    }


    private void setControlsRandomText() {
        ArrayList<Integer> list = getRandomList();

        int i = 0;
        for (Integer control : list) {
            Button button = (Button) controls.get(i).getValueControl();
            button.setText(controls.get(control).getTextLabel());
            i++;
        }
        nextButtonId = 0;
    }

    private void buildScreenControls(String text)
    {
        String labels[] = text.split(" ");
        int index=0;
        for (int i = 0; i < labels.length; i++) {
            if(!labels[i].trim().isEmpty()) {
                Button button = DynamicControls.getButton(this, labels[i]);
                ScreenControl screenControl = new ScreenControl();
                screenControl.setControlType(ControlType.Button);
                screenControl.setTextLabel(labels[i]);
                screenControl.setValueControl(button);
                screenControl.setControlId(String.valueOf(button.getId()));
                screenControl.setId(index);
                setButtonListener(screenControl);
                controls.put(index, screenControl);
                index++;
            }
        }
        textView = DynamicControls.getTextView(this, "");
        rgMode = getRadioGroupForMode();
        rgTheme = getRadioGroupForTheme();
    }

    private RadioGroup getRadioGroupForMode() {
        RadioGroup rg = DynamicControls.getRadioGroupControl(this,
                new String[]{Constants.REMOVE_BUTTON, Constants.REMOVE_TEXT},
                Arrays.asList(new String[]{Constants.REMOVE_BUTTON}.clone()));
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedButton = (RadioButton) linearLayout.findViewById(checkedId);
                if (selectedButton != null) {
                    switch (selectedButton.getText().toString()) {
                        case Constants.REMOVE_BUTTON:
                            mode = REMOVE;
                            break;
                        case Constants.REMOVE_TEXT:
                            mode = STAY;
                            break;
                    }
                }
            }
        });
        return rg;
    }

    private RadioGroup getRadioGroupForTheme() {
        RadioGroup rg = DynamicControls.getRadioGroupControl(this,
                new String[]{Constants.DAY_MODE, Constants.NIGHT_MODE},
                Arrays.asList(new String[]{Constants.NIGHT_MODE}.clone()));
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedButton = (RadioButton) linearLayout.findViewById(checkedId);
                if (selectedButton != null) {
                    setTheme(selectedButton.getText().toString());
                }
            }
        });
        return rg;
    }


    private void removeButton(Button button) {
        if(mode == REMOVE) {
            linearLayout.removeView(button);
        } else {
            button.setText("");
        }
    }

    private void setButtonListener(ScreenControl screenControl) {
        Button button = (Button) screenControl.getValueControl();
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ScreenControl nextControl = controls.get(nextButtonId);
                if(nextControl.getTextLabel().equals(button.getText())) {
                    textView.setText(textView.getText() + " " + button.getText());
                    removeButton(button);
                    if(nextButtonId == controls.size() - 1){
                        counter++;
                        textView.setText(getCounter());
                        if(mode == REMOVE) {
                            addControlsToLayoutRandomly();
                        } else {
                            setControlsRandomText();
                        }
                    }
                    else {
                        nextButtonId++;
                    }
                    enableModeRadioButtons();
                }
            }
        });
    }

    private void enableModeRadioButtons(){
        for(int k = 0; k < rgMode.getChildCount(); k++) {
            RadioButton rb = (RadioButton) rgMode.getChildAt(k);
            rb.setEnabled(nextButtonId == 0);
        }
    }

    private String getCounter() {
        return counter > 0 ? "(" + counter + ") " : "";
    }

    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    private void addRandomButton(Button button){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.addView(button);
                //setRandomPosition(button);
            }
        });
    }

    private void addStaticControls(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.addView(rgTheme);
                linearLayout.addView(rgMode);
                linearLayout.addView(textView);
            }
        });
    }

    private void setRandomPosition(Button button) {
        ObjectPosition position = getRandomPosition(button.getWidth(), button.getHeight());
        button.animate()
                .x(position.x)
                .y(position.y)
                .setDuration(0)
                .start();

    }

    private ObjectPosition getRandomPosition(int buttonWidth, int buttonHeight){
        Random R = new Random();
        int height = displaymetrics.heightPixels - getNavigationBarHeight();
        int width = displaymetrics.widthPixels - buttonWidth;
        float dx = 0;
        float dy = 0;
        do {
            dx = R.nextFloat() * width;
            dy = R.nextFloat() * height;
        } while (dx + buttonWidth > width && dy + buttonHeight > height && dy < 0);

        return new ObjectPosition(dx, dy);
    }

    private class ObjectPosition {
        public float x;
        public float y;

        public ObjectPosition (float x, float y){
            this.x = x;
            this.y = y;
        }
    }

    private void animateButtonToRandomPosition(Button button) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Random R = new Random();
                        int buttonWidth = button.getWidth();
                        int buttonHeight = button.getHeight();
                        int height = displaymetrics.heightPixels - getNavigationBarHeight();
                        int width = displaymetrics.widthPixels - buttonWidth;
                        float dx = 0;
                        float dy = 0;
                        do {
                            dx = R.nextFloat() * width;
                            dy = R.nextFloat() * height;
                        } while (dx + buttonWidth > width && dy + buttonHeight > height && dy < 0);
                        button.animate()
                                .x(dx)
                                .y(dy)
                                .setDuration(0)
                                .start();
                    }
                });
            }
        }, 0, 1000);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    mScaleFactor++;
                    setButtonTextSize(defaultTextSize + (mScaleFactor * SIZE));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mScaleFactor--;
                    setButtonTextSize(defaultTextSize  + (mScaleFactor * SIZE));
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void setButtonTextSize(float size){
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        for (int i = 0; i < controls.size(); i++){
            Button button = (Button) controls.get(i).getValueControl();
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    public void setTheme(String themeName) {
        setTheme(linearLayout, themeName);
    }
}
