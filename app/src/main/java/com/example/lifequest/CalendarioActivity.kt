package com.example.lifequest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CalendarioActivity : AppCompatActivity() {
    private lateinit var tareasLinearLayout: LinearLayout
    private lateinit var datePicker: DatePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Inicializo el linear layout de tareas y el date picker del calendario
        tareasLinearLayout = findViewById(R.id.listaTareasDia)
        datePicker = findViewById(R.id.calendario)

        // Configurar el listener para el DatePicker
        datePicker.init(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth
        ) { _, year, monthOfYear, dayOfMonth ->
            val fechaSeleccionada = "$year-${monthOfYear + 1}-$dayOfMonth"
            mostrarTareasPorFecha(fechaSeleccionada)
        }
    }

    private fun mostrarTareasPorFecha(fecha: String) {
        val db = obtenerDbHelper().readableDatabase

        val cursor = db.rawQuery("SELECT * FROM Tareas WHERE fechaInicio <= ? AND completada = 0", arrayOf(fecha))

        // Limpiar el LinearLayout antes de añadir nuevas vistas
        tareasLinearLayout.removeAllViews()

        // Recorrer las tareas y agregar las que pueden ser completadas
        while (cursor.moveToNext()) {
            val tarea = Tarea(
                cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getString(cursor.getColumnIndex("nombre")),
                cursor.getInt(cursor.getColumnIndex("monedas")),
                cursor.getInt(cursor.getColumnIndex("repeticiones")),
                cursor.getString(cursor.getColumnIndex("tipoRepeticion")),
                cursor.getString(cursor.getColumnIndex("fechaInicio")),
                cursor.getInt(cursor.getColumnIndex("completada")),
                cursor.getString(cursor.getColumnIndex("ultimaRepeticion")),
                cursor.getInt(cursor.getColumnIndex("vecesCompletada")),
                cursor.getString(cursor.getColumnIndex("usuario"))
            )

            // Verificar si la tarea tiene repetición pendiente
            if (isRepeticionPendiente(tarea, fecha)) {
                // Crear una vista para la tarea
                val tareaView = crearVistaTarea(tarea)
                tareasLinearLayout.addView(tareaView)
            }
        }

        cursor.close()
        db.close()
    }

    private fun isRepeticionPendiente(tarea: Tarea, fechaSeleccionada: String): Boolean {
        val ultimaRepeticion = tarea.ultimaRepeticion
        val tipoRepeticion = tarea.tipoRepeticion
        val repeticiones = tarea.repeticiones

        if (ultimaRepeticion == null) return false

        return when (tipoRepeticion) {
            "Diaria" -> obtenerDiferenciaEnDias(fechaSeleccionada, ultimaRepeticion) >= repeticiones
            "Semanal" -> obtenerDiferenciaEnSemanas(fechaSeleccionada, ultimaRepeticion) >= repeticiones
            else -> false
        }
    }


    private fun obtenerDiferenciaEnDias(fechaActual: String, fechaAnterior: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val actual = sdf.parse(fechaActual)
        val anterior = sdf.parse(fechaAnterior)
        return ((actual.time - anterior.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun obtenerDiferenciaEnSemanas(fechaActual: String, fechaAnterior: String): Int {
        val dias = obtenerDiferenciaEnDias(fechaActual, fechaAnterior)
        return dias / 7
    }

    private fun parseFecha(fecha: String?): Date {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatoFecha.parse(fecha) ?: Date()
    }


    private fun crearVistaTarea(tarea: Tarea): View {
        // Usamos un LinearLayout para cada tarea
        val tareaLayout = LinearLayout(this)
        tareaLayout.orientation = LinearLayout.VERTICAL
        tareaLayout.setPadding(16, 16, 16, 16)
        tareaLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Título de la tarea
        val nombreTarea = TextView(this)
        nombreTarea.text = tarea.nombre
        nombreTarea.textSize = 18f
        nombreTarea.setTextAppearance(this, R.style.texto)
        // Añadir las vistas al LinearLayout
        tareaLayout.addView(nombreTarea)

        return tareaLayout
    }

    private fun obtenerDbHelper(): SQLiteAyudante {
        return SQLiteAyudante(this, "LifeQuest", null, 1)
    }
}
