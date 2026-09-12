package com.example.tplus_jffz.ui.transvoucher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tplus_jffz.data.model.TransVoucherDetail
import com.example.tplus_jffz.databinding.ItemSaleDetailBinding

class TransVoucherDetailAdapter(
    private val details: List<TransVoucherDetail>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<TransVoucherDetailAdapter.ViewHolder>() {

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

        fun bind(detail: TransVoucherDetail, position: Int) {
            binding.tvMaterialName.text = detail.materialName ?: detail.materialCode
            binding.tvMaterialCode.text = "编码: ${detail.materialCode}"
            binding.tvSpecification.text = "规格: ${detail.specification ?: "-"}"
            binding.tvQty.text = "x ${detail.qty}"
            binding.tvPrice.text = ""
            binding.tvAmount.text = "批号: ${detail.batchNo ?: "-"}"
            binding.btnDelete.setOnClickListener { onDelete(position) }
        }
    }
}
