package com.example.lifequest

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class menuSuperior @JvmOverloads constructor(
    contexto: Context, atributos: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    contexto, atributos, defStyleAttr
) {
    private val PREFS_NAME = "theme_prefs"
    private val KEY_THEME = "current_theme"

    init {

        //Infla el layout del menu superior (????)
        LayoutInflater.from(contexto).inflate(R.layout.menu_superior, this, true)

        //Configura la navegacion
        findViewById<ImageButton>(R.id.home).setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
        findViewById<ImageButton>(R.id.stats).setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
        findViewById<ImageButton>(R.id.cambiarModo).setOnClickListener {
            toggleNightMode()
        }
        findViewById<Button>(R.id.comidasSup).setOnClickListener {
            val intent = Intent(context, ComidasDash::class.java)
            context.startActivity(intent)
        }
//        findViewById<Button>(R.id.ejerciciosSup).setOnClickListener {
//            val intent = Intent(context, EjerciciosDash::class.java)
//            context.startActivity(intent)
//        }
//        findViewById<Button>(R.id.diaSup).setOnClickListener {
//            val intent = Intent(context, diaDash::class.java)
//            context.startActivity(intent)
//        }
//        findViewById<Button>(R.id.perfil).setOnClickListener {
//            val intent = Intent(context, Perfil::class.java)
//            context.startActivity(intent)
//        }

    }

    private fun toggleNightMode() {
        // Obtiene el estado actual del modo de tema
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // Cambia a modo claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveNightModePreference(false)  // Guardamos que es modo claro
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                // Cambia a modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveNightModePreference(true)   // Guardamos que es modo oscuro
            }
        }
    }

    // Función para guardar la preferencia del modo de tema
    private fun saveNightModePreference(isNightMode: Boolean) {
        val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("night_mode", isNightMode)
            apply()
        }
    }

}