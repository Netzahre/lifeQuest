package com.example.lifequest

data class Logro(
    val id: Int,
    val nombre: String,
    val tareaAsociada: String,
    val repeticionesNecesarias: Int,
    val progreso: Int,
    val completado: Boolean
) {
    fun esCompletado(): Boolean {
        return completado
    }

    fun porcentajeProgreso(): Int {
        return (progreso * 100) / repeticionesNecesarias
    }
}



