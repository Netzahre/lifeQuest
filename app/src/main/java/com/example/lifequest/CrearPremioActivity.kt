package com.example.lifequest

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class CrearPremioActivity : AppCompatActivity() {
    private lateinit var nombrePremioEditText: EditText
    private lateinit var barraPremioSeekBar: SeekBar
    lateinit var cantidadMonedas: TextView
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_premio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar el menú superior
        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda(getString(R.string.ayuda_crear_premio))
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
        }

        // Configurar los botones de la interfaz
        val anadirPremioButton = findViewById<Button>(R.id.anadirPremio)
        val botonAtras = findViewById<Button>(R.id.atrasPremio)

        nombrePremioEditText = findViewById(R.id.nombrePremio)
        barraPremioSeekBar = findViewById(R.id.barraPremio)
        cantidadMonedas = findViewById(R.id.cantidadMonedas)
        barraPremioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cantidadMonedas.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Configurar los listeners de los botones
        anadirPremioButton.setOnClickListener {
            anadirPremio()
        }

        botonAtras.setOnClickListener {
            finish()
        }

    }

    // Muestra un mensaje en pantalla
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Añade un premio a la base de datos
    private fun anadirPremio() {
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.writableDatabase
        val nombre = nombrePremioEditText.text.toString().trim()
        val usuario = obtenerUsuarioActual()
        val costo = cantidadMonedas.text.toString().toInt()

        // Comprobar que los campos no están vacíos
        if (usuario.isEmpty()) {
            mostrarMensaje(getString(R.string.error_al_obtener_usuario))
            return

        }

        if (nombre.isEmpty()) {
            mostrarMensaje(getString(R.string.el_nombre_del_premio_no_puede_estar_vac_o))
            return
        }

        // Añadir el premio a la base de datos
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("costo", costo)
            put("usuario", usuario)
        }

        // Insertar el premio en la base de datos
        val resultado = db.insert("premios", null, valores)
        db.close()

        // Mostrar un mensaje en función del resultado
        if (resultado != -1L) {
            mostrarMensaje(getString(R.string.premio_a_adido_correctamente))
            finish()
        } else {
            mostrarMensaje(getString(R.string.no_se_ha_podido_a_adir_el_premio))
            finish()
        }

    }

    // Obtiene el usuario actual de la base de datos
    private fun obtenerUsuarioActual(): String {
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val cursor = db.rawQuery("SELECT usuario FROM sesionActual", null)
        val usuario = if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            ""
        }

        cursor.close()
        db.close()

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