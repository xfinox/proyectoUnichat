package com.jose.appchat.ui.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jose.appchat.R
import com.jose.appchat.SingninActivity
import com.jose.appchat.util.FirestoreUtil
import com.jose.appchat.util.StorageUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import java.io.ByteArrayOutputStream

class UserFragment : Fragment() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    private lateinit var editTextName: EditText
    private lateinit var editTextBio: EditText
    private lateinit var imageViewProfilePicture: ImageView
    private lateinit var btnSave: Button
    private var originalName: String = ""
    private var originalBio: String = ""
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        editTextName = view.findViewById(R.id.editText_name)
        editTextBio = view.findViewById(R.id.editText_bio)
        imageViewProfilePicture = view.findViewById(R.id.imageView_profile_picture)
        btnSave = view.findViewById(R.id.btn_save)

        // Inicialmente ocultar el botón de guardar
        btnSave.visibility = View.GONE

        imageViewProfilePicture.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
        }

        btnSave.setOnClickListener {
            btnSave.isEnabled = false // Deshabilitar el botón de guardar mientras se guarda
            if (::selectedImageBytes.isInitialized) {
                StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                    FirestoreUtil.updateCurrentUser(
                        editTextName.text.toString(),
                        editTextBio.text.toString(),
                        imagePath
                    ) {
                        // Mostrar mensaje de confirmación
                        Toast.makeText(requireContext(), "Se guardaron los cambios", Toast.LENGTH_SHORT).show()
                        pictureJustChanged = false
                        btnSave.isEnabled = true // Rehabilitar el botón de guardar después de guardar los cambios
                        btnSave.visibility = View.GONE // Ocultar el botón de guardar
                    }
                }
            } else {
                FirestoreUtil.updateCurrentUser(
                    editTextName.text.toString(),
                    editTextBio.text.toString(),
                    null
                ) {
                    // Mostrar mensaje de confirmación
                    Toast.makeText(requireContext(), "Se guardaron los cambios", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true // Rehabilitar el botón de guardar después de guardar los cambios
                    btnSave.visibility = View.GONE // Ocultar el botón de guardar
                }
            }
        }

        val btnSignOut = view.findViewById<Button>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Signing out...")
            progressDialog.show()

            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener {
                    progressDialog.dismiss()

                    // Redirigir al usuario a SignInActivity y limpiar el backstack
                    val intent = Intent(requireContext(), SingninActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish() // Finalizar la actividad actual para que no se pueda volver atrás
                }
        }

        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            Glide.with(this).load(selectedImageBytes)
                .transform(CircleCrop())
                .into(imageViewProfilePicture)

            pictureJustChanged = true
            enableSaveButton() // Habilitar el botón de guardar al cambiar la imagen
        }
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if (isVisible) {
                originalName = user?.name.orEmpty()
                originalBio = user?.bio.orEmpty()

                editTextName.setText(originalName)
                editTextBio.setText(originalBio)

                if (!pictureJustChanged && user?.profilePicturePath != null) {
                    Glide.with(this)
                        .load(StorageUtil.pathToReference(user.profilePicturePath!!))
                        .placeholder(R.drawable.profile)
                        .transform(CircleCrop())
                        .into(imageViewProfilePicture)
                }

                pictureJustChanged = false

                // Detectar cambios en nombre y bio después de inicializar los valores originales
                editTextName.addTextChangedListener { enableSaveButton() }
                editTextBio.addTextChangedListener { enableSaveButton() }
            }
        }
    }

    private fun enableSaveButton() {
        val nameChanged = editTextName.text.toString() != originalName
        val bioChanged = editTextBio.text.toString() != originalBio
        val imageChanged = pictureJustChanged

        if (nameChanged || bioChanged || imageChanged) {
            btnSave.visibility = View.VISIBLE
        } else {
            btnSave.visibility = View.GONE
        }
    }
}
