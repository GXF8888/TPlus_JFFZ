package com.example.tplus_jffz.ui.link

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.databinding.ActivityLinkBinding
import com.example.tplus_jffz.utils.HttpService
import kotlinx.coroutines.launch

class LinkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val savedUrl = RetrofitClient.getBaseUrl(this)
        val savedDb = RetrofitClient.getDatabaseName(this)
        val savedDbUser = RetrofitClient.getDatabaseUser(this)
        val savedDbPwd = RetrofitClient.getDatabasePassword(this)

        binding.etServerUrl.setText(savedUrl)
        binding.etDatabaseName.setText(savedDb)
        binding.etDbUser.setText(savedDbUser)
        binding.etDbPassword.setText(savedDbPwd)

        binding.btnTestConnection.setOnClickListener { testConnection() }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    private fun testConnection() {
        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }
        lifecycleScope.launch {
            val result = HttpService.post(this@LinkActivity, "/TPlus_JFFZ/unlogin", emptyMap())
            dialog.dismiss()
            val message = result?.Message ?: "未知错误"
            if (message == "null" || message == "nologin") {
                Toast.makeText(this@LinkActivity, "连接成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@LinkActivity, "连接失败: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveConfiguration() {
        val url = binding.etServerUrl.text.toString().trim()
        val dbName = binding.etDatabaseName.text.toString().trim()
        val dbUser = binding.etDbUser.text.toString().trim()
        val dbPwd = binding.etDbPassword.text.toString().trim()

        if (url.isEmpty()) {
            Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show()
            return
        }
        if (dbName.isEmpty()) {
            Toast.makeText(this, "请输入数据库账套", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.saveConfig(this, url, dbName, dbUser, dbPwd)
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        finish()
    }
}
