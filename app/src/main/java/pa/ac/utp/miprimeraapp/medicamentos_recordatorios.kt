package pa.ac.utp.miprimeraapp

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class medicamentos_recordatorios : AppCompatActivity() {

    private lateinit var etMedName: TextInputEditText
    private lateinit var etDose: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var rvMedications: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private val medicationList = mutableListOf<Medication>()
    private lateinit var adapter: MedicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_medicamentos_recordatorios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper.getInstance(this)
        etMedName = findViewById(R.id.etMedName)
        etDose = findViewById(R.id.etDose)
        etTime = findViewById(R.id.etTime)
        etEndDate = findViewById(R.id.etEndDate)
        btnSave = findViewById(R.id.btnSaveReminder)
        rvMedications = findViewById(R.id.rvMedications)

        rvMedications.layoutManager = LinearLayoutManager(this)
        adapter = MedicationAdapter(medicationList) { med, position ->
            confirmDeleteMedication(med, position)
        }
        rvMedications.adapter = adapter

        checkNotificationPermission()

        etTime.setOnClickListener {
            showTimePicker()
        }

        etEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                etEndDate.setText("$d/${m + 1}/$y")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSave.setOnClickListener {
            saveReminder()
        }

        loadMedications()
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
            .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
            .setTitleText("Seleccionar la hora")
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .build()

        picker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")

        picker.addOnPositiveButtonClickListener {
            etTime.setText(String.format("%02d:%02d", picker.hour, picker.minute))
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun loadMedications() {
        medicationList.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_MEDICAMENTOS, null, null, null, null, null, "${DatabaseHelper.COL_MED_ID} DESC")
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_NOMBRE))
                val dose = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_DOSIS))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_HORA))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_FECHA_FIN))
                medicationList.add(Medication(id, name, dose, time, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun confirmDeleteMedication(med: Medication, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar recordatorio")
            .setMessage("¿Deseas eliminar el recordatorio de ${med.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                cancelAlarm(med.id)
                eliminarMedicamento(med.id, position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cancelAlarm(medId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, medId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun eliminarMedicamento(id: Int, position: Int) {
        val db = dbHelper.writableDatabase
        val deleted = db.delete(DatabaseHelper.TABLE_MEDICAMENTOS, "${DatabaseHelper.COL_MED_ID} = ?", arrayOf(id.toString()))
        if (deleted > 0) {
            medicationList.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReminder() {
        val name = etMedName.text.toString().trim()
        val dose = etDose.text.toString().trim()
        val time = etTime.text.toString().trim()
        val date = etEndDate.text.toString().trim()

        if (name.isEmpty() || dose.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Por favor complete los campos", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val db = dbHelper.writableDatabase
            val values = android.content.ContentValues().apply {
                put(DatabaseHelper.COL_MED_NOMBRE, name)
                put(DatabaseHelper.COL_MED_DOSIS, dose)
                put(DatabaseHelper.COL_MED_HORA, time)
                put(DatabaseHelper.COL_MED_FECHA_FIN, date)
            }
            val newId = db.insert(DatabaseHelper.TABLE_MEDICAMENTOS, null, values)
            
            if (newId != -1L) {
                scheduleNotification(newId.toInt(), name, dose, time)
                Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                
                etMedName.text?.clear()
                etDose.text?.clear()
                etTime.text?.clear()
                etEndDate.text?.clear()
                loadMedications()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification(medId: Int, medName: String, dose: String, timeStr: String) {
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("MED_NAME", medName)
            putExtra("DOSE", dose)
            putExtra("MED_ID", medId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, medId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de seguridad con la alarma", Toast.LENGTH_SHORT).show()
        }
    }
}