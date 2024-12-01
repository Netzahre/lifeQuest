package com.example.lifequest

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class crearTarea : AppCompatActivity() {

    lateinit var cantidadMonedas : TextView
    lateinit var añadirTarea : Button
    lateinit var botonAtras : Button
    lateinit var nombreTextView: EditText
    lateinit var barraProgreso : SeekBar
    lateinit var cantidadRepeticiones : EditText
    lateinit var tipoRepeticion : Spinner
    lateinit var calendario : CalendarView

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
        añadirTarea = findViewById(R.id.añadirtar)
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
    }
}