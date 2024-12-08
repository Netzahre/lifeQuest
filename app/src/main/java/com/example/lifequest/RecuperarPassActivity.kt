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

class RecuperarPassActivity : AppCompatActivity() {
    lateinit var correoUser: EditText
    lateinit var recuperar: Button
    lateinit var cancelar: Button
    lateinit var urlTOS: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_pass)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        correoUser = findViewById(R.id.correoRecuperar)
        recuperar = findViewById(R.id.restablecer)
        cancelar = findViewById(R.id.atras)
        urlTOS = findViewById(R.id.urlTOS)

        cancelar.setOnClickListener {
            finish()
        }
        urlTOS.setOnClickListener {
            val intent = Intent(this, TOSActivity::class.java)
            startActivity(intent)
        }
        recuperar.setOnClickListener {
            recuperarPass()
        }
    }

    //Funcion para mostrar un mensaje en pantalla
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    /*Mi plan original era que mandara un correo pero la unica forma que encontre neceisataba un
    codigo de aplicacion y eso daba acceso a toda mi cuenta de gmail asi que decidi que no era una
    buena idea por motivos de seguridad :(
     */
    private fun recuperarPass() {
        val correo = correoUser.text.toString()
        if (correo.isEmpty()) {
            correoUser.error = "Por favor, introduce un correo"
            return
        }

        val db = SQLiteAyudante(this, "LifeQuest", null, 1)
        val cursor = db.readableDatabase.rawQuery(
            "SELECT contrasena FROM usuarios WHERE correo = '$correo'", null
        )

        if (cursor.moveToFirst()) {
            val contrasena = cursor.getString(0)
            mostrarMensaje("Contraseña: $contrasena")

            mostrarMensaje("Correo enviado con éxito")
        } else {
            correoUser.error = "Correo no registrado"
        }
        cursor.close()
        db.close()
    }
}