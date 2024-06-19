package com.jose.appchat.ui.Chats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.jose.appchat.AddActivity
import com.jose.appchat.R
import com.jose.appchat.recyclerview.item.ChatsAdapter
import com.jose.appchat.recyclerview.item.ListaChatsAdapter

class ChatsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatsAdapter: ChatsAdapter
    private val chatDataList = mutableListOf<ListaChatsAdapter.ChatData>()
    private val TAG = "ChatsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        // Configurar el FloatingActionButton para agregar contactos
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.btn_floatAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(activity, AddActivity::class.java)
            startActivity(intent)
        }

        // Inicializar el RecyclerView
        recyclerView = view.findViewById(R.id.ViewChatsActivos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatsAdapter = ChatsAdapter(chatDataList)
        recyclerView.adapter = chatsAdapter

        // Cargar los chats del usuario
        loadUserChats()

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadUserChats() {
        // Obtener el UID del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verificar que el usuario esté autenticado
        if (userId.isNullOrEmpty()) {
            Toast.makeText(activity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener la referencia a los chats del usuario
        val listaChatsAdapter = ListaChatsAdapter()
        listaChatsAdapter.verificarChatsUsuario { chatDataList ->
            this.chatDataList.clear()
            this.chatDataList.addAll(chatDataList)
            chatsAdapter.notifyDataSetChanged()
        }
    }
}
