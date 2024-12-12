package com.example.lifequest

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var tareasDash: Button
    private lateinit var logrosDash: Button
    private lateinit var tiendaDash: Button
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda(getString(R.string.ayuda_home))
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
        }

        tareasDash = findViewById(R.id.tareasDash)
        logrosDash = findViewById(R.id.logrosDash)
        tiendaDash = findViewById(R.id.agendaDash)

        tareasDash.setOnClickListener {
            val intent = Intent(this, TareasActivity::class.java)
            startActivity(intent)
        }

        logrosDash.setOnClickListener {
            val intent = Intent(this, LogrosActivity::class.java)
            startActivity(intent)
        }

        tiendaDash.setOnClickListener {
            val intent = Intent(this, TiendaActivity::class.java)
            startActivity(intent)
        }

    }

    // Método para mostrar un mensaje en pantalla
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
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
