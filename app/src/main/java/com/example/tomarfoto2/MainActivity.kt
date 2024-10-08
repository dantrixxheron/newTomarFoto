package com.example.tomarfoto2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var btnTomarFoto: Button
    private lateinit var btnMostrarFotos: Button
    private lateinit var imgFoto: ImageView
    private lateinit var photoListView: ListView
    private lateinit var photoAdapter: ArrayAdapter<String>
    private var photoList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnMostrarFotos = findViewById(R.id.btnMostrarFotos)
        imgFoto = findViewById(R.id.imgFoto)
        photoListView = findViewById(R.id.photoListView)

        // Initialize the adapter and attach it to the ListView
        photoAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, photoList)
        photoListView.adapter = photoAdapter
        // Cargar las fotos de manera inicial
        mostrarFotos()

        // Set up click listeners for buttons

        btnTomarFoto.setOnClickListener {
            tomarFoto()
        }

        btnMostrarFotos.setOnClickListener {
            mostrarFotos()
        }

        // Handle ListView item click to display the selected photo
        photoListView.setOnItemClickListener { _, _, position, _ ->
            val photoPath = photoList[position]
            val bitmap = BitmapFactory.decodeFile(photoPath)
            imgFoto.setImageBitmap(bitmap)
        }
    }

    private fun tomarFoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgFoto.setImageBitmap(imageBitmap)

            // Save the photo in the "imagenes" folder
            val folder = File(getExternalCacheDir(), "imagenes")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val file = File(folder, "foto_${System.currentTimeMillis()}.jpg")
            try {
                val out = FileOutputStream(file)
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                // Add the file path to the list and notify the adapter
                photoList.add(file.absolutePath)
                photoAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mostrarFotos() {
        val folder = File(getExternalCacheDir(), "imagenes")
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
                // Mostrar la primera imagen si hay fotos disponibles
                if (photoList.isNotEmpty()) {
                    val firstPhotoPath = photoList[0]
                    val bitmap = BitmapFactory.decodeFile(firstPhotoPath)
                    imgFoto.setImageBitmap(bitmap)
                }
            }
        } else {
            Toast.makeText(this, "No hay fotos en la carpeta", Toast.LENGTH_SHORT).show()
        }
    }
}
