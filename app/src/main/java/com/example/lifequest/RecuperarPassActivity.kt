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
    private lateinit var correoUser: EditText
    private lateinit var recuperar: Button
    private lateinit var cancelar: Button
    private lateinit var urlTOS: TextView

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
            correoUser.error = getString(R.string.por_favor_introduce_un_correo)
            return
        }

        val db = SQLiteAyudante(this, "LifeQuest", null, 1)
        val cursor = db.readableDatabase.rawQuery(
            "SELECT contrasena FROM usuarios WHERE correo = '$correo'", null
        )

        if (cursor.moveToFirst()) {
            val contrasena = cursor.getString(0)
            mostrarMensaje(getString(R.string.contrasena_recuperada, contrasena))

            mostrarMensaje(getString(R.string.correo_enviado_con_xito))
        } else {
            correoUser.error = getString(R.string.correo_no_registrado)
        }
        cursor.close()
        db.close()
    }
}