package pa.ac.utp.miprimeraapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class LineGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintLine = Paint().apply {
        color = Color.parseColor("#2E7D32")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val paintPoint = Paint().apply {
        color = Color.parseColor("#1B5E20")
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

    private var data = listOf(2000, 4500, 3000, 8000, 6000, 9500, 4000)
    private val labels = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")

    fun setData(newData: List<Int>) {
        if (newData.isNotEmpty()) {
            data = newData
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = 100f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 60f

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        val maxVal = 10000f // Escala basada en 10,000 pasos

        // Dibujar escala lateral izquierda (Números)
        paintText.textAlign = Paint.Align.RIGHT
        for (i in 0..5) {
            val value = (2000 * i)
            val y = height - paddingBottom - (graphHeight * (value / maxVal))
            canvas.drawText(value.toString(), paddingLeft - 15f, y + 10f, paintText)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid)
        }

        if (data.isEmpty()) return

        val stepX = graphWidth / (data.size - 1).coerceAtLeast(1)
        val path = Path()

        for (i in data.indices) {
            val x = paddingLeft + (i * stepX)
            val ratio = data[i].toFloat() / maxVal
            val y = height - paddingBottom - (graphHeight * ratio.coerceAtMost(1.1f))

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Puntos
            canvas.drawCircle(x, y, 8f, paintPoint)

            // Etiquetas inferiores (Días)
            paintText.textAlign = Paint.Align.CENTER
            if (i < labels.size) {
                canvas.drawText(labels[i], x, height - 15f, paintText)
            }
        }

        canvas.drawPath(path, paintLine)
    }
}