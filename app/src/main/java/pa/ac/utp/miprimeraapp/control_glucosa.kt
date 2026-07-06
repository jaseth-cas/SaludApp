package pa.ac.utp.miprimeraapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class control_glucosa : AppCompatActivity() {

    private lateinit var tilGlucosa: TextInputLayout
    private lateinit var etGlucosa: EditText
    private lateinit var etNotas: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var btnGuardar: MaterialButton
    private lateinit var txtFecha: TextView
    private lateinit var txtUltimaLectura: TextView
    private lateinit var glucoseGraph: GlucoseGraphView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control_glucosa)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper.getInstance(this)
        initViews()
        setupDateTime()
        setupListeners()
        
        loadLastReading()
        loadGraphData()
        validateForm()
    }

    private fun initViews() {
        tilGlucosa = findViewById(R.id.tilGlucosa)
        etGlucosa = findViewById(R.id.etGlucosa)
        etNotas = findViewById(R.id.etNotas)
        rgTipo = findViewById(R.id.rgTipo)
        btnGuardar = findViewById(R.id.btnGuardar)
        txtFecha = findViewById(R.id.txtFecha)
        txtUltimaLectura = findViewById(R.id.txtUltimaLectura)
        glucoseGraph = findViewById(R.id.glucoseGraph)
    }

    private fun setupDateTime() {
        val sdf = SimpleDateFormat("'REGISTRO: Hoy - 'EEEE, dd/MM/yyyy | hh:mm a", Locale("es", "ES"))
        val fechaFormateada = sdf.format(Date())
        txtFecha.text = fechaFormateada.replaceFirstChar { it.uppercase() }
    }

    private fun setupListeners() {
        etGlucosa.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        rgTipo.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }

        btnGuardar.setOnClickListener {
            if (performFinalCheck()) {
                saveData()
            }
        }
    }

    private fun loadLastReading() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_GLUCOSA, arrayOf(DatabaseHelper.COL_GLUCOSA_VALOR, DatabaseHelper.COL_GLUCOSA_TIPO, DatabaseHelper.COL_GLUCOSA_FECHA),
            null, null, null, null, "${DatabaseHelper.COL_GLUCOSA_ID} DESC", "1")
        
        if (cursor.moveToFirst()) {
            val valor = cursor.getInt(0)
            val tipo = cursor.getString(1)
            val fecha = cursor.getString(2)
            txtUltimaLectura.text = "Última lectura: $valor mg/dL ($tipo; $fecha)"
        }
        cursor.close()
    }

    private fun loadGraphData() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_GLUCOSA, arrayOf(DatabaseHelper.COL_GLUCOSA_VALOR),
            null, null, null, null, "${DatabaseHelper.COL_GLUCOSA_ID} DESC", "10")
        
        val readings = mutableListOf<Int>()
        while (cursor.moveToNext()) {
            readings.add(cursor.getInt(0))
        }
        cursor.close()
        
        glucoseGraph.setData(readings.reversed())
    }

    private fun validateForm() {
        val glucosaStr = etGlucosa.text.toString().trim()
        val glucosaValue = glucosaStr.toIntOrNull()
        
        val isNotEmpty = glucosaStr.isNotEmpty()
        val isInRange = glucosaValue != null && glucosaValue in 20..600
        val isTipoSelected = rgTipo.checkedRadioButtonId != -1

        btnGuardar.isEnabled = isNotEmpty && isInRange && isTipoSelected

        if (btnGuardar.isEnabled) {
            btnGuardar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E4F7E")) 
        } else {
            btnGuardar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CBD5E1"))
        }

        if (isNotEmpty && glucosaStr.length >= 2) {
            if (glucosaValue == null || glucosaValue < 20) {
                tilGlucosa.error = "Valor mínimo: 20"
            } else if (glucosaValue > 600) {
                tilGlucosa.error = "Valor máximo: 600"
            } else {
                tilGlucosa.error = null
            }
        } else {
            tilGlucosa.error = null
        }
    }

    private fun performFinalCheck(): Boolean {
        val glucosaStr = etGlucosa.text.toString().trim()
        val glucosaValue = glucosaStr.toIntOrNull()

        when {
            glucosaStr.isEmpty() -> {
                tilGlucosa.error = "Campo obligatorio"
                etGlucosa.requestFocus()
                return false
            }
            glucosaValue == null || glucosaValue < 20 -> {
                tilGlucosa.error = "Valor demasiado bajo (mín. 20 mg/dL)"
                etGlucosa.requestFocus()
                return false
            }
            glucosaValue > 600 -> {
                tilGlucosa.error = "Valor fuera de rango (máx. 600 mg/dL)"
                etGlucosa.requestFocus()
                return false
            }
            else -> tilGlucosa.error = null
        }

        if (rgTipo.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Seleccione el momento de la medición", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveData() {
        val valor = etGlucosa.text.toString().toInt()
        val tipo = when (rgTipo.checkedRadioButtonId) {
            R.id.rbAyunas -> "Ayunas"
            R.id.rbAntes -> "Antes de Almuerzo"
            R.id.rbDespues -> "Después de Almuerzo"
            R.id.rbCena -> "Cena"
            else -> ""
        }
        val notas = etNotas.text.toString().trim().ifEmpty { "Sin notas" }
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DatabaseHelper.COL_GLUCOSA_VALOR, valor)
            put(DatabaseHelper.COL_GLUCOSA_TIPO, tipo)
            put(DatabaseHelper.COL_GLUCOSA_FECHA, fecha)
            put(DatabaseHelper.COL_GLUCOSA_NOTAS, notas)
        }

        val success = db.insert(DatabaseHelper.TABLE_GLUCOSA, null, values)

        if (success != -1L) {
            Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show()
            loadLastReading()
            loadGraphData()
        }

        clearForm()
    }

    private fun clearForm() {
        etGlucosa.text.clear()
        etNotas.text.clear()
        rgTipo.clearCheck()
        tilGlucosa.error = null
        btnGuardar.isEnabled = false
        etGlucosa.clearFocus()
        validateForm()
    }
}
