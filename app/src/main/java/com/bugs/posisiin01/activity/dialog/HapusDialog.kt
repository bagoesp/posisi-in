package com.bugs.posisiin01.activity.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.bugs.posisiin01.R

class HapusDialog() : DialogFragment(), View.OnClickListener {

    private lateinit var btnHapus: Button
    private lateinit var btnBatal: Button

    var listener: DeleteSampel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as DeleteSampel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
        val view = inflater.inflate(R.layout.hapus_dialog, container, false)
        btnHapus = view.findViewById(R.id.btn_hapus)
        btnBatal = view.findViewById(R.id.btn_batal_hapus)

        btnHapus.setOnClickListener(this)
        btnBatal.setOnClickListener(this)

        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            btnHapus.id -> {
                listener!!.deleteConfirm()
                dismiss()
            }

            btnBatal.id -> {
                dismiss()
            }
        }
    }
}

interface DeleteSampel {
    fun deleteConfirm()
}