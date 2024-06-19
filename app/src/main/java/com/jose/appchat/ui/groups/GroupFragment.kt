package com.jose.appchat.ui.groups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jose.appchat.MainActivity
import com.jose.appchat.R

class GroupFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)
        // Configura el FloatingActionButton para agregar contactos
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.btn_floatGroup)
        fabAdd.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        return view
    }


}