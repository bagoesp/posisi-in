package com.bugs.posisiin01.activity.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugs.posisiin01.activity.KelolaActivity
import com.bugs.posisiin01.databinding.SampelItemBinding
import com.bugs.posisiin01.model.DataSampel

class SampelAdapter (
    private val activity: KelolaActivity,
    private val listSampel : List<DataSampel>
) : RecyclerView.Adapter<SampelAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SampelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listSampel[position])
    }

    override fun getItemCount(): Int = listSampel.size

    inner class ViewHolder(private val binding: SampelItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DataSampel) {
            binding.tvLabelSampel.text = activity.initLokasi(item.lokasi!!)
            binding.tvAp1Level.text = item.ap1.toString()
            binding.tvAp2Level.text = item.ap2.toString()
            binding.tvAp3Level.text = item.ap3.toString()
            binding.tvCreationTime.text = DateUtils.getRelativeTimeSpanString(item.waktu_input!!)

            binding.btnDeleteSampel.setOnClickListener {
                activity.dialogHapus(item)
            }
        }
    }
}