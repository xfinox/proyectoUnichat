package com.jose.appchat.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jose.appchat.R

class StatusFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var fabUploadStatus: FloatingActionButton
    private lateinit var progressBarUpload: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estados, container, false)



        return view
    }






}
