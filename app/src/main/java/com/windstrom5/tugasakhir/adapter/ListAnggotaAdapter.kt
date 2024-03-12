package com.windstrom5.tugasakhir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import de.hdodenhof.circleimageview.CircleImageView
import com.windstrom5.tugasakhir.databinding.ItemAnggotaBinding

class ListAnggotaAdapter(
    private var pekerjaList: List<Pekerja>,
    private var adminList: List<Admin>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class PekerjaViewHolder(private val binding: ItemAnggotaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentPekerja: Pekerja) {
            // Set data to views for Pekerja
            Glide.with(itemView.context)
                .load("https://9ca5-125-163-245-254.ngrok-free.app/storage/${currentPekerja.profile}")
                .into(binding.profileImageView)
            binding.nameTextView.text = currentPekerja.nama
            binding.roleTextView.text = "Pekerja"
        }
    }

    inner class AdminViewHolder(private val binding: ItemAnggotaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentAdmin: Admin) {
            // Set data to views for Admin
            Glide.with(itemView.context)
                .load("https://9ca5-125-163-245-254.ngrok-free.app/storage/${currentAdmin.profile}")
                .into(binding.profileImageView)
            binding.nameTextView.text = currentAdmin.nama
            binding.roleTextView.text = "Admin"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAnggotaBinding.inflate(inflater, parent, false)
        return when (viewType) {
            VIEW_TYPE_PEKERJA -> PekerjaViewHolder(binding)
            VIEW_TYPE_ADMIN -> AdminViewHolder(binding)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PekerjaViewHolder -> {
                val currentPekerja = pekerjaList[position - adminList.size]
                holder.bind(currentPekerja)
            }
            is AdminViewHolder -> {
                val currentAdmin = adminList[position]
                holder.bind(currentAdmin)
            }
        }
    }

    override fun getItemCount(): Int {
        return pekerjaList.size + adminList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < pekerjaList.size) {
            VIEW_TYPE_PEKERJA
        } else {
            VIEW_TYPE_ADMIN
        }
    }

    fun updateData(newPekerjaList: List<Pekerja>, newAdminList: List<Admin>){
        pekerjaList = newPekerjaList
        adminList = newAdminList
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_PEKERJA = 0
        private const val VIEW_TYPE_ADMIN = 1
    }
}
