package pa.ac.utp.miprimeraapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class HydrationGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintBar = Paint().apply {
        color = Color.parseColor("#1E88E5")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = Color.parseColor("#475569")
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val paintGrid = Paint().apply {
        color = Color.parseColor("#E2E8F0")
        strokeWidth = 2f
        isAntiAlias = true
    }

    private var data = listOf<Int>()
    // Reordenamos para que el lunes sea el primer dia (índice 0)
    private val labels = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")

    fun setData(newData: List<Int>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) {
            canvas.drawText("Sin datos registrados", width / 2f, height / 2f, paintText)
            return
        }

        val maxVal = 8f // Meta estándar 8 vasos
        val paddingLeft = 80f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 60f
        
        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom
        
        val barWidth = (graphWidth / (data.size * 1.5f))
        val spacing = graphWidth / data.size

        // Dibujar escala lateral (Vasos)
        paintText.textAlign = Paint.Align.RIGHT
        for (i in 0..4) {
            val value = (2 * i) // 0, 2, 4, 6, 8 vasos
            val y = height - paddingBottom - (graphHeight * (value / maxVal))
            canvas.drawText(value.toString(), paddingLeft - 10f, y + 10f, paintText)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid)
        }

        for (i in data.indices) {
            val value = data[i].toFloat().coerceAtMost(maxVal * 1.2f)
            val barH = (value / maxVal) * graphHeight
            val left = paddingLeft + (i * spacing) + (spacing - barWidth) / 2
            val top = height - paddingBottom - barH
            val right = left + barWidth
            val bottom = height - paddingBottom

            // Barra
            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, 8f, 8f, paintBar)

            // Etiqueta dia
            paintText.textAlign = Paint.Align.CENTER
            if (i < labels.size) {
                canvas.drawText(labels[i], left + barWidth / 2, height - 15f, paintText)
            }
        }
    }
}