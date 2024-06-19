package com.jose.appchat.ui.Chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jose.appchat.recyclerview.item.ListaChatsAdapter

class ChatsViewModel : ViewModel() {

    private val _chatDataList = MutableLiveData<List<ListaChatsAdapter.ChatData>>()
    val chatDataList: LiveData<List<ListaChatsAdapter.ChatData>> get() = _chatDataList

    init {
        if (_chatDataList.value == null || _chatDataList.value!!.isEmpty()) {
            loadUserChats()
        }
    }

    private fun loadUserChats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val listaChatsAdapter = ListaChatsAdapter()

        listaChatsAdapter.verificarChatsUsuario { chatDataList ->
            _chatDataList.value = chatDataList
        }
    }
}
