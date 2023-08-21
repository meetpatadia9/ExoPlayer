package com.ipsmeet.exoplayer.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.ipsmeet.exoplayer.databinding.ActivityMainBinding
import com.ipsmeet.exoplayer.viewmodel.PermissionViewModel

@UnstableApi class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionModel: PermissionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        permissionModel.requestPermission(this)

        binding.layoutMusicPlayer.setOnClickListener {
            startActivity(
                Intent(
                    this, MusicListActivity::class.java
                )
            )
        }

        binding.layoutVideoPlayer.setOnClickListener {
            startActivity(
                Intent(
                    this, VideoFoldersActivity::class.java
                )
            )
        }
    }
}