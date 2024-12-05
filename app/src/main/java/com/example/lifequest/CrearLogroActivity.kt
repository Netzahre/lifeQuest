package com.example.lifequest

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class CrearLogroActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLiteAyudante
    private lateinit var tareasSpinner: Spinner
    private lateinit var tareasLista: List<String>
    private lateinit var barraProgreso: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_logro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        barraProgreso = findViewById(R.id.barraLogro)
        val nombreLogroInput = findViewById<EditText>(R.id.nombreLogro)
        val premioLogroInput = findViewById<TextView>(R.id.cantidadMonedas)
        val repeticionesInput = findViewById<EditText>(R.id.cantidadRepeticiones)
        tareasSpinner = findViewById(R.id.tareaAsignada) // Spinner para seleccionar la tarea
        val botonGuardar = findViewById<Button>(R.id.anadirLogro)
        barraProgreso.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                premioLogroInput.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        // Poblar el Spinner con las tareas
        cargarTareasEnSpinner()

        botonGuardar.setOnClickListener {
            // Obtener los valores de los campos de texto
            val nombre = nombreLogroInput.text.toString()
            val premioLogroInput = premioLogroInput.text.toString()
            val tareaSeleccionada = tareasSpinner.selectedItem as? String
            val repeticionesStr = repeticionesInput.text.toString()

            // Verificar que todos los campos estén llenos y que repeticiones sea un número válido
            if (nombre.isNotEmpty() && premioLogroInput.isNotEmpty() && tareaSeleccionada != null && repeticionesStr.isNotEmpty()) {
                val repeticiones = repeticionesStr.toIntOrNull()
                if (repeticiones != null && repeticiones > 0) {
                    // Crear el logro en la base de datos
                    crearLogro(nombre, premioLogroInput, tareaSeleccionada, repeticiones)

                    // Mostrar un mensaje de éxito
                    Toast.makeText(this, "¡Logro creado exitosamente!", Toast.LENGTH_SHORT).show()
                    finish()

                } else {
                    Toast.makeText(this, "Por favor ingresa un número válido para las repeticiones", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para cargar las tareas en el Spinner
    private fun cargarTareasEnSpinner() {
        val db = dbHelper.readableDatabase
        val usuario = obtenerUsuarioActual()

        // Consulta para obtener las tareas del usuario actual
        val cursor = db.rawQuery("SELECT nombre FROM Tareas WHERE usuario = ?", arrayOf(usuario))
        // Extraer nombres de tareas en una lista
        tareasLista = mutableListOf<String>().apply {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("nombre")))
            }
        }
        cursor.close()

        // Configurar el adaptador del Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tareasLista
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tareasSpinner.adapter = adapter
    }

    // Método para insertar el logro en la base de datos
    private fun crearLogro(
        nombre: String,
        premio: String,
        tareaAsociada: String,
        repeticionesNecesarias: Int
    ) {
        val usuario = obtenerUsuarioActual()
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("nombre", nombre)
            put("premio", premio)
            put("tarea_asociada", tareaAsociada)
            put("repeticiones_necesarias", repeticionesNecesarias)
            put("progreso", 0)
            put("completado", 0)
            put("usuario", usuario)
        }
        db.insert("logros", null, values)
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




