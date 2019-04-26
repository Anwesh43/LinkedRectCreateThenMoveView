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
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val rWSizeFactor : Float = 8f
val rHSizeFactor : Float = 5f
val angleDeg : Float = 90f
val delay : Long = 20
val hOffset : Float = 0.3f
val textSizeFactor : Float = 10f

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
    arrayOf(Paint.Style.FILL, Paint.Style.STROKE).forEach {
        paint.style = it
        drawRect(RectF(-w / 2, -h / 2, w / 2, -h / 2 + h / 2 * sc), paint)
    }
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
    val x : Float = (gap + hRect / 2) * sf * sc2.divideScale(1, parts)
    val deg : Float = angleDeg * sf * sc2.divideScale(0, parts)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.style = Paint.Style.FILL
    paint.textSize = Math.min(w, h) / textSizeFactor
    save()
    translate(w / 2 + x, h / 2)
    drawText("${i + 1}", -x, -hOffset * h, paint)
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

class RectCreateThenMoveView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, rects, parts)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RCTMNode(var i : Int, val state : State = State()) {

        private var next : RCTMNode? = null
        private var prev : RCTMNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RCTMNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRCTMNode(i, state.scale, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RCTMNode {
            var curr : RCTMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RectCreateThenMove(var i : Int) {

        private var curr : RCTMNode = RCTMNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectCreateThenMoveView) {

        private val animator : Animator = Animator(view)
        private val rctm : RectCreateThenMove = RectCreateThenMove(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            rctm.draw(canvas, paint)
            animator.animate {
                rctm.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rctm.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity: Activity) : RectCreateThenMoveView {
            val view : RectCreateThenMoveView = RectCreateThenMoveView(activity)
            activity.setContentView(view)
            return view
        }
    }
}