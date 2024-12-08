package com.example.lifequest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class LogrosActivity : AppCompatActivity() {
    private val logrosSeleccionados = mutableSetOf<Int>()
    private var modoSeleccionActivo = false
    private lateinit var contenedorLogros: LinearLayout
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logros)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda("Este es el menu de logros, aqui puedes ver los logros que has completado y los que te faltan por completar.")
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
        }
        val botonCrear = findViewById<Button>(R.id.crearLogro)
        val botonBorrar = findViewById<Button>(R.id.borrarLogro)
        contenedorLogros = findViewById(R.id.contenedorLogros)

        // Carga los logros desde la base de datos
        cargarLogrosDesdeBD()

        botonCrear.setOnClickListener {
            val intent = android.content.Intent(this, CrearLogroActivity::class.java)
            startActivity(intent)
        }

        botonBorrar.setOnClickListener {
            if (!modoSeleccionActivo) {
                // Activa el modo de selección
                modoSeleccionActivo = true
                botonBorrar.text = "Confirmar"
                mostrarMensaje("Selecciona los logros que deseas borrar")
            } else {
                // Confirma y borra los logros seleccionados
                if (logrosSeleccionados.isEmpty()) {
                    mostrarMensaje("No se seleccionaron logros")
                } else {
                    eliminarLogrosSeleccionados()
                    mostrarMensaje("Logros eliminados")
                }
                // Desactiva el modo de selección
                modoSeleccionActivo = false
                botonBorrar.text = "Borrar"
                resetearEstadoLogros(contenedorLogros)
            }
        }
    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Cargar logros desde la base de datos y mostrarlos en la vista al reanudar la actividad
    @Override
    override fun onResume() {
        super.onResume()
        val logros = cargarLogrosDesdeBD()
        mostrarLogros(logros)
    }


    // Crea una vista para un logro con los datos proporcionados
    private fun crearVistaLogro(
        id: Int, nombre: String, descripcion: String
    ): View {
        // Inflar la vista de logro
        val inflater = LayoutInflater.from(this)
        val nuevoLogro = inflater.inflate(R.layout.logro, contenedorLogros, false)

        val nombreView = nuevoLogro.findViewById<TextView>(R.id.nombre)
        val gananciaView = nuevoLogro.findViewById<TextView>(R.id.ganancia)

        nombreView.text = nombre
        gananciaView.text = descripcion

        nuevoLogro.setOnClickListener {
            if (modoSeleccionActivo) {
                // Cambia la opacidad del logro y lo añade o elimina de la lista de logros seleccionados
                if (logrosSeleccionados.contains(id)) {
                    logrosSeleccionados.remove(id)
                    nuevoLogro.alpha = 1.0f
                } else {
                    logrosSeleccionados.add(id)
                    nuevoLogro.alpha = 0.5f
                }
            }
        }
        return nuevoLogro
    }

    // Elimina los logros seleccionados de la base de datos
    private fun eliminarLogrosSeleccionados() {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase

        // Elimina los logros seleccionados de la base de datos
        logrosSeleccionados.forEach { id ->
            bd.execSQL("DELETE FROM logros WHERE id = ?", arrayOf(id))
        }

        logrosSeleccionados.clear()
        bd.close()

        // Recargar los logros desde la base de datos y mostrarlos en la vista
        val logros = cargarLogrosDesdeBD()
        mostrarLogros(logros)
    }

    // Muestra los logros en la vista
    private fun mostrarLogros(logros: List<Logro>) {
        contenedorLogros.removeAllViews()

        // Dividir logros en dos grupos: no completados primero, completados después
        val noCompletados = logros.filter { !it.completado }
        val completados = logros.filter { it.completado }

        // Procesar primero los no completados
        noCompletados.forEach { logro ->
            val descripcion =
                "Tarea: ${logro.tareaAsociada}, Progreso: ${logro.progreso}/${logro.repeticionesNecesarias}"
            val vistaLogro = crearVistaLogro(logro.id, logro.nombre, descripcion)
            contenedorLogros.addView(vistaLogro)
        }

        // Luego procesar los completados
        completados.forEach { logro ->
            val descripcion = "¡Logro completado! ¡Felicidades!"
            val vistaLogro = crearVistaLogro(logro.id, logro.nombre, descripcion)
            contenedorLogros.addView(vistaLogro)
        }
    }

    // Restaura la opacidad de todos los logros
    private fun resetearEstadoLogros(contenedorLogros: LinearLayout) {
        for (i in 0 until contenedorLogros.childCount) {
            val vista = contenedorLogros.getChildAt(i)
            if (vista is CardView) {
                vista.alpha = 1.0f // Restaura la opacidad
            }
        }
    }

    // Carga los logros desde la base de datos
    private fun cargarLogrosDesdeBD(): List<Logro> {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val usuarioActual = obtenerUsuarioActual()

        if (usuarioActual == null) {
            mostrarMensaje("No se ha encontrado un usuario activo")
            bd.close()
            return emptyList()
        }

        // Obtener logros del usuario actual
        val cursor = bd.rawQuery(
            "SELECT id, nombre, tarea_asociada, repeticiones_necesarias, progreso, completado " +
                    "FROM logros WHERE usuario = ?",
            arrayOf(usuarioActual)
        )

        // Procesar los logros obtenidos
        val logros = mutableListOf<Logro>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val nombre = cursor.getString(1)
            val tareaAsociada = cursor.getString(2)
            val repeticionesNecesarias = cursor.getInt(3)
            val progreso = cursor.getInt(4)
            val completado = cursor.getInt(5) == 1

            val logro =
                Logro(id, nombre, tareaAsociada, repeticionesNecesarias, progreso, completado)
            logros.add(logro)
        }

        cursor.close()
        bd.close()

        return logros
    }

    // Obtiene el usuario actual de la base de datos
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
            Toast.makeText(
                this, "El reconocimiento de voz no está disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
                        menuSuperior.mostrarAyuda("Este es el menu de logros, aqui puedes ver los logros que has completado y los que te faltan por completar.")
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
                        Toast.makeText(this, "No se reconoció la acción", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
