package com.example.lifequest

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registro : AppCompatActivity() {
    lateinit var botonRegistro: Button

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
        botonRegistro.setOnClickListener {
            val nombre = findViewById<android.widget.EditText>(R.id.nombre).text.toString()
            val correo = findViewById<android.widget.EditText>(R.id.correo).text.toString()
            val contrasena = findViewById<android.widget.EditText>(R.id.contrasena).text.toString()
            registrarUsuario(nombre, correo, contrasena)
            mostrarToast("Usuario registrado")
            val intent = android.content.Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Función para registrar un usuario
    fun registrarUsuario(nombre: String, correo: String, contrasena: String) {
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        if (usuarioExiste(nombre)) {
            mostrarToast("El usuario ya existe")
            db.close()
            return
        }
        if (correoRegistrado(correo)) {
            mostrarToast("El correo ya está registrado")
            db.close()
            return
        }
        val query = "INSERT INTO usuarios (nombre, correo, contrasena, monedas, tareas_completadas, monedas_gastadas) VALUES ('$nombre', '$correo', '$contrasena', 0, 0, 0)"
        db.execSQL(query)
        db.close()
    }

    // Función para verificar si un usuario ya existe
    fun usuarioExiste(nombre: String): Boolean {
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val query = "SELECT * FROM usuarios WHERE nombre = '$nombre'"
        val cursor = db.rawQuery(query, null)
        val existe = cursor.count > 0
        cursor.close()
        db.close()
        return existe
    }

    // Función para verificar si un correo ya está registrado
    fun correoRegistrado(correo: String): Boolean {
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val query = "SELECT * FROM usuarios WHERE correo = '$correo'"
        val cursor = db.rawQuery(query, null)
        val registrado = cursor.count > 0
        cursor.close()
        db.close()
        return registrado
    }


}