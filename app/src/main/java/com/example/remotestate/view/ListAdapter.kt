package com.example.remotestate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.remotestate.R
import com.example.remotestate.model.SaveData
import kotlinx.android.synthetic.main.item_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class ListAdapter(val context: Context, onListItemClick: OnListItemClick) :
    RecyclerView.Adapter<ListAdapter.DataViewHolder>() {

    private var performances: MutableList<SaveData>? = mutableListOf()
    private var onListItemClick: OnListItemClick? = null

    init {
        this.onListItemClick = onListItemClick
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(location: SaveData?) {
            val lat = "${context.getString(R.string.latitude)} ${location?.latitude?.toString()}"
            itemView.latTV?.text = lat
            val long = "${context.getString(R.string.longitude)} ${location?.longitude?.toString()}"
            itemView.longTV?.text = long
            val date = "${context.getString(R.string.time)} ${getDate(location?.timeStamp, "dd/MM/yyyy hh:mm:ss")}"
            itemView.timeTV?.text = date
            itemView.setOnClickListener {
                onListItemClick?.onListItemClicked(location)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_layout, parent,
                false
            )
        )

    override fun getItemCount(): Int = performances?.size ?: 0

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) =
        holder.bind(performances?.get(position))

    fun addData(list: MutableList<SaveData>) {
        performances?.clear()
        performances?.addAll(list)
    }

    fun getAdapterDataList(): List<*>? {
        return performances
    }

    fun getAdapterItem(): Any? {
        if (!performances.isNullOrEmpty()) {
            return performances?.get(0)
        }
        return null
    }

    fun getDate(milliSeconds: String?, dateFormat: String?): String? {
        val formatter = SimpleDateFormat(dateFormat, Locale.US)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds?.toLong() ?: 0L
        return formatter.format(calendar.time)
    }


    interface OnListItemClick {
        fun onListItemClicked(clickedItem: SaveData?)
    }
}