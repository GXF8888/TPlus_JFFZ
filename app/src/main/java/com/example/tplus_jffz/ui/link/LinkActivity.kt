package com.example.tplus_jffz.ui.link

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.LinkRequest
import com.example.tplus_jffz.databinding.ActivityLinkBinding
import kotlinx.coroutines.launch

class LinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load saved URL
        val savedUrl = RetrofitClient.getBaseUrl(this)
        binding.etServerUrl.setText(savedUrl)

        binding.btnTestConnection.setOnClickListener { testConnection() }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    private fun testConnection() {
        val url = binding.etServerUrl.text.toString().trim()
        if (url.isEmpty()) {
            binding.tilServerUrl.error = "请输入服务器地址"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnTestConnection.isEnabled = false

        lifecycleScope.launch {
            try {
                // Temporarily set base URL for testing
                val testClient = RetrofitClient.getApi(this@LinkActivity)
                val response = testClient.checkLogin()

                if (response.isSuccessful) {
                    Toast.makeText(this@LinkActivity, "连接成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LinkActivity, "连接失败: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LinkActivity, "连接错误: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnTestConnection.isEnabled = true
            }
        }
    }

    private fun saveConfiguration() {
        val url = binding.etServerUrl.text.toString().trim()
        if (url.isEmpty()) {
            binding.tilServerUrl.error = "请输入服务器地址"
            return
        }

        RetrofitClient.saveBaseUrl(this, url)
        Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
}
