package pa.ac.utp.miprimeraapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class HydrationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dbHelper = DatabaseHelper.getInstance(context)
        val db = dbHelper.readableDatabase
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        
        var currentGlasses = 0
        val cursor = db.query(DatabaseHelper.TABLE_HIDRATACION, arrayOf(DatabaseHelper.COL_HIDRATACION_CANTIDAD),
            "${DatabaseHelper.COL_HIDRATACION_FECHA} = ?", arrayOf(today), null, null, null)
        
        if (cursor.moveToFirst()) {
            currentGlasses = cursor.getInt(0)
        }
        cursor.close()

        if (currentGlasses < 8) {
            showNotification(context, currentGlasses)
        }
    }

    private fun showNotification(context: Context, count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hydration_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Recordatorio de Hidratación", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("¡Hora de beber agua!")
            .setContentText("Llevas $count de 8 vasos hoy. ¡Mantente hidratado!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }
}