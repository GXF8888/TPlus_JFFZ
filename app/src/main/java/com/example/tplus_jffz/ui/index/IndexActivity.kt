package com.example.tplus_jffz.ui.index

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.data.model.AppConfig
import com.example.tplus_jffz.ui.login.LoginActivity
import com.example.tplus_jffz.ui.rdrecord.RDRecordActivity
import com.example.tplus_jffz.ui.saledelivery.SaleDeliveryActivity
import com.example.tplus_jffz.ui.updatepwd.UpdatePwdActivity
import com.example.tplus_jffz.utils.HttpService
import kotlinx.coroutines.launch

class IndexActivity : AppCompatActivity() {
    private val icons = intArrayOf(R.drawable.xhd, R.drawable.clckd, R.drawable.updatepwd, R.drawable.tuichu)
    private val names = arrayOf("销售出库", "材料出库", "修改密码", "注销")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        
        val gridView = findViewById<GridView>(R.id.gridView)
        gridView.adapter = MenuAdapter()
        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> startActivity(Intent(this, SaleDeliveryActivity::class.java))
                1 -> startActivity(Intent(this, RDRecordActivity::class.java))
                2 -> startActivity(Intent(this, UpdatePwdActivity::class.java))
                3 -> logout()
            }
        }
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("是否注销登录？")
            .setNegativeButton("确定") { _, _ ->
                val dialog = ProgressDialog(this).apply {
                    setMessage("正在连接...")
                    setCancelable(false)
                    show()
                }
                lifecycleScope.launch {
                    val result = HttpService.post(this@IndexActivity, "/TPlus_JFFZ/logout", emptyMap())
                    dialog.dismiss()
                    val message = result?.Message ?: "未知错误"
                    if (message == "null") {
                        startActivity(Intent(this@IndexActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@IndexActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setPositiveButton("取消", null)
            .show()
    }

    private inner class MenuAdapter : BaseAdapter() {
        override fun getCount() = names.size
        override fun getItem(position: Int) = position
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = layoutInflater.inflate(R.layout.item_index_menu, parent, false)
            view.findViewById<ImageView>(R.id.image).setImageResource(icons[position])
            view.findViewById<TextView>(R.id.text).text = names[position]
            return view
        }
    }
}
