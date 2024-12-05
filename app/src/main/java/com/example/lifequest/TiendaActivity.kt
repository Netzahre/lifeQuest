package com.example.lifequest

import android.content.DialogInterface
import android.content.ContentValues
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
import android.database.sqlite.SQLiteDatabase

class TiendaActivity : AppCompatActivity() {
    private val premiosSeleccionados = mutableSetOf<Int>() // Almacena IDs de premios seleccionados
    private var modoSeleccionActivo = false // Controla si el modo de selección está activo
    private lateinit var contenedorPremios: FlexboxLayout
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
        contenedorPremios = findViewById(R.id.contenedorPremios)

        // Carga los premios desde la base de datos
        cargarPremios()

        botonCrear.setOnClickListener {
            val intent = android.content.Intent(this, CrearPremioActivity::class.java)
            startActivity(intent)
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


                    // Eliminar premios de la base de datos
                    eliminarPremiosDeDB(premiosSeleccionados)

                    Toast.makeText(this, "Premios eliminados", Toast.LENGTH_SHORT).show()
                }
                modoSeleccionActivo = false
                resetearEstadoPremios(contenedorPremios)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPremios()
    }

    private fun eliminarPremiosDeDB(premiosSeleccionados: MutableSet<Int>) {
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.writableDatabase
        for (id in premiosSeleccionados) {
            // Elimina los premios seleccionados de la base de datos
            db.delete("premios", "id=?", arrayOf(id.toString()))
        }
        db.close()
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

    private fun cargarPremios() {
        contenedorPremios.removeAllViews() // Limpia el contenedor antes de cargar los premios
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.readableDatabase

        val usuarioActual = obtenerUsuarioActual()  // Método para obtener el usuario actual

        // Consulta SQL ajustada para obtener solo los campos id, nombre y costo
        val query = "SELECT id, nombre, costo FROM premios WHERE usuario = ?"
        val cursor = db.rawQuery(query, arrayOf(usuarioActual))

        val premiosList = mutableListOf<Premio>()

        // Recogemos todos los premios y los almacenamos en la lista premiosList
        while (cursor.moveToNext()) {
            val premioId = cursor.getInt(cursor.getColumnIndex("id"))
            val premioNombre = cursor.getString(cursor.getColumnIndex("nombre"))
            val premioCosto = cursor.getInt(cursor.getColumnIndex("costo"))

            // Creamos el objeto Premio y lo agregamos a la lista
            val premio = Premio(premioId, premioNombre, premioCosto)
            premiosList.add(premio)
        }

        cursor.close()

        // Usamos los objetos Premio para actualizar la vista
        for (premio in premiosList) {
            val inflater = LayoutInflater.from(this)
            val nuevoPremio = inflater.inflate(R.layout.premio, contenedorPremios, false)

            // Actualizamos los textos con la información del objeto Premio
            nuevoPremio.findViewById<TextView>(R.id.nombrePremio).text = premio.nombre
            nuevoPremio.findViewById<TextView>(R.id.costoPremio).text = premio.costo.toString()

            // Asignamos un tag único para identificar el premio
            nuevoPremio.tag = premio.id

            nuevoPremio.setOnClickListener {
                // Si no estamos en modo selección, mostramos el diálogo de compra
                if (!modoSeleccionActivo) {
                    //Verificamos que el usuario pueda comprar el premio
                    val monedasDisponibles = db.rawQuery("SELECT monedas FROM usuarios WHERE usuario = '$usuarioActual'", null)
                    monedasDisponibles.moveToFirst()
                    val monedas = monedasDisponibles.getInt(0)
                    if (monedas < premio.costo) {
                        Toast.makeText(this, "No tienes suficientes monedas para comprar este premio", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {
                        monedasDisponibles.close()
                        mostrarDialogoCompra(nuevoPremio)
                    }
                } else {
                    // Modo de selección para eliminar
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

            // Agregamos el nuevo premio al contenedor
            contenedorPremios.addView(nuevoPremio)
        }
    }

    private fun mostrarDialogoCompra(premioView: View) {
        val premioNombre =
            (premioView as CardView).findViewById<TextView>(R.id.nombrePremio).text.toString()
        val premioPrecio = premioView.findViewById<TextView>(R .id.costoPremio).text.toString()
        val usuarioActual = obtenerUsuarioActual()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar compra")
            .setMessage("¿Deseas comprar el premio '$premioNombre'?")
            .setPositiveButton("Comprar") { dialog: DialogInterface, id: Int ->
                // Lógica para procesar la compra del premio
                Toast.makeText(this, "Compra confirmada para $premioNombre", Toast.LENGTH_SHORT)
                    .show()

                val db = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
                //Añadimos las monedsa gastadas al usuario y lo restamos de monedas disponibles
                db.execSQL("UPDATE usuarios SET monedas = monedas - $premioPrecio WHERE usuario = '$usuarioActual'")
                db.execSQL("UPDATE usuarios SET monedas_gastadas  = monedas_gastadas  + $premioPrecio WHERE usuario = '$usuarioActual'")
                db.close()

            }
            .setNegativeButton("Cancelar") { dialog: DialogInterface, id: Int ->
                dialog.dismiss()  // Cierra el diálogo sin hacer nada
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun obtenerUsuarioActual(): String? {
        val bd = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val cursor = bd.rawQuery("SELECT usuario FROM sesionActual LIMIT 1", null)
        var usuario: String? = null
        if (cursor.moveToFirst()) {
            usuario = cursor.getString(0)
        }
        cursor.close()
        bd.close()
        return usuario
    }
}
