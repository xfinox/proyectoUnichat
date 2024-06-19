package com.jose.appchat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jose.appchat.databinding.ActivityMainBinding
import com.jose.appchat.ui.Chats.ChatsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)  // Configura la Toolbar como ActionBar

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_chats,
                R.id.navigation_groups,
                R.id.navigation_status,
                R.id.navigation_user
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Manejar el Intent cuando la actividad ya está en ejecución
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.hasExtra("showChatsFragment")) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, ChatsFragment())
                .commit()
        }
    }

}
