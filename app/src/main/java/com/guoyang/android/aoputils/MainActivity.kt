package com.guoyang.android.aoputils

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var nornalSum = 0
    var singleSum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            normal()
            single()
        }
    }

    fun normal(){
        normal.text = "点击次数:${nornalSum++}次"
    }

    @SingleClick
    fun single(){
        single.text = "防止多次点击:${singleSum++}次"
    }
}
