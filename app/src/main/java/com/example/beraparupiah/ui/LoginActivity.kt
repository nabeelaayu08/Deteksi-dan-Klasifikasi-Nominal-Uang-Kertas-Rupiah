package com.example.beraparupiah.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.beraparupiah.R
import com.example.beraparupiah.data.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton

class LoginActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var btnGoogleSignIn: SignInButton

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            authManager.handleSignInResult(
                task,
                onSuccess = { user ->
                    Toast.makeText(this, "Welcome ${user.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                },
                onFailure = { exception ->
                    Toast.makeText(
                        this,
                        "Sign in failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize AuthManager dengan Web Client ID dari google-services.json
        val webClientId = getString(R.string.default_web_client_id)
        authManager = AuthManager(this, webClientId)

        // Check if user already logged in
        if (authManager.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in)

        btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE)
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = authManager.getSignInIntent()
        signInLauncher.launch(signInIntent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}