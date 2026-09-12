package com.example.tplus_jffz.ui.index

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tplus_jffz.R
import com.example.tplus_jffz.databinding.ActivityIndexBinding
import com.example.tplus_jffz.ui.rdrecord.RDRecordActivity
import com.example.tplus_jffz.ui.saledelivery.SaleDeliveryActivity
import com.example.tplus_jffz.ui.transvoucher.TransVoucherActivity
import com.example.tplus_jffz.ui.updatepwd.UpdatePwdActivity

class IndexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val menuItems = listOf(
            MenuItem(R.string.menu_sale_delivery, R.drawable.circle_background) {
                startActivity(Intent(this, SaleDeliveryActivity::class.java))
            },
            MenuItem(R.string.menu_rd_record, R.drawable.circle_background) {
                startActivity(Intent(this, RDRecordActivity::class.java))
            },
            MenuItem(R.string.menu_trans_voucher, R.drawable.circle_background) {
                startActivity(Intent(this, TransVoucherActivity::class.java))
            },
            MenuItem(R.string.menu_update_pwd, R.drawable.circle_background) {
                startActivity(Intent(this, UpdatePwdActivity::class.java))
            },
            MenuItem(R.string.menu_logout, R.drawable.circle_background) {
                logout()
            }
        )

        binding.recyclerMenu.layoutManager = LinearLayoutManager(this)
        binding.recyclerMenu.adapter = MenuAdapter(menuItems)
    }

    private fun logout() {
        // Clear local session and return to login
        finish()
    }

    data class MenuItem(
        val titleRes: Int,
        val iconRes: Int,
        val onClick: () -> Unit
    )
}