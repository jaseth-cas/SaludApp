package pa.ac.utp.miprimeraapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class actividad_fisica : AppCompatActivity() {

    private lateinit var tvSteps: TextView
    private lateinit var tvStepsAverage: TextView
    private lateinit var btnStartStop: MaterialButton
    private lateinit var btnHistory: MaterialButton
    private lateinit var progressBarSteps: ProgressBar
    private lateinit var lineGraph: LineGraphView
    private lateinit var dbHelper: DatabaseHelper

    private val stepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val steps = intent?.getIntExtra("steps", 0) ?: 0
            updateUI(steps)
            loadGraphData() // Update graph and average in real-time
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actividad_fisica)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper.getInstance(this)
        tvSteps = findViewById(R.id.tvSteps)
        tvStepsAverage = findViewById(R.id.tvStepsAverage)
        btnStartStop = findViewById(R.id.btnStartStop)
        btnHistory = findViewById(R.id.btnHistorySteps)
        progressBarSteps = findViewById(R.id.progressBarSteps)
        lineGraph = findViewById(R.id.lineGraph)

        checkPermission()

        btnStartStop.setOnClickListener {
            val sharedPref = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
            val isRunning = sharedPref.getBoolean("serviceRunning", false)
            
            if (isRunning) {
                stopTracking()
            } else {
                startTracking()
            }
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistorialPasosActivity::class.java))
        }
        
        loadInitialData()
        loadGraphData()
    }

    private fun startTracking() {
        val serviceIntent = Intent(this, StepTrackerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        
        getSharedPreferences("StepPrefs", Context.MODE_PRIVATE).edit().putBoolean("serviceRunning", true).apply()
        updateButtonState(true)
    }

    private fun stopTracking() {
        val serviceIntent = Intent(this, StepTrackerService::class.java)
        stopService(serviceIntent)
        
        getSharedPreferences("StepPrefs", Context.MODE_PRIVATE).edit().putBoolean("serviceRunning", false).apply()
        updateButtonState(false)
    }

    private fun updateButtonState(isRunning: Boolean) {
        if (isRunning) {
            btnStartStop.text = "PARAR"
            btnStartStop.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            btnStartStop.text = "INICIAR"
            btnStartStop.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        }
    }

    private fun updateUI(steps: Int) {
        tvSteps.text = "$steps"
        progressBarSteps.progress = steps
    }

    private fun loadInitialData() {
        val sharedPref = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        updateButtonState(sharedPref.getBoolean("serviceRunning", false))

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_ACTIVIDAD, arrayOf(DatabaseHelper.COL_ACTIVIDAD_PASOS),
            "${DatabaseHelper.COL_ACTIVIDAD_FECHA} = ?", arrayOf(date), null, null, null)
        
        if (cursor.moveToFirst()) {
            updateUI(cursor.getInt(0))
        } else {
            updateUI(0)
        }
        cursor.close()
    }

    private fun loadGraphData() {
        val db = dbHelper.readableDatabase
        val stepsList = mutableListOf<Int>()
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, -6)
        }

        var totalStepsWeek = 0
        var daysWithData = 0

        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val cursor = db.query(DatabaseHelper.TABLE_ACTIVIDAD, arrayOf(DatabaseHelper.COL_ACTIVIDAD_PASOS),
                "${DatabaseHelper.COL_ACTIVIDAD_FECHA} = ?", arrayOf(dateStr), null, null, null)
            
            if (cursor.moveToFirst()) {
                val steps = cursor.getInt(0)
                stepsList.add(steps)
                totalStepsWeek += steps
                if (steps > 0) daysWithData++
            } else {
                stepsList.add(0)
            }
            cursor.close()
            cal.add(Calendar.DATE, 1)
        }
        
        lineGraph.setData(stepsList)
        
        // Calculate average
        val average = if (daysWithData > 0) totalStepsWeek / daysWithData else 0
        tvStepsAverage.text = "Promedio semanal: $average pasos"
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stepReceiver, IntentFilter("STEP_UPDATE"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stepReceiver, IntentFilter("STEP_UPDATE"))
        }
        loadInitialData()
        loadGraphData()
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(stepReceiver)
        } catch (e: Exception) {}
    }
}