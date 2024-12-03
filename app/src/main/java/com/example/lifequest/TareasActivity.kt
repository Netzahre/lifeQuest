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

        val botonCrear = findViewById<Button>(R.id.crearTarea)
        val botonBorrar = findViewById<Button>(R.id.borrarTarea)
        val contenedorTareas = findViewById<LinearLayout>(R.id.contenedorTareas)

        // Carga los logros desde la base de datos
        cagarTareas(contenedorTareas)

        botonCrear.setOnClickListener {
            val intent = android.content.Intent(this, CrearTareaActivity::class.java)
            startActivity(intent)
        }

        botonBorrar.setOnClickListener {
            if (!modoSeleccionActivo) {
                // Activa el modo de selección
                modoSeleccionActivo = true
                Toast.makeText(this, "Selecciona las tareas a borrar", Toast.LENGTH_SHORT).show()
            } else {
                // Confirma y borra los logros seleccionados
                if (tareasSeleccionadas.isEmpty()) {
                    Toast.makeText(this, "No has seleccionado ningúna tarea", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    for (id in tareasSeleccionadas) {
                        val logroAEliminar = contenedorTareas.findViewWithTag<CardView>(id)
                        contenedorTareas.removeView(logroAEliminar)
                        // Aquí deberías eliminar el logro de la base de datos
                    }
                    tareasSeleccionadas.clear()
                    Toast.makeText(this, "Logros eliminados", Toast.LENGTH_SHORT).show()
                }
                // Desactiva el modo de selección
                modoSeleccionActivo = false
                resetearEstadoLogros(contenedorTareas)
            }
        }
    }

    private fun resetearEstadoLogros(contenedorTareas: LinearLayout) {
        // Itera sobre todos los hijos del contenedor
        for (i in 0 until contenedorTareas.childCount) {
            val vista = contenedorTareas.getChildAt(i)
            // Verifica si el hijo es del tipo CardView antes de operar
            if (vista is androidx.cardview.widget.CardView) {
                vista.alpha = 1.0f // Restaura la opacidad
            }
        }
    }


    private fun cagarTareas(contenedorLogros: LinearLayout) {
        // Aquí obtendrás los logros de la base de datos SQLite
        val logros = listOf(
            Pair("Logro 1", "Descripción 1"),
            Pair("Logro 2", "Descripción 2"),
            Pair("Logro 3", "Descripción 3")
        ) // Ejemplo de datos

        for ((index, tarea) in logros.withIndex()) {
            val inflater = LayoutInflater.from(this)
            val nuevaTarea = inflater.inflate(R.layout.tarea, null)

            nuevaTarea.findViewById<TextView>(R.id.nombre).text = tarea.first
            nuevaTarea.findViewById<TextView>(R.id.ganancia).text = tarea.second

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 8, 16, 8)
            nuevaTarea.layoutParams = layoutParams

            nuevaTarea.tag = index // ID asignado a cada logro

            nuevaTarea.setOnClickListener {
                if (modoSeleccionActivo) {
                    if (tareasSeleccionadas.contains(index)) {
                        tareasSeleccionadas.remove(index)
                        nuevaTarea.alpha = 1.0f
                    } else {
                        tareasSeleccionadas.add(index)
                        nuevaTarea.alpha = 0.5f
                    }
                }
            }

            contenedorLogros.addView(nuevaTarea)
        }
    }
}