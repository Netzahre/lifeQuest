package com.example.lifequest

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
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

    // Variables de la vista
    lateinit var cantidadMonedas: TextView
    lateinit var anadirTarea: Button
    lateinit var botonAtras: Button
    lateinit var nombreTextView: EditText
    lateinit var barraProgreso: SeekBar
    lateinit var cantidadRepeticiones: EditText
    lateinit var tipoRepeticion: Spinner
    lateinit var datePicker: DatePicker
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_tarea)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Configurar el menu superior
        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda("Desde aquí puedes añadir una nueva tarea a tu lista.")
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
        }

        // Inicializar variables de la vista
        var listaspinner = arrayOf("veces", "dias", "semanas")
        tipoRepeticion = findViewById(R.id.tipoRepeticion)
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

        // Listener para la barra de progreso de monedas
        barraProgreso.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cantidadMonedas.text = progress.toString()
            }

            // Métodos no utilizados en este caso pero necesarios para la interfaz
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Listener para el botón de añadir tarea
        anadirTarea.setOnClickListener {
            añadirTarea()
        }

        // Listener para el botón de regresar
        botonAtras.setOnClickListener {
            finish()
        }
    }

    // Método para mostrar un mensaje en pantalla
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Método para formatear la fecha en milisegundos a un formato de fecha
    private fun formatearFecha(fechaEnMilisegundos: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = Date(fechaEnMilisegundos)
        return sdf.format(fecha)
    }

    // Método para añadir una tarea a la base de datos
    fun añadirTarea() {
        val usuarioActual = obtenerUsuarioActual()
        // Verificar si se obtuvo el usuario actual
        if (usuarioActual == null) {
            mostrarMensaje("No se ha podido obtener el usuario actual")
            return
        }
        val nombre = nombreTextView.text.toString()
        val monedas = cantidadMonedas.text.toString().toIntOrNull() ?: 0
        val repeticiones = cantidadRepeticiones.text.toString().toIntOrNull() ?: 1
        val tipo = tipoRepeticion.selectedItem.toString()
        val year = datePicker.year
        val month = datePicker.month
        val dayOfMonth = datePicker.dayOfMonth

        // Crear un objeto de tipo Calendar para obtener la fecha en milisegundos y formatearla
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val fechaEnMilisegundos = calendar.timeInMillis
        val fechaFormateada = formatearFecha(fechaEnMilisegundos)

        if (nombre.isEmpty()) {
            mostrarMensaje("El nombre de la tarea no puede estar vacío")
            return
        }

        if (repeticiones <= 0) {
            mostrarMensaje("El número de repeticiones debe ser mayor que 0")
            return
        }

        // Crear una instancia de la base de datos y añadir la tarea
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("monedas", monedas)
            put("repeticiones", repeticiones)
            put("tipoRepeticion", tipo)
            put("fechaInicio", fechaFormateada)
            put("usuario", usuarioActual)
        }

        // Insertar la tarea en la base de datos
        val resultado = bd.insert("Tareas", null, values)
        mostrarMensaje("Tarea añadida correctamente")
        bd.close()

        // Mostrar un mensaje en función del resultado
        if (resultado != -1L) {
            mostrarMensaje("Tarea añadida correctamente")
            finish()
        }
    }

    // Método para obtener el usuario actual de la base de datos
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
            mostrarMensaje("Error al obtener el usuario actual")
        }
        return usuario
    }

    // Método para iniciar el reconocimiento de voz
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora para transcribir tu voz")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            mostrarMensaje("Error al iniciar el reconocimiento de voz")
        }
    }

    // Método para manejar el resultado del reconocimiento de voz
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.let {
                val accion = it[0].lowercase()
                when (accion) {
                    "tareas" -> {
                        val intent = Intent(this, TareasActivity::class.java)
                        startActivity(intent)
                    }

                    "logros" -> {
                        val intent = Intent(this, LogrosActivity::class.java)
                        startActivity(intent)
                    }

                    "tienda" -> {
                        val intent = Intent(this, TiendaActivity::class.java)
                        startActivity(intent)
                    }

                    "perfil" -> {
                        val intent = Intent(this, PerfilActivity::class.java)
                        startActivity(intent)
                    }

                    "ayuda" -> {
                        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
                        menuSuperior.mostrarAyuda("Desde aquí puedes añadir una nueva tarea a tu lista.")
                    }

                    "añadir tarea" -> {
                        val intent = Intent(this, CrearTareaActivity::class.java)
                        startActivity(intent)
                    }

                    "añadir logro" -> {
                        val intent = Intent(this, CrearLogroActivity::class.java)
                        startActivity(intent)
                    }

                    "cambiar modo" -> {
                        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
                        menuSuperior.cambiarModo()
                    }

                    "añadir premio" -> {
                        val intent = Intent(this, CrearPremioActivity::class.java)
                        startActivity(intent)
                    }

                    "Terminos de uso" -> {
                        val intent = Intent(this, TOSActivity::class.java)
                        startActivity(intent)
                    }

                    else -> {
                        // Regex para extraer la acción y los valores asociados (por ejemplo, "monedas 15")
                        val regex =
                            Regex("(\\D+)\\s+(\\d+)")
                        val matchResult = regex.find(accion)
                        if (matchResult != null) {
                            // Extraer la palabra clave y el número
                            val key =
                                matchResult.groupValues[1].trim()
                            val value =
                                matchResult.groupValues[2].toInt()
                            when (key) {
                                // Actualizar los valores de la tarea en función de la palabra clave
                                "monedas" -> {
                                    cantidadMonedas.text = value.toString()
                                }

                                "repeticiones" -> {
                                    cantidadRepeticiones.setText(value.toString())
                                }

                                else -> {
                                    // Regex para buscar una palabra clave y un valor (por ejemplo, "nombre tarea")
                                    val regex =
                                        Regex("(\\D+)\\s+(\\D+)")
                                    val matchResult = regex.find(accion)
                                    if (matchResult != null) {
                                        // Extraer la palabra clave y el valor
                                        val key =
                                            matchResult.groupValues[1].trim()
                                        val value =
                                            matchResult.groupValues[2].trim()
                                        when (key) {
                                            "nombre" -> {
                                                nombreTextView.setText(value)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}