package com.example.developmentofuserinterfaceshw1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class RoadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val shoulderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 193, 7)
        strokeWidth = dp(3f)
    }
    private val lanePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(205, 255, 255, 255)
        strokeWidth = dp(2f)
        pathEffect = DashPathEffect(floatArrayOf(dp(22f), dp(24f)), 0f)
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(32, 255, 255, 255)
        strokeWidth = dp(1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val shoulderWidth = resources.getDimension(R.dimen.road_side_inset)
        val roadLeft = shoulderWidth
        val roadRight = width - shoulderWidth
        val roadWidth = roadRight - roadLeft
        val laneWidth = roadWidth / LANE_COUNT

        shoulderPaint.shader = LinearGradient(
            0f,
            0f,
            0f,
            height,
            Color.rgb(22, 122, 82),
            Color.rgb(9, 75, 58),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, shoulderPaint)

        roadPaint.shader = LinearGradient(
            roadLeft,
            0f,
            roadRight,
            0f,
            intArrayOf(
                Color.rgb(45, 49, 53),
                Color.rgb(72, 76, 80),
                Color.rgb(45, 49, 53)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            RectF(roadLeft, -dp(18f), roadRight, height + dp(18f)),
            dp(26f),
            dp(26f),
            roadPaint
        )

        canvas.drawLine(roadLeft + dp(4f), 0f, roadLeft + dp(4f), height, edgePaint)
        canvas.drawLine(roadRight - dp(4f), 0f, roadRight - dp(4f), height, edgePaint)

        for (lane in 1 until LANE_COUNT) {
            val x = roadLeft + laneWidth * lane
            canvas.drawLine(x, dp(8f), x, height - dp(8f), lanePaint)
            canvas.drawLine(x + dp(3f), 0f, x + dp(3f), height, glowPaint)
        }
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    companion object {
        private const val LANE_COUNT = 5
    }
}
