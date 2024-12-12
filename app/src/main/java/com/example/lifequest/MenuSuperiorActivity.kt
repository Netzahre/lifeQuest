package com.example.lifequest

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate

// Clase que representa el menú superior de la aplicación que se muestra en todas las actividades
class MenuSuperiorActivity @JvmOverloads constructor(
    contexto: Context, atributos: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    contexto, atributos, defStyleAttr
) {
    // Propiedades de la clase para obtener los botones del menú superior en las actividades
    val microfono: ImageButton
        get() = findViewById(R.id.mic)

    init {
        // Inflar el layout del menú superior en la vista
        LayoutInflater.from(contexto).inflate(R.layout.menu_superior, this, true)

        // Obtener el nombre de usuario y el modo oscuro de la base de datos y configurar el menú superior
        val nombreUsuario = findViewById<TextView>(R.id.nombre_usuario)
        val db = SQLiteAyudante(contexto, "LifeQuest", null, 1).writableDatabase
        val cursor = db.rawQuery("SELECT usuario, modoOscuro FROM sesionActual", null)
        val modoOscuroIndex = cursor.getColumnIndex("modoOscuro")
        val nombreusuarioIndex = cursor.getColumnIndex("usuario")

        // Configurar el menú superior
        if (cursor.moveToFirst()) {
            if (modoOscuroIndex != -1 && nombreusuarioIndex != -1) {
                val modoOscuro = cursor.getInt(modoOscuroIndex)
                val usuario = cursor.getString(nombreusuarioIndex)
                nombreUsuario.text = usuario
                if (modoOscuro == 1) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
        cursor.close()

        // Configurar los botones del menú superior
        findViewById<ImageButton>(R.id.homa).setOnClickListener {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }

        findViewById<ImageButton>(R.id.cambiarModo).setOnClickListener {
            cambiarModo()
        }

        findViewById<LinearLayout>(R.id.perfil).setOnClickListener {
            val intent = Intent(context, PerfilActivity::class.java)
            context.startActivity(intent)
        }

        findViewById<Button>(R.id.tareasMenuSuperior).setOnClickListener {
            val intent = Intent(context, TareasActivity::class.java)
            context.startActivity(intent)
        }

        findViewById<Button>(R.id.tiendaMenuSuperior).setOnClickListener {
            val intent = Intent(context, TiendaActivity::class.java)
            context.startActivity(intent)
        }

        findViewById<Button>(R.id.logrosMenuSuperior).setOnClickListener {
            val intent = Intent(context, LogrosActivity::class.java)
            context.startActivity(intent)
        }
    }

    // Método para configurar el texto de ayuda de un botón
    fun configurarTextoDeAyuda(textoAyuda: String) {
        findViewById<ImageButton>(R.id.ayuda).setOnClickListener {
            mostrarAyuda(textoAyuda)
        }
    }

    // Método para mostrar un mensaje de ayuda
    fun mostrarAyuda(mensaje: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.ayuda))
            .setMessage(mensaje)
            .setPositiveButton(context.getString(R.string.entendido), null)
            .show()
    }

    // Método para cambiar el modo de la aplicación
    fun cambiarModo() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                guardarPreferencia(false)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                guardarPreferencia(true)
            }
        }
    }

    // Método para guardar la preferencia del modo oscuro en la base de datos
    private fun guardarPreferencia(isNightMode: Boolean) {
        val db = SQLiteAyudante(context, "LifeQuest", null, 1).writableDatabase
        db.execSQL("UPDATE sesionActual SET modoOscuro = ${if (isNightMode) 1 else 0}")
        db.execSQL("UPDATE usuarios SET modoOscuro = ${if (isNightMode) 1 else 0}")
        db.close()
    }
}