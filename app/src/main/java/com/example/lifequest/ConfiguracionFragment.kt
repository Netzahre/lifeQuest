import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifequest.LoginActivity
import com.example.lifequest.R
import com.example.lifequest.SQLiteAyudante
import java.io.ByteArrayOutputStream

class ConfiguracionFragment : Fragment() {

    private lateinit var imagenPerfil: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_configuracion, container, false)

        imagenPerfil = view.findViewById(R.id.imagenPerfil)
        val cambiarImagenPerfil = view.findViewById<Button>(R.id.cambiarImagenPerfil)

        // Botón para cambiar imagen de perfil (abrir galería) y cargar imagen guardada (no funciona)
        cambiarImagenPerfil.setOnClickListener {
            mostrarMensaje("Esto no funciona, me descoloca el fragment") // No funciona
//            abrirGaleria()
        }

        val cerrarSesion = view.findViewById<Button>(R.id.cerrarSesion)
        cerrarSesion.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        // Cargar imagen guardada
//        cargarImagenDesdeSQLite()

        return view
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    // Abrir galería
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Obtener imagen seleccionada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

                // Establecer la imagen redimensionada en el ImageView
                imagenPerfil.setImageBitmap(bitmap)

                // Guardar la imagen redimensionada en SQLite
                guardarImagenEnSQLite(bitmap)
            }
        }
    }


    // Guardar imagen en SQLite
    private fun guardarImagenEnSQLite(bitmap: Bitmap) {
        val db = SQLiteAyudante(requireContext(), "LifeQuest", null, 1).writableDatabase
        val contentValues = ContentValues()

        // Convertir Bitmap a ByteArray
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        contentValues.put("imagenPerfil", imageBytes)
        db.update("usuarios", contentValues, "usuario = ?", arrayOf("nombreUsuario"))
        db.close()

        mostrarMensaje("Imagen de perfil actualizada")
    }

    // Cargar imagen desde SQLite
    private fun cargarImagenDesdeSQLite() {
        val db = SQLiteAyudante(requireContext(), "LifeQuest", null, 1).readableDatabase
        val cursor = db.rawQuery("SELECT imagenPerfil FROM usuarios WHERE usuario = 'nombreUsuario'", null)
        if (cursor.moveToFirst()) {
            val imageBytes = cursor.getBlob(0)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imagenPerfil.setImageBitmap(bitmap)
        }
        cursor.close()
        db.close()
    }
}
