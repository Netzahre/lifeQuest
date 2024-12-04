package com.example.lifequest

data class Tarea(
    val id: Int,
    val nombre: String,
    val monedas: Int,
    val repeticiones: Int, // Número de repeticiones (por ejemplo, 2 diarias, 1 semanal)
    val tipoRepeticion: String, // Tipo de repetición ("Diaria", "Semanal", "Veces")
    val fechaInicio: String, // Fecha de inicio de la tarea
    var completada: Int, // 0 si no completada, 1 si completada
    var ultimaRepeticion: String?, // Fecha de la última repetición
    var vecesCompletada: Int, // Número de veces que la tarea ha sido completada
    val usuario: String // Usuario al que pertenece la tarea
)

