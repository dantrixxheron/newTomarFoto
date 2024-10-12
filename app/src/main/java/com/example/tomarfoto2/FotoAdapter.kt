package com.example.tomarfoto2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FotoAdapter(
    private val photoList: List<String>,
    private val menuItemClickListener: MenuItemClickListener,
    private val itemClickListener: (Int) -> Unit
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    interface MenuItemClickListener {
        fun onModificar(position: Int)
        fun onRetomar(position: Int)
        fun onEliminar(position: Int)
    }

    class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoName: TextView = itemView.findViewById(R.id.photoNameTextView)
        val menuButton: Button = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val photoPath = photoList[position]
        holder.photoName.text = photoPath.substringAfterLast("/") // Muestra solo el nombre del archivo

        // Configura el listener para el botón del menú
        holder.menuButton.setOnClickListener {
            // Muestra las opciones del menú
            mostrarMenu(holder.itemView, position)
        }

        // Configura el listener para el clic en el nombre de la foto
        holder.itemView.setOnClickListener {
            itemClickListener(position)
        }
    }

    private fun mostrarMenu(view: View, position: Int) {
        val popupMenu = android.widget.PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.photo_menu, popupMenu.menu)

        // Configura las acciones del menú
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_modificar -> {
                    menuItemClickListener.onModificar(position)
                    true
                }
                R.id.action_retomar -> {
                    menuItemClickListener.onRetomar(position)
                    true
                }
                R.id.action_eliminar -> {
                    menuItemClickListener.onEliminar(position)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun getItemCount(): Int {
        return photoList.size
    }
}
