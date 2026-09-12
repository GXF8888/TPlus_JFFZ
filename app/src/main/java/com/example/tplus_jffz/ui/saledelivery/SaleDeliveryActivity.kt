package com.example.tplus_jffz.ui.saledelivery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tplus_jffz.R
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.SaleOrderDetail
import com.example.tplus_jffz.data.model.SaleOrderRequest
import com.example.tplus_jffz.databinding.ActivitySaleDeliveryBinding
import com.example.tplus_jffz.ui.scan.ScanActivity
import com.example.tplus_jffz.utils.BarcodeMaterialHelper
import kotlinx.coroutines.launch

class SaleDeliveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaleDeliveryBinding
    private val details = mutableListOf<SaleOrderDetail>()
    private lateinit var adapter: SaleDetailAdapter

    companion object {
        const val REQUEST_SCAN = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = SaleDetailAdapter(details) { position ->
            deleteDetail(position)
        }
        binding.recyclerDetails.layoutManager = LinearLayoutManager(this)
        binding.recyclerDetails.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnScan.setOnClickListener { launchScanner() }
        binding.fabAddDetail.setOnClickListener { launchScanner() }
        binding.btnSave.setOnClickListener { saveOrder(false) }
        binding.btnSubmit.setOnClickListener { saveOrder(true) }
    }

    private fun launchScanner() {
        val intent = ScanActivity.createIntent(this)
        startActivityForResult(intent, REQUEST_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCAN && resultCode == RESULT_OK) {
            val barcode = data?.getStringExtra(ScanActivity.EXTRA_BARCODE) ?: return
            addDetailFromBarcode(barcode)
        }
    }

    private fun addDetailFromBarcode(barcode: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = BarcodeMaterialHelper.queryMaterialByBarcode(this@SaleDeliveryActivity, barcode)
            binding.progressBar.visibility = View.GONE

            result.onSuccess { material ->
                val detail = SaleOrderDetail(
                    materialCode = material.code,
                    materialName = material.name,
                    specification = material.specification,
                    unit = material.unit,
                    qty = 1.0,
                    price = material.price,
                    amount = material.price
                )
                details.add(detail)
                adapter.notifyItemInserted(details.size - 1)
                updateTotals()
                Toast.makeText(this@SaleDeliveryActivity, "已添加: ${material.name}", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                // Fallback: add with barcode only if server query fails
                val detail = SaleOrderDetail(
                    materialCode = barcode,
                    materialName = "物料 ($barcode)",
                    qty = 1.0,
                    price = 0.0,
                    amount = 0.0
                )
                details.add(detail)
                adapter.notifyItemInserted(details.size - 1)
                updateTotals()
                Toast.makeText(this@SaleDeliveryActivity, "${error.message}，已添加条码", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteDetail(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setPositiveButton(R.string.yes) { _, _ ->
                details.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateTotals()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun updateTotals() {
        val totalQty = details.sumOf { it.qty }
        val totalAmount = details.sumOf { it.amount }
        binding.tvTotal.text = getString(
            R.string.label_total_qty, totalQty
        ) + " / " + getString(R.string.label_total_amount, totalAmount)
    }

    private fun saveOrder(submit: Boolean) {
        val customer = binding.etCustomer.text.toString().trim()
        val warehouse = binding.etWarehouse.text.toString().trim()

        if (details.isEmpty()) {
            Toast.makeText(this, "请至少添加一条明细", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@SaleDeliveryActivity)
                val request = SaleOrderRequest(
                    customerId = customer.ifEmpty { null },
                    warehouseId = warehouse.ifEmpty { null },
                    details = details
                )
                val response = api.addSaleOrder(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        this@SaleDeliveryActivity,
                        if (submit) "提交成功" else "保存成功",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (submit) finish()
                } else {
                    Toast.makeText(
                        this@SaleDeliveryActivity,
                        response.body()?.message ?: "操作失败",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SaleDeliveryActivity,
                    "网络错误: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
