package com.jose.appchat

import ContactAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.model.Contact
import com.jose.appchat.ui.Chats.ChatLogActivity

class AddActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var contactList: MutableList<Contact>
    private var contactsRef: DatabaseReference? = null
    private var contactsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        database = FirebaseDatabase.getInstance().reference

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        contactList = mutableListOf()
        contactAdapter = ContactAdapter(contactList) { contact ->
            openChatLog(contact)
        }
        recyclerView.adapter = contactAdapter

        // Cargar la lista de contactos desde Firebase
        loadContacts()

        val btnCancel = findViewById<Button>(R.id.btn_cancelar)
        val btnAgregar = findViewById<Button>(R.id.btn_add)
        val editTextEmail = findViewById<EditText>(R.id.editText_Email)
        val editTextNombre = findViewById<EditText>(R.id.editTextText)

        btnCancel.setOnClickListener {
            finish()
        }

        // Obtener datos del Intent
        val userId = intent.getStringExtra("userId") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val userEmail = intent.getStringExtra("userEmail") ?: ""

        // Mostrar datos recibidos
        editTextEmail.setText(userEmail)
        editTextNombre.setText(userName)

        btnAgregar.setOnClickListener {
            val email = editTextEmail.text.toString()
            val nombre = editTextNombre.text.toString()

            if (email.isNotEmpty() && nombre.isNotEmpty()) {
                // Verificar si el correo está asociado a una cuenta en Firebase
                checkContactExists(userId, email, nombre)
            } else {
                Toast.makeText(this@AddActivity, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadContacts() {
        // Obtener el UID del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verificar que el usuario esté autenticado
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this@AddActivity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener la referencia a los contactos del usuario
        contactsRef = database.child("contacts").child(userId)

        // Escuchar cambios en los contactos
        contactsListener = contactsRef?.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()
                for (postSnapshot in snapshot.children) {
                    val contact = postSnapshot.getValue(Contact::class.java)
                    contact?.let {
                        contactList.add(it)
                    }
                }
                contactAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddActivity, "Error al cargar la lista de contactos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkContactExists(userId: String, email: String, nombre: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        contactsRef?.orderByChild("email")?.equalTo(email)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(this@AddActivity, "El contacto ya está en tu lista", Toast.LENGTH_SHORT).show()
                } else {
                    addContact(userId, email, nombre)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddActivity, "Error al verificar el contacto", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addContact(userId: String, email: String, nombre: String) {
        // Obtener la referencia a la base de datos "users"
        val usersRef = database.child("users")

        // Consultar si el correo proporcionado existe en "users"
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Obtener el UID del primer usuario encontrado (debería ser único por diseño)
                    val userSnapshot = snapshot.children.first()
                    val userId = userSnapshot.key
                    val profilePicturePath = userSnapshot.child("profilePicturePath").getValue(String::class.java)

                    // Verificar que el UID del usuario actual esté disponible
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId.isNullOrEmpty()) {
                        Toast.makeText(this@AddActivity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Verificar que no estás intentando agregarte a ti mismo como contacto
                    if (userId == currentUserId) {
                        Toast.makeText(this@AddActivity, "No puedes agregarte a ti mismo como contacto", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Crear el objeto Contact con nombre, correo, userId y profilePicturePath
                    val contact = Contact(userId!!, nombre, email, profilePicturePath)

                    // Agregar el contacto a la lista de contactos del usuario actual en "contacts"
                    val contactsRef = database.child("contacts").child(currentUserId)
                    val newContactId = contactsRef.push().key ?: ""

                    contactsRef.child(newContactId).setValue(contact)
                        .addOnSuccessListener {
                            Toast.makeText(this@AddActivity, "Contacto agregado exitosamente", Toast.LENGTH_SHORT).show()
                            sendBroadcastForNewChat()  // Enviar el broadcast aquí
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@AddActivity, "Error al agregar el contacto", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@AddActivity, "El correo proporcionado no existe", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddActivity, "Error al verificar el correo", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendBroadcastForNewChat() {
        val intent = Intent("com.jose.appchat.NEW_CHAT_CREATED")
        sendBroadcast(intent)
    }

    private fun openChatLog(contact: Contact) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatId = if (currentUserId < contact.userId) "$currentUserId-${contact.userId}" else "${contact.userId}-$currentUserId"

        val intent = Intent(this, ChatLogActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("userIdReceiver", contact.userId)
            putExtra("userIdSender", currentUserId)
            putExtra("userName", contact.nombre)  // Pasar el nombre del contacto
            putExtra("userEmail", contact.email)  // Pasar el email del contacto
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar el listener de Firebase para evitar errores al cerrar sesión
        contactsListener?.let { contactsRef?.removeEventListener(it) }
    }
}
