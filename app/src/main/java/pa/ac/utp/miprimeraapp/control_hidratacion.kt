package pa.ac.utp.miprimeraapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class control_hidratacion : AppCompatActivity() {

    private var currentGlasses = 0
    private val goalGlasses = 8

    private lateinit var tvPercentage: TextView
    private lateinit var tvCurrentWater: TextView
    private lateinit var pbWater: ProgressBar
    private lateinit var btnAddGlass: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var hydrationGraph: HydrationGraphView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control_hidratacion)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper.getInstance(this)
        tvPercentage = findViewById(R.id.tvPercentage)
        tvCurrentWater = findViewById(R.id.tvCurrentWater)
        pbWater = findViewById(R.id.pbWater)
        btnAddGlass = findViewById(R.id.btnAddGlass)
        btnReset = findViewById(R.id.btnReset)
        hydrationGraph = findViewById(R.id.hydrationGraph)

        loadCurrentDayGlasses()
        loadGraphData()
        updateUI()
        setupReminders()

        btnAddGlass.setOnClickListener {
            addGlass()
        }

        btnReset.setOnClickListener {
            currentGlasses = 0
            saveGlassesToDb()
            updateUI()
            loadGraphData()
        }
    }

    private fun addGlass() {
        currentGlasses += 1
        saveGlassesToDb()
        updateUI()
        loadGraphData()
    }

    private fun loadCurrentDayGlasses() {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_HIDRATACION, arrayOf(DatabaseHelper.COL_HIDRATACION_CANTIDAD),
            "${DatabaseHelper.COL_HIDRATACION_FECHA} = ?", arrayOf(date), null, null, null)
        
        if (cursor.moveToFirst()) {
            currentGlasses = cursor.getInt(0)
        } else {
            currentGlasses = 0
        }
        cursor.close()
    }

    private fun loadGraphData() {
        val db = dbHelper.readableDatabase
        val glassesList = mutableListOf<Int>()
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        // Ajustar al lunes de la semana actual
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, -6)
        }

        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val cursor = db.query(DatabaseHelper.TABLE_HIDRATACION, arrayOf(DatabaseHelper.COL_HIDRATACION_CANTIDAD),
                "${DatabaseHelper.COL_HIDRATACION_FECHA} = ?", arrayOf(dateStr), null, null, null)
            
            if (cursor.moveToFirst()) {
                glassesList.add(cursor.getInt(0))
            } else {
                glassesList.add(0)
            }
            cursor.close()
            cal.add(Calendar.DATE, 1)
        }
        
        hydrationGraph.setData(glassesList)
    }

    private fun saveGlassesToDb() {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DatabaseHelper.COL_HIDRATACION_FECHA, date)
            put(DatabaseHelper.COL_HIDRATACION_CANTIDAD, currentGlasses)
        }
        
        db.replace(DatabaseHelper.TABLE_HIDRATACION, null, values)
    }

    private fun updateUI() {
        pbWater.progress = currentGlasses
        tvCurrentWater.text = "$currentGlasses / $goalGlasses Vasos"
        val percentage = (currentGlasses.toFloat() / goalGlasses * 100).toInt()
        tvPercentage.text = "$percentage%"
    }

    private fun setupReminders() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val interval = 5 * 60 * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + interval

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            interval,
            pendingIntent
        )
    }
}