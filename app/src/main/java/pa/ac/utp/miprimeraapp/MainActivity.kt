package pa.ac.utp.miprimeraapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cardPeso = findViewById<CardView>(R.id.card1)
        val cardPresion = findViewById<CardView>(R.id.card2)
        val cardGlucosa = findViewById<CardView>(R.id.card3)
        val cardActividad = findViewById<CardView>(R.id.card4)
        val cardHidratacion = findViewById<CardView>(R.id.card5)
        val cardMedicamentos = findViewById<CardView>(R.id.card6)
        val btnSettings = findViewById<android.widget.ImageButton>(R.id.btnSettings)

        btnSettings.setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }

        //  Seguimiento de Peso
        cardPeso.setOnClickListener {
            startActivity(Intent(this, actividad_peso::class.java))
        }

        //  Presión Arterial
        cardPresion.setOnClickListener {
            startActivity(Intent(this, presiona_arterial::class.java))
        }

        //  Control de Glucosa
        cardGlucosa.setOnClickListener {
            startActivity(Intent(this, control_glucosa::class.java))
        }

        //  Actividad Física
        cardActividad.setOnClickListener {
            startActivity(Intent(this, actividad_fisica::class.java))
        }

        //  Hidratación
        cardHidratacion.setOnClickListener {
            startActivity(Intent(this, control_hidratacion::class.java))
        }

        //  Medicamentos
        cardMedicamentos.setOnClickListener {
            startActivity(Intent(this, medicamentos_recordatorios::class.java))
        }
    }
}