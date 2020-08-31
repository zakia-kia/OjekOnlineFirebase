package com.zakia.idn.ojekonlinefirebase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zakia.idn.ojekonlinefirebase.R
import com.zakia.idn.ojekonlinefirebase.model.Booking
import kotlinx.android.synthetic.main.history_item.view.*

class HistoryAdapter (
    private val mValues : List<Booking>
): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    //manggil layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mValues.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.mAwal.text = item.lokasiAwal
        holder.mTanggal.text = item.tanggal
        holder.mTujuan.text = item.lokasiTujuan
    }

    inner class ViewHolder (mView : View) : RecyclerView.ViewHolder(mView) {
        val mAwal : TextView = mView.tv_item1
        var mTujuan : TextView = mView.tv_item_tujuan
        var mTanggal : TextView = mView.item_tanggal

    }


}