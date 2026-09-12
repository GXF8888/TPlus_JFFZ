package com.example.tplus_jffz.ui.rdrecord

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.data.model.BaseResponse
import com.example.tplus_jffz.databinding.ActivityRdRecordBinding
import com.example.tplus_jffz.ui.login.LoginActivity
import com.example.tplus_jffz.utils.HttpService
import com.example.tplus_jffz.utils.SoundManager
import kotlinx.coroutines.launch

class RDRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRdRecordBinding
    private var orderBarArray = ""
    private val listYL = mutableListOf<Map<String, String>>()
    private val listWL = mutableListOf<Map<String, String>>()
    private var isTab1 = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRdRecordBinding.inflate(layoutInflater)
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
        binding.btnSearchOrder.setOnClickListener { getOrder(binding.etOrderBar.text.toString()) }

        binding.etInvBar.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                getInventory(binding.etInvBar.text.toString())
                true
            } else false
        }
        binding.btnSearchInv.setOnClickListener { getInventory(binding.etInvBar.text.toString()) }

        binding.btnUpload.setOnClickListener { upload() }
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
            val result = HttpService.post(this@RDRecordActivity, "/TPlus_JFFZ/st_getrdrecord", hashMapOf("OrderBar" to orderBar))
            dialog.dismiss()
            handleOrderResult(result, orderBar)
        }
    }

    private fun isOrderExists(orderCode: String): Boolean {
        return listWL.any { it["OrderBar"] == orderCode }
    }

    private fun handleOrderResult(result: BaseResponse?, orderBar: String) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                val resultSet = result?.ResultSet ?: emptyList()
                for (i in resultSet.indices) {
                    val obj = resultSet[i]
                    if (i == 0) {
                        orderBarArray = if (orderBarArray.isEmpty()) {
                            "'${obj["VoucherCode"]}'"
                        } else {
                            "$orderBarArray,'${obj["VoucherCode"]}'"
                        }
                    }
                    listWL.add(hashMapOf(
                        "xuhao" to (listWL.size + 1).toString(),
                        "OrderBar" to (obj["OrderBar"] ?: ""),
                        "VoucherCode" to (obj["VoucherCode"] ?: ""),
                        "mxid" to (obj["mxid"] ?: ""),
                        "InvId" to (obj["InvId"] ?: ""),
                        "InvCode" to (obj["InvCode"] ?: ""),
                        "InvName" to (obj["InvName"] ?: ""),
                        "InvStd" to (obj["InvStd"] ?: ""),
                        "quantity" to (obj["quantity"] ?: ""),
                        "BaseUnitName" to (obj["BaseUnitName"] ?: ""),
                        "quantity2" to (obj["quantity2"] ?: ""),
                        "SubUnitName" to (obj["SubUnitName"] ?: ""),
                        "price" to (obj["price"] ?: ""),
                        "taxRate" to (obj["taxRate"] ?: ""),
                        "taxPrice" to (obj["taxPrice"] ?: "")
                    ))
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
        if (listWL.isEmpty()) {
            Toast.makeText(this, "无材料订单未录信息！", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }
        lifecycleScope.launch {
            val result = HttpService.post(this@RDRecordActivity, "/TPlus_JFFZ/st_currentstock", hashMapOf("InvBar" to invBar))
            dialog.dismiss()
            handleInventoryResult(result)
        }
    }

    private fun handleInventoryResult(result: BaseResponse?) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                val resultSet = result?.ResultSet?.firstOrNull() ?: emptyMap()
                val invCode = resultSet["InvCode"] ?: ""
                val wlItem = listWL.find { it["InvCode"] == invCode }
                if (wlItem == null) {
                    SoundManager.playError()
                    Toast.makeText(this, "当前订单无该存货！", Toast.LENGTH_SHORT).show()
                    return
                }

                val batch = resultSet["batch"] ?: ""
                val baseQuantity: String
                val subQuantity: String
                if (batch.isEmpty()) {
                    baseQuantity = wlItem["quantity"] ?: ""
                    subQuantity = wlItem["quantity2"] ?: ""
                } else {
                    baseQuantity = resultSet["BaseQuantity"] ?: ""
                    subQuantity = resultSet["SubQuantity"] ?: ""
                }

                SoundManager.playSuccess()

                val dialog = ProgressDialog(this).apply {
                    setMessage("正在连接...")
                    setCancelable(false)
                    show()
                }
                lifecycleScope.launch {
                    val params = hashMapOf(
                        "OrderBarArray" to orderBarArray,
                        "VoucherCode" to (wlItem["VoucherCode"] ?: ""),
                        "mxid" to (wlItem["mxid"] ?: ""),
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
                        "price" to (wlItem["price"] ?: ""),
                        "taxRate" to (wlItem["taxRate"] ?: ""),
                        "taxPrice" to (wlItem["taxPrice"] ?: "")
                    )
                    val addResult = HttpService.post(this@RDRecordActivity, "/TPlus_JFFZ/st_addrdrecord", params)
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

    private fun handleAddResult(result: BaseResponse?) {
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
            val result = HttpService.post(this@RDRecordActivity, "/TPlus_JFFZ/st_getrdrecordinfo", emptyMap())
            dialog.dismiss()
            handleRefreshResult(result)
        }
    }

    private fun handleRefreshResult(result: BaseResponse?) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                orderBarArray = result?.OrderCodeArray ?: ""
                listYL.clear()
                val resultSet = result?.ResultSet ?: emptyList()
                for (i in resultSet.indices.reversed()) {
                    val obj = resultSet[i]
                    listYL.add(hashMapOf(
                        "xuhao" to (i + 1).toString(),
                        "id" to (obj["id"] ?: ""),
                        "VoucherCode" to (obj["VoucherCode"] ?: ""),
                        "mxid" to (obj["mxid"] ?: ""),
                        "InvCode" to (obj["InvCode"] ?: ""),
                        "InvName" to (obj["InvName"] ?: ""),
                        "Quantity" to (obj["Quantity"] ?: ""),
                        "Batch" to (obj["Batch"] ?: ""),
                        "WhName" to (obj["WhName"] ?: "")
                    ))
                }
                listWL.clear()
                val resultSet2 = result?.ResultSet2 ?: emptyList()
                for (obj in resultSet2) {
                    listWL.add(hashMapOf(
                        "xuhao" to (listWL.size + 1).toString(),
                        "OrderBar" to (obj["OrderBar"] ?: ""),
                        "VoucherCode" to (obj["VoucherCode"] ?: ""),
                        "mxid" to (obj["mxid"] ?: ""),
                        "InvId" to (obj["InvId"] ?: ""),
                        "InvCode" to (obj["InvCode"] ?: ""),
                        "InvName" to (obj["InvName"] ?: ""),
                        "InvStd" to (obj["InvStd"] ?: ""),
                        "quantity" to (obj["quantity"] ?: ""),
                        "BaseUnitName" to (obj["BaseUnitName"] ?: ""),
                        "quantity2" to (obj["quantity2"] ?: ""),
                        "SubUnitName" to (obj["SubUnitName"] ?: ""),
                        "price" to (obj["price"] ?: ""),
                        "taxRate" to (obj["taxRate"] ?: ""),
                        "taxPrice" to (obj["taxPrice"] ?: "")
                    ))
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
                    val result = HttpService.post(this@RDRecordActivity, "/TPlus_JFFZ/upload", hashMapOf("type" to "材料出库单"))
                    dialog.dismiss()
                    val message = result?.Message ?: "未知错误"
                    when (message) {
                        "null" -> {
                            AlertDialog.Builder(this@RDRecordActivity)
                                .setTitle("提示")
                                .setMessage("上传成功！")
                                .setNegativeButton("确定") { _, _ ->
                                    finish()
                                    startActivity(Intent(this@RDRecordActivity, RDRecordActivity::class.java))
                                }
                                .show()
                        }
                        "nologin" -> showReloginDialog()
                        else -> Toast.makeText(this@RDRecordActivity, message, Toast.LENGTH_LONG).show()
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
            val view = convertView ?: layoutInflater.inflate(R.layout.item_rdrecord_yl, parent, false)
            val item = listYL[position]
            view.findViewById<TextView>(R.id.tvXuhao).text = item["xuhao"]
            view.findViewById<TextView>(R.id.tvVoucherCode).text = item["VoucherCode"]
            view.findViewById<TextView>(R.id.tvInvCode).text = item["InvCode"]
            view.findViewById<TextView>(R.id.tvInvName).text = item["InvName"]
            view.findViewById<TextView>(R.id.tvQuantity).text = item["Quantity"]
            view.findViewById<TextView>(R.id.tvBatch).text = item["Batch"]
            return view
        }
    }

    private inner class WLAdapter : BaseAdapter() {
        override fun getCount() = listWL.size
        override fun getItem(position: Int) = listWL[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_rdrecord_wl, parent, false)
            val item = listWL[position]
            view.findViewById<TextView>(R.id.tvXuhao).text = item["xuhao"]
            view.findViewById<TextView>(R.id.tvVoucherCode).text = item["VoucherCode"]
            view.findViewById<TextView>(R.id.tvInvCode).text = item["InvCode"]
            view.findViewById<TextView>(R.id.tvInvName).text = item["InvName"]
            view.findViewById<TextView>(R.id.tvQuantity).text = item["quantity"]
            return view
        }
    }
}