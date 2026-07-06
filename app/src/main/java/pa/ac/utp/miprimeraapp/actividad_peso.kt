package pa.ac.utp.miprimeraapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class actividad_peso : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actividad_peso)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper.getInstance(this)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)

        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialPeso::class.java))
        }

        // Referencias XML
        val etEdad = findViewById<EditText>(R.id.etEdad)
        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etEstatura = findViewById<EditText>(R.id.etEstatura)

        val swPeso = findViewById<SwitchCompat>(R.id.swPesoUnit)
        val swEstatura = findViewById<SwitchCompat>(R.id.swEstaturaUnit)

        val btnCalcular = findViewById<Button>(R.id.btnCalcular)

        val tvIMC = findViewById<TextView>(R.id.tvIMC)
        val tvPesoIdeal = findViewById<TextView>(R.id.tvPesoIdeal)
        val tvGrasa = findViewById<TextView>(R.id.tvGrasa)
        val tvClasificacion = findViewById<TextView>(R.id.tvClasificacion)

        // Switches
        swPeso.setOnCheckedChangeListener { _, isChecked ->
            etPeso.hint = if (isChecked) "Peso (Lb)" else "Peso (Kg)"
            etPeso.text.clear()
        }

        swEstatura.setOnCheckedChangeListener { _, isChecked ->
            etEstatura.hint = if (isChecked) "Estatura (in)" else "Estatura (cm)"
            etEstatura.text.clear()
        }

        // Botón CALCULAR
        btnCalcular.setOnClickListener {

            val sEdad = etEdad.text.toString()
            val sPeso = etPeso.text.toString()
            val sEstatura = etEstatura.text.toString()


            if (sEdad.isEmpty() || sPeso.isEmpty() || sEstatura.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val edad = sEdad.toInt()
            var peso = sPeso.toDouble()
            var estatura = sEstatura.toDouble()


            if (swPeso.isChecked) {
                peso *= 0.453592
            }

            if (swEstatura.isChecked) {
                estatura *= 2.54
            }


            val estaturaMetros = estatura / 100
            val imc = peso / (estaturaMetros * estaturaMetros)
            val pesoIdeal = 22 * (estaturaMetros * estaturaMetros)
            val grasa = (1.20 * imc) + (0.23 * edad) - 16.2


            tvIMC.text = String.format("IMC\n%.1f", imc)
            tvPesoIdeal.text = String.format("Ideal\n%.1f kg", pesoIdeal)
            tvGrasa.text = String.format("Grasa\n%.1f%%", grasa)

            tvClasificacion.text = categorizarIMC(imc)

            saveWeightToDb(peso, imc)
        }
    }

    private fun saveWeightToDb(peso: Double, imc: Double) {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DatabaseHelper.COL_PESO_FECHA, date)
            put(DatabaseHelper.COL_PESO_VALOR, peso)
            put(DatabaseHelper.COL_PESO_IMC, imc)
        }
        db.insert(DatabaseHelper.TABLE_PESO, null, values)
        Toast.makeText(this, "Registro de peso guardado", Toast.LENGTH_SHORT).show()
    }

    private fun categorizarIMC(imc: Double): String {
        return when {
            imc < 18.5 -> "Bajo Peso"
            imc < 25 -> "Normal"
            imc < 30 -> "Sobrepeso"
            imc < 35 -> "Obesidad I"
            imc < 40 -> "Obesidad II"
            else -> "Obesidad III"
        }
    }
}