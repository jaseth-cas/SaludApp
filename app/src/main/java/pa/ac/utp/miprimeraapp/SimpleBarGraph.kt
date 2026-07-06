package pa.ac.utp.miprimeraapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SimpleBarGraph @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintBar = Paint().apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = Color.parseColor("#475569")
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val paintGrid = Paint().apply {
        color = Color.parseColor("#E2E8F0")
        strokeWidth = 2f
        isAntiAlias = true
    }

    private var data = listOf<Int>()
    private val labels = listOf("L", "M", "M", "J", "V", "S", "D")

    fun setData(newData: List<Int>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw empty state
        if (data.isEmpty()) {
            canvas.drawText("Sin datos suficientes", width / 2f, height / 2f, paintText)
            return
        }

        val maxVal = data.maxOrNull()?.coerceAtLeast(1000) ?: 1000
        val padding = 60f
        val graphHeight = height - padding * 2
        val graphWidth = width - padding * 2
        
        val barWidth = (graphWidth / (data.size * 1.5f))
        val spacing = graphWidth / data.size

        // Draw horizontal grid lines
        for (i in 0..4) {
            val y = height - padding - (graphHeight / 4 * i)
            canvas.drawLine(padding, y, width - padding, y, paintGrid)
        }

        for (i in data.indices) {
            val barHeight = (data[i].toFloat() / maxVal) * graphHeight
            val left = padding + (i * spacing) + (spacing - barWidth) / 2
            val top = height - padding - barHeight
            val right = left + barWidth
            val bottom = height - padding

            // Draw shadow/background for bar
            paintBar.alpha = 40
            canvas.drawRect(left, height - padding - graphHeight, right, bottom, paintBar)
            
            // Draw actual bar
            paintBar.alpha = 255
            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, 10f, 10f, paintBar)

            // Draw label
            if (i < labels.size) {
                canvas.drawText(labels[i], left + barWidth / 2, height - 15f, paintText)
            }
        }
    }
}