package com.example.lifequest

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
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
import java.util.Locale

class CrearLogroActivity : AppCompatActivity() {
    private lateinit var db: SQLiteAyudante
    private lateinit var tareasSpinner: Spinner
    private lateinit var tareasLista: List<String>
    private lateinit var barraProgreso: SeekBar
    private lateinit var nombreLogroInput: EditText
    private lateinit var premioLogroInput: TextView
    private lateinit var repeticionesInput: EditText
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_logro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Configurar el menú superior
        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda(getString(R.string.ayuda_crear_logro))
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
        }

        db = SQLiteAyudante(this, "LifeQuest", null, 1)
        barraProgreso = findViewById(R.id.barraLogro)
        nombreLogroInput = findViewById(R.id.nombreLogro)
        premioLogroInput = findViewById(R.id.cantidadMonedas)
        tareasSpinner = findViewById(R.id.tareaAsignada)

        val botonGuardar = findViewById<Button>(R.id.anadirLogro)
        repeticionesInput = findViewById(R.id.cantidadRepeticiones)
        val atras = findViewById<Button>(R.id.atrasLogro)

        barraProgreso.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                premioLogroInput.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Poblar el Spinner con las tareas
        cargarTareasEnSpinner()

        // Botón para guardar el logro
        botonGuardar.setOnClickListener {
            crearLogro()
        }

        // Botón para regresar a la pantalla anterior
        atras.setOnClickListener {
            finish()
        }
    }

    // Método para mostrar mensajes en pantalla
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Método para cargar las tareas en el Spinner
    private fun cargarTareasEnSpinner() {
        val db = db.readableDatabase
        val usuario = obtenerUsuarioActual()
        val dias = getString(R.string.dias)
        val semanas = getString(R.string.semanas)

        // Consulta para obtener las tareas del usuario actual y cuya repeticion sea dias o semanas
        val cursor = db.rawQuery(
            "SELECT nombre FROM Tareas WHERE usuario = ? AND tipoRepeticion IN (?, ?)",
            arrayOf(usuario, dias, semanas)
        )

        // Extraer nombres de tareas en una lista
        tareasLista = mutableListOf<String>().apply {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("nombre")))
            }
        }

        cursor.close()
        db.close()

        if (tareasLista.isEmpty()) {
            mostrarMensaje(getString(R.string.no_hay_tareas_con_repeticiones_diarias_o_semanales))
            finish()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tareasLista
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tareasSpinner.adapter = adapter
    }

    // Método para insertar el logro en la base de datos
    private fun crearLogro() {
        val nombre = nombreLogroInput.text.toString()
        val monedas = premioLogroInput.text.toString()
        val tareaAsociada = tareasSpinner.selectedItem as? String
        val repeticionesNecesarias = repeticionesInput.text.toString()
        val usuario = obtenerUsuarioActual()

        // Validar que los campos no estén vacíos
        if (usuario == null) {
            mostrarMensaje(getString(R.string.error_al_obtener_usuario))
            return
        }
        if (nombre.isEmpty()) {
            mostrarMensaje(getString(R.string.por_favor_ingrese_un_nombre_para_el_logro))
            return
        }
        if (monedas.isEmpty()) {
            mostrarMensaje(getString(R.string.por_favor_ingrese_un_premio_para_el_logro))
            return
        }
        if (tareaAsociada == null) {
            mostrarMensaje(getString(R.string.por_favor_seleccione_una_tarea_asociada))
            return
        }
        if (repeticionesNecesarias.isEmpty()) {
            mostrarMensaje(getString(R.string.por_favor_ingrese_un_n_mero_de_repeticiones_necesarias))
            return
        }

        // Insertar el logro en la base de datos
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("premio", monedas)
            put("tarea_asociada", tareaAsociada)
            put("repeticiones_necesarias", repeticionesNecesarias)
            put("progreso", 0)
            put("completado", 0)
            put("usuario", usuario)
        }

        val resultado = db.insert("logros", null, values)
        db.close()

        // Mostrar un mensaje en función del resultado
        if (resultado != -1L) {
            mostrarMensaje(getString(R.string.logro_creado_exitosamente))
            finish()
        } else {
            mostrarMensaje(getString(R.string.error_al_crear_el_logro))
            finish()
        }

    }

    // Método para obtener el usuario activo
    private fun obtenerUsuarioActual(): String? {
        var usuario: String? = null
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase

        try {
            val cursor = bd.rawQuery("SELECT usuario FROM sesionActual", null)
            if (cursor.moveToFirst()) {
                usuario = cursor.getString(0)
                cursor.close()
                bd.close()
                return usuario
            }

        } catch (e: Exception) {
            mostrarMensaje(getString(R.string.error_al_obtener_usuario))
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
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.mensaje_inicio_deteccion_voz))
        }

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(
                this, getString(R.string.mensaje_error_No_reconocimiento_voz),
                Toast.LENGTH_SHORT
            ).show()
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
                    getString(R.string.tareas) -> {
                        val intent = Intent(this, TareasActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.logros) -> {
                        val intent = Intent(this, LogrosActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.tienda) -> {
                        val intent = Intent(this, TiendaActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.perfil) -> {
                        val intent = Intent(this, PerfilActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.ayuda) -> {
                        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
                        menuSuperior.mostrarAyuda(getString(R.string.ayuda_crear_logro))
                    }

                    getString(R.string.anadir_tarea) -> {
                        val intent = Intent(this, CrearTareaActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.anadir_logro)-> {
                        val intent = Intent(this, CrearLogroActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.cambiar_modo) -> {
                        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
                        menuSuperior.cambiarModo()
                    }

                    getString(R.string.anadir_premio) -> {
                        val intent = Intent(this, CrearPremioActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.TOS) -> {
                        val intent = Intent(this, TOSActivity::class.java)
                        startActivity(intent)
                    }

                    else -> {
                        mostrarMensaje(getString(R.string.accion_voz_No_reconocida))
                    }
                }
            }
        }
    }
}




