package com.example.lifequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TareasActivity : AppCompatActivity() {
    private val tareasSeleccionadas = mutableSetOf<Int>()
    private var modoSeleccionActivo = false
    private lateinit var tareasLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tareasLayout = findViewById(R.id.contenedorTareas)

        // Cargar tareas de la base de datos
        cargarTareas()

        val botonCrear = findViewById<Button>(R.id.crearTarea)
        val botonBorrar = findViewById<Button>(R.id.borrarTarea)

        botonCrear.setOnClickListener {
            val intent = Intent(this, CrearTareaActivity::class.java)
            startActivity(intent)
        }

        botonBorrar.setOnClickListener {
            borrarTareasSeleccionadas()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarTareas()  // Recarga las tareas cuando la actividad vuelve al foco
    }

    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date()) // Devuelve la fecha actual como String en formato "yyyy-MM-dd"
    }

    private fun cargarTareas() {
        val usuarioActivo = obtenerUsuarioActual()
        if (usuarioActivo != null) {
            val fechaActual = obtenerFechaActual()  // Obtén la fecha actual

            val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase

            val cursor = bd.rawQuery(
                "SELECT * FROM Tareas WHERE usuario = ? AND fechaInicio <= ? ORDER BY fechaInicio ASC",
                arrayOf(usuarioActivo, fechaActual)
            )

            val tareasList = mutableListOf<Tarea>()

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val monedas = cursor.getInt(2)
                val repeticiones = cursor.getInt(3)
                val tipoRepeticion = cursor.getString(4)
                val fechaInicio = cursor.getString(5)
                val completada = cursor.getInt(6)
                val ultimaRepeticion = cursor.getString(7)
                val vecesCompletada = cursor.getInt(8)
                val usuario = cursor.getString(9)

                val tarea = Tarea(
                    id,
                    nombre,
                    monedas,
                    repeticiones,
                    tipoRepeticion,
                    fechaInicio,
                    completada,
                    ultimaRepeticion,
                    vecesCompletada,
                    usuario
                )
                tareasList.add(tarea)
            }
            cursor.close()
            bd.close()

            // Actualiza las tareas en la vista
            actualizarTareasEnVista(tareasList)
        }
    }

    private fun actualizarTareasEnVista(tareasList: List<Tarea>) {
        tareasLayout.removeAllViews() // Limpiar el LinearLayout

        tareasList.forEach { tarea ->
            val tareaView = layoutInflater.inflate(R.layout.tarea, tareasLayout, false)
            val nombreTextView = tareaView.findViewById<TextView>(R.id.nombre)
            val gananciaTextView = tareaView.findViewById<TextView>(R.id.ganancia)
            val checkBox = tareaView.findViewById<CheckBox>(R.id.checkbox)

            nombreTextView.text = tarea.nombre
            gananciaTextView.text = "${tarea.monedas} monedas"

            // Deshabilitar el CheckBox si la tarea ya está completada o no es el momento adecuado para marcarla
            checkBox.isChecked = tarea.completada == 1
            checkBox.isEnabled = !isRepeticionPendiente(tarea) && tarea.vecesCompletada < tarea.repeticiones

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !isRepeticionPendiente(tarea)) {
                    tarea.completada = 1
                    tarea.vecesCompletada += 1
                    tarea.ultimaRepeticion = obtenerFechaActual()
                    actualizarTareaEnBaseDeDatos(tarea)
                    darRecompensa(tarea)

                    if (tarea.tipoRepeticion != "Diaria" && tarea.tipoRepeticion != "Semanal") {
                        if (tarea.vecesCompletada >= tarea.repeticiones) {
                            eliminarTarea(tarea)
                        }
                    }

                } else {
                    tarea.completada = 0
                    actualizarTareaEnBaseDeDatos(tarea)
                }
            }

            // Manejar la selección/deselección de tareas
            tareaView.setOnClickListener {
                if (modoSeleccionActivo) {
                    if (tareasSeleccionadas.contains(tarea.id)) {
                        tareasSeleccionadas.remove(tarea.id)
                    } else {
                        tareasSeleccionadas.add(tarea.id)
                    }
                    tareaView.alpha = if (tareasSeleccionadas.contains(tarea.id)) 0.5f else 1f
                }
            }

            tareasLayout.addView(tareaView) // Agregar la vista de la tarea al LinearLayout
        }
    }

    private fun eliminarTarea(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL(
            "DELETE FROM Tareas WHERE id = ?",
            arrayOf(tarea.id)
        )
        bd.close()

        // Recargar las tareas después de eliminar una
        cargarTareas()
    }
    private fun darRecompensa(tarea: Tarea) {
        val usuarioActivo = obtenerUsuarioActual() ?: return
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase

        bd.execSQL(
            "UPDATE Usuarios SET monedas = monedas + ? WHERE usuario = ?",
            arrayOf(tarea.monedas, usuarioActivo)
        )

        bd.close()
    }

    private fun actualizarTareaEnBaseDeDatos(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL(
            "UPDATE Tareas SET completada = ?, vecesCompletada = ?, ultimaRepeticion = ? WHERE id = ?",
            arrayOf(tarea.completada, tarea.vecesCompletada, tarea.ultimaRepeticion, tarea.id)
        )
        bd.close()
    }


    private fun isRepeticionPendiente(tarea: Tarea): Boolean {
        // Verifica si ha pasado el tiempo necesario para completar la tarea
        val ultimaRepeticion = tarea.ultimaRepeticion
        val tipoRepeticion = tarea.tipoRepeticion
        val repeticiones = tarea.repeticiones

        if (ultimaRepeticion == null) {
            return false // Si nunca se ha completado, se puede completar
        }

        // Obtener la fecha actual
        val fechaActual = obtenerFechaActual()

        return when (tipoRepeticion) {
            "Diaria" -> {
                // Comprobar si han pasado 'repeticiones' días desde la última repetición
                val diferenciaDias = obtenerDiferenciaEnDias(fechaActual, ultimaRepeticion)
                diferenciaDias < repeticiones
            }

            "Semanal" -> {
                // Comprobar si han pasado 'repeticiones' semanas desde la última repetición
                val diferenciaSemanas = obtenerDiferenciaEnSemanas(fechaActual, ultimaRepeticion)
                diferenciaSemanas < repeticiones
            }

            "Veces" -> {
                // Si el tipo es "Veces", solo se puede marcar como completada si no ha alcanzado el número de veces
                tarea.vecesCompletada < repeticiones
            }

            else -> false
        }
    }

    private fun obtenerDiferenciaEnDias(fecha1: String, fecha2: String): Int {
        // Lógica para calcular la diferencia en días entre dos fechas
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = sdf.parse(fecha1)
        val date2 = sdf.parse(fecha2)

        val diferencia = date1.time - date2.time
        return (diferencia / (1000 * 60 * 60 * 24)).toInt() // Convertir la diferencia a días
    }

    private fun obtenerDiferenciaEnSemanas(fecha1: String, fecha2: String): Int {
        // Lógica para calcular la diferencia en semanas entre dos fechas
        val diasDiferencia = obtenerDiferenciaEnDias(fecha1, fecha2)
        return diasDiferencia / 7
    }


    private fun borrarTareasSeleccionadas() {
        val usuarioActivo = obtenerUsuarioActual()
        if (usuarioActivo == null) {
            Toast.makeText(this, "No se encontró un usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        if (!modoSeleccionActivo) {
            modoSeleccionActivo = true
            Toast.makeText(this, "Selecciona las tareas a borrar", Toast.LENGTH_SHORT).show()
        } else {
            if (tareasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ninguna tarea", Toast.LENGTH_SHORT).show()
                return
            }

            // Eliminar las tareas de la base de datos
            val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
            for (id in tareasSeleccionadas) {
                bd.execSQL(
                    "DELETE FROM Tareas WHERE id = ? AND usuario = ?",
                    arrayOf(id.toString(), usuarioActivo)
                )
            }
            bd.close()

            // Limpiar el LinearLayout y recargar las tareas
            tareasSeleccionadas.clear()
            Toast.makeText(this, "Tareas eliminadas", Toast.LENGTH_SHORT).show()

            // Desactivar el modo selección
            modoSeleccionActivo = false
            cargarTareas()
        }
    }

    fun obtenerUsuarioActual(): String? {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val cursor = bd.rawQuery("SELECT usuario FROM sesionActual LIMIT 1", null)
        var usuario: String? = null
        if (cursor.moveToFirst()) {
            usuario = cursor.getString(0)
        }
        cursor.close()
        bd.close()
        return usuario
    }
}
