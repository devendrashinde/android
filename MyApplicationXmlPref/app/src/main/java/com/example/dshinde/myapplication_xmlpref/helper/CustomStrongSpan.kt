package com.example.dshinde.myapplication_xmlpref.helper

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomStrongSpan(
    private val color: Int = Color.parseColor("#2E86DE"), // nice blue
    private val bold: Boolean = true
) : MetricAffectingSpan() {

    override fun updateDrawState(tp: TextPaint) {
        apply(tp)
    }

    override fun updateMeasureState(tp: TextPaint) {
        apply(tp)
    }

    private fun apply(tp: TextPaint) {
        tp.color = color
        if (bold) tp.typeface = Typeface.create(tp.typeface, Typeface.BOLD)
    }
}
