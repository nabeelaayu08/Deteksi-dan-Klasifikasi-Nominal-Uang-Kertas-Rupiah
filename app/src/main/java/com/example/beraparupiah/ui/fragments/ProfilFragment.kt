package com.example.beraparupiah.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.beraparupiah.R
import com.example.beraparupiah.data.auth.AuthManager
import com.example.beraparupiah.ui.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ProfilFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var panduanLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        loadUserData()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        imgProfile = view.findViewById(R.id.img_profile)
        tvName = view.findViewById(R.id.tv_name)
        tvEmail = view.findViewById(R.id.tv_email)
        btnLogout = view.findViewById(R.id.btn_logout)
        panduanLayout = view.findViewById(R.id.panduan)
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            // Set name
            tvName.text = user.displayName ?: "User"

            // Set email
            tvEmail.text = user.email ?: ""

            // Load profile photo dengan Glide
            val photoUrl = user.photoUrl

            if (photoUrl != null) {
                // Ada foto dari Google
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_myplaces) // Placeholder Android default
                    .into(imgProfile)
            } else {
                // Tidak ada foto, gunakan placeholder
                imgProfile.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // Tambahkan listener untuk Panduan Aplikasi
        panduanLayout.setOnClickListener {
            showPanduanDialog()
        }
    }

    private fun showPanduanDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_panduan_aplikasi, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Setup tombol close
        dialogView.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        val webClientId = getString(R.string.default_web_client_id)
        val authManager = AuthManager(requireActivity(), webClientId)

        authManager.signOut {
            // Navigate to login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}