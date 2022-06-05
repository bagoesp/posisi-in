package com.bugs.posisiin01.activity.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.bugs.posisiin01.R
import com.bugs.posisiin01.activity.OnlineActivity
import com.bugs.posisiin01.model.Location

class TujuanDialog: DialogFragment(), View.OnClickListener {

    private lateinit var acTujuan: AutoCompleteTextView
    private lateinit var btnCariRute: Button
    private lateinit var btnBatal: Button

    var listener: SetTujuan? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as SetTujuan
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
        val view = inflater.inflate(R.layout.lokasi_tujuan_dialog, container, false)
        acTujuan = view.findViewById(R.id.ac_tujuan)
        btnCariRute = view.findViewById(R.id.btn_cari_rute)
        btnBatal = view.findViewById(R.id.btn_batal_tujuan)

        // adapter ac lokasi
        val arrayLokasi = resources.getStringArray(R.array.lokasi_tujuan)
        val lokasiAdapter = ArrayAdapter(requireContext(), R.layout.ac_lokasi_tujuan, arrayLokasi)
        acTujuan.setAdapter(lokasiAdapter)

        btnCariRute.setOnClickListener(this)
        btnBatal.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btn_cari_rute -> {
                listener!!.setTujuanValue(ambilLokasi())
                dismiss()
            }
            R.id.btn_batal_tujuan -> {
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun ambilLokasi() : Location? {
        return when (acTujuan.text.toString()) {
            resources.getString(R.string.r306) -> Location.R306
            resources.getString(R.string.r307) -> Location.R307
            resources.getString(R.string.r302) -> Location.R302
            resources.getString(R.string.r303) -> Location.R303
            resources.getString(R.string.r304) -> Location.R304
            resources.getString(R.string.r305) -> Location.R305
            resources.getString(R.string.aula) -> Location.AULA
            resources.getString(R.string.lobi) -> Location.LOB2
            resources.getString(R.string.tlpi) -> Location.TLPI
            resources.getString(R.string.tlpa) -> Location.TLPA
            else -> null
        }
    }

}

interface SetTujuan {
    fun setTujuanValue(lokasi: Location?)
}