package com.logicallynx.autoacta

import S3Comms
import S3Comms.fetchEmail
import S3Comms.fetchRemainingMinutesWithCallbacks
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.ContentAlpha
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.wear.compose.material.Text

import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.material.ChipColors
import androidx.wear.compose.material.ChipDefaults

@Composable
fun SaveAudioActivityBack(showContent: Boolean, onShowContentChange: (Boolean) -> Unit, audioFile: File) {


    Box() {
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

fun getAudioDuration(file: File): Long {
    return 10
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagInputField(title: String, onTagsChanged: (List<String>) -> Unit) {
    var text by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }

    onTagsChanged(tags)

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(0.89f)


    ){
        androidx.compose.material3.Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary // Ensure text color contrasts well with the background
        )
    }

    Spacer(modifier = Modifier.height(8.dp))


    TextField(
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
        value = text,
        onValueChange = { text = it },
        label = { Text(LocalContext.current.getString(R.string.s_tags)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            if (text.isNotEmpty()) {
                tags = tags + text
                text = "" // Clear the input field after adding the tag
                onTagsChanged(tags)
            }
        }),
        modifier = Modifier
            .fillMaxWidth()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                    if (text.isNotEmpty()) {
                        tags = tags + text
                        text = "" // Clear the input field after adding the tag
                        onTagsChanged(tags)
                        true
                    } else false
                } else false
            }
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Displaying tags as chips
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp),

    ) {
        tags.forEach { tag ->
            Chip(
                label = { Text(tag) },
                onClick = {
                    tags = tags.filterNot { it == tag }
                    onTagsChanged(tags)
                          }, // Remove tag on click
                colors = ChipDefaults.chipColors()
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }

}

@Composable
fun Chip(
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    colors: ChipColors = ChipDefaults.chipColors()
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            label()
        }
    }
}


@Composable
fun SaveAudioCategory(title: String, content: (String) -> Unit, defaultContent: String = "", fillBeforeMake: Boolean){
    var isValid by remember { mutableStateOf(fillBeforeMake) }



    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(0.89f)


    ){
        androidx.compose.material3.Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary // Ensure text color contrasts well with the background
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    var text = remember { mutableStateOf(defaultContent) }

    TextField(
        value = text.value,
        onValueChange = {
            isValid = it.isNotEmpty()
            text.value = it
            content(it)  // Callback invoked with the current text
        },
        isError = !isValid && fillBeforeMake,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.secondary),
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    )
    if (!isValid&& fillBeforeMake) {
        Text(text = LocalContext.current.getString(R.string.valid_text), color = Color.Red)
    }
    Spacer(modifier = Modifier.height(16.dp))



}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAudioActivity(
    onClose: () -> Unit,
    audioFile: File

) {
    
    // States for input fields
    var jobName by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var email by remember { mutableStateOf("") }

    val fillBeforeMake = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchEmail { result ->
            if (result != null) {
                email = result
            }
        }
    }

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
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 30.dp),
            contentAlignment = Alignment.TopCenter,


        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier

            ) {
                Box( modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(5.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center){
                    Text(
                        text = LocalContext.current.getString(R.string.mm_rec_fin),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        // Ensure text color contrasts well with the background
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))


                Column(modifier = Modifier.fillMaxWidth(fraction = 0.8f)){
                    SaveAudioCategory(title = LocalContext.current.getString(R.string.mm_sum_name), content = {newText -> jobName = newText}, fillBeforeMake = fillBeforeMake.value)
                    SaveAudioCategory(title = LocalContext.current.getString(R.string.mm_sum_desc), content = {newText -> jobDescription = newText}, fillBeforeMake = fillBeforeMake.value)
                    SaveAudioCategory(title = LocalContext.current.getString(R.string.mm_email_export), content = {newText -> email = newText}, defaultContent = email, fillBeforeMake = fillBeforeMake.value)
                    TagInputField(
                        title = "Tags",
                        onTagsChanged = { updatedTags ->
                            tags = updatedTags  // Update the state with new tags
                        }
                    )
                }











            }

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .padding(30.dp),
                contentAlignment = Alignment.BottomCenter,
            ){

                val snackbarHostState = remember { SnackbarHostState() }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                val context = LocalContext.current
                val (showConfirmDialog, setShowConfirmDialog) = remember { mutableStateOf(false) }


                SaveAndCloseButtons(
                    onMakeSummary = {
                        if(jobName == "" || jobDescription == "" || email == "" ) {
                            fillBeforeMake.value = true
                            Log.i("MakeSummaryButton", (jobName == "" || jobDescription == "" || email == "").toString())

                        }
                        else {


                            fetchRemainingMinutesWithCallbacks(object :
                                S3Comms.FetchMinutesCallback {
                                    override fun onSuccess(minutes: Int) {
                                        var remainingMinutes = minutes

                                        val durationMillis = getAudioDuration(audioFile)
                                        var durationMinutes =
                                            TimeUnit.MILLISECONDS.toMinutes(durationMillis).toInt()
                                        if (durationMinutes == 0) {
                                            durationMinutes = 1
                                        }

                                        remainingMinutes -= durationMinutes
                                        Log.i("DiscountMinutes", remainingMinutes.toString())

                                        // Update the minutes on the server
                                        S3Comms.updateRemainingMinutes(remainingMinutes)

                                        // Call your AWS upload function with the collected data
                                        S3Comms.uploadToS3(
                                            audioFile,
                                            jobName,
                                            jobDescription,
                                            email,
                                            tags
                                        )

                                        // Then close the composable
                                        onClose()


                                }

                                override fun onError(e: Exception) {
                                    // Handle error, possibly update UI to show error message
                                    Log.e("FetchError", "Error fetching minutes: ${e.message}")
                                }
                            })

                            Toast.makeText(context, context.getString(R.string.toast_onyourway), Toast.LENGTH_SHORT).show()

                        }

                    },
                    onDiscard = {
                        setShowConfirmDialog(true)
                    }
                )


                if (showConfirmDialog) {
                    ConfirmDiscardDialog(
                        onConfirm = {
                            onClose()  // Perform the close operation
                        },
                        onDismiss = {
                            setShowConfirmDialog(false)  // Simply close the dialog
                        }
                    )
                }
            }








        }


    }
}

