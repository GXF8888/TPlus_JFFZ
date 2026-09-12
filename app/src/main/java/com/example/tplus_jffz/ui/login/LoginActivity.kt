package com.example.tplus_jffz.ui.login

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.AppConfig
import com.example.tplus_jffz.data.model.BaseResponse
import com.example.tplus_jffz.databinding.ActivityLoginBinding
import com.example.tplus_jffz.ui.index.IndexActivity
import com.example.tplus_jffz.ui.link.LinkActivity
import com.example.tplus_jffz.utils.HttpService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val PREFS_NAME = "login_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        loadSavedCredentials()
        
        binding.ivLogo.setOnClickListener {
            startActivity(Intent(this, LinkActivity::class.java))
        }
        
        binding.btnLogin.setOnClickListener { attemptLogin() }
    }
    
    private fun loadSavedCredentials() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUser = prefs.getString("user_code", "")
        val remember = prefs.getBoolean("remember_password", false)
        binding.etUserCode.setText(savedUser)
        binding.cbRemember.isChecked = remember
    }

    private fun attemptLogin() {
        val userCode = binding.etUserCode.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (userCode.isEmpty()) {
            Toast.makeText(this, "用户编码不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        
        val url = RetrofitClient.getBaseUrl(this)
        val dbCode = RetrofitClient.getDatabaseUser(this)
        val dbPwd = RetrofitClient.getDatabasePassword(this)
        val dbName = RetrofitClient.getDatabaseName(this)
        
        AppConfig.URL = url
        AppConfig.DBCODE = dbCode
        AppConfig.DBPWD = dbPwd
        AppConfig.DBNAME = dbName
        
        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            val params = hashMapOf(
                "DBCode" to dbCode,
                "DBPwd" to dbPwd,
                "DBName" to dbName,
                "UserCode" to userCode,
                "UserPwd" to password,
                "MUTEX" to ""
            )
            val result = HttpService.post(this@LoginActivity, "/TPlus_JFFZ/login", params)
            dialog.dismiss()
            handleLoginResult(result, userCode, password)
        }
    }

    private fun handleLoginResult(result: BaseResponse?, userCode: String, password: String) {
        val message = result?.Message ?: "未知错误"
        when (message) {
            "null" -> {
                AppConfig.USERCODE = result?.ResultSet?.firstOrNull()?.get("UserCode") ?: userCode
                AppConfig.MUTEX = result?.ResultSet?.firstOrNull()?.get("Mutex") ?: ""
                AppConfig.USERNAME = result?.ResultSet?.firstOrNull()?.get("UserName") ?: ""
                
                if (binding.cbRemember.isChecked) {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                        .putString("user_code", userCode)
                        .putBoolean("remember_password", true)
                        .apply()
                }
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, IndexActivity::class.java))
                finish()
            }
            "new" -> {
                showFirstTimePwdDialog(userCode)
            }
            "nologin" -> {
                AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("当前用户已登录！")
                    .setNegativeButton("重新登录") { _, _ ->
                        lifecycleScope.launch {
                            val params = hashMapOf(
                                "DBCode" to AppConfig.DBCODE,
                                "DBPwd" to AppConfig.DBPWD,
                                "DBName" to AppConfig.DBNAME,
                                "UserCode" to userCode,
                                "UserPwd" to password,
                                "MUTEX" to ""
                            )
                            HttpService.post(this@LoginActivity, "/TPlus_JFFZ/unlogin", params)
                            attemptLogin()
                        }
                    }
                    .setPositiveButton("取消", null)
                    .show()
            }
            else -> {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showFirstTimePwdDialog(userCode: String) {
        val editText = android.widget.EditText(this)
        AlertDialog.Builder(this)
            .setTitle("首次登录请修改密码")
            .setView(editText)
            .setPositiveButton("取消", null)
            .setNegativeButton("确定") { _, _ -> }
            .create().apply {
                setCancelable(false)
                show()
                getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                    val newPwd = editText.text.toString()
                    if (newPwd.isEmpty()) {
                        Toast.makeText(context, "密码不能为空！", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    lifecycleScope.launch {
                        val params = hashMapOf(
                            "DBCode" to AppConfig.DBCODE,
                            "DBPwd" to AppConfig.DBPWD,
                            "DBName" to AppConfig.DBNAME,
                            "UserCode" to userCode,
                            "UserPwd" to newPwd,
                            "Type" to "ONE"
                        )
                        val result = HttpService.post(context as LoginActivity, "/TPlus_JFFZ/updateuserpwd", params)
                        if (result?.Message == "null") {
                            dismiss()
                            Toast.makeText(context, "修改成功，请重新登录", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, result?.Message ?: "修改失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
}
