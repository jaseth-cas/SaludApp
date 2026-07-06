package pa.ac.utp.miprimeraapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "salud_app.db"
        private const val DATABASE_VERSION = 2

        // Singleton instance
        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }

        // Glucosa
        const val TABLE_GLUCOSA = "glucosa"
        const val COL_GLUCOSA_ID = "id"
        const val COL_GLUCOSA_VALOR = "valor"
        const val COL_GLUCOSA_TIPO = "tipo"
        const val COL_GLUCOSA_FECHA = "fecha"
        const val COL_GLUCOSA_NOTAS = "notas"

        // Presión Arterial
        const val TABLE_PRESION = "presion_arterial"
        const val COL_PRESION_ID = "id"
        const val COL_PRESION_SISTOLICA = "sistolica"
        const val COL_PRESION_DIASTOLICA = "diastolica"
        const val COL_PRESION_PULSO = "pulso"
        const val COL_PRESION_BRAZO = "brazo"
        const val COL_PRESION_FECHA = "fecha"
        const val COL_PRESION_HORA = "hora"

        // Hidratación
        const val TABLE_HIDRATACION = "hidratacion"
        const val COL_HIDRATACION_ID = "id"
        const val COL_HIDRATACION_FECHA = "fecha"
        const val COL_HIDRATACION_CANTIDAD = "cantidad"

        // Actividad Física
        const val TABLE_ACTIVIDAD = "actividad_fisica"
        const val COL_ACTIVIDAD_ID = "id"
        const val COL_ACTIVIDAD_FECHA = "fecha"
        const val COL_ACTIVIDAD_PASOS = "pasos"

        // Medicamentos
        const val TABLE_MEDICAMENTOS = "medicamentos"
        const val COL_MED_ID = "id"
        const val COL_MED_NOMBRE = "nombre"
        const val COL_MED_DOSIS = "dosis"
        const val COL_MED_HORA = "hora"
        const val COL_MED_FECHA_FIN = "fecha_fin"

        // Historial Peso
        const val TABLE_PESO = "historial_peso"
        const val COL_PESO_ID = "id"
        const val COL_PESO_FECHA = "fecha"
        const val COL_PESO_VALOR = "peso"
        const val COL_PESO_IMC = "imc"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createGlucosa = ("CREATE TABLE $TABLE_GLUCOSA ($COL_GLUCOSA_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_GLUCOSA_VALOR INTEGER, $COL_GLUCOSA_TIPO TEXT, $COL_GLUCOSA_FECHA TEXT, $COL_GLUCOSA_NOTAS TEXT)")

        val createPresion = ("CREATE TABLE $TABLE_PRESION ($COL_PRESION_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_PRESION_SISTOLICA INTEGER, $COL_PRESION_DIASTOLICA INTEGER, $COL_PRESION_PULSO INTEGER, "
                + "$COL_PRESION_BRAZO TEXT, $COL_PRESION_FECHA TEXT, $COL_PRESION_HORA TEXT)")

        val createHidratacion = ("CREATE TABLE $TABLE_HIDRATACION ($COL_HIDRATACION_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_HIDRATACION_FECHA TEXT, $COL_HIDRATACION_CANTIDAD INTEGER, UNIQUE($COL_HIDRATACION_FECHA))")

        val createActividad = ("CREATE TABLE $TABLE_ACTIVIDAD ($COL_ACTIVIDAD_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ACTIVIDAD_FECHA TEXT, $COL_ACTIVIDAD_PASOS INTEGER, UNIQUE($COL_ACTIVIDAD_FECHA))")

        val createMed = ("CREATE TABLE $TABLE_MEDICAMENTOS ($COL_MED_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_MED_NOMBRE TEXT, $COL_MED_DOSIS TEXT, $COL_MED_HORA TEXT, $COL_MED_FECHA_FIN TEXT)")

        val createPeso = ("CREATE TABLE $TABLE_PESO ($COL_PESO_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_PESO_FECHA TEXT, $COL_PESO_VALOR REAL, $COL_PESO_IMC REAL)")

        db.execSQL(createGlucosa)
        db.execSQL(createPresion)
        db.execSQL(createHidratacion)
        db.execSQL(createActividad)
        db.execSQL(createMed)
        db.execSQL(createPeso)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple upgrade logic
        if (oldVersion < 2) {
            // Add initial demo data for activity graph
            db.execSQL("INSERT OR IGNORE INTO $TABLE_ACTIVIDAD ($COL_ACTIVIDAD_FECHA, $COL_ACTIVIDAD_PASOS) VALUES ('01/05/2024', 3000)")
            db.execSQL("INSERT OR IGNORE INTO $TABLE_ACTIVIDAD ($COL_ACTIVIDAD_FECHA, $COL_ACTIVIDAD_PASOS) VALUES ('02/05/2024', 5400)")
            db.execSQL("INSERT OR IGNORE INTO $TABLE_ACTIVIDAD ($COL_ACTIVIDAD_FECHA, $COL_ACTIVIDAD_PASOS) VALUES ('03/05/2024', 4200)")
        }
    }

    fun clearAllData() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_GLUCOSA")
        db.execSQL("DELETE FROM $TABLE_PRESION")
        db.execSQL("DELETE FROM $TABLE_HIDRATACION")
        db.execSQL("DELETE FROM $TABLE_ACTIVIDAD")
        db.execSQL("DELETE FROM $TABLE_MEDICAMENTOS")
        db.execSQL("DELETE FROM $TABLE_PESO")
    }
}