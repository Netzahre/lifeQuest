package com.example.lifequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    lateinit var botonLogin: Button
    lateinit var botonRegistro: Button
    lateinit var passOlvidada: TextView
    lateinit var urlTOS: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botonLogin = findViewById(R.id.acceder)
        botonRegistro = findViewById(R.id.accederRegistro)
        passOlvidada = findViewById(R.id.passOlvidada)
        urlTOS = findViewById(R.id.urlTOS)

        botonLogin.setOnClickListener {
            val usuario = findViewById<EditText>(R.id.usuario).text.toString()
            val contrasena = findViewById<EditText>(R.id.contrasenauser).text.toString()

            if (loginUsuario(usuario, contrasena)) {
                mostrarToast("Inicio de sesion exitoso")
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                mostrarToast("Usuario o contraseña incorrectos")
            }
        }

        botonRegistro.setOnClickListener {
            val intent = android.content.Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        passOlvidada.setOnClickListener {
            val intent = android.content.Intent(this, RecuperarPassActivity::class.java)
            startActivity(intent)
        }

        urlTOS.setOnClickListener {
            val intent = android.content.Intent(this, TOSActivity::class.java)
            startActivity(intent)
        }
    }

    fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun loginUsuario(usuario: String, contrasena: String): Boolean {
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarToast("Por favor, rellene todos los campos")
            return false
        }

        val db = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM usuarios WHERE usuario = '$usuario' AND contrasena = '$contrasena'",
            null
        )
        if (cursor.moveToFirst()) {
            val modoOscuroIndex = cursor.getColumnIndex("modoOscuro")
            if (modoOscuroIndex != -1) {
                val modoOscuro = cursor.getInt(modoOscuroIndex)
                val sesionActual = db.rawQuery("SELECT * FROM sesionActual", null)
                if (sesionActual.moveToFirst()) {
                    db.execSQL("DELETE FROM sesionActual")
                }
                db.execSQL("INSERT INTO sesionActual (usuario, modoOscuro) VALUES ('$usuario', '$modoOscuro')")
            }
            cursor.close()
            db.close()
            return true
        }
        db.close()
        return false
    }
}