package com.bugs.posisiin01.activity.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.bugs.posisiin01.R

class SimpanDialog: DialogFragment(), View.OnClickListener {

    private lateinit var btnSimpan: Button
    private lateinit var btnBatalSimpan: Button

    var listener: SimpanSampel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as SimpanSampel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
        val view = inflater.inflate(R.layout.simpan_dialog, container, false)

        btnSimpan = view.findViewById(R.id.btn_simpan)
        btnBatalSimpan = view.findViewById(R.id.btn_batal_simpan)

        btnBatalSimpan.setOnClickListener(this)
        btnSimpan.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.btn_simpan -> {
                listener!!.simpanConfirm()
                dismiss()
            }
            R.id.btn_batal_simpan -> {
                dismiss()
            }
        }
    }
}

interface SimpanSampel{
    fun simpanConfirm()
}