package com.example.autoacta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.ContentAlpha
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun SaveAudioActivityBack(showContent: Boolean, onShowContentChange: (Boolean) -> Unit, audioFile: File) {


    Box(modifier = Modifier.fillMaxSize()) {
        // Your app's content goes here
        // ...

        if (showContent) {
            // Full-screen semi-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        onClick = { /* Absorb clicks */ },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            SaveAudioActivity(onClose = { onShowContentChange(false) }, audioFile)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAudioActivity(onClose: () -> Unit, audioFile: File) {
    
    // States for input fields
    var jobName by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var actors by remember { mutableStateOf("") }

    val isVisible = remember { mutableStateOf(false) }

    // Example of making the Box visible after a delay (e.g., 1000 milliseconds = 1 second)
    // This coroutine launches when AnimatedScreenFillingBox is first composed.
    // Remember to import kotlinx.coroutines.delay
    LaunchedEffect(key1 = true) {
        delay(0) // Delay in milliseconds
        isVisible.value = true // Makes the Box visible
    }

    AnimatedVisibility(
        visible = isVisible.value,
        enter = scaleIn( // Enter animation that scales the Box in from 0 to its full size
            // You can adjust these parameters to change the animation's behavior
            initialScale = 0.0f, // Start fully scaled down
            animationSpec = tween(durationMillis = 500), // Duration of the animation
            // Center the scaling origin
            transformOrigin = TransformOrigin.Center
        ),
        exit = scaleOut( // Enter animation that scales the Box in from 0 to its full size
            // You can adjust these parameters to change the animation's behavior
            animationSpec = tween(durationMillis = 500), // Duration of the animation
            // Center the scaling origin
            transformOrigin = TransformOrigin.Center
        )
    ) {

        Box(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 150.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(color = MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,

        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)

            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiary, // Use a theme-appropriate color
                            shape = RoundedCornerShape(8.dp) // Rounded corners for the background
                        )
                        .padding(horizontal = 110.dp, vertical = 4.dp) // Add some padding around the text
                ) {
                    Text(
                        text = "New Record",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer // Ensure text color contrasts well with the background
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                var outlinedColors = OutlinedTextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary, // Color for the border when the TextField is focused
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled), // Color for the border when the TextField is unfocused
                    focusedLabelColor = MaterialTheme.colorScheme.secondary, // Color for the label when the TextField is focused
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled), // Color for the label when the TextField is unfocused
                )

                // Record Title Input
                OutlinedTextField(
                    colors = outlinedColors,
                    value = jobName,
                    onValueChange = { jobName = it },
                    label = { Text("Summary title") },
                    singleLine = true,

                    )
                Spacer(modifier = Modifier.height(8.dp))

                // Notes Input
                OutlinedTextField(
                    colors = outlinedColors,
                    value = jobDescription,
                    onValueChange = { jobDescription = it },
                    label = { Text("Summary description") },
                    modifier = Modifier.height(150.dp), // Adjustable based on needs
                    singleLine = false,
                    maxLines = 3 // Expandable up to 5 lines before scrolling
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Actors Input
                OutlinedTextField(
                    colors = outlinedColors,
                    value = actors,
                    onValueChange = { actors = it },
                    label = { Text("Actors") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))



                FoldersDropdown()

                Spacer(modifier = Modifier.height(16.dp))

                SaveAndCloseButtons(
                    onMakeSummary = {
                        // Call your AWS upload function with the collected data
                        S3Comms.uploadToS3(audioFile, jobName, jobDescription, actors)




                        // Then close the composable
                        onClose()
                    },
                    onDiscard = onClose
                )            }
        }
    }
}

@Composable
fun SaveAndCloseButtons(onMakeSummary: () -> Unit, onDiscard: () -> Unit) {    // Row container for horizontal alignment
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp), // Adjust padding as needed
        horizontalArrangement = Arrangement.SpaceBetween, // Adjust the arrangement as needed
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First Button
        Button(
            onClick = onMakeSummary,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.weight(1f)) {
            Text("Make summary")
        }

        // Spacer could be used for fixed spacing between buttons if needed
        // Spacer(modifier = Modifier.width(8.dp))

        // Second Button
        Button(
            onClick = onDiscard,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.weight(1f)) {
            Text("Discard")
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersDropdown(){
    var isExpanded by remember { mutableStateOf(false) }
    var folder by remember { mutableStateOf("Default") }
    var folders by remember { mutableStateOf(listOf("Default")) }
    var showDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = {  }) {
            TextField(
                value = folder,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropDown,
                            contentDescription = if (isExpanded) "Close menu" else "Open menu"
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false })
            {
                folders.forEach { folderName ->
                    DropdownMenuItem(
                        text = { Text(folderName) },
                        onClick = {
                            folder = folderName
                            isExpanded = false
                        }
                    )
                }

            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showDialog = true }) {
            Text("Add Folder")
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            // Customize your dialog appearance here
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add a new folder")
                TextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Folder Name") }
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank() && newFolderName !in folders) {
                            folders = folders + newFolderName
                            folder = newFolderName // Optionally set the new folder as the selected one
                        }
                        newFolderName = "" // Reset for next use
                        showDialog = false
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}