@Composable
fun ConfirmDiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalContext.current.getString(R.string.mm_confirm_discard_title)) },
        text = { Text(LocalContext.current.getString(R.string.mm_confirm_discard_desc), color = MaterialTheme.colorScheme.primaryContainer) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(LocalContext.current.getString(R.string.discard))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(LocalContext.current.getString(R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.secondary

    )
}

@Composable
fun MakeSummaryNotification(){

}



data class LanguageOption(
    val code: String,
    val name: String
)

data class TranslateConfig(
    val source: String,
    val target: String
)
/**

@Composable
fun TranslateDialog(
    sourceLanguage: (LanguageOption) -> Unit,
    targetLanguage: (LanguageOption) -> Unit,
    showLangDialog: (Boolean) -> Unit
) {


    var sourceLanguageOrig = LanguageOption(Locale.getDefault().language,Locale.getDefault().displayName)
    var targetLanguageOrig = LanguageOption(Locale.getDefault().language,Locale.getDefault().displayName)


    Column(
        modifier = Modifier
            .zIndex(1f)
            .background(color = MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.Center,

        )
    {
        LanguageDropdown(onLanguageSelected = { newLanguage ->
            sourceLanguageOrig = newLanguage
        })
        LanguageDropdown(onLanguageSelected = { newLanguage ->
            targetLanguageOrig = newLanguage
        })

        SaveAndCloseTranslateButtons(
            onTranslate = {
                sourceLanguage(sourceLanguageOrig)
                targetLanguage(targetLanguageOrig)

                showLangDialog(false)
            },
            onDiscard = {
                showLangDialog(false)
            }
        )
    }


}

@Composable
fun TranslateButton(initialSourceLanguage: LanguageOption,
                    initialTargetLanguage: LanguageOption) {
    var showLangDialog by remember { mutableStateOf(false) }
    var sourceLanguage by remember { mutableStateOf(initialSourceLanguage) }
    var targetLanguage by remember { mutableStateOf(initialTargetLanguage) }



    Button(
        onClick = { showLangDialog = true},
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary) )
    {
        Text("Configure translation")
    }

    if(showLangDialog){
        Box(
            modifier = Modifier.fillMaxSize(), // This ensures the Box occupies the entire screen
            contentAlignment = Alignment.Center // This aligns the children to the center of the Box
        ){
            TranslateDialog(sourceLanguage = {sourceLanguage = it}, targetLanguage = {targetLanguage = it}, showLangDialog = {showLangDialog = it})
        }
    }
    Log.i("translate source", sourceLanguage.name)
    Log.i("translate target", targetLanguage.name)
}

@Composable
fun SaveAndCloseTranslateButtons(onTranslate: () -> Unit, onDiscard: () -> Unit) {    // Row container for horizontal alignment
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp), // Adjust padding as needed
        horizontalArrangement = Arrangement.SpaceBetween, // Adjust the arrangement as needed
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First Button
        Button(
            onClick = onTranslate,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f)) {
            Text("Set translate config")
        }

        // Spacer could be used for fixed spacing between buttons if needed
        // Spacer(modifier = Modifier.width(8.dp))

        // Second Button
        Button(
            onClick = onDiscard,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
            modifier = Modifier.weight(1f)) {
            Text("Discard")
        }

    }
}
**/
@Composable
fun SaveAndCloseButtons(onMakeSummary: () -> Unit, onDiscard: () -> Unit) {    // Row container for horizontal alignment
    Row(
        modifier = Modifier
            .fillMaxWidth(), // Adjust padding as needed
        horizontalArrangement = Arrangement.SpaceBetween, // Adjust the arrangement as needed
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First Button
        Button(
            onClick = onMakeSummary,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f)) {
            Text(LocalContext.current.getString(R.string.mm_make))
        }

        // Spacer could be used for fixed spacing between buttons if needed
        Spacer(modifier = Modifier.width(16.dp))

        // Second Button
        Button(
            onClick = onDiscard,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
            modifier = Modifier.weight(1f)) {
            Text(LocalContext.current.getString(R.string.discard))
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
    ){
        var exposedColors = ExposedDropdownMenuDefaults.textFieldColors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.Black,
            disabledContainerColor = Color.Black,
            focusedPlaceholderColor = Color.Black,
            unfocusedPlaceholderColor = Color.Black,
            unfocusedTextColor = Color.White,
            cursorColor = Color.Black,
            focusedLabelColor = Color.Black, // Color for the label when the TextField is focused
            unfocusedLabelColor = Color.Black.copy(alpha = ContentAlpha.disabled), // Color for the label when the TextField is unfocused,


        )
        var exposedColors2 = ExposedDropdownMenuDefaults.textFieldColors(
            Color.Black,
            Color.Black,
            Color.Black,Color.Black,Color.Black,Color.Black,Color.Black,Color.Black,Color.Black,Color.Black,

            // Color for the label when the TextField is unfocused,


        )

        var menuItemColors = MenuDefaults.itemColors(
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,



        )











        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {  }) {
            OutlinedTextField(
                value = folder,
                onValueChange = {},
                readOnly = true,
                label = { Text("Folder") },
                trailingIcon = {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropDown,
                            contentDescription = if (isExpanded) "Close menu" else "Open menu"
                        )
                    }
                },



                colors = exposedColors,
                modifier = Modifier
                    .menuAnchor()

            )
            ExposedDropdownMenu(

                modifier = Modifier.wrapContentWidth(),
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false })
            {
                folders.forEach { folderName ->
                    DropdownMenuItem(
                        text = { Text(folderName) },
                        onClick = {
                            folder = folderName
                            isExpanded = false
                        },
                        colors = menuItemColors
                    )
                }

            }

        }

        //Spacer(modifier = Modifier.height(16.dp))


        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add folder",
                tint = Color.White

                )
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            // Customize your dialog appearance here
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add a new folder")
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Folder Name") }
                )
                Spacer(Modifier.height(8.dp))
                Row {
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
                    Button(
                        onClick = {
                             // Reset for next use
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Cancel")
                    }
                }

            }
        }
    }
}

