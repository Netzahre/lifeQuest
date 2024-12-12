package com.example.lifequest

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PerfilActivity : AppCompatActivity() {

    // Función que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Botón de atras
        val botonAtras = findViewById<Button>(R.id.atrasPerfil)
        botonAtras.setOnClickListener {
            finish()
        }

        // Configuración de la barra de estado y navegación para que sean transparentes y se vea el fondo
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = PerfilPagerAdapter(this)
        viewPager.adapter = adapter

        // Asociar el TabLayout con el ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.datos)
                1 -> getString(R.string.configuracion)
                else -> getString(R.string.otra_pestana)
            }
        }.attach()
    }
}
