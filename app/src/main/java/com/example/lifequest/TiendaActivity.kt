package com.example.lifequest

import android.content.DialogInterface
import android.content.ContentValues
import android.content.Intent
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
import android.speech.RecognizerIntent
import java.util.Locale

class TiendaActivity : AppCompatActivity() {
    private val premiosSeleccionados = mutableSetOf<Int>()
    private var modoSeleccionActivo = false
    private lateinit var contenedorPremios: FlexboxLayout
    private val SPEECH_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
        menuSuperior.configurarTextoDeAyuda("En la tienda puedes comprar premios con las monedas que has ganado. ¡No olvides revisar tus tareas y logros para ganar más monedas!")
        menuSuperior.microfono.setOnClickListener {
            startSpeechToText()
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
                // Activa el modo de selección
                modoSeleccionActivo = true
                botonBorrar.text = "Confirmar"
                mostrarMensaje("Selecciona los premios que deseas eliminar")
            } else {
                // Verifica si hay premios seleccionados
                if (premiosSeleccionados.isEmpty()) {
                    mostrarMensaje("No se seleccionaron premios")
                } else {
                    // Elimina los premios seleccionados y actualiza el contenedor
                    for (id in premiosSeleccionados) {
                        val premioAEliminar = contenedorPremios.findViewWithTag<CardView>(id)
                        contenedorPremios.removeView(premioAEliminar)
                    }
                    eliminarPremiosDeDB(premiosSeleccionados)
                    premiosSeleccionados.clear()
                    mostrarMensaje("Premios eliminados")
                }
                modoSeleccionActivo = false
                botonBorrar.text = "Borrar"
                resetearEstadoPremios(contenedorPremios)
            }
        }
    }

    // Método para mostrar mensajes en pantalla
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        cargarPremios()
    }

    // Método para eliminar premios de la base de datos
    private fun eliminarPremiosDeDB(premiosSeleccionados: MutableSet<Int>) {
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.writableDatabase
        for (id in premiosSeleccionados) {
            // Elimina los premios seleccionados de la base de datos
            db.delete("premios", "id=?", arrayOf(id.toString()))
        }
        db.close()
    }

    // Método para restablecer la opacidad de los premios
    private fun resetearEstadoPremios(contenedorPremios: FlexboxLayout) {
        for (i in 0 until contenedorPremios.childCount) {
            val vista = contenedorPremios.getChildAt(i)
            if (vista is CardView) {
                vista.alpha = 1.0f
            }
        }
    }

    // Método para cargar los premios desde la base de datos
    private fun cargarPremios() {
        contenedorPremios.removeAllViews()
        val dbHelper = SQLiteAyudante(this, "LifeQuest", null, 1)
        val db = dbHelper.readableDatabase

        val usuarioActual = obtenerUsuarioActual()  // Método para obtener el usuario actual

        // Consulta para obtener los premios del usuario actual
        val query = "SELECT id, nombre, costo FROM premios WHERE usuario = ?"
        val cursor = db.rawQuery(query, arrayOf(usuarioActual))

        val premiosList = mutableListOf<Premio>()

        // Iteramos sobre los resultados de la consulta
        while (cursor.moveToNext()) {
            val premioIdIndex = cursor.getColumnIndex("id")
            val premioId = cursor.getInt(premioIdIndex)
            val premioCostoIndex = cursor.getColumnIndex("costo")

            if (premioCostoIndex == -1 || premioIdIndex == -1) {
                mostrarMensaje("Error al cargar los premios")
                return
            }

            val premiNombreIndex = cursor.getColumnIndex("nombre")
            val premioNombre = cursor.getString(premiNombreIndex)
            val premioCosto = cursor.getInt(premioCostoIndex)

            // Creamos un objeto Premio con los datos obtenidos
            val premio = Premio(premioId, premioNombre, premioCosto)
            premiosList.add(premio)
        }

        cursor.close()

        // Iteramos sobre la lista de premios para mostrarlos en la interfaz
        for (premio in premiosList) {
            val inflater = LayoutInflater.from(this)
            val nuevoPremio = inflater.inflate(R.layout.premio, contenedorPremios, false)

            nuevoPremio.findViewById<TextView>(R.id.nombrePremio).text = premio.nombre
            nuevoPremio.findViewById<TextView>(R.id.costoPremio).text = premio.costo.toString()

            // Asignamos el ID del premio al contenedor
            nuevoPremio.tag = premio.id

            nuevoPremio.setOnClickListener {
                // Si no estamos en modo selección, mostramos el diálogo de compra
                if (!modoSeleccionActivo) {
                    if (verificarCompra(premio)) {
                        mostrarDialogoCompra(nuevoPremio)
                    } else {
                        mostrarMensaje("No tienes suficientes monedas para comprar este premio")
                    }

                } else {
                    // Modo de selección para eliminar
                    val premioId = nuevoPremio.tag as Int
                    if (premiosSeleccionados.contains(premioId)) {
                        premiosSeleccionados.remove(premioId)
                        nuevoPremio.alpha = 1.0f
                    } else {
                        premiosSeleccionados.add(premioId)
                        nuevoPremio.alpha = 0.5f
                    }
                }
            }

            // Agregamos el nuevo premio al contenedor
            contenedorPremios.addView(nuevoPremio)
        }
    }

    //Metodo para verificar que el usuario pueda comprar el premio
    private fun verificarCompra(premio: Premio): Boolean {
        val usuarioActual = obtenerUsuarioActual()
        val db = SQLiteAyudante(this, "LifeQuest", null, 1).readableDatabase
        val monedasDisponibles =
            db.rawQuery("SELECT monedas FROM usuarios WHERE usuario = '$usuarioActual'", null)
        monedasDisponibles.moveToFirst()
        val monedas = monedasDisponibles.getInt(0)
        monedasDisponibles.close()
        db.close()
        return monedas >= premio.costo
    }

    //Metodo para mostrar dialogo de compra
    private fun mostrarDialogoCompra(premioView: View) {
        val premioNombre =
            (premioView as CardView).findViewById<TextView>(R.id.nombrePremio).text.toString()
        val premioPrecio = premioView.findViewById<TextView>(R.id.costoPremio).text.toString()
        val usuarioActual = obtenerUsuarioActual()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar compra")
            .setMessage("¿Deseas comprar el premio '$premioNombre'?")
            .setPositiveButton("Comprar") { dialog: DialogInterface, id: Int ->
                // Lógica para procesar la compra del premio
                mostrarMensaje("Premio comprado")

                val db = SQLiteAyudante(this, "LifeQuest", null, 1).writableDatabase
                // Actualiza la cantidad de monedas del usuario y las monedas gastadas
                db.execSQL("UPDATE usuarios SET monedas = monedas - $premioPrecio WHERE usuario = '$usuarioActual'")
                db.execSQL("UPDATE usuarios SET monedas_gastadas  = monedas_gastadas  + $premioPrecio WHERE usuario = '$usuarioActual'")
                db.close()

            }.setNegativeButton("Cancelar") { dialog: DialogInterface, id: Int ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    // Método para obtener el usuario actual
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

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora para transcribir tu voz")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(
                this, "El reconocimiento de voz no está disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.let {
                val accion = it[0].lowercase()
                when (accion) {
                    "tareas" -> {
                        val intent = Intent(this, TareasActivity::class.java)
                        startActivity(intent)
                    }

                    "logros" -> {
                        val intent = Intent(this, LogrosActivity::class.java)
                        startActivity(intent)
                    }

                    "tienda" -> {
                        val intent = Intent(this, TiendaActivity::class.java)
                        startActivity(intent)
                    }

                    "perfil" -> {
                        val intent = Intent(this, PerfilActivity::class.java)
                        startActivity(intent)
                    }

                    "ayuda" -> {
                        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
                        menuSuperior.mostrarAyuda("En la tienda puedes comprar premios con las monedas que has ganado. ¡No olvides revisar tus tareas y logros para ganar más monedas!")
                    }

                    "añadir tarea" -> {
                        val intent = Intent(this, CrearTareaActivity::class.java)
                        startActivity(intent)
                    }

                    "añadir logro" -> {
                        val intent = Intent(this, CrearLogroActivity::class.java)
                        startActivity(intent)
                    }

                    "cambiar modo" -> {
                        val menuSuperior = findViewById<MenuSuperiorActivity>(R.id.menuSuperior)
                        menuSuperior.cambiarModo()
                    }

                    "añadir premio" -> {
                        val intent = Intent(this, CrearPremioActivity::class.java)
                        startActivity(intent)
                    }

                    "Terminos de uso" -> {
                        val intent = Intent(this, TOSActivity::class.java)
                        startActivity(intent)
                    }

                    else -> {
                        Toast.makeText(this, "No se reconoció la acción", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
