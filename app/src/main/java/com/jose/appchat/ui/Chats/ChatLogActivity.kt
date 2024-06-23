package com.jose.appchat.ui.Chats

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jose.appchat.AddActivity
import com.jose.appchat.MainActivity
import com.jose.appchat.R
import com.jose.appchat.recyclerview.item.ChatMessageAdapter

class ChatLogActivity : AppCompatActivity() {

    private lateinit var chatId: String
    private lateinit var userIdReceiver: String
    private lateinit var userIdSender: String
    private lateinit var userNombre: String
    private lateinit var userEmail: String
    private lateinit var currentUserId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var editTextMessage: EditText
    private lateinit var btnSend: Button
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var toolbar: Toolbar
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        // Obtener los datos del Intent
        chatId = intent.getStringExtra("chatId") ?: ""
        userIdReceiver = intent.getStringExtra("userIdReceiver") ?: ""
        userIdSender = intent.getStringExtra("userIdSender") ?: ""
        userNombre = intent.getStringExtra("userName") ?: ""
        userEmail = intent.getStringExtra("userEmail") ?: ""
        // Obtener el UID del usuario actualmente autenticado
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        Log.d(TAG, "UserName recibido en ChatLogActivity: $userNombre")
        Log.d(TAG, "UserEmail recibido en ChatLogActivity: $userEmail") // Log para verificar el email

        // Imprimir los IDs en la consola
        Log.d(TAG, "chatId recibido: $chatId")
        Log.d(TAG, "userIdReceiver recibido: $userIdReceiver")
        Log.d(TAG, "userIdSender recibido: $userIdSender")
        Log.d(TAG, "UID del usuario actual: $currentUserId")

        // Inicializar las vistas
        recyclerView = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        btnSend = findViewById(R.id.btnSend)
        toolbar = findViewById(R.id.toolbar)

        // Configurar el Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = userNombre // Configurar el nombre del contacto en el Toolbar

        // Configurar el RecyclerView
        setupRecyclerView()

        // Configurar el botón de envío
        btnSend.setOnClickListener {
            val messageText = editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                editTextMessage.setText("") // Limpiar el EditText después de enviar el mensaje
                scrollToLastMessage()
            }
        }

        // Detectar cuando el teclado se muestra
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (isKeyboardVisible()) {
                scrollToLastMessage()
            }
        }

        // Verificar y actualizar la foto del contacto al abrir el chat
        checkAndUpdateContactProfilePicture(userIdReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat_log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToChatsFragment()
                true
            }
            R.id.action_add_contact -> {
                openAddContactActivity(userIdReceiver, userNombre, userEmail)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToChatsFragment() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun openAddContactActivity(userId: String, userName: String, userEmail: String) {
        val intent = Intent(this, AddActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("userName", userName)
            putExtra("userEmail", userEmail)
        }
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        messageAdapter = ChatMessageAdapter(chatId, currentUserId, userIdReceiver)
        layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.apply {
            layoutManager = this@ChatLogActivity.layoutManager
            adapter = messageAdapter
        }

        // Desplazar al último mensaje cuando se actualicen los datos
        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                scrollToLastMessage()
            }
        })
    }

    private fun sendMessage(messageText: String) {
        messageAdapter.sendMessage(messageText)
        // Desplazar al último mensaje
        scrollToLastMessage()
    }

    private fun checkAndUpdateContactProfilePicture(userId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userRef = database.child("users").child(userId)
        val contactRef = database.child("contacts").child(currentUserId)

        userRef.child("profilePicturePath").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                val userProfilePicturePath = userSnapshot.getValue(String::class.java)

                contactRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(contactSnapshot: DataSnapshot) {
                        for (contact in contactSnapshot.children) {
                            val contactUserId = contact.child("userId").getValue(String::class.java)
                            if (contactUserId == userId) {
                                val contactProfilePicturePath = contact.child("profilePicturePath").getValue(String::class.java)
                                if (userProfilePicturePath != contactProfilePicturePath) {
                                    contact.ref.child("profilePicturePath").setValue(userProfilePicturePath)
                                }
                                break
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar error de base de datos si es necesario
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error de base de datos si es necesario
            }
        })
    }

    private fun isKeyboardVisible(): Boolean {
        val rootView = findViewById<View>(android.R.id.content)
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keypadHeight = screenHeight - rect.bottom
        return keypadHeight > screenHeight * 0.15 // Umbral arbitrario para determinar si el teclado está visible
    }

    private fun scrollToLastMessage() {
        recyclerView.post {
            layoutManager.scrollToPositionWithOffset(messageAdapter.itemCount - 1, editTextMessage.height)
        }
    }

    private fun openChatLog(contactUserId: String, contactUserName: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatId = if (currentUserId < contactUserId) "$currentUserId-$contactUserId" else "$contactUserId-$currentUserId"

        val intent = Intent(this, ChatLogActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("userIdReceiver", contactUserId)
            putExtra("userIdSender", currentUserId)
            putExtra("userName", contactUserName) // Pasar el nombre del usuario al ChatLogActivity
        }
        startActivity(intent)
    }

}
