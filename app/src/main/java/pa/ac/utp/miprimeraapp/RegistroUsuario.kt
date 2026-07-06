package pa.ac.utp.miprimeraapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class RegistroUsuario : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_usuario)

        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etEdad = findViewById<TextInputEditText>(R.id.etEdad)
        val etPeso = findViewById<TextInputEditText>(R.id.etPeso)
        val etEstatura = findViewById<TextInputEditText>(R.id.etEstatura)
        val cbAceptar = findViewById<CheckBox>(R.id.cbAceptar)
        val btnContinuar = findViewById<MaterialButton>(R.id.btnContinuar)
        val tvPrivacidad = findViewById<TextView>(R.id.tvPrivacidad)

        tvPrivacidad.text = """
            AVISO DE PRIVACIDAD - SALUDAPP
            (Cumplimiento Ley N° 81 de 2019, Panamá)
            
            1. RESPONSABLE DEL TRATAMIENTO:
            Jaseth Castillo (jasetha10@gmail.com | +507 6849-7917).
            
            2. DATOS SENSIBLES: SaludApp procesa datos de salud (glucosa, presión, peso, medicamentos). Estos datos se almacenan exclusivamente de forma LOCAL en su dispositivo.
            
            3. FINALIDAD: Monitoreo personal de parámetros de salud y gestión de recordatorios. No se comparten datos con terceros.
            
            4. DERECHOS ARCO: Usted tiene derecho al Acceso, Rectificación, Cancelación y Oposición. 
               - Rectificación: Puede editar su perfil en Configuración.
               - Cancelación: Puede usar el botón "Eliminar toda mi información" para borrar permanentemente la base de datos local.
            
            5. SEGURIDAD: Usted es el custodio de su información. Se recomienda mantener el bloqueo de pantalla activo en su dispositivo.
            
            Al presionar "COMENZAR", usted otorga su CONSENTIMIENTO INFORMADO y EXPRESO para el tratamiento de sus datos según los términos descritos.
        """.trimIndent()

        cbAceptar.setOnCheckedChangeListener { _, isChecked ->
            btnContinuar.isEnabled = isChecked
        }

        btnContinuar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val edad = etEdad.text.toString().trim()
            val pesoStr = etPeso.text.toString().trim()
            val estaturaStr = etEstatura.text.toString().trim()

            if (nombre.isEmpty() || edad.isEmpty() || pesoStr.isEmpty() || estaturaStr.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val peso = pesoStr.toFloat()
            val estatura = estaturaStr.toFloat()
            val edadInt = edad.toInt()

            // Calcular IMC inicial
            val estaturaMetros = estatura / 100
            val imcInicial = peso / (estaturaMetros * estaturaMetros)

            // Guardar en SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("userName", nombre)
                putInt("userAge", edadInt)
                putFloat("userWeight", peso)
                putFloat("userHeight", estatura)
                putBoolean("isRegistered", true)
                apply()
            }

            // Inicializar Registros en SQLite
            saveInitialDataToSQLite(peso, imcInicial.toDouble())

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun saveInitialDataToSQLite(peso: Float, imc: Double) {
        val dbHelper = DatabaseHelper.getInstance(this)
        val db = dbHelper.writableDatabase
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // 1. Primer registro de Peso con IMC calculado
        val valuesPeso = ContentValues().apply {
            put(DatabaseHelper.COL_PESO_FECHA, today)
            put(DatabaseHelper.COL_PESO_VALOR, peso.toDouble())
            put(DatabaseHelper.COL_PESO_IMC, imc)
        }
        db.insert(DatabaseHelper.TABLE_PESO, null, valuesPeso)

        // 2. Primer registro de Actividad (0 pasos para hoy)
        val valuesActividad = ContentValues().apply {
            put(DatabaseHelper.COL_ACTIVIDAD_FECHA, today)
            put(DatabaseHelper.COL_ACTIVIDAD_PASOS, 0)
        }
        db.replace(DatabaseHelper.TABLE_ACTIVIDAD, null, valuesActividad)

        // 3. Hidratación inicial (0 vasos para hoy)
        val valuesAgua = ContentValues().apply {
            put(DatabaseHelper.COL_HIDRATACION_FECHA, today)
            put(DatabaseHelper.COL_HIDRATACION_CANTIDAD, 0)
        }
        db.replace(DatabaseHelper.TABLE_HIDRATACION, null, valuesAgua)
    }
}