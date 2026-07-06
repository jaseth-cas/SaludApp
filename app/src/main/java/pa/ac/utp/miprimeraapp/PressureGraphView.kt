package pa.ac.utp.miprimeraapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PressureGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintSis = Paint().apply {
        color = Color.parseColor("#D32F2F") // Rojo para Sistólica
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintDia = Paint().apply {
        color = Color.parseColor("#1976D2") // Azul para Diastólica
        strokeWidth = 5f
        style = Paint.Style.STROKE
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

    private var dataSis = listOf<Int>()
    private var dataDia = listOf<Int>()

    fun setData(sis: List<Int>, dia: List<Int>) {
        dataSis = sis
        dataDia = dia
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (dataSis.isEmpty()) {
            canvas.drawText("Sin registros de presión", width / 2f, height / 2f, paintText)
            return
        }

        val paddingLeft = 80f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 80f
        
        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom
        
        val maxVal = 200f
        val minVal = 40f

        // Escala lateral
        paintText.textAlign = Paint.Align.RIGHT
        for (i in 0..4) {
            val value = 40 + (40 * i)
            val y = height - paddingBottom - (graphHeight * ((value - minVal) / (maxVal - minVal)))
            canvas.drawText(value.toString(), paddingLeft - 10f, y + 10f, paintText)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid)
        }

        val stepX = if (dataSis.size > 1) graphWidth / (dataSis.size - 1) else graphWidth
        
        val pathSis = Path()
        val pathDia = Path()

        for (i in dataSis.indices) {
            val x = paddingLeft + (i * stepX)
            
            // Sistolica
            val ySis = height - paddingBottom - (graphHeight * ((dataSis[i].toFloat() - minVal) / (maxVal - minVal)))
            if (i == 0) pathSis.moveTo(x, ySis) else pathSis.lineTo(x, ySis)
            
            // Diastolica
            if (i < dataDia.size) {
                val yDia = height - paddingBottom - (graphHeight * ((dataDia[i].toFloat() - minVal) / (maxVal - minVal)))
                if (i == 0) pathDia.moveTo(x, yDia) else pathDia.lineTo(x, yDia)
            }
        }

        canvas.drawPath(pathSis, paintSis)
        canvas.drawPath(pathDia, paintDia)
        
        // Leyenda
        paintText.textAlign = Paint.Align.LEFT
        canvas.drawCircle(paddingLeft, height - 20f, 8f, paintSis.apply { style = Paint.Style.FILL })
        canvas.drawText("SIS", paddingLeft + 15f, height - 12f, paintText)
        
        canvas.drawCircle(paddingLeft + 100f, height - 20f, 8f, paintDia.apply { style = Paint.Style.FILL })
        canvas.drawText("DIA", paddingLeft + 115f, height - 12f, paintText)
        
        // Restore paint styles for next draw
        paintSis.style = Paint.Style.STROKE
        paintDia.style = Paint.Style.STROKE
    }
}