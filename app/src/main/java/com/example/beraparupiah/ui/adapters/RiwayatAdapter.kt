package com.example.beraparupiah.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.beraparupiah.R
import com.example.beraparupiah.data.model.RiwayatDeteksi
import java.text.SimpleDateFormat
import java.util.*

class RiwayatAdapter(
    private val onInfoClick: (RiwayatDeteksi) -> Unit,
    private val onSelectionChanged: (Int) -> Unit  // Callback jumlah item selected
) : ListAdapter<RiwayatDeteksi, RiwayatAdapter.RiwayatViewHolder>(RiwayatDiffCallback()) {

    // ✅ State untuk selection mode
    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) {
                selectedItems.clear()
            }
            notifyDataSetChanged()
        }

    private val selectedItems = mutableSetOf<RiwayatDeteksi>()

    // ✅ Fungsi untuk get selected items
    fun getSelectedItems(): List<RiwayatDeteksi> = selectedItems.toList()

    // ✅ Fungsi untuk clear selection
    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            riwayat = item,
            isSelectionMode = isSelectionMode,
            isSelected = selectedItems.contains(item),
            onInfoClick = onInfoClick,
            onLongClick = {
                // ✅ Long press = aktifkan selection mode
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectedItems.add(item)
                    onSelectionChanged(selectedItems.size)
                    notifyDataSetChanged()
                }
            },
            onItemClick = {
                if (isSelectionMode) {
                    // ✅ Kalau selection mode aktif, toggle selection
                    if (selectedItems.contains(item)) {
                        selectedItems.remove(item)
                    } else {
                        selectedItems.add(item)
                    }
                    onSelectionChanged(selectedItems.size)
                    notifyItemChanged(position)
                }
            }
        )
    }

    class RiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_select)
        private val tvNominal: TextView = itemView.findViewById(R.id.tv_item_nominal)
        private val tvConfidence: TextView = itemView.findViewById(R.id.tv_item_confidence)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_item_date)
        private val imgInfo: ImageView = itemView.findViewById(R.id.img_arrow)

        fun bind(
            riwayat: RiwayatDeteksi,
            isSelectionMode: Boolean,
            isSelected: Boolean,
            onInfoClick: (RiwayatDeteksi) -> Unit,
            onLongClick: () -> Unit,
            onItemClick: () -> Unit
        ) {
            // Format data
            tvNominal.text = "Rp ${formatNominal(riwayat.nominal)}"
            val confidencePercent = (riwayat.confidence * 100).toInt()
            tvConfidence.text = "Confidence: $confidencePercent%"
            tvDate.text = formatDate(riwayat.timestamp)

            // ✅ Show/hide checkbox based on selection mode
            checkbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            checkbox.isChecked = isSelected

            // ✅ Hide info button saat selection mode
            imgInfo.visibility = if (isSelectionMode) View.GONE else View.VISIBLE

            // ✅ Long press listener
            itemView.setOnLongClickListener {
                onLongClick()
                true
            }

            // ✅ Click listener
            itemView.setOnClickListener {
                if (isSelectionMode) {
                    onItemClick()
                }
            }

            // ✅ Checkbox click
            checkbox.setOnClickListener {
                onItemClick()
            }

            // ✅ Info button click (hanya aktif kalau bukan selection mode)
            imgInfo.setOnClickListener {
                if (!isSelectionMode) {
                    onInfoClick(riwayat)
                }
            }
        }

        private fun formatNominal(nominal: String): String {
            return try {
                val number = nominal.toLong()
                String.format(Locale("id", "ID"), "%,d", number).replace(',', '.')
            } catch (e: Exception) {
                nominal
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            return sdf.format(Date(timestamp))
        }
    }

    class RiwayatDiffCallback : DiffUtil.ItemCallback<RiwayatDeteksi>() {
        override fun areItemsTheSame(oldItem: RiwayatDeteksi, newItem: RiwayatDeteksi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RiwayatDeteksi, newItem: RiwayatDeteksi): Boolean {
            return oldItem == newItem
        }
    }
}