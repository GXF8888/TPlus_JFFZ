package com.example.tplus_jffz.ui.updatepwd

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.UpdatePwdRequest
import com.example.tplus_jffz.databinding.ActivityUpdatePwdBinding
import kotlinx.coroutines.launch

class UpdatePwdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePwdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePwdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUpdatePwd.setOnClickListener { attemptUpdate() }
    }

    private fun attemptUpdate() {
        val oldPwd = binding.etOldPassword.text.toString()
        val newPwd = binding.etNewPassword.text.toString()
        val confirmPwd = binding.etConfirmPassword.text.toString()

        if (oldPwd.isEmpty()) {
            binding.tilOldPassword.error = "请输入原密码"
            return
        }
        if (newPwd.isEmpty()) {
            binding.tilNewPassword.error = "请输入新密码"
            return
        }
        if (newPwd != confirmPwd) {
            binding.tilConfirmPassword.error = "两次输入的密码不一致"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdatePwd.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@UpdatePwdActivity)
                // Note: userCode should come from logged-in user session
                val userCode = getUserCode() // Implement this method
                val request = UpdatePwdRequest(userCode, oldPwd, newPwd)
                val response = api.updatePassword(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@UpdatePwdActivity, "密码修改成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@UpdatePwdActivity,
                        response.body()?.message ?: "修改失败", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UpdatePwdActivity,
                    "网络错误: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnUpdatePwd.isEnabled = true
            }
        }
    }

    private fun getUserCode(): String {
        // TODO: Retrieve from shared preferences or session manager
        return "admin"
    }
}