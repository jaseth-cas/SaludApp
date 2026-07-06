package pa.ac.utp.miprimeraapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class StepTrackerService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private lateinit var dbHelper: DatabaseHelper
    private var previousTotalSteps = 0f

    override fun onCreate() {
        super.onCreate()
        dbHelper = DatabaseHelper.getInstance(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val sharedPref = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)

        createNotificationChannel()
        startForeground(1, createNotification("Contando pasos..."))

        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val totalSteps = event.values[0]
            if (previousTotalSteps == 0f) {
                previousTotalSteps = totalSteps
                savePreviousSteps(totalSteps)
            }
            
            val currentSteps = (totalSteps - previousTotalSteps).toInt()
            updateStepsInDb(currentSteps)
            updateNotification(currentSteps)
        }
    }

    private fun updateStepsInDb(steps: Int) {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DatabaseHelper.COL_ACTIVIDAD_FECHA, date)
            put(DatabaseHelper.COL_ACTIVIDAD_PASOS, steps)
        }
        db.replace(DatabaseHelper.TABLE_ACTIVIDAD, null, values)
        
        // Broadcast to Activity if it's open
        val intent = Intent("STEP_UPDATE")
        intent.putExtra("steps", steps)
        sendBroadcast(intent)
    }

    private fun savePreviousSteps(steps: Float) {
        val sharedPref = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putFloat("previousTotalSteps", steps).apply()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "step_channel", "Seguimiento de Pasos",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(content: String): Notification {
        val notificationIntent = Intent(this, actividad_fisica::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "step_channel")
            .setContentTitle("SaludApp - Actividad Física")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_actividad)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = createNotification("Pasos de hoy: $steps")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}