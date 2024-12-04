package com.example.lifequest

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CrearTareaActivity : AppCompatActivity() {

    lateinit var cantidadMonedas: TextView
    lateinit var anadirTarea: Button
    lateinit var botonAtras: Button
    lateinit var nombreTextView: EditText
    lateinit var barraProgreso: SeekBar
    lateinit var cantidadRepeticiones: EditText
    lateinit var tipoRepeticion: Spinner
    lateinit var datePicker: DatePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_tarea)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var listaspinner = arrayOf("veces", "dias", "semanas")
        tipoRepeticion = findViewById<Spinner>(R.id.tipoRepeticion)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaspinner)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoRepeticion.adapter = adapter

        cantidadMonedas = findViewById(R.id.cantidadMonLabel)
        anadirTarea = findViewById(R.id.anadirtar)
        botonAtras = findViewById(R.id.botonAtras)
        nombreTextView = findViewById(R.id.nombreTarea)
        barraProgreso = findViewById(R.id.barraProgreso)
        cantidadRepeticiones = findViewById(R.id.cantidadRepeticiones)
        datePicker = findViewById(R.id.calendario)

        barraProgreso.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cantidadMonedas.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        anadirTarea.setOnClickListener {
            añadirTarea()
        }

        botonAtras.setOnClickListener {
            finish()
        }
    }
    private fun formatearFecha(fechaEnMilisegundos: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = Date(fechaEnMilisegundos)
        return sdf.format(fecha)
    }

    fun añadirTarea() {
        val usuarioActual = obtenerUsuarioActual()
        if (usuarioActual == null) {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val nombre = nombreTextView.text.toString()
            val monedas = cantidadMonedas.text.toString().toIntOrNull() ?: 0
            val repeticiones = cantidadRepeticiones.text.toString().toIntOrNull() ?: 1
            val tipo = tipoRepeticion.selectedItem.toString()
            val year = datePicker.year
            val month = datePicker.month
            val dayOfMonth = datePicker.dayOfMonth

            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val fechaEnMilisegundos = calendar.timeInMillis
            val fechaFormateada = formatearFecha(fechaEnMilisegundos)

            val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
            val values = ContentValues().apply {
                put("nombre", nombre)
                put("monedas", monedas)
                put("repeticiones", repeticiones)
                put("tipoRepeticion", tipo)
                put("fechaInicio", fechaFormateada)
                put("usuario", usuarioActual)
            }
            bd.insert("Tareas", null, values)
            Toast.makeText(this, "Tarea añadida", Toast.LENGTH_SHORT).show()
            bd.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al añadir tarea", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    fun obtenerUsuarioActual(): String? {
        var usuario: String? = null
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        try {
            var cursor = bd.rawQuery("SELECT usuario FROM sesionActual", null)
            if (cursor.moveToFirst()) {
                usuario = cursor.getString(0)
                cursor.close()
                bd.close()
                return usuario
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener usuario: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return usuario
    }
}