package pa.ac.utp.miprimeraapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class HistorialPasosActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private val listaPasos = mutableListOf<RegistroPasos>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_pasos)

        dbHelper = DatabaseHelper.getInstance(this)
        val lv = findViewById<ListView>(R.id.lvHistorialPasos)

        loadData()
        lv.adapter = PasosAdapter(this, listaPasos)
    }

    private fun loadData() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_ACTIVIDAD, null, null, null, null, null, "${DatabaseHelper.COL_ACTIVIDAD_ID} DESC")
        
        while (cursor.moveToNext()) {
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACTIVIDAD_FECHA))
            val pasos = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACTIVIDAD_PASOS))
            listaPasos.add(RegistroPasos(fecha, pasos))
        }
        cursor.close()
    }
}

data class RegistroPasos(val fecha: String, val pasos: Int)

class PasosAdapter(val context: Context, val data: List<RegistroPasos>) : BaseAdapter() {
    override fun getCount() = data.size
    override fun getItem(position: Int) = data[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_historial_pasos, parent, false)
        val item = data[position]

        view.findViewById<TextView>(R.id.tvFechaPasos).text = item.fecha
        view.findViewById<TextView>(R.id.tvCantidadPasos).text = "${item.pasos} pasos"
        
        val tvStatus = view.findViewById<TextView>(R.id.tvMetaStatus)
        
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        
        if (item.fecha == today) {
            tvStatus.text = "En progreso"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
        } else {
            tvStatus.text = "Finalizado"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#64748B"))
            
            if (item.pasos >= 10000) {
                tvStatus.text = "Meta Cumplida"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            }
        }
        
        return view
    }
}