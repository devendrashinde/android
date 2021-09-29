package com.example.dshinde.myapplication_xmlpref.activities.drawables;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

import com.example.dshinde.myapplication_xmlpref.listners.RelationshipViewListener;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RelationshipView extends View {
    public static final int HLINE_WIDTH = 100;
    public static final int TEXT_HEIGHT = 100;
    public static final int RELATION_TEXT_HEIGHT = 30;
    public static final int THICKNESS = 10;
    private RelationshipViewListener relationshipViewListener=null;
    private int viewHeight=3000;
    private int viewWidth=2000;
    Map<String, Set<String>> relationShips;
    String relationShipFrom;

    public RelationshipView(Context context, String relationShipFrom, Map<String, Set<String>> relationShips) {
        super(context);
        this.relationShips = relationShips;
        this.relationShipFrom = relationShipFrom;
        updateHeight();
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    public void setRelationshipViewListener(RelationshipViewListener relationshipViewListener) {
        this.relationshipViewListener = relationshipViewListener;
    }

    // onMeasure must be included otherwise one or both scroll views will be compressed to zero pixels
    // and the scrollview will then be invisible

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {

        int width = viewWidth;
        int height = viewHeight + 50; // Since 3000 is bottom of last Rect to be drawn added and 50 for padding.
        setMeasuredDimension(width, height);
    }

    protected void onDraw(Canvas canvas) {
        int x = 10;
        int y = 10;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xff74AC23);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TEXT_HEIGHT);
        drawName(canvas, x, y + TEXT_HEIGHT, relationShipFrom, paint);
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    private void drawName(Canvas canvas, int x, int y, String name, Paint paint) {
        canvas.drawText(name, x + THICKNESS, y, paint);
        int offset = getChildsAndDraw(canvas, x, y, name, paint, 0);
        viewHeight = y + (offset * TEXT_HEIGHT);
        if(relationshipViewListener != null) {
            relationshipViewListener.updateLayoutParam(viewWidth, viewHeight);
        }
    }

    private int getChildsAndDraw(Canvas canvas, int x, int y, String name, Paint paint, int offset) {
        String[] childs = getChildrens(name);
        return drawChildrens(canvas, x, y, name, childs, paint, offset);
    }

    private int drawChildrens(Canvas canvas, int left, int top, String name, String[] childs, Paint paint, int offset) {
        for(int child=1; child <= childs.length; child++){
            String childName = childs[child - 1].split(",")[0];
            String relation = childs[child - 1].split(",")[1];
            LeftTop leftTop = drawChild(canvas, paint, left, top, childName, relation,child + offset);
            offset += drawChildrens(canvas, leftTop.getLeft(), leftTop.getTop(), childName, getChildrens(childName), paint, 0);
        }
        return offset + childs.length;
    }

    private LeftTop drawChild(Canvas canvas, Paint paint, int left, int top, String childName, String relation, int childNumber) {
        int vLineBottom = top + (childNumber * TEXT_HEIGHT);
        drawVerticalLine(canvas, paint, left, top, vLineBottom);
        drawHorizontalLine(canvas, paint, left, vLineBottom);
        int childLeft = left + TEXT_HEIGHT + THICKNESS;
        int childTop = top + (childNumber * TEXT_HEIGHT) + THICKNESS;
        paint.setTextSize(TEXT_HEIGHT);
        canvas.drawText(childName, childLeft, childTop, paint);
        float offset = paint.measureText(childName);
        paint.setTextSize(RELATION_TEXT_HEIGHT);
        canvas.drawText("(" + relation +")", childLeft + offset+ 10, childTop, paint);
        return new LeftTop(childLeft, childTop);
    }

    private void drawHorizontalLine(Canvas canvas, Paint paint, int lineLeft, int lineTop) {
        canvas.drawRect(lineLeft, lineTop, lineLeft + HLINE_WIDTH, lineTop + THICKNESS, paint);
    }

    private String[] getChildrens(String name) {
        if(relationShips.containsKey(name)) {
            Object[] temp = relationShips.get(name).toArray();
            return Arrays.copyOf(temp, temp.length, String[].class);
        }
        return new String[]{};
    }

    private void drawVerticalLine(Canvas canvas, Paint paint, int lineLeft, int lineTop, int lineBottom) {
        canvas.drawRect(lineLeft, lineTop, lineLeft + THICKNESS, lineBottom, paint);
    }

    private class LeftTop {
        int left;
        int top;

        public LeftTop(int left, int top){
            this.left = left;
            this.top = top;
        }

        public int getLeft() {
            return left;
        }

        public int getTop() {
            return top;
        }
    }

    public void updateHeight(){
        int offset = relationShips.size();
        for (Map.Entry<String, Set<String>> entry : relationShips.entrySet()) {
            offset += entry.getValue().size();
        }
        viewHeight = offset * TEXT_HEIGHT;
    }
}