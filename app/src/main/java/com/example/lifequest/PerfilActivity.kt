package com.example.lifequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class PerfilActivity : AppCompatActivity() {
    lateinit var nombreUsuario : TextView
    lateinit var cantidadMonedas : TextView
    lateinit var cantidadTareas : TextView
    lateinit var monedasGastadas : TextView
    lateinit var emailUsuario : TextView
    lateinit var cerrarSesion : Button
    lateinit var cerrarPerfil : Button
    lateinit var urlTOS: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
        nombreUsuario = findViewById(R.id.mosrarUsuarioPerfil)
        cantidadMonedas = findViewById(R.id.mostrarMonedasPerfil)
        cantidadTareas = findViewById(R.id.tareascompletadas)
        monedasGastadas = findViewById(R.id.monedasGastadas)
        emailUsuario = findViewById(R.id.mostrarEmailPerfil)
        cerrarSesion = findViewById(R.id.cerrarSesion)
        urlTOS = findViewById(R.id.urlTOS)
        cerrarPerfil = findViewById(R.id.atrasPerfil)

        val cursor = db.rawQuery("SELECT usuario FROM sesionActual", null)
        if (cursor.moveToFirst()) {
            val cursor = db.rawQuery("SELECT * FROM usuarios WHERE usuario = '${cursor.getString(0)}'", null)
            if (cursor.moveToFirst()) {
                nombreUsuario.text = cursor.getString(0)
                cantidadMonedas.text = cursor.getInt(3).toString()
                cantidadTareas.text = cursor.getInt(4).toString()
                monedasGastadas.text = cursor.getInt(5).toString()
                emailUsuario.text = cursor.getString(1)


            }
        }
        urlTOS.setOnClickListener {
            val intent = Intent(this, TOSActivity::class.java)
            startActivity(intent)
        }
        cerrarSesion.setOnClickListener {
            db.execSQL("DELETE FROM sesionActual")
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        cerrarPerfil.setOnClickListener {
            finish()

        }
    }
}