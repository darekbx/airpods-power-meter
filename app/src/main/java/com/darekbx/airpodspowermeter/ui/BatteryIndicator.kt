package com.darekbx.airpodspowermeter.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BatteryIndicator(context: Context, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    companion object {
        private const val LOW_POWER_LIMIT = 15
    }

    private val borderPaint by lazy {
        Paint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 1F
        }
    }

    private val borderFillPaint by lazy {
        Paint().apply {
            color = Color.GRAY
            style = Paint.Style.FILL
        }
    }

    private val greenPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#4CDA63")
            style = Paint.Style.FILL
        }
    }

    private val redPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#E63232")
            style = Paint.Style.FILL
        }
    }

    var _power: Int = 0

    var power: Int
        set(value) {
            _power = value
            invalidate()
        }
    get() = _power

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) return

        val padding = 2F
        val plusContactWidth = 6F
        val batteryWidth = width.toFloat() - (plusContactWidth + padding)
        val batteryHeigth = height.toFloat()
        val plusContactHeight = height.toFloat() / 3F
        val plusTop = (height / 2) - (plusContactHeight / 2)

        val percentWidth = batteryWidth * (_power.toFloat() / 100F)
        val paint = when {
            _power <= LOW_POWER_LIMIT -> redPaint
            else -> greenPaint
        }
        canvas.drawRoundRect(1F, 1F, percentWidth - padding, batteryHeigth - padding, 4F, 4F, paint)

        canvas.drawRoundRect(1F, 1F, batteryWidth - padding, batteryHeigth - padding, 4F, 4F, borderPaint)
        canvas.drawRect(batteryWidth - padding, plusTop,
            batteryWidth + plusContactWidth, plusTop + plusContactHeight, borderFillPaint)
    }
}
