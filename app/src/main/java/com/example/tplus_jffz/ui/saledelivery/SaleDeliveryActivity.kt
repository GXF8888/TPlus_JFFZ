package com.example.tplus_jffz.ui.saledelivery

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.data.model.AppConfig
import com.example.tplus_jffz.databinding.ActivitySaleDeliveryBinding
import com.example.tplus_jffz.ui.login.LoginActivity
import com.example.tplus_jffz.utils.HttpService
import com.example.tplus_jffz.utils.SoundManager
import kotlinx.coroutines.launch

class SaleDeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaleDeliveryBinding
    private var orderCodeArray = ""
    private val listYL = mutableListOf<Map<String, String>>()
    private val listWL = mutableListOf<Map<String, String>>()
    private val listWLYC = mutableListOf<Map<String, String>>()
    private var isTab1 = true // true=已录明细, false=未录明细

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SoundManager.init(this)

        binding.tab1.setOnClickListener { switchTab(true) }
        binding.tab2.setOnClickListener { switchTab(false) }
        
        binding.etOrderBar.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                getOrder(binding.etOrderBar.text.toString())
                true
            } else false
        }
        
        binding.btnSearchOrder.setOnClickListener {
            getOrder(binding.etOrderBar.text.toString())
        }
        
        binding.etInvBar.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                getInventory(binding.etInvBar.text.toString())
                true
            } else false
        }
        
        binding.btnSearchInv.setOnClickListener {
            getInventory(binding.etInvBar.text.toString())
        }
        
        binding.btnUpload.setOnClickListener { upload() }
        
        binding.listView.adapter = WLAdapter()
        switchTab(true)
    }

    private fun switchTab(tab1: Boolean) {
        isTab1 = tab1
        if (tab1) {
            binding.tab1.setBackgroundColor(getColor(R.color.tab_selected))
            binding.tab2.setBackgroundColor(getColor(R.color.tab_unselected))
            binding.listView.adapter = YLAdapter()
            refreshYL()
        } else {
            binding.tab1.setBackgroundColor(getColor(R.color.tab_unselected))
            binding.tab2.setBackgroundColor(getColor(R.color.tab_selected))
            binding.listView.adapter = WLAdapter()
            refreshWL()
        }
    }

    private fun getOrder(orderBar: String) {
        if (orderBar.isEmpty()) {
            Toast.makeText(this, "订单条码不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        if (isOrderExists(orderBar)) {
            binding.etOrderBar.setText("")
            Toast.makeText(this, "订单已引入！", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            val params = hashMapOf("OrderBar" to orderBar)
            val result = HttpService.post(this@SaleDeliveryActivity, "/TPlus_JFFZ/sa_getsaleorder", params)
            dialog.dismiss()
            handleOrderResult(result, orderBar)
        }
    }

    private fun isOrderExists(orderCode: String): Boolean {
        return listWL.any { it["OrderBar"] == orderCode }
    }

    private fun handleOrderResult(result: com.example.tplus_jffz.data.model.BaseResponse?, orderBar: String) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                val resultSet = result?.ResultSet ?: emptyList()
                for (i in resultSet.indices) {
                    val obj = resultSet[i]
                    if (i == 0) {
                        orderCodeArray = if (orderCodeArray.isEmpty()) {
                            "'${obj["VoucherCode"]}'"
                        } else {
                            "$orderCodeArray,'${obj["VoucherCode"]}'"
                        }
                    }
                    val map = hashMapOf(
                        "xuhao" to (listWL.size + 1).toString(),
                        "OrderBar" to (obj["OrderBar"] ?: ""),
                        "VoucherCode" to (obj["VoucherCode"] ?: ""),
                        "mxid" to (obj["mxid"] ?: ""),
                        "InvId" to (obj["InvId"] ?: ""),
                        "InvCode" to (obj["InvCode"] ?: ""),
                        "InvName" to (obj["InvName"] ?: ""),
                        "InvStd" to (obj["InvStd"] ?: ""),
                        "quantity" to (obj["quantity"] ?: ""),
                        "BaseUnitId" to (obj["BaseUnitId"] ?: ""),
                        "BaseUnitCode" to (obj["BaseUnitCode"] ?: ""),
                        "BaseUnitName" to (obj["BaseUnitName"] ?: ""),
                        "quantity2" to (obj["quantity2"] ?: ""),
                        "SubUnitId" to (obj["SubUnitId"] ?: ""),
                        "SubUnitCode" to (obj["SubUnitCode"] ?: ""),
                        "SubUnitName" to (obj["SubUnitName"] ?: ""),
                        "price" to (obj["price"] ?: ""),
                        "taxRate" to (obj["taxRate"] ?: ""),
                        "taxPrice" to (obj["taxPrice"] ?: ""),
                        "taxAmount" to (obj["taxAmount"] ?: "")
                    )
                    listWL.add(map)
                }
                refreshWL()
                binding.etOrderBar.setText("")
                binding.etOrderBar.requestFocus()
                SoundManager.playSuccess()
            }
            "nologin" -> showReloginDialog()
            else -> {
                SoundManager.playError()
                binding.etOrderBar.setText("")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getInventory(invBar: String) {
        if (invBar.isEmpty()) {
            Toast.makeText(this, "存货编码不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        if (listWL.isEmpty() && listWLYC.isEmpty()) {
            Toast.makeText(this, "无销售订单未录信息！", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            val params = hashMapOf("InvBar" to invBar)
            val result = HttpService.post(this@SaleDeliveryActivity, "/TPlus_JFFZ/st_currentstock", params)
            dialog.dismiss()
            handleInventoryResult(result)
        }
    }

    private fun handleInventoryResult(result: com.example.tplus_jffz.data.model.BaseResponse?) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                val resultSet = result?.ResultSet?.firstOrNull() ?: emptyMap()
                val invCode = resultSet["InvCode"] ?: ""
                
                // Find in listWL
                var found = false
                var wlItem: Map<String, String>? = null
                for (item in listWL) {
                    if (item["InvCode"] == invCode) {
                        wlItem = item
                        found = true
                        break
                    }
                }
                
                // Find in listWLYC if not in listWL
                if (!found) {
                    for (item in listWLYC) {
                        if (item["InvCode"] == invCode) {
                            wlItem = item
                            found = true
                            break
                        }
                    }
                }
                
                if (!found) {
                    SoundManager.playError()
                    Toast.makeText(this, "当前订单无该存货！", Toast.LENGTH_SHORT).show()
                    return
                }
                
                val voucherCode = wlItem!!["VoucherCode"] ?: ""
                val mxid = wlItem["mxid"] ?: ""
                val quantity = wlItem["quantity"] ?: ""
                val quantity2 = wlItem["quantity2"] ?: ""
                val price = wlItem["price"] ?: ""
                val taxRate = wlItem["taxRate"] ?: ""
                val taxPrice = wlItem["taxPrice"] ?: ""
                
                SoundManager.playSuccess()
                
                val batch = resultSet["batch"] ?: ""
                val baseQuantity: String
                val subQuantity: String
                if (batch.isEmpty()) {
                    baseQuantity = quantity
                    subQuantity = quantity2
                } else {
                    baseQuantity = resultSet["BaseQuantity"] ?: ""
                    subQuantity = resultSet["SubQuantity"] ?: ""
                }
                
                val dialog = ProgressDialog(this).apply {
                    setMessage("正在连接...")
                    setCancelable(false)
                    show()
                }
                
                lifecycleScope.launch {
                    val params = hashMapOf(
                        "OrderCodeArray" to orderCodeArray,
                        "VoucherCode" to voucherCode,
                        "mxid" to mxid,
                        "WhId" to (resultSet["WhId"] ?: ""),
                        "WhCode" to (resultSet["WhCode"] ?: ""),
                        "WhName" to (resultSet["WhName"] ?: ""),
                        "InvBar" to (resultSet["InvBar"] ?: ""),
                        "InvId" to (resultSet["InvId"] ?: ""),
                        "InvCode" to (resultSet["InvCode"] ?: ""),
                        "InvName" to (resultSet["InvName"] ?: ""),
                        "InvStd" to (resultSet["InvStd"] ?: ""),
                        "batch" to batch,
                        "BaseQuantity" to baseQuantity,
                        "SubQuantity" to subQuantity,
                        "price" to price,
                        "taxRate" to taxRate,
                        "taxPrice" to taxPrice
                    )
                    val addResult = HttpService.post(this@SaleDeliveryActivity, "/TPlus_JFFZ/sa_addsaleorder", params)
                    dialog.dismiss()
                    handleAddResult(addResult)
                }
            }
            "nologin" -> showReloginDialog()
            else -> {
                SoundManager.playError()
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleAddResult(result: com.example.tplus_jffz.data.model.BaseResponse?) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                refreshData()
                binding.etInvBar.setText("")
                binding.etInvBar.requestFocus()
                SoundManager.playSuccess()
            }
            "nologin" -> showReloginDialog()
            else -> {
                SoundManager.playError()
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun refreshData() {
        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }
        lifecycleScope.launch {
            val result = HttpService.post(this@SaleDeliveryActivity, "/TPlus_JFFZ/sa_getsaleorderinfo", emptyMap())
            dialog.dismiss()
            handleRefreshResult(result)
        }
    }

    private fun handleRefreshResult(result: com.example.tplus_jffz.data.model.BaseResponse?) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                orderCodeArray = result?.OrderCodeArray ?: ""
                listYL.clear()
                val resultSet = result?.ResultSet ?: emptyList()
                for (i in resultSet.indices.reversed()) {
                    val obj = resultSet[i]
                    listYL.add(hashMapOf(
                        "xuhao" to (i + 1).toString(),
                        "id" to (obj["id"] ?: ""),
                        "VoucherCode" to (obj["VoucherCode"] ?: ""),
                        "mxid" to (obj["mxid"] ?: ""),
                        "WhId" to (obj["WhId"] ?: ""),
                        "WhCode" to (obj["WhCode"] ?: ""),
                        "WhName" to (obj["WhName"] ?: ""),
                        "InvId" to (obj["InvId"] ?: ""),
                        "InvCode" to (obj["InvCode"] ?: ""),
                        "InvName" to (obj["InvName"] ?: ""),
                        "InvStd" to (obj["InvStd"] ?: ""),
                        "Batch" to (obj["Batch"] ?: ""),
                        "Quantity" to (obj["Quantity"] ?: ""),
                        "Quantity2" to (obj["Quantity2"] ?: ""),
                        "Price" to (obj["Price"] ?: ""),
                        "TaxRate" to (obj["TaxRate"] ?: ""),
                        "TaxPrice" to (obj["TaxPrice"] ?: "")
                    ))
                }
                
                listWL.clear()
                listWLYC.clear()
                val resultSet2 = result?.ResultSet2 ?: emptyList()
                for (obj in resultSet2) {
                    val map = hashMapOf(
                        "xuhao" to (listWL.size + 1).toString(),
                        "OrderBar" to (obj["OrderBar"] ?: ""),
                        "VoucherCode" to (obj["VoucherCode"] ?: ""),
                        "mxid" to (obj["mxid"] ?: ""),
                        "InvId" to (obj["InvId"] ?: ""),
                        "InvCode" to (obj["InvCode"] ?: ""),
                        "InvName" to (obj["InvName"] ?: ""),
                        "InvStd" to (obj["InvStd"] ?: ""),
                        "quantity" to (obj["quantity"] ?: ""),
                        "BaseUnitId" to (obj["BaseUnitId"] ?: ""),
                        "BaseUnitCode" to (obj["BaseUnitCode"] ?: ""),
                        "BaseUnitName" to (obj["BaseUnitName"] ?: ""),
                        "quantity2" to (obj["quantity2"] ?: ""),
                        "SubUnitId" to (obj["SubUnitId"] ?: ""),
                        "SubUnitCode" to (obj["SubUnitCode"] ?: ""),
                        "SubUnitName" to (obj["SubUnitName"] ?: ""),
                        "price" to (obj["price"] ?: ""),
                        "taxRate" to (obj["taxRate"] ?: ""),
                        "taxPrice" to (obj["taxPrice"] ?: ""),
                        "taxAmount" to (obj["taxAmount"] ?: "")
                    )
                    val qty = (obj["quantity"] ?: "0").toFloatOrNull() ?: 0f
                    if (qty > 0) {
                        listWL.add(map)
                    } else {
                        listWLYC.add(map)
                    }
                }
                
                if (isTab1) refreshYL() else refreshWL()
                binding.etInvBar.setText("")
                binding.etInvBar.requestFocus()
            }
            "nologin" -> showReloginDialog()
            else -> Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun upload() {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("是否上传？")
            .setNegativeButton("确定") { _, _ ->
                val dialog = ProgressDialog(this).apply {
                    setMessage("正在连接...")
                    setCancelable(false)
                    show()
                }
                lifecycleScope.launch {
                    val params = hashMapOf("type" to "销货单")
                    val result = HttpService.post(this@SaleDeliveryActivity, "/TPlus_JFFZ/upload", params)
                    dialog.dismiss()
                    val message = result?.Message ?: "未知错误"
                    when (message) {
                        "null" -> {
                            AlertDialog.Builder(this@SaleDeliveryActivity)
                                .setTitle("提示")
                                .setMessage("上传成功！")
                                .setNegativeButton("确定") { _, _ ->
                                    finish()
                                    startActivity(Intent(this@SaleDeliveryActivity, SaleDeliveryActivity::class.java))
                                }
                                .show()
                        }
                        "nologin" -> showReloginDialog()
                        else -> Toast.makeText(this@SaleDeliveryActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setPositiveButton("取消", null)
            .show()
    }

    private fun showReloginDialog() {
        SoundManager.playError()
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("当前账号在其他地方登陆，请重新登录！")
            .setNegativeButton("确定") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .show()
    }

    private fun refreshYL() {
        (binding.listView.adapter as? YLAdapter)?.notifyDataSetChanged()
    }

    private fun refreshWL() {
        (binding.listView.adapter as? WLAdapter)?.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("退出将会清除当前信息，请确认当前单据是否已上传！")
            .setNegativeButton("确定") { _, _ -> finish() }
            .setPositiveButton("取消", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }

    private inner class YLAdapter : BaseAdapter() {
        override fun getCount() = listYL.size
        override fun getItem(position: Int) = listYL[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_saledelivery_yl, parent, false)
            val item = listYL[position]
            view.findViewById<TextView>(R.id.tvXuhao).text = item["xuhao"]
            view.findViewById<TextView>(R.id.tvVoucherCode).text = item["VoucherCode"]
            view.findViewById<TextView>(R.id.tvInvCode).text = item["InvCode"]
            view.findViewById<TextView>(R.id.tvInvName).text = item["InvName"]
            view.findViewById<TextView>(R.id.tvQuantity).text = item["Quantity"]
            view.findViewById<TextView>(R.id.tvBatch).text = item["Batch"]
            view.findViewById<TextView>(R.id.tvWhName).text = item["WhName"]
            return view
        }
    }

    private inner class WLAdapter : BaseAdapter() {
        override fun getCount() = listWL.size
        override fun getItem(position: Int) = listWL[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_saledelivery_wl, parent, false)
            val item = listWL[position]
            view.findViewById<TextView>(R.id.tvXuhao).text = item["xuhao"]
            view.findViewById<TextView>(R.id.tvVoucherCode).text = item["VoucherCode"]
            view.findViewById<TextView>(R.id.tvInvCode).text = item["InvCode"]
            view.findViewById<TextView>(R.id.tvInvName).text = item["InvName"]
            view.findViewById<TextView>(R.id.tvQuantity).text = item["quantity"]
            view.findViewById<TextView>(R.id.tvUnit).text = item["BaseUnitName"]
            return view
        }
    }
}
