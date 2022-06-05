package com.bugs.posisiin01.activity.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bugs.posisiin01.R
import com.bugs.posisiin01.activity.KelolaActivity
import com.google.firebase.auth.FirebaseAuth

class LoginDialog: DialogFragment(), View.OnClickListener {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnBatalLogin: Button

    var listener: AutentikasiAdmin? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as AutentikasiAdmin
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
        val view = inflater.inflate(R.layout.login_dialog, container, false)
        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.et_password)
        btnLogin = view.findViewById(R.id.btn_login)
        btnBatalLogin = view.findViewById(R.id.btn_batal_login)

        btnLogin.setOnClickListener(this)
        btnBatalLogin.setOnClickListener(this)

        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btn_login -> {
                autentikasi()
            }

            R.id.btn_batal_login -> {
                dismiss()
            }
        }
    }

    private fun autentikasi(){
        val email = etEmail.text.toString().trim() { it <= ' ' }
        val password = etPassword.text.toString().trim() { it <= ' ' }
        listener!!.setEmailPassword(email, password)
        //clearForm()
        dismiss()
    }

    private fun clearForm() {
        etEmail.text.clear()
        etPassword.text.clear()
    }

}

interface AutentikasiAdmin {
    fun setEmailPassword(email:String, password: String)
}