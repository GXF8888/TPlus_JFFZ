package com.example.tplus_jffz.ui.index

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tplus_jffz.databinding.ItemMenuBinding

class MenuAdapter(private val items: List<IndexActivity.MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class MenuViewHolder(private val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: IndexActivity.MenuItem) {
            binding.tvTitle.setText(item.titleRes)
            binding.ivIcon.setImageResource(item.iconRes)
            binding.root.setOnClickListener { item.onClick() }
        }
    }
}