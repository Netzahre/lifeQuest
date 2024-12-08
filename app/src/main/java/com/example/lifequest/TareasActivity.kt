package com.example.lifequest

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
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
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda("Aquí puedes ver tus tareas pendientes y completarlas. Mantén pulsado el botón de borrar para seleccionar tareas y eliminar")
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
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

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        cargarTareas()
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun cargarTareas() {
        val usuarioActivo = obtenerUsuarioActual()
        if (usuarioActivo != null) {
            val fechaActual = obtenerFechaActual()

            val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase

            val cursor = bd.rawQuery(
                """
                SELECT * FROM Tareas 
                WHERE usuario = ? 
                AND (
                    (tipoRepeticion = 'dias' AND 
                     (julianday(?) - julianday(ultimaRepeticion) >= repeticiones OR ultimaRepeticion IS NULL)) 
                    OR 
                    (tipoRepeticion = 'semanas' AND 
                     (julianday(?) - julianday(ultimaRepeticion) >= repeticiones * 7 OR ultimaRepeticion IS NULL)) 
                    OR 
                    (tipoRepeticion = 'veces')
                ) AND (fechaInicio <= ? OR fechaInicio IS NULL)
                ORDER BY fechaInicio ASC
            """,
                arrayOf(usuarioActivo, fechaActual, fechaActual, fechaActual)
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
            if (tarea.tipoRepeticion == "veces") {
                gananciaTextView.text =
                    "${tarea.monedas} monedas - completado ${tarea.vecesCompletada} veces de ${tarea.repeticiones}"
                gananciaTextView.text = "${tarea.monedas} monedas"
            } else {
                gananciaTextView.text = "${tarea.monedas} monedas"
            }

            checkBox.isChecked = false;

            checkBox.isEnabled = !isRepeticionPendiente(tarea)

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
                    if (tareasSeleccionadas.contains(tarea.id)) {
                        tareasSeleccionadas.remove(tarea.id)
                        tareaView.alpha = 1f
                    } else {
                        tareasSeleccionadas.add(tarea.id)
                        tareaView.alpha = 0.5f
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Mantén pulsado 'Borrar tareas' para activar la selección",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            tareasLayout.addView(tareaView)
        }
    }

    private fun isRepeticionPendiente(tarea: Tarea): Boolean {
        val fechaActual = obtenerFechaActual()
        return tarea.ultimaRepeticion != null && tarea.ultimaRepeticion != fechaActual
    }

    private fun completarTarea(tarea: Tarea) {
        tarea.vecesCompletada += 1
        tarea.ultimaRepeticion = obtenerFechaActual()

        actualizarTareaEnBaseDeDatos(tarea)
        darRecompensa(tarea)
        actualizarLogros(tarea)

        // Actualizamos las tareas completadas del usuario
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL(
            "UPDATE Usuarios SET tareas_completadas = tareas_completadas + 1 WHERE usuario = ?",
            arrayOf(tarea.usuario)
        )
        bd.close()

        // Quitar de la vista las tareas completadas diarias/semanales
        if (tarea.tipoRepeticion == "dias" || tarea.tipoRepeticion == "semanas") {
            cargarTareas()
        } else if (tarea.vecesCompletada >= tarea.repeticiones) {
            // Eliminar tareas no recurrentes si se completaron todas las repeticiones
            eliminarTarea(tarea)
        }
        mostrarMensaje("¡Tarea completada! +${tarea.monedas} monedas")
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

    private fun actualizarLogros(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase

        val cursor = bd.rawQuery(
            "SELECT * FROM logros WHERE tarea_asociada = ? AND completado = 0",
            arrayOf(tarea.nombre)
        )

        while (cursor.moveToNext()) {
            val progresoActual = cursor.getInt(cursor.getColumnIndexOrThrow("progreso")) + 1
            val repeticionesNecesarias =
                cursor.getInt(cursor.getColumnIndexOrThrow("repeticiones_necesarias"))
            val logroId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val premio = cursor.getInt(cursor.getColumnIndexOrThrow("premio"))

            val values = ContentValues().apply {
                put("progreso", progresoActual)
                if (progresoActual >= repeticionesNecesarias) {
                    put("completado", 1)

                    // Sumar las monedas del premio al saldo del usuario
                    darPremioLogro(premio)
                }
            }

            bd.update(
                "logros",
                values,
                "id = ?",
                arrayOf(logroId.toString())
            )
        }

        cursor.close()
        bd.close()
    }

    private fun darPremioLogro(premio: Int) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        val usuarioActual = obtenerUsuarioActual() // Método que devuelve el usuario actual

        // Leer el saldo actual de monedas del usuario
        val cursor = bd.rawQuery(
            "SELECT monedas FROM Usuarios WHERE usuario = ?",
            arrayOf(usuarioActual)
        )
        var saldoActual = 0
        if (cursor.moveToFirst()) {
            saldoActual = cursor.getInt(cursor.getColumnIndexOrThrow("monedas"))
        }
        cursor.close()

        // Actualizar el saldo de monedas
        val nuevoSaldo = saldoActual + premio
        val values = ContentValues().apply {
            put("monedas", nuevoSaldo)
        }

        bd.update(
            "Usuarios",
            values,
            "usuario = ?",
            arrayOf(usuarioActual)
        )

        bd.close()
    }

    private fun borrarTareasSeleccionadas() {
        if (tareasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "No hay tareas seleccionadas para eliminar", Toast.LENGTH_SHORT)
                .show()
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

    private fun eliminarTarea(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL("DELETE FROM Tareas WHERE id = ?", arrayOf(tarea.id))
        bd.close()
        cargarTareas()
    }

    private fun actualizarTareaEnBaseDeDatos(tarea: Tarea) {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        bd.execSQL(
            "UPDATE Tareas SET completada = ?, vecesCompletada = ?, ultimaRepeticion = ? WHERE id = ?",
            arrayOf(tarea.completada, tarea.vecesCompletada, tarea.ultimaRepeticion, tarea.id)
        )
        bd.close()
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
                            menuSuperior.mostrarAyuda("Aquí puedes ver tus tareas pendientes y completarlas. Mantén pulsado el botón de borrar para seleccionar tareas y eliminar")
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
