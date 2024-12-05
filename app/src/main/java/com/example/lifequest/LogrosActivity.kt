package com.example.lifequest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LogrosActivity : AppCompatActivity() {
    private val logrosSeleccionados = mutableSetOf<Int>()
    private var modoSeleccionActivo = false
    private lateinit var contenedorLogros: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logros)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
                Toast.makeText(this, "Selecciona los logros a borrar", Toast.LENGTH_SHORT).show()
            } else {
                // Confirma y borra los logros seleccionados
                if (logrosSeleccionados.isEmpty()) {
                    Toast.makeText(this, "No has seleccionado ningún logro", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    eliminarLogrosSeleccionados()
                    Toast.makeText(this, "Logros eliminados", Toast.LENGTH_SHORT).show()
                }
                // Desactiva el modo de selección
                modoSeleccionActivo = false
                resetearEstadoLogros(contenedorLogros)
            }
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        val logros = cargarLogrosDesdeBD()
        mostrarLogros(logros)
    }


    private fun crearVistaLogro(
        id: Int, nombre: String, descripcion: String, completado: Boolean
    ): View {
        val inflater = LayoutInflater.from(this)
        val nuevoLogro = inflater.inflate(R.layout.logro, null)

        val nombreView = nuevoLogro.findViewById<TextView>(R.id.nombre)
        val gananciaView = nuevoLogro.findViewById<TextView>(R.id.ganancia)

        nombreView.text = nombre
        gananciaView.text = descripcion

        if (completado) {
            nuevoLogro.setBackgroundColor(Color.parseColor("#FFC107")) // Amarillo
        }

        nuevoLogro.setOnClickListener {
            if (modoSeleccionActivo) {
                // Manejo de selección de logros
                if (logrosSeleccionados.contains(id)) {
                    logrosSeleccionados.remove(id)  // Deseleccionamos el logro
                    nuevoLogro.alpha = 1.0f // Restaura la opacidad
                } else {
                    logrosSeleccionados.add(id)  // Seleccionamos el logro
                    nuevoLogro.alpha = 0.5f // Cambia la opacidad al seleccionarlo
                }
            }
        }

        return nuevoLogro
    }

    private fun eliminarLogrosSeleccionados() {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase

        logrosSeleccionados.forEach { id ->
            bd.execSQL("DELETE FROM logros WHERE id = ?", arrayOf(id))
        }

        logrosSeleccionados.clear()  // Limpia la selección
        bd.close()

        val logros = cargarLogrosDesdeBD()
        mostrarLogros(logros)  // Actualiza la vista con los logros restantes
    }

    private fun mostrarLogros(logros: List<Logro>) {
        contenedorLogros.removeAllViews() // Limpia el contenedor antes de añadir nuevos logros

        logros.forEach { logro ->
            val descripcion = if (logro.completado) {
                "¡Logro completado! ¡Felicidades!"
            } else {
                "Tarea: ${logro.tareaAsociada}, Progreso: ${logro.progreso}/${logro.repeticionesNecesarias}"
            }

            val vistaLogro = crearVistaLogro(logro.id, logro.nombre, descripcion, logro.completado)
            contenedorLogros.addView(vistaLogro)
        }
    }


    private fun resetearEstadoLogros(contenedorLogros: LinearLayout) {
        for (i in 0 until contenedorLogros.childCount) {
            val vista = contenedorLogros.getChildAt(i)
            if (vista is CardView) {
                vista.alpha = 1.0f // Restaura la opacidad
            }
        }
    }


    private fun cargarLogrosDesdeBD(): List<Logro> {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val usuarioActual = obtenerUsuarioActual()

        if (usuarioActual == null) {
            Toast.makeText(this, "No se encontró un usuario activo", Toast.LENGTH_SHORT).show()
            bd.close()
            return emptyList()
        }

        val cursor = bd.rawQuery(
            "SELECT id, nombre, tarea_asociada, repeticiones_necesarias, progreso, completado " +
                    "FROM logros WHERE usuario = ?",
            arrayOf(usuarioActual)
        )

        val logros = mutableListOf<Logro>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val nombre = cursor.getString(1)
            val tareaAsociada = cursor.getString(2)
            val repeticionesNecesarias = cursor.getInt(3)
            val progreso = cursor.getInt(4)
            val completado = cursor.getInt(5) == 1

            val logro = Logro(id, nombre, tareaAsociada, repeticionesNecesarias, progreso, completado)
            logros.add(logro)
        }

        cursor.close()
        bd.close()

        return logros
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
