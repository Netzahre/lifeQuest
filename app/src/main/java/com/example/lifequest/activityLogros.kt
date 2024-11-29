package com.example.lifequest

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activityLogros : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logros)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val botonCrear = findViewById<Button>(R.id.crearLogro)
        val contenedorLogros = findViewById<LinearLayout>(R.id.contenedorLogros) // Ajusta este ID

        botonCrear.setOnClickListener {
            // Infla el diseño del logro
            val inflater = LayoutInflater.from(this)
            val nuevoLogro = inflater.inflate(R.layout.logro, null)

            // Personaliza los textos del logro
            nuevoLogro.findViewById<TextView>(R.id.nombre).text = "Logro Ejemplo"
            nuevoLogro.findViewById<TextView>(R.id.ganancia).text = "Descripción del logro"

            // Agrega el nuevo logro al contenedor
            contenedorLogros.addView(nuevoLogro)
        }

    }
}