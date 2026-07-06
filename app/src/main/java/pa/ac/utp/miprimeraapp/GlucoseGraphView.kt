package pa.ac.utp.miprimeraapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GlucoseGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintLine = Paint().apply {
        color = Color.parseColor("#E53935") // Rojo para glucosa
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintPoint = Paint().apply {
        color = Color.parseColor("#B71C1C")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = Color.parseColor("#475569")
        textSize = 26f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val paintGrid = Paint().apply {
        color = Color.parseColor("#E2E8F0")
        strokeWidth = 2f
        isAntiAlias = true
    }

    private var data = listOf<Int>()

    fun setData(newData: List<Int>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) {
            canvas.drawText("Sin registros de glucosa", width / 2f, height / 2f, paintText)
            return
        }

        val paddingLeft = 80f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 60f
        
        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom
        
        val maxVal = (data.maxOrNull()?.toFloat() ?: 200f).coerceAtLeast(150f)
        val minVal = 0f

        // Escala lateral
        paintText.textAlign = Paint.Align.RIGHT
        val steps = 4
        for (i in 0..steps) {
            val value = (maxVal / steps) * i
            val y = height - paddingBottom - (graphHeight * (value / maxVal))
            canvas.drawText(value.toInt().toString(), paddingLeft - 10f, y + 10f, paintText)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid)
        }

        val stepX = if (data.size > 1) graphWidth / (data.size - 1) else graphWidth
        val path = Path()

        for (i in data.indices) {
            val x = paddingLeft + (i * stepX)
            val y = height - paddingBottom - (graphHeight * (data[i].toFloat() / maxVal))

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            canvas.drawCircle(x, y, 6f, paintPoint)
        }

        canvas.drawPath(path, paintLine)
    }
}