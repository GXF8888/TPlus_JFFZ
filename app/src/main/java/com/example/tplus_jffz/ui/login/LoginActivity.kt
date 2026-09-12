package com.example.tplus_jffz.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.LoginRequest
import com.example.tplus_jffz.databinding.ActivityLoginBinding
import com.example.tplus_jffz.ui.index.IndexActivity
import com.example.tplus_jffz.ui.link.LinkActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val PREFS_NAME = "login_prefs"
    private val KEY_USER_CODE = "user_code"
    private val KEY_REMEMBER = "remember_password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedCredentials()
        setupListeners()
    }

    private fun loadSavedCredentials() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUser = prefs.getString(KEY_USER_CODE, "")
        val remember = prefs.getBoolean(KEY_REMEMBER, false)
        binding.etUserCode.setText(savedUser)
        binding.cbRemember.isChecked = remember
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnServerConfig.setOnClickListener {
            startActivity(Intent(this, LinkActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val userCode = binding.etUserCode.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (userCode.isEmpty()) {
            binding.tilUserCode.error = "请输入用户名"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "请输入密码"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@LoginActivity)
                val database = RetrofitClient.getDatabaseName(this@LoginActivity)
                if (database.isNullOrEmpty()) {
                    Toast.makeText(this@LoginActivity, "请先在服务器设置中填写数据库账套", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val request = LoginRequest(
                    userCode = userCode,
                    password = password,
                    database = database,
                    dbName = database,
                    accId = database
                )
                val response = api.login(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    saveCredentials(userCode)
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, IndexActivity::class.java))
                    finish()
                } else {
                    val body = response.body()
                    val errorMsg = body?.message
                        ?: body?.serverMessage
                        ?: if (response.isSuccessful) "登录失败" else "服务器返回错误 (HTTP ${response.code()})"
                    Toast.makeText(
                        this@LoginActivity,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "网络错误: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun saveCredentials(userCode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USER_CODE, if (binding.cbRemember.isChecked) userCode else "")
            putBoolean(KEY_REMEMBER, binding.cbRemember.isChecked)
            apply()
        }
    }
}
