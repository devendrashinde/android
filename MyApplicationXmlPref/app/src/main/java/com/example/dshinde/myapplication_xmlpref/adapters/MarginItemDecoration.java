package com.example.dshinde.myapplication_xmlpref.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class MarginItemDecoration extends RecyclerView.ItemDecoration
{
    private int margin;
    public MarginItemDecoration(int margin){
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = margin;
        }
        outRect.bottom = margin;
        outRect.left = margin;
        outRect.right = margin;
    }
}
