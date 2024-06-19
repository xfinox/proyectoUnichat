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
import com.jose.appchat.glide.GlideApp
import com.jose.appchat.util.FirestoreUtil
import com.jose.appchat.util.StorageUtil
import java.io.ByteArrayOutputStream

class UserFragment : Fragment() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    private lateinit var editText_name: TextView
    private lateinit var editText_bio: TextView
    private lateinit var imageView_profile_picture: ImageView
    private lateinit var btn_save: Button
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        editText_name = view.findViewById(R.id.editText_name)
        editText_bio = view.findViewById(R.id.editText_bio)
        imageView_profile_picture = view.findViewById(R.id.imageView_profile_picture)
        btn_save = view.findViewById(R.id.btn_save)

        // Inicialmente deshabilitar el botón de guardar
        btn_save.isEnabled = false

        imageView_profile_picture.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
        }

        // Detectar cambios en nombre y bio
        editText_name.addTextChangedListener { enableSaveButton() }
        editText_bio.addTextChangedListener { enableSaveButton() }

        btn_save.setOnClickListener {
            btn_save.isEnabled = false // Deshabilitar el botón de guardar mientras se guarda
            if (::selectedImageBytes.isInitialized) {
                StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                    FirestoreUtil.updateCurrentUser(
                        editText_name.text.toString(),
                        editText_bio.text.toString(),
                        imagePath
                    ) {
                        // Mostrar mensaje de confirmación
                        Toast.makeText(requireContext(), "Se guardaron los cambios", Toast.LENGTH_SHORT).show()
                        pictureJustChanged = false
                        btn_save.isEnabled = true // Rehabilitar el botón de guardar después de guardar los cambios
                    }
                }
            } else {
                FirestoreUtil.updateCurrentUser(
                    editText_name.text.toString(),
                    editText_bio.text.toString(),
                    null
                ) {
                    // Mostrar mensaje de confirmación
                    Toast.makeText(requireContext(), "Se guardaron los cambios", Toast.LENGTH_SHORT).show()
                    btn_save.isEnabled = true // Rehabilitar el botón de guardar después de guardar los cambios
                }
            }
        }

        val btn_sign_out = view.findViewById<Button>(R.id.btn_sign_out)
        btn_sign_out.setOnClickListener {
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

            GlideApp.with(this).load(selectedImageBytes)
                .into(imageView_profile_picture)

            pictureJustChanged = true
            enableSaveButton() // Habilitar el botón de guardar al cambiar la imagen
        }
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if (isVisible) {
                editText_name.setText(user?.name)
                editText_bio.setText(user?.bio)

                if (!pictureJustChanged && user?.profilePicturePath != null) {
                    GlideApp.with(this)
                        .load(StorageUtil.pathToReference(user.profilePicturePath!!))
                        .placeholder(R.drawable.profile)
                        .into(imageView_profile_picture)
                }

                pictureJustChanged = false
            }
        }
    }

    private fun enableSaveButton() {
        btn_save.isEnabled = true
    }
}
