package pa.ac.utp.miprimeraapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class presiona_arterial : AppCompatActivity() {
    private lateinit var btnFecha: Button
    private lateinit var txtFecha: TextView
    private lateinit var timePicker: TimePicker
    private lateinit var npSistolica: NumberPicker
    private lateinit var npDiastolica: NumberPicker
    private lateinit var npPulso: NumberPicker
    private lateinit var rgBrazo: RadioGroup
    private lateinit var txtResultado: TextView
    private lateinit var btnAnalizar: Button
    private lateinit var pressureGraph: PressureGraphView
    private var fechaSeleccionada: String? = null
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_presiona_arterial)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper.getInstance(this)
        btnFecha = findViewById(R.id.btnFecha)
        txtFecha = findViewById(R.id.txtFecha)
        timePicker = findViewById(R.id.timePicker)
        npSistolica = findViewById(R.id.npSistolica)
        npDiastolica = findViewById(R.id.npDiastolica)
        npPulso = findViewById(R.id.npPulso)
        rgBrazo = findViewById(R.id.rgBrazo)
        txtResultado = findViewById(R.id.txtResultado)
        btnAnalizar = findViewById(R.id.btnAnalizar)
        pressureGraph = findViewById(R.id.pressureGraph)


        timePicker.setIs24HourView(true)

        npSistolica.minValue = 80
        npSistolica.maxValue = 200

        npDiastolica.minValue = 40
        npDiastolica.maxValue = 130

        npPulso.minValue = 40
        npPulso.maxValue = 180


        btnFecha.setOnClickListener {
            mostrarDatePicker()
        }

        btnAnalizar.setOnClickListener {
            analizarMedicion()
        }
        
        loadGraphData()
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
                txtFecha.text = "Fecha: $fechaSeleccionada"
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    private fun loadGraphData() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PRESION, arrayOf(DatabaseHelper.COL_PRESION_SISTOLICA, DatabaseHelper.COL_PRESION_DIASTOLICA),
            null, null, null, null, "${DatabaseHelper.COL_PRESION_ID} DESC", "10")
        
        val sisList = mutableListOf<Int>()
        val diaList = mutableListOf<Int>()
        
        while (cursor.moveToNext()) {
            sisList.add(cursor.getInt(0))
            diaList.add(cursor.getInt(1))
        }
        cursor.close()
        
        pressureGraph.setData(sisList.reversed(), diaList.reversed())
    }

    private fun analizarMedicion() {

        if (fechaSeleccionada == null) {
            Toast.makeText(this, "Debe seleccionar una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        if (rgBrazo.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Debe seleccionar el brazo", Toast.LENGTH_SHORT).show()
            return
        }

        val sistolica = npSistolica.value
        val diastolica = npDiastolica.value
        val pulso = npPulso.value

        val hora = String.format("%02d:%02d", timePicker.hour, timePicker.minute)

        val brazo = findViewById<RadioButton>(rgBrazo.checkedRadioButtonId).text

        val clasificacion = clasificarPresion(sistolica, diastolica)

        saveToDatabase(sistolica, diastolica, pulso, brazo.toString(), fechaSeleccionada!!, hora)

        txtResultado.text = """
            Resumen de la medición
            
            Fecha: $fechaSeleccionada
            Hora: $hora
            Presión sistólica: $sistolica mmHg
            Presión diastólica: $diastolica mmHg
            Pulso cardíaco: $pulso BPM
            Brazo utilizado: $brazo
            
            Clasificación: $clasificacion
        """.trimIndent()
        
        loadGraphData()
    }

    private fun clasificarPresion(sistolica: Int, diastolica: Int): String {
        return when {
            sistolica < 90 || diastolica < 60 -> "Presión baja"
            sistolica in 90..119 && diastolica in 60..79 -> "Presión normal"
            sistolica in 120..129 && diastolica < 80 -> "Presión elevada"
            else -> "Hipertensión"
        }
    }

    private fun saveToDatabase(sis: Int, dia: Int, pulse: Int, arm: String, date: String, time: String) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DatabaseHelper.COL_PRESION_SISTOLICA, sis)
            put(DatabaseHelper.COL_PRESION_DIASTOLICA, dia)
            put(DatabaseHelper.COL_PRESION_PULSO, pulse)
            put(DatabaseHelper.COL_PRESION_BRAZO, arm)
            put(DatabaseHelper.COL_PRESION_FECHA, date)
            put(DatabaseHelper.COL_PRESION_HORA, time)
        }
        db.insert(DatabaseHelper.TABLE_PRESION, null, values)
    }
}