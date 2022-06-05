package com.bugs.posisiin01.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bugs.posisiin01.databinding.ActivityPengaturanBinding

class PengaturanActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityPengaturanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengaturanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKelolaPengaturan.setOnClickListener(this)
        binding.btnTambahPengaturan.setOnClickListener(this)
        binding.btnSsidPengaturan.setOnClickListener(this)
        binding.btnBackPengaturan.setOnClickListener(this)
        binding.btnKvaluePengaturan.setOnClickListener(this)

        showSnack()
    }

    override fun onClick(v: View) {
        when(v.id) {
            binding.btnKelolaPengaturan.id -> {
                val intent = Intent(this, KelolaActivity::class.java)
                startActivity(intent)
            }
            binding.btnTambahPengaturan.id -> {
                val intent = Intent(this, TambahActivity::class.java)
                startActivity(intent)
            }
            binding.btnSsidPengaturan.id -> {
                val intent = Intent(this, SsidActivity::class.java)
                startActivity(intent)
            }
            binding.btnBackPengaturan.id -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            binding.btnKvaluePengaturan.id -> {
                val intent = Intent(this, KvalueActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showSnack() {
        showSnackBarCustom("Berhasil login", false)
    }
}