package com.jose.appchat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jose.appchat.util.FirestoreUtil

class SingninActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 1
    }

    private val signInProviders = listOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true)
            .build()
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singnin)

        val btnsesion = findViewById<Button>(R.id.btn_iniciarsesion)
        val correo = findViewById<TextView>(R.id.editText_Correo)
        val contraseña = findViewById<TextView>(R.id.editText_Contraseña)
        val btnSignInWithGoogle = findViewById<Button>(R.id.btnSignInWithGoogle)




        btnSignInWithGoogle.setOnClickListener {
            //para registrarse
            signInWithGoogleClicked(it)
        }
        btnsesion.setOnClickListener {
            val email = correo.text.toString()
            val password = contraseña.text.toString()

            // Verificar que los campos de correo y contraseña no estén vacíos
            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar("Por favor ingresa correo y contraseña.")
                return@setOnClickListener
            }

            // Intenta iniciar sesión con correo y contraseña usando Firebase Authentication
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = FirebaseAuth.getInstance().currentUser
                        // Aquí podrías manejar lo que sucede después de iniciar sesión correctamente
                        // Por ejemplo, navegar a la siguiente actividad o actualizar la IU
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    } else {
                        // If sign in fails, display a message to the user.
                        showSnackbar("Falló el inicio de sesión: ${task.exception?.message}")
                    }
                }
        }



    }

    private fun signInWithGoogleClicked(view: View) {

        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(signInProviders)
            .setLogo(R.drawable.ic_launcher)
            .build()
        startActivityForResult(intent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {


                FirestoreUtil.initCurrentUserIfFirstTime {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)




                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) {
                    return
                }

                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK -> {
                        showSnackbar("No hay conexión")
                    }
                    ErrorCodes.UNKNOWN_ERROR -> {
                        showSnackbar("Error desconocido")
                    }
                    else -> {
                        showSnackbar("Error desconocido")
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

}
