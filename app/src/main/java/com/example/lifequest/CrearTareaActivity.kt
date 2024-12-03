package com.example.lifequest

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
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
    lateinit var calendario: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_tarea)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cantidadMonedas = findViewById(R.id.cantidadMonLabel)
        anadirTarea = findViewById(R.id.añadirtar)
        botonAtras = findViewById(R.id.botonAtras)
        nombreTextView = findViewById(R.id.nombreTarea)
        barraProgreso = findViewById(R.id.barraProgreso)
        cantidadRepeticiones = findViewById(R.id.cantidadRepeticiones)
        tipoRepeticion = findViewById(R.id.tipoRepeticion)
        calendario = findViewById(R.id.calendario)

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

    fun añadirTarea() {
        val usuarioActual = obtenerUsuarioActual()
        if (usuarioActual == null) {
            // Manejar el caso donde no hay usuario activo
            return
        }

        val nombre = nombreTextView.text.toString()
        val monedas = cantidadMonedas.text.toString().toIntOrNull() ?: 0
        val repeticiones = cantidadRepeticiones.text.toString().toIntOrNull() ?: 1
        val tipo = tipoRepeticion.selectedItem.toString()
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val bd = SQLiteAyudante(this, "LifeQuestDB", null, 1).writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("monedas", monedas)
            put("repeticiones", repeticiones)
            put("tipoRepeticion", tipo)
            put("fechaInicio", fecha)
            put("completada", 0)
            put("usuario", usuarioActual)
        }
        bd.insert("Tareas", null, values)
        bd.close()
        finish()
    }

    fun obtenerUsuarioActual(): String? {
        val bd = SQLiteAyudante(this, "LifeQuestDB", null, 1).readableDatabase
        val cursor = bd.rawQuery("SELECT usuario FROM sesionActual", null)
        var usuario: String? = null
        if (cursor.moveToFirst()) {
            usuario = cursor.getString(0)
        }
        cursor.close()
        bd.close()
        return usuario
    }

}