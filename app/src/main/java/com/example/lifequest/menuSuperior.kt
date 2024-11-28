package com.example.lifequest

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate

class menuSuperior @JvmOverloads constructor(
    contexto: Context, atributos: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    contexto, atributos, defStyleAttr
) {

    init {
        //Infla el layout del menu superior (????)
        LayoutInflater.from(contexto).inflate(R.layout.menu_superior, this, true)

        //Configura la navegacion
        findViewById<ImageButton>(R.id.home).setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }

        findViewById<ImageButton>(R.id.cambiarModo).setOnClickListener {
            cambiarModo()
        }
    }

    private fun cambiarModo() {
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

    private fun guardarPreferencia(isNightMode: Boolean) {
        val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("night_mode", isNightMode)
            apply()
        }
    }

}