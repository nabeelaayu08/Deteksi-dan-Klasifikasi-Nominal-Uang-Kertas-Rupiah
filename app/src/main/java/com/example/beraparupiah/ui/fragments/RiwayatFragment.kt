package com.example.beraparupiah.ui.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beraparupiah.R
import com.example.beraparupiah.data.model.RiwayatDeteksi
import com.example.beraparupiah.ui.adapters.RiwayatAdapter
import com.example.beraparupiah.viewmodel.DeteksiViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RiwayatFragment : Fragment() {

    private lateinit var viewModel: DeteksiViewModel
    private lateinit var adapter: RiwayatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    // ✅ Action bar views
    private lateinit var cardActionBar: MaterialCardView
    private lateinit var tvSelectedCount: TextView
    private lateinit var btnCancelSelection: ImageView
    private lateinit var btnDeleteSelected: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_riwayat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_riwayat)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        cardActionBar = view.findViewById(R.id.card_action_bar)
        tvSelectedCount = view.findViewById(R.id.tv_selected_count)
        btnCancelSelection = view.findViewById(R.id.btn_cancel_selection)
        btnDeleteSelected = view.findViewById(R.id.btn_delete_selected)

        setupRecyclerView()
        setupViewModel()
        setupActionBar()
        setupBackPress()
    }

    private fun setupRecyclerView() {
        adapter = RiwayatAdapter(
            onInfoClick = { riwayat ->
                // ✅ Hanya aktif kalau bukan selection mode
                if (!adapter.isSelectionMode) {
                    showDetailDialog(riwayat)
                }
            },
            onSelectionChanged = { count ->
                // ✅ Update UI saat selection berubah
                updateSelectionUI(count)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RiwayatFragment.adapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[DeteksiViewModel::class.java]

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            viewModel.getRiwayatByUser(it).observe(viewLifecycleOwner) { riwayatList ->
                if (riwayatList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    layoutEmptyState.visibility = View.GONE
                    adapter.submitList(riwayatList)
                }
            }
        }
    }

    private fun setupActionBar() {
        // ✅ Cancel selection
        btnCancelSelection.setOnClickListener {
            exitSelectionMode()
        }

        // ✅ Delete selected items
        btnDeleteSelected.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    // ✅ Handle back press saat selection mode aktif
    private fun setupBackPress() {
        val callback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                exitSelectionMode()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Update callback enabled state based on selection mode
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                callback.isEnabled = adapter.isSelectionMode
            }
        })
    }

    // ✅ Update UI berdasarkan jumlah item selected
    private fun updateSelectionUI(count: Int) {
        if (count > 0) {
            cardActionBar.visibility = View.VISIBLE
            tvSelectedCount.text = "$count item dipilih"
        } else {
            // Kalau tidak ada yang dipilih, keluar dari selection mode
            exitSelectionMode()
        }
    }

    // ✅ Keluar dari selection mode
    private fun exitSelectionMode() {
        adapter.clearSelection()
        cardActionBar.visibility = View.GONE
    }

    // ✅ Konfirmasi delete
    private fun showDeleteConfirmation() {
        val selectedItems = adapter.getSelectedItems()
        val count = selectedItems.size

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus $count riwayat deteksi?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteSelectedItems(selectedItems)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ✅ Delete items
    private fun deleteSelectedItems(items: List<RiwayatDeteksi>) {
        viewModel.deleteMultiple(items)
        exitSelectionMode()

        // Optional: Show toast
        // Toast.makeText(requireContext(), "${items.size} item berhasil dihapus", Toast.LENGTH_SHORT).show()
    }

    // Dialog detail (sama seperti sebelumnya)
    private fun showDetailDialog(riwayat: RiwayatDeteksi) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_detail_riwayat, null)

        val imgMoney = dialogView.findViewById<ImageView>(R.id.img_detail_money)
        val tvNominal = dialogView.findViewById<TextView>(R.id.tv_detail_nominal)
        val tvConfidence = dialogView.findViewById<TextView>(R.id.tv_detail_confidence)
        val tvConfidenceLabel = dialogView.findViewById<TextView>(R.id.tv_confidence_label)
        val tvDateTime = dialogView.findViewById<TextView>(R.id.tv_detail_datetime)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btn_close)

        tvNominal.text = "Rp ${formatNominal(riwayat.nominal)}"

        val confidencePercent = (riwayat.confidence * 100).toInt()
        tvConfidence.text = "$confidencePercent%"

        tvConfidenceLabel.text = when {
            confidencePercent >= 80 -> "Sangat Yakin"
            confidencePercent >= 60 -> "Yakin"
            else -> "Kurang Yakin"
        }

        tvDateTime.text = formatDateTime(riwayat.timestamp)

        if (!riwayat.imagePath.isNullOrEmpty() && File(riwayat.imagePath).exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(riwayat.imagePath)
                imgMoney.setImageBitmap(bitmap)
                imgMoney.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                imgMoney.setImageResource(android.R.drawable.ic_secure)
                imgMoney.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        } else {
            imgMoney.setImageResource(android.R.drawable.ic_secure)
            imgMoney.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun formatNominal(nominal: String): String {
        return try {
            val number = nominal.toLong()
            String.format(Locale("id", "ID"), "%,d", number).replace(',', '.')
        } catch (e: Exception) {
            nominal
        }
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}