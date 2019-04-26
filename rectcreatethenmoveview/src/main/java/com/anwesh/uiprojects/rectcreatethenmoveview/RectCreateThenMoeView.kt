package com.anwesh.uiprojects.rectcreatethenmoveview

/**
 * Created by anweshmishra on 26/04/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val rects : Int = 2
val parts : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val rWSizeFactor : Float = 8f
val rHSizeFactor : Float = 5f
val angleDeg : Float = 90f

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float  {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawRCBar(w : Float, h : Float, sc : Float, paint : Paint) {
    drawRect(RectF(-w / 2, -h / 2, w / 2, -h / 2 + h / 2 * sc), paint)
}

fun Canvas.drawRCTMNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / 2
    val wRect : Float = w / rWSizeFactor
    val hRect : Float = h / rHSizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val sf : Float = 1f - 2 * (i % 2).toFloat()
    val x : Float = gap * sf * sc2.divideScale(1, parts)
    val deg : Float = angleDeg * sf * sc2.divideScale(0, parts)
    save()
    translate(w / 2 + x, h / 2)
    rotate(deg)
    for (j in 0..(rects - 1)) {
        val sc : Float = sc1.divideScale(j, rects)
        save()
        scale(1f, 1f - 2 * j)
        drawRCBar(wRect, hRect, sc, paint)
        restore()
    }
    restore()

}