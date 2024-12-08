package com.example.lifequest

// Clase que representa un logro
data class Logro(
    val id: Int,
    val nombre: String,
    val tareaAsociada: String,
    val repeticionesNecesarias: Int,
    val progreso: Int,
    val completado: Boolean
)




