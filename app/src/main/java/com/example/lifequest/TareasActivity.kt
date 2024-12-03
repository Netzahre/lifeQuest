package com.example.lifequest

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TareasActivity : AppCompatActivity() {
    private val tareasSeleccionadas = mutableSetOf<Int>() // Almacena IDs de logros seleccionados
    private var modoSeleccionActivo = false // Controla si el modo de selección está activo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tareas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda("Aqui puedes ver todas tus tareas y borrar las que ya no necesites, o crear nuevas tareas. Puedes tambien completar tareas para ganar monedas.")

        val botonCrear = findViewById<Button>(R.id.crearTarea)
        val botonBorrar = findViewById<Button>(R.id.borrarTarea)
        val contenedorTareas = findViewById<LinearLayout>(R.id.contenedorTareas)

        cargarTareas(contenedorTareas)

        botonCrear.setOnClickListener {
            val intent = android.content.Intent(this, CrearTareaActivity::class.java)
            startActivity(intent)
        }

        botonBorrar.setOnClickListener {
            borrarTareasSeleccionadas(contenedorTareas)
        }

    }

    private fun resetearEstadoTareas(contenedorTareas: LinearLayout) {
        // Itera sobre todos los hijos del contenedor
        for (i in 0 until contenedorTareas.childCount) {
            val vista = contenedorTareas.getChildAt(i)
            // Verifica si el hijo es del tipo CardView antes de operar
            if (vista is CardView) {
                vista.alpha = 1.0f // Restaura la opacidad
            }
        }
    }

    private fun cargarTareas(contenedorTareas: LinearLayout) {
        // Obtener el usuario activo
        val usuarioActivo = obtenerUsuarioActual()
        if (usuarioActivo == null) {
            Toast.makeText(this, "No se encontró un usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener tareas del usuario activo desde la base de datos SQLite
        val bd = SQLiteAyudante(this, "LifeQuestDB", null, 1).readableDatabase
        val cursor = bd.rawQuery("SELECT * FROM Tareas WHERE usuario = ?", arrayOf(usuarioActivo))
        val tareas = mutableListOf<Triple<Int, String, String>>() // ID, nombre, monedas

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val nombre = cursor.getString(cursor.getColumnIndex("nombre"))
            val monedas = cursor.getInt(cursor.getColumnIndex("monedas"))
            tareas.add(Triple(id, nombre, monedas.toString()))
        }
        cursor.close()
        bd.close()

        // Crear las vistas para las tareas
        for ((id, nombre, monedas) in tareas) {
            val inflater = LayoutInflater.from(this)
            val nuevaTarea = inflater.inflate(R.layout.tarea, null)

            nuevaTarea.findViewById<TextView>(R.id.nombre).text = nombre
            nuevaTarea.findViewById<TextView>(R.id.ganancia).text = monedas

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 8, 16, 8)
            nuevaTarea.layoutParams = layoutParams

            nuevaTarea.tag = id // Asigna el ID de la tarea como etiqueta

            nuevaTarea.setOnClickListener {
                if (modoSeleccionActivo) {
                    if (tareasSeleccionadas.contains(id)) {
                        tareasSeleccionadas.remove(id)
                        nuevaTarea.alpha = 1.0f
                    } else {
                        tareasSeleccionadas.add(id)
                        nuevaTarea.alpha = 0.5f
                    }
                }
            }
            contenedorTareas.addView(nuevaTarea)
        }
    }

    fun obtenerUsuarioActual(): String? {
        val bd = SQLiteAyudante(this, "LifeQuestDB", null, 1).readableDatabase
        val cursor = bd.rawQuery("SELECT usuario FROM sesionActual LIMIT 1", null)
        var usuario: String? = null
        if (cursor.moveToFirst()) {
            usuario = cursor.getString(0)
        }
        cursor.close()
        bd.close()
        return usuario
    }

    private fun borrarTareasSeleccionadas(contenedorTareas: LinearLayout) {
        val usuarioActivo = obtenerUsuarioActual()
        if (usuarioActivo == null) {
            Toast.makeText(this, "No se encontró un usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        if (!modoSeleccionActivo) {
            // Activa el modo de selección para poder borrar
            modoSeleccionActivo = true
            Toast.makeText(this, "Selecciona las tareas a borrar", Toast.LENGTH_SHORT).show()
        } else {
            // Si no hay tareas seleccionadas, muestra un mensaje
            if (tareasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ninguna tarea", Toast.LENGTH_SHORT).show()
                return
            }

            // Borrar tareas seleccionadas
            val bd = SQLiteAyudante(this, "LifeQuestDB", null, 1).writableDatabase
            for (id in tareasSeleccionadas) {
                val tareaAEliminar = contenedorTareas.findViewWithTag<CardView>(id)
                contenedorTareas.removeView(tareaAEliminar)

                // Eliminar la tarea de la base de datos asociada al usuario activo
                bd.execSQL(
                    "DELETE FROM Tareas WHERE id = ? AND usuario = ?",
                    arrayOf(id.toString(), usuarioActivo)
                )
            }
            bd.close()

            tareasSeleccionadas.clear()
            Toast.makeText(this, "Tareas eliminadas", Toast.LENGTH_SHORT).show()

            // Desactiva el modo de selección y restaura el estado visual
            modoSeleccionActivo = false
            resetearEstadoTareas(contenedorTareas)
        }
    }

}