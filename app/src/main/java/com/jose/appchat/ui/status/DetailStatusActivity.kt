package com.jose.appchat.ui.status

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jose.appchat.R
import com.jose.appchat.model.State
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailStatusActivity : AppCompatActivity() {

    private lateinit var viewPagerStatusDetail: ViewPager2
    private lateinit var statusAdapter: DetailAdapterStatus
    private lateinit var statusList: MutableList<State>
    private val TAG = "DetailStatusActivity"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_status)

        val userId = intent.getStringExtra("USER_ID")
        val username = intent.getStringExtra("USERNAME")

        val imageViewProfile = findViewById<ImageView>(R.id.imageViewProfile)
        val textViewUsername = findViewById<TextView>(R.id.textViewUsername)
        val textViewStatusTime = findViewById<TextView>(R.id.textViewStatusTime)

        textViewUsername.text = username

        viewPagerStatusDetail = findViewById(R.id.viewPagerStatusDetail)
        statusList = mutableListOf()
        statusAdapter = DetailAdapterStatus(statusList)
        viewPagerStatusDetail.adapter = statusAdapter

        viewPagerStatusDetail.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val state = statusList[position]
                val formattedTime = formatTimestamp(state.timestamp)
                textViewStatusTime.text = formattedTime

                // Cargar la foto de perfil correspondiente
                Log.d(TAG, "Selected state userId: ${state.userId}")
                loadProfileImage(state.userId, imageViewProfile)
            }
        })

        if (userId != null) {
            loadUserStates(userId)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
                "Hoy, ${sdf.format(calendar.time)}"
            }
            calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
                "Ayer, ${sdf.format(calendar.time)}"
            }
            else -> {
                val fullSdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                fullSdf.format(calendar.time)
            }
        }
    }

    private fun loadUserStates(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/states/$userId")
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                statusList.clear()
                snapshot.children.forEach { stateSnapshot ->
                    val state = stateSnapshot.getValue(State::class.java)
                    if (state != null && currentTime - state.timestamp <= twentyFourHoursInMillis) {
                        statusList.add(state)
                    }
                }
                statusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load states", error.toException())
            }
        })
    }

    private fun loadProfileImage(userId: String, imageView: ImageView) {
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImagePath = snapshot.child("profilePicturePath").value.toString()
                Log.d(TAG, "Profile image path for userId $userId: $profileImagePath")
                if (profileImagePath.isNotEmpty()) {
                    val storageRef = FirebaseStorage.getInstance().reference.child(profileImagePath)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val profileImageUrl = uri.toString()
                        Log.d(TAG, "Profile image URL for userId $userId: $profileImageUrl")
                        Glide.with(this@DetailStatusActivity).load(profileImageUrl).transform(
                            CircleCrop()
                        ).into(imageView)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get download URL", exception)
                    }
                } else {
                    Log.e(TAG, "Profile image path is empty for userId $userId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load profile image", error.toException())
            }
        })
    }

}
