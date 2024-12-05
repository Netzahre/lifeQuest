package com.example.lifequest

import android.content.ContentValues
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

        cargarTareas()

        val botonCrear = findViewById<Button>(R.id.crearTarea)
        val botonBorrar = findViewById<Button>(R.id.borrarTarea)

        botonCrear.setOnClickListener {
            val intent = Intent(this, CrearTareaActivity::class.java)
            startActivity(intent)
        }

        botonBorrar.setOnClickListener {
            if (!modoSeleccionActivo) {
                // Activar modo selección
                modoSeleccionActivo = true
                tareasSeleccionadas.clear()
                botonBorrar.text = "Confirmar"
                Toast.makeText(this, "Selecciona las tareas a eliminar", Toast.LENGTH_SHORT).show()
            } else {
                // Confirmar eliminación de tareas seleccionadas
                borrarTareasSeleccionadas()
                modoSeleccionActivo = false
                botonBorrar.text = "Borrar"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarTareas()
    }

    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date()) // Devuelve la fecha actual como String en formato "yyyy-MM-dd"
    }

    private fun cargarTareas() {
        val usuarioActivo = obtenerUsuarioActual()
        if (usuarioActivo != null) {
            val fechaActual = obtenerFechaActual()

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

            actualizarTareasEnVista(tareasList)
        }
    }

    private fun actualizarTareasEnVista(tareasList: List<Tarea>) {
        tareasLayout.removeAllViews()

        tareasList.forEach { tarea ->
            val tareaView = layoutInflater.inflate(R.layout.tarea, tareasLayout, false)
            val nombreTextView = tareaView.findViewById<TextView>(R.id.nombre)
            val gananciaTextView = tareaView.findViewById<TextView>(R.id.ganancia)
            val checkBox = tareaView.findViewById<CheckBox>(R.id.checkbox)

            nombreTextView.text = tarea.nombre
            gananciaTextView.text = "${tarea.monedas} monedas"

            checkBox.isChecked = tarea.completada == 1

            if (tarea.tipoRepeticion == "dias" || tarea.tipoRepeticion == "semanas") {
                checkBox.isEnabled = !isRepeticionPendiente(tarea)
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !isRepeticionPendiente(tarea)) {
                    completarTarea(tarea)
                } else {
                    tarea.completada = 0
                    actualizarTareaEnBaseDeDatos(tarea)
                }
            }

            tareaView.setOnClickListener {
                if (modoSeleccionActivo) {
                    // Cambiar estado de selección
                    if (tareasSeleccionadas.contains(tarea.id)) {
                        tareasSeleccionadas.remove(tarea.id)
                        tareaView.alpha = 1f // Quitar selección
                    } else {
                        tareasSeleccionadas.add(tarea.id)
                        tareaView.alpha = 0.5f // Marcar como seleccionada
                    }
                } else {
                    // Otras acciones cuando no está en modo selección
                    Toast.makeText(
                        this,
                        "Mantén pulsado 'Borrar tareas' para activar la selección",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun completarTarea(tarea: Tarea) {
        tarea.completada = 1
        tarea.vecesCompletada += 1
        tarea.ultimaRepeticion = obtenerFechaActual()

        actualizarTareaEnBaseDeDatos(tarea)
        darRecompensa(tarea)
        actualizarLogros(tarea)

        if (tarea.tipoRepeticion != "dias" && tarea.tipoRepeticion != "semanas") {
            if (tarea.vecesCompletada >= tarea.repeticiones) {
                eliminarTarea(tarea)
            }
        }
    }

    private fun actualizarLogros(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase

        val cursor = bd.rawQuery(
            "SELECT id, repeticiones_necesarias, progreso, completado FROM logros WHERE tarea_asociada = ? AND completado = 0",
            arrayOf(tarea.nombre)
        )

        while (cursor.moveToNext()) {
            val logroId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val repeticionesNecesarias = cursor.getInt(cursor.getColumnIndexOrThrow("repeticiones_necesarias"))
            val progresoActual = cursor.getInt(cursor.getColumnIndexOrThrow("progreso"))

            val nuevoProgreso = progresoActual + 1
            val completado = if (nuevoProgreso >= repeticionesNecesarias) 1 else 0

            val values = ContentValues().apply {
                put("progreso", nuevoProgreso)
                put("completado", completado)
            }

            bd.update("logros", values, "id = ?", arrayOf(logroId.toString()))
        }

        cursor.close()
        bd.close()
    }

    private fun eliminarTarea(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL("DELETE FROM Tareas WHERE id = ?", arrayOf(tarea.id))
        bd.close()
        cargarTareas()
    }

    private fun darRecompensa(tarea: Tarea) {
        val usuarioActivo = obtenerUsuarioActual() ?: return
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL("UPDATE Usuarios SET monedas = monedas + ? WHERE usuario = ?", arrayOf(tarea.monedas, usuarioActivo))
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

    private fun obtenerUsuarioActual(): String? {
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

    private fun isRepeticionPendiente(tarea: Tarea): Boolean {
        val ultimaRepeticion = tarea.ultimaRepeticion
        val tipoRepeticion = tarea.tipoRepeticion
        val repeticiones = tarea.repeticiones

        if (ultimaRepeticion == null) return false

        val fechaActual = obtenerFechaActual()

        return when (tipoRepeticion) {
            "Diaria" -> obtenerDiferenciaEnDias(fechaActual, ultimaRepeticion) < repeticiones
            "Semanal" -> obtenerDiferenciaEnSemanas(fechaActual, ultimaRepeticion) < repeticiones
            else -> false
        }
    }

    private fun obtenerDiferenciaEnDias(fechaActual: String, fechaAnterior: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val actual = sdf.parse(fechaActual)
        val anterior = sdf.parse(fechaAnterior)
        return ((actual.time - anterior.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun obtenerDiferenciaEnSemanas(fechaActual: String, fechaAnterior: String): Int {
        val dias = obtenerDiferenciaEnDias(fechaActual, fechaAnterior)
        return dias / 7
    }

    private fun borrarTareasSeleccionadas() {
        if (tareasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "No hay tareas seleccionadas para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        tareasSeleccionadas.forEach { id ->
            bd.execSQL("DELETE FROM Tareas WHERE id = ?", arrayOf(id))
        }
        tareasSeleccionadas.clear()
        bd.close()

        Toast.makeText(this, "Tareas eliminadas", Toast.LENGTH_SHORT).show()

        // Restablecer la transparencia de las tareas
        for (i in 0 until tareasLayout.childCount) {
            val tareaView = tareasLayout.getChildAt(i)
            tareaView.alpha = 1f
        }

        cargarTareas()
    }

}
