package com.example.lifequest

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class DatosFragment : Fragment() {
    private lateinit var imagenPerfil: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_datos, container, false)
        //Cargar imagen de perfil
        imagenPerfil = view.findViewById(R.id.imagenPerfil)

        val nombreUsuario = view.findViewById<TextView>(R.id.mosrarUsuarioPerfil)
        val cantidadMonedas = view.findViewById<TextView>(R.id.mostrarMonedasPerfil)
        val cantidadTareas = view.findViewById<TextView>(R.id.tareascompletadas)
        val monedasGastadas = view.findViewById<TextView>(R.id.monedasGastadas)
        val emailUsuario = view.findViewById<TextView>(R.id.mostrarEmailPerfil)
        val db = SQLiteAyudante(requireContext(), "LifeQuest", null, 1).readableDatabase

        // buscar datos del usuario en la base de datos
        val usuario = obtenerUsuarioActual()
        val cursorDatos = db.rawQuery("SELECT * FROM usuarios WHERE usuario = '$usuario'", null)
        if (cursorDatos.moveToFirst()) {
            nombreUsuario.text = cursorDatos.getString(0)
            cantidadMonedas.text = cursorDatos.getInt(3).toString()
            cantidadTareas.text = cursorDatos.getInt(4).toString()
            monedasGastadas.text = cursorDatos.getInt(5).toString()
            emailUsuario.text = cursorDatos.getString(1)
        }
        cursorDatos.close()
        db.close()

        return view
    }

    //Cargar imagen desde SQLite. No pude hacer que funcione
    private fun cargarImagenDesdeSQLite() {
        val db = SQLiteAyudante(requireContext(), "LifeQuest", null, 1).readableDatabase
        val cursor =
            db.rawQuery(
                "SELECT imagen FROM usuarios WHERE usuario = '${obtenerUsuarioActual()}'",
                null
            )
        //Si hay una imagen en la base de datos, cargarla
        if (cursor.moveToFirst()) {
            val byteArray = cursor.getBlob(0)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            imagenPerfil.setImageBitmap(bitmap)
        }
        cursor.close()
        db.close()
    }

    //Obtener usuario actual
    private fun obtenerUsuarioActual(): String {
        val db = SQLiteAyudante(requireContext(), "LifeQuest", null, 1).readableDatabase
        val cursor = db.rawQuery("SELECT usuario FROM sesionActual", null)
        val usuario = if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            ""
        }
        cursor.close()
        db.close()
        return usuario
    }
}