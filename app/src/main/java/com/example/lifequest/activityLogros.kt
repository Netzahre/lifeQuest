package com.example.lifequest

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activityLogros : AppCompatActivity() {
    private val logrosSeleccionados = mutableSetOf<Int>() // Almacena IDs de logros seleccionados
    private var modoSeleccionActivo = false // Controla si el modo de selección está activo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logros)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botonCrear = findViewById<Button>(R.id.crearPremio)
        val botonBorrar = findViewById<Button>(R.id.borrarLogro)
        val contenedorLogros = findViewById<LinearLayout>(R.id.contenedorLogros)

        // Carga los logros desde la base de datos
        cargarLogrosDesdeBD(contenedorLogros)

        botonCrear.setOnClickListener {
            // Lógica para añadir un nuevo logro
            val inflater = LayoutInflater.from(this)
            val nuevoLogro = inflater.inflate(R.layout.logro, null)

            // Personaliza los textos del logro
            nuevoLogro.findViewById<TextView>(R.id.nombre).text = "Logro Ejemplo"
            nuevoLogro.findViewById<TextView>(R.id.ganancia).text = "Descripción del logro"

            // Configura los márgenes manualmente
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 8, 16, 8)
            nuevoLogro.layoutParams = layoutParams

            // Asigna un ID temporal para el logro
            val logroId = contenedorLogros.childCount // ID temporal basado en posición
            nuevoLogro.tag = logroId

            // Lógica de selección (solo en modo de selección)
            nuevoLogro.setOnClickListener {
                if (modoSeleccionActivo) {
                    if (logrosSeleccionados.contains(logroId)) {
                        logrosSeleccionados.remove(logroId)
                        nuevoLogro.alpha = 1.0f // Deseleccionado
                    } else {
                        logrosSeleccionados.add(logroId)
                        nuevoLogro.alpha = 0.5f // Seleccionado
                    }
                }
            }

            // Agrega el nuevo logro al contenedor
            contenedorLogros.addView(nuevoLogro)
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
                    for (id in logrosSeleccionados) {
                        val logroAEliminar = contenedorLogros.findViewWithTag<CardView>(id)
                        contenedorLogros.removeView(logroAEliminar)
                        // Aquí deberías eliminar el logro de la base de datos
                    }
                    logrosSeleccionados.clear()
                    Toast.makeText(this, "Logros eliminados", Toast.LENGTH_SHORT).show()
                }
                // Desactiva el modo de selección
                modoSeleccionActivo = false
                resetearEstadoLogros(contenedorLogros)
            }
        }
    }

    private fun resetearEstadoLogros(contenedorLogros: LinearLayout) {
        // Itera sobre todos los hijos del contenedor
        for (i in 0 until contenedorLogros.childCount) {
            val vista = contenedorLogros.getChildAt(i)
            // Verifica si el hijo es del tipo CardView antes de operar
            if (vista is androidx.cardview.widget.CardView) {
                vista.alpha = 1.0f // Restaura la opacidad
            }
        }
    }


    private fun cargarLogrosDesdeBD(contenedorLogros: LinearLayout) {
        // Aquí obtendrás los logros de la base de datos SQLite
        val logros = listOf(
            Pair("Logro 1", "Descripción 1"),
            Pair("Logro 2", "Descripción 2"),
            Pair("Logro 3", "Descripción 3")
        ) // Ejemplo de datos

        for ((index, logro) in logros.withIndex()) {
            val inflater = LayoutInflater.from(this)
            val nuevoLogro = inflater.inflate(R.layout.logro, null)

            nuevoLogro.findViewById<TextView>(R.id.nombre).text = logro.first
            nuevoLogro.findViewById<TextView>(R.id.ganancia).text = logro.second

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 8, 16, 8)
            nuevoLogro.layoutParams = layoutParams

            nuevoLogro.tag = index // ID asignado a cada logro

            nuevoLogro.setOnClickListener {
                if (modoSeleccionActivo) {
                    if (logrosSeleccionados.contains(index)) {
                        logrosSeleccionados.remove(index)
                        nuevoLogro.alpha = 1.0f
                    } else {
                        logrosSeleccionados.add(index)
                        nuevoLogro.alpha = 0.5f
                    }
                }
            }

            contenedorLogros.addView(nuevoLogro)
        }
    }
}
