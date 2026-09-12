package com.example.tplus_jffz.ui.saledelivery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tplus_jffz.data.model.SaleOrderDetail
import com.example.tplus_jffz.databinding.ItemSaleDetailBinding

class SaleDetailAdapter(
    private val details: List<SaleOrderDetail>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<SaleDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSaleDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(details[position], position)
    }

    override fun getItemCount() = details.size

    inner class ViewHolder(private val binding: ItemSaleDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(detail: SaleOrderDetail, position: Int) {
            binding.tvMaterialName.text = detail.materialName ?: detail.materialCode
            binding.tvMaterialCode.text = "编码: ${detail.materialCode}"
            binding.tvSpecification.text = "规格: ${detail.specification ?: "-"}"
            binding.tvQty.text = "x ${detail.qty}"
            binding.tvPrice.text = "单价: %.2f".format(detail.price)
            binding.tvAmount.text = "%.2f".format(detail.amount)
            binding.btnDelete.setOnClickListener { onDelete(position) }
        }
    }
}
