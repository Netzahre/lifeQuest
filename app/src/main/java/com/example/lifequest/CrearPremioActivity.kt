package com.example.lifequest

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CrearPremioActivity : AppCompatActivity() {
    private lateinit var nombrePremioEditText: EditText
    private lateinit var barraPremioSeekBar: SeekBar
    lateinit var cantidadMonedas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_premio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val añadirPremioButton = findViewById<Button>(R.id.añadirPremio)
        val botonAtras = findViewById<Button>(R.id.botonAtras)

        nombrePremioEditText = findViewById(R.id.nombrePremio)
        barraPremioSeekBar = findViewById(R.id.barraPremio)
        cantidadMonedas = findViewById(R.id.cantidadMonedas)
        barraPremioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cantidadMonedas.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        añadirPremioButton.setOnClickListener {
           añadirPremio()
        }

        botonAtras.setOnClickListener {
            finish()
        }

    }

    private fun añadirPremio() {
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.writableDatabase
        val nombre = nombrePremioEditText.text.toString().trim()
        val usuario = obtenerUsuarioActual()
        val costo = cantidadMonedas.text.toString().toInt()

        if (usuario.isEmpty()) {
            Toast.makeText(this, "No se encontró un usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese un nombre para el premio", Toast.LENGTH_SHORT).show()
            return
        }

        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("costo", costo)
            put("usuario", usuario)
        }

        val resultado = db.insert("premios", null, valores)
        db.close()

        if (resultado != -1L) {
            Toast.makeText(this, "Premio añadido exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al añadir el premio", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun obtenerUsuarioActual(): String {
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery("SELECT usuario FROM sesionActual", null)
        val usuario = if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            ""
        }

        cursor.close()
        db.close()

        return usuario
    }


}