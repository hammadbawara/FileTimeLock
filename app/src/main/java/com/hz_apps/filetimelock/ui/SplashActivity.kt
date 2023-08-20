package com.hz_apps.filetimelock.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.ui.files.FilesActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            launchApp()
        }

    }

    private suspend fun launchApp() {
        val intent = Intent(this, FilesActivity::class.java)
        startActivity(intent)
        finish()
    }
}