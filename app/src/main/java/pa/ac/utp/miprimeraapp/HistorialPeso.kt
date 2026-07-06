package pa.ac.utp.miprimeraapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class HistorialPeso : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: PesoAdapter
    private val listaRegistros = mutableListOf<RegistroPeso>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial_peso)

        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        dbHelper = DatabaseHelper.getInstance(this)
        val lvHistorial = findViewById<ListView>(R.id.lvHistorial)

        loadDataFromDb()

        adapter = PesoAdapter(this, listaRegistros)
        lvHistorial.adapter = adapter

        // Mantener presionado para eliminar
        lvHistorial.setOnItemLongClickListener { _, _, position, _ ->
            val item = listaRegistros[position]
            AlertDialog.Builder(this)
                .setTitle("Eliminar registro")
                .setMessage("¿Deseas eliminar este pesaje del historial?")
                .setPositiveButton("Eliminar") { _, _ ->
                    eliminarRegistro(item.id, position)
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    private fun loadDataFromDb() {
        listaRegistros.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PESO, null, null, null, null, null, "${DatabaseHelper.COL_PESO_ID} DESC")
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PESO_ID))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PESO_FECHA))
                val peso = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PESO_VALOR))
                val imc = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PESO_IMC))
                listaRegistros.add(RegistroPeso(id, fecha, peso, imc))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun eliminarRegistro(id: Int, position: Int) {
        val db = dbHelper.writableDatabase
        val deleted = db.delete(DatabaseHelper.TABLE_PESO, "${DatabaseHelper.COL_PESO_ID} = ?", arrayOf(id.toString()))
        if (deleted > 0) {
            listaRegistros.removeAt(position)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show()
        }
    }
}

data class RegistroPeso(
    val id: Int,
    val fecha: String,
    val peso: Double,
    val imc: Double
)

class PesoAdapter(
    private val context: Context,
    private val data: List<RegistroPeso>
) : BaseAdapter() {

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_historial_peso, parent, false)

        val item = data[position]

        val ivIcono = view.findViewById<ImageView>(R.id.ivIconoIMC)
        val tvFecha = view.findViewById<TextView>(R.id.tvFecha)
        val tvPesoIMC = view.findViewById<TextView>(R.id.tvPesoIMC)

        tvFecha.text = "Fecha: ${item.fecha}"

        tvPesoIMC.text = String.format(
            Locale.getDefault(),
            "Peso: %.1f kg | IMC: %.1f",
            item.peso,
            item.imc
        )

        val nombreIcono = when {
            item.imc < 18.5 -> "bajopeso"
            item.imc < 25.0 -> "normal"
            item.imc < 30.0 -> "sobrepeso"
            item.imc < 35.0 -> "obesidad1"
            item.imc < 40.0 -> "obesidad2"
            else -> "obesidad3"
        }

        val resId = context.resources.getIdentifier(
            nombreIcono,
            "drawable",
            context.packageName
        )

        if (resId != 0) {
            ivIcono.setImageResource(resId)
        } else {
            ivIcono.setImageResource(android.R.drawable.ic_menu_info_details)
        }

        return view
    }
}