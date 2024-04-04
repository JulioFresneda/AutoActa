package com.example.autoacta

import S3Comms.listSummaries
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JobItem(jobConfig: S3Comms.JobConfig, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Text(text = jobConfig.jobName)
            Text(text = jobConfig.language)
        }
    }
}

@Composable
fun summariesPage(){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val summaries = remember { mutableStateListOf<S3Comms.JobConfig>() }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedSummary by remember { mutableStateOf<S3Comms.JobConfig?>(null) }




    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                val fetchedSummaries = listSummaries(context)
                summaries.clear()
                summaries.addAll(fetchedSummaries)
                isLoading = false
            } catch (e: Exception) {
                // Handle any errors appropriately
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(summaries) { summary ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))

                        .clickable {
                            selectedSummary = summary
                            showDialog = true
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp) // Center content horizontally
                    )  {
                        Text(
                            text = "Job Name: ${summary.jobName}"

                        )
                        Text(
                            text = "Language: ${summary.language}"
                        )
                    }
                }
            }
        }


        if (isLoading) {
            var progress by remember { mutableFloatStateOf(0.1F) }

            LaunchedEffect(key1 = Unit) {
                for (i in 1..100) {
                    progress = i.toFloat()/100F
                    delay(50)
                }
            }

            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .align(Alignment.Center),
                color = Color.Gray,
                strokeWidth = 5.dp,
            )
        }
    }

    if (showDialog && selectedSummary != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Summary Details") },
            text = {
                Column {
                    Text("Job Name: ${selectedSummary?.jobName}")
                    Text("Description: ${selectedSummary?.description}")
                    Text("Actors: ${selectedSummary?.actors}")
                    Text("Language: ${selectedSummary?.language}")
                    Text("Audio Filename: ${selectedSummary?.audioFilename}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}