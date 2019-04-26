package com.anwesh.uiprojects.linkedrectcreatethenmoveview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.rectcreatethenmoveview.RectCreateThenMoveView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RectCreateThenMoveView.create(this)
    }
}
