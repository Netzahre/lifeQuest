package com.example.lifequest

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroActivity : AppCompatActivity() {
    private lateinit var botonRegistro: Button
    private lateinit var botonAtras: Button
    private lateinit var urlTOS: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botonRegistro = findViewById(R.id.botonRegistro)
        botonAtras = findViewById(R.id.botonAtras)
        urlTOS = findViewById(R.id.urlTOS)

        botonRegistro.setOnClickListener {
            val nombre = findViewById<EditText>(R.id.nombreRegistro).text.toString()
            val correo = findViewById<EditText>(R.id.correoRegistro).text.toString()
            val contrasena = findViewById<EditText>(R.id.contrasenaRegistro).text.toString()
            val contrasenaConfirmacion =
                findViewById<EditText>(R.id.contrasenaConfirmacion).text.toString()

            if (registrarUsuario(nombre, correo, contrasena, contrasenaConfirmacion)) {
                mostrarMensaje(getString(R.string.usuario_registrado_con_exito))
                val intent = android.content.Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        urlTOS.setOnClickListener {
            val intent = android.content.Intent(this, TOSActivity::class.java)
            startActivity(intent)
        }

        botonAtras.setOnClickListener {
            finish()
        }

    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Función para registrar un usuario
    private fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        contrasenaConfirmacion: String
    ): Boolean {
        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || contrasenaConfirmacion.isEmpty()) {
            mostrarMensaje(getString(R.string.rellenar_todos_campos))
            return false
        }
        if (contrasena != contrasenaConfirmacion) {
            mostrarMensaje(getString(R.string.las_contrase_as_no_coinciden))
            return false
        }
        if (usuarioExiste(nombre)) {
            mostrarMensaje(getString(R.string.el_usuario_ya_existe))
            return false
        }
        if (correoRegistrado(correo)) {
            mostrarMensaje(getString(R.string.el_correo_ya_est_registrado))
            return false
        }
        try {
            val db = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
            val values = ContentValues()
            values.put("usuario", nombre)
            values.put("correo", correo)
            values.put("contrasena", contrasena)
            values.put("monedas", 0)
            values.put("tareas_completadas", 0)
            values.put("monedas_gastadas", 0)
            values.put("modoOscuro", 0)
            db.insert("usuarios", null, values)
            db.close()
            return true

        } catch (e: Exception) {
            mostrarMensaje(getString(R.string.error_al_registrar_el_usuario))
            return false
        }

    }

    // Función para verificar si un usuario ya existe
    private fun usuarioExiste(nombre: String): Boolean {
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val query = "SELECT * FROM usuarios WHERE usuario = '$nombre'"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            cursor.close()
            db.close()
            return true
        }
        cursor.close()
        db.close()
        return false
    }

    // Función para verificar si un correo ya está registrado
    private fun correoRegistrado(correo: String): Boolean {
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val query = "SELECT * FROM usuarios WHERE correo = '$correo'"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            cursor.close()
            db.close()
            return true
        }
        cursor.close()
        db.close()
        return false
    }
}