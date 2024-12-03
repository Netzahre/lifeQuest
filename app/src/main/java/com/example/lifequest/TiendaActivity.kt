package com.example.lifequest

import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.flexbox.FlexboxLayout

class TiendaActivity : AppCompatActivity() {
    private val premiosSeleccionados = mutableSetOf<Int>() // Almacena IDs de premios seleccionados
    private var modoSeleccionActivo = false // Controla si el modo de selección está activo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botonCrear = findViewById<Button>(R.id.crearPremio)
        val botonBorrar = findViewById<Button>(R.id.borrarPremio)
        val contenedorPremios = findViewById<FlexboxLayout>(R.id.contenedorPremios) // Cambio aquí

        // Carga los premios (usando datos simulados por ahora)
        cargarPremios(contenedorPremios)

        botonCrear.setOnClickListener {
            // Lógica para añadir un nuevo premio
            val inflater = LayoutInflater.from(this)
            val nuevoPremio = inflater.inflate(
                R.layout.premio,
                contenedorPremios,
                false
            ) // Inflar en el contenedor para respetar la jerarquía

            // Personaliza los textos del premio
            nuevoPremio.findViewById<TextView>(R.id.nombrePremio).text = "Nuevo Premio"
            nuevoPremio.findViewById<TextView>(R.id.costoPremio).text = "Descripción del premio"

            // Se asegura de que la vista inflada tenga las dimensiones correctas
            val layoutParams = FlexboxLayout.LayoutParams(
                100.dpToPx(),  // Convertimos 100dp a píxeles según la densidad de la pantalla
                100.dpToPx()   // Lo mismo para la altura
            )

            // Asigna un ID temporal para el premio
            val premioId = contenedorPremios.childCount
            nuevoPremio.tag = premioId

            nuevoPremio.setOnClickListener {
                // Verifica si estamos en modo selección o no
                if (!modoSeleccionActivo) {
                    // Si no estamos en modo borrado, mostramos el diálogo de compra
                    mostrarDialogoCompra(nuevoPremio)
                } else {
                    // Modo de selección para borrado
                    val premioId = nuevoPremio.tag as Int
                    if (premiosSeleccionados.contains(premioId)) {
                        premiosSeleccionados.remove(premioId)
                        nuevoPremio.alpha = 1.0f // Deseleccionado
                    } else {
                        premiosSeleccionados.add(premioId)
                        nuevoPremio.alpha = 0.5f // Seleccionado
                    }
                }
            }

            contenedorPremios.addView(nuevoPremio)
        }


        botonBorrar.setOnClickListener {
            if (!modoSeleccionActivo) {
                modoSeleccionActivo = true
                Toast.makeText(this, "Selecciona los premios a borrar", Toast.LENGTH_SHORT).show()
            } else {
                if (premiosSeleccionados.isEmpty()) {
                    Toast.makeText(this, "No has seleccionado ningún premio", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Elimina los premios seleccionados y actualiza el contenedor
                    for (id in premiosSeleccionados) {
                        val premioAEliminar = contenedorPremios.findViewWithTag<CardView>(id)
                        contenedorPremios.removeView(premioAEliminar)
                    }
                    premiosSeleccionados.clear()

                    // Actualiza los tags y las vistas después de la eliminación
                    actualizarTagsPremios(contenedorPremios)

                    Toast.makeText(this, "Premios eliminados", Toast.LENGTH_SHORT).show()
                }
                modoSeleccionActivo = false
                resetearEstadoPremios(contenedorPremios)
            }
        }
    }

    // Función para actualizar los tags de los premios restantes después de una eliminación
    private fun actualizarTagsPremios(contenedorPremios: FlexboxLayout) {
        for (i in 0 until contenedorPremios.childCount) {
            val vista = contenedorPremios.getChildAt(i)
            if (vista is CardView) {
                vista.tag = i // Actualiza el tag de cada CardView con su índice actual
            }
        }
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun resetearEstadoPremios(contenedorPremios: FlexboxLayout) {
        // Itera sobre todos los hijos del contenedor
        for (i in 0 until contenedorPremios.childCount) {
            val vista = contenedorPremios.getChildAt(i)
            // Verifica si el hijo es del tipo CardView antes de operar
            if (vista is androidx.cardview.widget.CardView) {
                vista.alpha = 1.0f // Restaura la opacidad
            }
        }
    }

    private fun cargarPremios(contenedorPremios: FlexboxLayout) {
        // Aquí obtendrás los premios (por ahora simulados)
        val premios = listOf(
            Pair("Premio 1", "Descripción 1"),
            Pair("Premio 2", "Descripción 2"),
            Pair("Premio 3", "Descripción 3")
        ) // Datos simulados

        for ((index, premio) in premios.withIndex()) {
            val inflater = LayoutInflater.from(this)
            val nuevoPremio = inflater.inflate(R.layout.premio, contenedorPremios, false)

            // Personaliza los textos
            nuevoPremio.findViewById<TextView>(R.id.nombrePremio).text = premio.first
            nuevoPremio.findViewById<TextView>(R.id.costoPremio).text = premio.second

            nuevoPremio.tag = index // ID asignado a cada premio

            nuevoPremio.setOnClickListener {
                // Verifica si estamos en modo selección o no
                if (!modoSeleccionActivo) {
                    // Si no estamos en modo borrado, mostramos el diálogo de compra
                    mostrarDialogoCompra(nuevoPremio)
                } else {
                    // Modo de selección para borrado
                    val premioId = nuevoPremio.tag as Int
                    if (premiosSeleccionados.contains(premioId)) {
                        premiosSeleccionados.remove(premioId)
                        nuevoPremio.alpha = 1.0f // Deseleccionado
                    } else {
                        premiosSeleccionados.add(premioId)
                        nuevoPremio.alpha = 0.5f // Seleccionado
                    }
                }
            }

            contenedorPremios.addView(nuevoPremio)
        }
    }
    private fun mostrarDialogoCompra(premioView: View) {
        val premioNombre =
            (premioView as CardView).findViewById<TextView>(R.id.nombrePremio).text.toString()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar compra")
            .setMessage("¿Deseas comprar el premio '$premioNombre'?")
            .setPositiveButton("Comprar") { dialog: DialogInterface, id: Int ->
                // Lógica para procesar la compra del premio
                Toast.makeText(this, "Compra confirmada para $premioNombre", Toast.LENGTH_SHORT).show()

                // Eliminar el premio del contenedor
                val contenedorPremios = findViewById<FlexboxLayout>(R.id.contenedorPremios)
                contenedorPremios.removeView(premioView)  // Elimina la vista del contenedor

                // Si el premio estaba en la lista de premios seleccionados, eliminarlo de la lista
                val premioId = premioView.tag as? Int
                premioId?.let {
                    premiosSeleccionados.remove(it)
                }

                // Aquí deberías agregar la lógica para realizar la compra, actualizar la base de datos, etc.
                // Por ejemplo, actualizar el estado de los premios en la base de datos (si es necesario)
            }
            .setNegativeButton("Cancelar") { dialog: DialogInterface, id: Int ->
                dialog.dismiss()  // Cierra el diálogo sin hacer nada
            }

        val dialog = builder.create()
        dialog.show()
    }

}

