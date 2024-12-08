package com.example.lifequest

import ConfiguracionFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// Clase que se encarga de manejar los fragmentos de la vista de perfil
class PerfilPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    // Retorna la cantidad de fragmentos
    override fun getItemCount(): Int = 2

    // Retorna el fragmento correspondiente a la posición
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DatosFragment()
            1 -> ConfiguracionFragment()
            else -> throw IllegalArgumentException("Posición no válida")
        }
    }
}
