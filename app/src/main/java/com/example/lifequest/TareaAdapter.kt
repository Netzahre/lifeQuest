package com.example.lifequest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TareaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cardView: CardView = view.findViewById(R.id.cardView)
    val nombreTextView: TextView = view.findViewById(R.id.nombre)
    val gananciaTextView: TextView = view.findViewById(R.id.ganancia)
    val checkBox: CheckBox = view.findViewById(R.id.checkbox)
}

class TareaAdapter(
    private var tareas: List<Tarea>,
    private val onClick: (Tarea) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<TareaViewHolder>() {

    var modoSeleccion = false // Controla si el modo de selección está activado
    private val tareasSeleccionadas = mutableSetOf<Int>()

    // Método para activar el modo de selección
    fun activarModoSeleccion() {
        modoSeleccion = true
        notifyDataSetChanged() // Notifica que los datos han cambiado
    }

    fun desactivarModoSeleccion() {
        modoSeleccion = false
        tareasSeleccionadas.clear() // Limpia las tareas seleccionadas
        notifyDataSetChanged() // Notifica que los datos han cambiado
    }

    // Método para actualizar las tareas después de realizar cambios
    fun actualizarTareas(tareas: List<Tarea>) {
        this.tareas = tareas
        notifyDataSetChanged() // Notifica a RecyclerView que los datos han cambiado
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.nombreTextView.text = tarea.nombre
        holder.gananciaTextView.text = "${tarea.monedas} monedas"

        // Deshabilitar el CheckBox si la tarea ya está completada o no es el momento adecuado para marcarla
        holder.checkBox.isChecked = tarea.completada == 1
        holder.checkBox.isEnabled =
            !isRepeticionPendiente(tarea) && tarea.vecesCompletada < tarea.repeticiones

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isRepeticionPendiente(tarea)) {
                tarea.completada = 1
                tarea.vecesCompletada += 1
                tarea.ultimaRepeticion = obtenerFechaActual()
                actualizarTareaEnBaseDeDatos(holder, tarea)
                darRecompensa(holder, tarea)

                if (tarea.vecesCompletada >= tarea.repeticiones) {
                    eliminarTarea(tarea)
                }
            } else {
                tarea.completada = 0
                actualizarTareaEnBaseDeDatos(holder, tarea)
            }
        }

        // Cambiar la opacidad de la tarjeta según si está en modo de selección
        if (modoSeleccion) {
            holder.cardView.alpha = if (tareasSeleccionadas.contains(tarea.id)) 0.5f else 1f
        } else {
            holder.cardView.alpha = 1f // Totalmente opaco cuando no está en modo selección
        }

        // Maneja la selección o deselección de la tarea
        holder.cardView.setOnClickListener {
            if (modoSeleccion) {
                // Maneja la selección de la tarea en modo selección
                if (tareasSeleccionadas.contains(tarea.id)) {
                    tareasSeleccionadas.remove(tarea.id) // Desmarcar la tarea
                } else {
                    tareasSeleccionadas.add(tarea.id) // Marcar la tarea
                }
                notifyDataSetChanged() // Actualiza el RecyclerView
            } else {
                onClick(tarea)
            }
        }
    }

    override fun getItemCount(): Int = tareas.size


    private fun isRepeticionPendiente(tarea: Tarea): Boolean {
        // Verifica si ha pasado el tiempo necesario para completar la tarea
        val ultimaRepeticion = tarea.ultimaRepeticion
        val tipoRepeticion = tarea.tipoRepeticion
        val repeticiones = tarea.repeticiones

        if (ultimaRepeticion == null) {
            return false // Si nunca se ha completado, se puede completar
        }

        // Obtener la fecha actual
        val fechaActual = obtenerFechaActual()

        return when (tipoRepeticion) {
            "Diaria" -> {
                // Comprobar si han pasado 'repeticiones' días desde la última repetición
                val diferenciaDias = obtenerDiferenciaEnDias(fechaActual, ultimaRepeticion)
                diferenciaDias < repeticiones
            }

            "Semanal" -> {
                // Comprobar si han pasado 'repeticiones' semanas desde la última repetición
                val diferenciaSemanas = obtenerDiferenciaEnSemanas(fechaActual, ultimaRepeticion)
                diferenciaSemanas < repeticiones
            }

            "Veces" -> {
                // Si el tipo es "Veces", solo se puede marcar como completada si no ha alcanzado el número de veces
                tarea.vecesCompletada < repeticiones
            }

            else -> false
        }
    }

    private fun obtenerDiferenciaEnDias(fecha1: String, fecha2: String): Int {
        // Lógica para calcular la diferencia en días entre dos fechas
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = sdf.parse(fecha1)
        val date2 = sdf.parse(fecha2)

        val diferencia = date1.time - date2.time
        return (diferencia / (1000 * 60 * 60 * 24)).toInt() // Convertir la diferencia a días
    }

    private fun obtenerDiferenciaEnSemanas(fecha1: String, fecha2: String): Int {
        // Lógica para calcular la diferencia en semanas entre dos fechas
        val diasDiferencia = obtenerDiferenciaEnDias(fecha1, fecha2)
        return diasDiferencia / 7
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun actualizarTareaEnBaseDeDatos(holder: TareaViewHolder, tarea: Tarea) {
        // Actualiza la tarea en la base de datos con el nuevo estado de completada, vecesCompletada y fecha
        val bd = SQLiteAyudante(holder.itemView.context, "LifeQuest", null, 1).writableDatabase
        bd.execSQL(
            "UPDATE Tareas SET completada = ?, vecesCompletada = ?, ultimaRepeticion = ? WHERE id = ? AND usuario = ?",
            arrayOf(
                tarea.completada,
                tarea.vecesCompletada,
                tarea.ultimaRepeticion,
                tarea.id,
                tarea.usuario
            )
        )
        bd.close()
    }

    private fun darRecompensa(holder: TareaViewHolder, tarea: Tarea) {
        // Aquí puedes dar monedas, XP, o cualquier tipo de recompensa
        val db = SQLiteAyudante(holder.itemView.context, "LifeQuest", null, 1).writableDatabase
        db.execSQL("UPDATE usuarios SET monedas = monedas + ? WHERE usuario = ?", arrayOf(tarea.monedas, tarea.usuario))
        Toast.makeText(
            holder.itemView.context,
            "¡Recompensa obtenida por completar la tarea!",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Función para eliminar la tarea en base de datos y en la lista
    fun eliminarTarea(tarea: Tarea) {
        val bd = SQLiteAyudante(context, "LifeQuest", null, 1).writableDatabase
        bd.execSQL("DELETE FROM Tareas WHERE id = ?", arrayOf(tarea.id.toString()))
        bd.close()

        // Eliminar la tarea de la lista en el RecyclerView
        tareas = tareas.filterNot { it.id == tarea.id }
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }

}




