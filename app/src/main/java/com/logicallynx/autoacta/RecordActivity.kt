package com.logicallynx.autoacta

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.logicallynx.autoacta.ui.theme.AutoActaTheme

class RecordActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val audioFilePath = intent.getStringExtra("audioFilePath")
        enableEdgeToEdge()
        setContent {
            AutoActaTheme {
                PlayAudioButton(audioFilePath)
            }
        }
    }

    @Composable
    fun PlayAudioButton(audioFilePath: String?) {
        Box(
            contentAlignment = Alignment.Center, // Align the button to the center of the Box
            modifier = Modifier.fillMaxSize() // Box occupies the entire screen
        ) {
            Button(onClick = {
                playAudio(audioFilePath)
            }) {
                Text("Play Audio")
            }
        }
    }

    private fun playAudio(audioFilePath: String?) {
        if (audioFilePath != null) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
