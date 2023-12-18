package com.example.dshinde.myapplication_xmlpref.helper;

import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.core.view.MotionEventCompat;

public class VerticalResizeTouchHandler implements View.OnTouchListener
{
    private final LinearLayout mParent;
    private final View mView1;
    private final View mView2;

    private int mDownY;
    private int mStartY;

    public VerticalResizeTouchHandler(View view1, View view2)
    {
        if(view1.getParent() != view2.getParent()){
            throw new IllegalStateException("both views must have the same parent");
        }
        try{
            mParent = (LinearLayout)view1.getParent();
            mView1 = view1;
            mView2 = view2;
        }
        catch(ClassCastException e){
            throw new IllegalStateException("parent must be an instance of LinearLayout.");
        }
    }

    public void setDivider(View gripper)
    {
        gripper.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch(MotionEventCompat.getActionMasked(event)){
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) event.getRawY();
                mStartY = (int)(v.getTop() + event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
            {
                float newY = mStartY + (int)(event.getRawY() - mDownY);
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) mView1.getLayoutParams();
                LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) mView2.getLayoutParams();
                lp1.weight = newY / mParent.getHeight();
                lp2.weight = 1.0f - lp1.weight;
                mView1.setLayoutParams(lp1);
                mView2.setLayoutParams(lp2);
            }
            break;
        }
        return true;
    }
}
