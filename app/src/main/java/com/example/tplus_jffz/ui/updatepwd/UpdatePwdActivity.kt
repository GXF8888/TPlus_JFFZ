package com.example.tplus_jffz.ui.updatepwd

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.R
import com.example.tplus_jffz.data.model.AppConfig
import com.example.tplus_jffz.databinding.ActivityUpdatePwdBinding
import com.example.tplus_jffz.ui.login.LoginActivity
import com.example.tplus_jffz.utils.HttpService
import kotlinx.coroutines.launch

class UpdatePwdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdatePwdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePwdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnUpdate.setOnClickListener { updatePassword() }
    }

    private fun updatePassword() {
        val oldPwd = binding.etOldPwd.text.toString()
        val newPwd = binding.etNewPwd.text.toString()
        val confirmPwd = binding.etConfirmPwd.text.toString()

        if (oldPwd.isEmpty()) {
            Toast.makeText(this, "原密码不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPwd.isEmpty()) {
            Toast.makeText(this, "新密码不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        if (confirmPwd.isEmpty()) {
            Toast.makeText(this, "确认密码不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPwd != confirmPwd) {
            Toast.makeText(this, "新密码与确认密码不一致！", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ProgressDialog(this).apply {
            setMessage("正在连接...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            val params = hashMapOf(
                "Type" to "TWO",
                "UserPwd" to oldPwd,
                "newPwd" to newPwd
            )
            val result = HttpService.post(this@UpdatePwdActivity, "/TPlus_JFFZ/updateuserpwd", params)
            dialog.dismiss()
            val message = result?.Message ?: "未知错误"
            when (message) {
                "null" -> {
                    AlertDialog.Builder(this@UpdatePwdActivity)
                        .setTitle("提示")
                        .setMessage("修改成功，请重新登录！")
                        .setNegativeButton("确定") { _, _ ->
                            startActivity(Intent(this@UpdatePwdActivity, LoginActivity::class.java))
                            finish()
                        }
                        .show()
                }
                "nologin" -> showReloginDialog()
                else -> Toast.makeText(this@UpdatePwdActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showReloginDialog() {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("当前账号在其他地方登陆，请重新登录！")
            .setNegativeButton("确定") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .show()
    }
}
