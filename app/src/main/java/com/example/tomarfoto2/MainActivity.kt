package com.example.tomarfoto2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), FotoAdapter.MenuItemClickListener {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_RETAKE_PHOTO = 2  // Nuevo requestCode para retomar la foto

    private var photoToRetake: String? = null
    private lateinit var btnTomarFoto: Button
    private lateinit var imgFoto: ImageView
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoAdapter: FotoAdapter

    private var photoList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        imgFoto = findViewById(R.id.imgFoto)
        photoRecyclerView = findViewById(R.id.photoRecyclerView)

        // Inicializa el adaptador y lo conecta al RecyclerView
        photoRecyclerView.layoutManager = LinearLayoutManager(this)
        photoAdapter = FotoAdapter(photoList, this) { position ->
            // Mostrar la foto seleccionada
            val photoPath = photoList[position]
            val bitmap = BitmapFactory.decodeFile(photoPath)
            imgFoto.setImageBitmap(bitmap)
        }
        photoRecyclerView.adapter = photoAdapter

        // Cargar las fotos de manera inicial
        mostrarFotos()

        // Set up click listeners for buttons
        btnTomarFoto.setOnClickListener {
            tomarFoto()
        }
    }

    override fun onModificar(position: Int) {
        modificarNombreFoto(photoList[position], position)
    }

    override fun onRetomar(position: Int) {
        retomarFoto(photoList[position])
    }

    override fun onEliminar(position: Int) {
        eliminarFoto(photoList[position], position)
    }

    private fun modificarNombreFoto(photoPath: String, position: Int) {
        val file = File(photoPath)
        val originalName = file.name

        // Crear un cuadro de diálogo con un EditText
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Modificar nombre de archivo")

        val input = EditText(this)
        input.setText(originalName)
        alertDialog.setView(input)

        alertDialog.setPositiveButton("Guardar") { _, _ ->
            val nuevoNombre = input.text.toString()
            if (nuevoNombre.isNotEmpty()) {
                val nuevoArchivo = File(file.parent, nuevoNombre)
                if (file.renameTo(nuevoArchivo)) {
                    // Actualizar la lista con la nueva ruta
                    photoList[position] = nuevoArchivo.absolutePath
                    photoAdapter.notifyItemChanged(position)
                    Toast.makeText(this, "Nombre modificado a $nuevoNombre", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al renombrar el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
        alertDialog.setNegativeButton("Cancelar", null)
        alertDialog.show()
    }

    private fun eliminarFoto(photoPath: String, position: Int) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Confirmar eliminación")
        alertDialog.setMessage("¿Estás seguro de que quieres eliminar esta foto?")
        alertDialog.setPositiveButton("Sí") { _, _ ->
            // Eliminar archivo del sistema
            val file = File(photoPath)
            if (file.exists()) {
                file.delete()
            }

            // Eliminar de la lista y notificar cambios
            photoList.removeAt(position)
            photoAdapter.notifyItemRemoved(position)

            Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show()
        }
        alertDialog.setNegativeButton("No", null)
        alertDialog.show()
    }

    private fun tomarFoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun retomarFoto(photoPath: String) {
        photoToRetake = photoPath  // Guardar la ruta de la foto que se va a retomar
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            startActivityForResult(takePictureIntent, REQUEST_RETAKE_PHOTO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap

            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Guardar nueva foto
                    guardarFotoNueva(imageBitmap)
                }

                REQUEST_RETAKE_PHOTO -> {
                    // Reemplazar la foto existente
                    photoToRetake?.let { photoPath ->
                        reemplazarFoto(photoPath, imageBitmap)
                    }
                }
            }
        }
    }

    private fun guardarFotoNueva(imageBitmap: Bitmap) {
        val folder = File(getExternalFilesDir(null), "imagenes")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file = File(folder, "foto_${System.currentTimeMillis()}.jpg")
        try {
            val out = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()

            // Agregar el archivo a la lista de fotos
            val fileName = file.absolutePath
            photoList.add(fileName)
            photoAdapter.notifyDataSetChanged()

            Toast.makeText(this, "Foto guardada: $fileName", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reemplazarFoto(photoPath: String, imageBitmap: Bitmap) {
        val file = File(photoPath)
        if (file.exists()) {
            try {
                val out = FileOutputStream(file)
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                // Actualizar la imagen en la lista
                val position = photoList.indexOf(file.absolutePath)
                photoAdapter.notifyItemChanged(position)

                Toast.makeText(this, "Foto reemplazada", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al reemplazar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarFotos() {
        val folder = File(getExternalFilesDir(null), "imagenes")
        if (folder.exists()) {
            val files = folder.listFiles()
            if (files != null) {
                photoList.clear()
                for (file in files) {
                    if (file.isFile) {
                        photoList.add(file.absolutePath)
                    }
                }
                photoAdapter.notifyDataSetChanged()
                if (photoList.isNotEmpty()) {
                    val firstPhotoPath = photoList[0]
                    val bitmap = BitmapFactory.decodeFile(firstPhotoPath)
                    imgFoto.setImageBitmap(bitmap)
                } else {
                    imgFoto.setImageBitmap(null)
                    Toast.makeText(this, "No hay fotos para mostrar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se encontraron archivos en la carpeta", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se encontró la carpeta de imágenes", Toast.LENGTH_SHORT).show()
        }
    }
}
