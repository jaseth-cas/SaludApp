package pa.ac.utp.miprimeraapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracion)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etName = findViewById<TextInputEditText>(R.id.etConfigName)
        val etAge = findViewById<TextInputEditText>(R.id.etConfigAge)
        val etWeight = findViewById<TextInputEditText>(R.id.etConfigWeight)
        val btnUpdate = findViewById<MaterialButton>(R.id.btnUpdateProfile)
        val swBiometric = findViewById<SwitchMaterial>(R.id.swBiometric)
        val btnPrivacy = findViewById<MaterialButton>(R.id.btnPrivacyPolicy)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDeleteData)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        
        // Cargar datos actuales (Rectificación)
        etName.setText(sharedPref.getString("userName", ""))
        etAge.setText(sharedPref.getInt("userAge", 0).toString())
        etWeight.setText(sharedPref.getFloat("userWeight", 0f).toString())
        swBiometric.isChecked = sharedPref.getBoolean("biometricEnabled", false)

        // Lógica de Rectificación
        btnUpdate.setOnClickListener {
            val name = etName.text.toString().trim()
            val ageStr = etAge.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()

            if (name.isNotEmpty() && ageStr.isNotEmpty() && weightStr.isNotEmpty()) {
                with(sharedPref.edit()) {
                    putString("userName", name)
                    putInt("userAge", ageStr.toInt())
                    putFloat("userWeight", weightStr.toFloat())
                    apply()
                }
                Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Seguridad Proactiva (Biometría)
        swBiometric.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("biometricEnabled", isChecked).apply()
            val status = if (isChecked) "activado" else "desactivado"
            Toast.makeText(this, "Acceso biométrico $status", Toast.LENGTH_SHORT).show()
        }

        // Transparencia (Aviso Ley 81 y Contacto)
        btnPrivacy.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Aviso de Privacidad (Ley N° 81)")
                .setMessage("""
                    RESPONSABLE:
                    Jaseth Castillo
                    Gmail: jasetha10@gmail.com
                    Número: +507 6849-7917
                    
                    TÉRMINOS Y CONDICIONES:
                    1. Sus datos de salud son PRIVADOS y se almacenan solo en este dispositivo.
                    2. No recolectamos datos innecesarios.
                    3. Usted tiene control total (ARCO) sobre su información.
                    4. La seguridad proactiva se garantiza mediante cifrado local y opción de acceso biométrico.
                    
                    Para dudas o ejercicio de derechos, contáctenos por los medios arriba descritos.
                """.trimIndent())
                .setPositiveButton("ENTENDIDO", null)
                .show()
        }

        // Derecho de Cancelación (Borrado Total)
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿ELIMINAR TODA SU INFORMACIÓN?")
                .setMessage("Esta acción es IRREVERSIBLE. Se borrarán todos los registros de glucosa, presión, peso, medicamentos y su perfil personal según el Art. 15 de la Ley 81.")
                .setPositiveButton("ELIMINAR TODO") { _, _ ->
                    // 1. Detener el servicio de pasos si está activo
                    stopService(Intent(this, StepTrackerService::class.java))

                    // 2. Limpiar Base de Datos
                    val dbHelper = DatabaseHelper.getInstance(this)
                    dbHelper.clearAllData()
                    
                    // 3. Limpiar TODAS las SharedPreferences
                    val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val stepPref = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
                    val myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                    
                    sharedPref.edit().clear().apply()
                    stepPref.edit().clear().apply()
                    myPrefs.edit().clear().apply()
                    
                    Toast.makeText(this, "Toda su información ha sido eliminada del dispositivo", Toast.LENGTH_LONG).show()
                    
                    val intent = Intent(this, Splash::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("CANCELAR", null)
                .show()
        }
    }
}