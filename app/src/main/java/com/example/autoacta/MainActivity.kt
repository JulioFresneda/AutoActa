package com.example.autoacta

import S3Comms
import S3Comms.listSummaries
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.autoacta.ui.theme.AutoActaTheme
import java.io.File
import java.io.IOException

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.amplifyframework.ui.authenticator.forms.FieldKey

import com.amplifyframework.ui.authenticator.ui.Authenticator
import com.amplifyframework.ui.authenticator.ui.SignInFooter
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.lazy.items

// Docx
// TODO - Cuadrar informes
// Backend
// TODO - Mejorar prompt chatgpt
// TODO - Añadir etapa translate
// Frontend
// TODO - App: Añadir opciones exportacion
// TODO - App: Exportar al email




data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File

    private var showMakeSummaryDialog by mutableStateOf(false)

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }





        setContent {
            Authenticator(
                signInContent = { state ->
                    val scope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier
                            .fillMaxSize() // Fill the max size of the parent
                            .padding(16.dp), // Add padding around the Column
                        verticalArrangement = Arrangement.Center, // Center items vertically
                        horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
                    ) {
                        val email = state.form.fields[FieldKey.Email]!!.state.content
                        val password = state.form.fields[FieldKey.Password]!!.state.content

                        // Use a nicer looking TextField, Material Design by default
                        OutlinedTextField(
                            value = email,
                            onValueChange = { newValue ->
                                // Ensure the new value is updated properly in your state management
                                state.form.fields[FieldKey.Email]!!.state.content = newValue
                            },
                            label = { Text("Email") },
                            singleLine = true, // Makes the TextField a single line input
                            modifier = Modifier.fillMaxWidth(0.8f) // Use 80% of the width
                        )

                        Spacer(modifier = Modifier.height(16.dp)) // Add space between the text fields

                        OutlinedTextField(
                            value = password,
                            onValueChange = { newValue ->
                                // Ensure the new value is updated properly in your state management
                                state.form.fields[FieldKey.Password]!!.state.content = newValue
                            },
                            label = { Text("Password") },
                            singleLine = true, // Makes the TextField a single line input
                            visualTransformation = PasswordVisualTransformation(), // Hides the password input
                            modifier = Modifier.fillMaxWidth(0.8f) // Use 80% of the width
                        )

                        Spacer(modifier = Modifier.height(24.dp)) // Add space between the TextField and Button

                        Button(
                            onClick = { scope.launch { state.signIn() } },
                            modifier = Modifier.align(Alignment.CenterHorizontally) // Center the button
                        ) {
                            Text("Sign In")
                        }

                        // If you have a footer, you can place it here,
                        // or consider customizing it further as needed.
                        SignInFooter(state)


                        Button(
                            onClick = { SetUpLogin()},
                            modifier = Modifier.align(Alignment.CenterHorizontally) // Center the button
                        ) {
                            Text("Sign In With Google")
                        }
                    }
                }
            )
            { state ->
                AutoActaTheme {
                    MainUI()
                    if (showMakeSummaryDialog) {
                        // Show the full-screen composable when requested
                        SaveAudioActivityBack(
                            showContent = showMakeSummaryDialog,
                            onShowContentChange = { showMakeSummaryDialog = it },
                            audioFile = audioFile)
                    }


                }
            }
        }
    }


    // Sets up login using AWS Amplify with Google as the social sign-in provider.
    fun SetUpLogin() {
        Amplify.Auth.fetchAuthSession(
            { result ->
                if (!result.isSignedIn) {
                    // No user is signed in, proceed with the sign-in process
                    try {
                        Amplify.Auth.signInWithSocialWebUI(
                            AuthProvider.google(),
                            this,
                            {
                                // Handle successful sign-in
                                val intent = intent
                                finish()  // Finish the current activity
                                startActivity(intent)  // Restart the activity
                            },
                            {
                                // Handle sign-in failure
                                Log.e("AuthQuickstart", "Sign in failed", it)
                            }
                        )
                    } catch (error: AmplifyException) {
                        // Log initialization failure
                        Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
                    }
                } else {
                    // User is already signed in, handle accordingly
                    Log.i("MyAmplifyApp", "User is already signed in.")
                    // Here, you can redirect the user to the main activity or refresh the current activity
                }
            },
            { error ->
                // Handle error in fetching the auth session
                Log.e("MyAmplifyApp", "Error fetching auth session", error)
            }
        )
    }




    @Composable
    fun MainUI(){
        val navController = rememberNavController()
        val items = listOf(
            NavigationItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            NavigationItem(
                title = "Summaries",
                selectedIcon = Icons.Filled.Call,
                unselectedIcon = Icons.Outlined.Call,
            ),
            NavigationItem(
                title = "Premium plan",
                selectedIcon = Icons.Filled.Star,
                unselectedIcon = Icons.Outlined.Star,
            ),
            NavigationItem(
                title = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
            ),
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var selectedItemIndex by rememberSaveable {
                mutableStateOf(0)
            }
            // Define your custom colors here
            val customColors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primary, // Custom color when item is selected
                unselectedContainerColor = Color.Transparent, // Typically transparent for unselected
                selectedIconColor = MaterialTheme.colorScheme.tertiary, // Custom icon color when item is selected
                unselectedIconColor = MaterialTheme.colorScheme.secondary, // Custom icon color when item is unselected
                selectedTextColor = Color.White, // Custom text color when item is selected
                unselectedTextColor = Color.DarkGray, // Custom text color when item is unselected
                selectedBadgeColor = MaterialTheme.colorScheme.secondary, // Custom badge color when item is selected
                unselectedBadgeColor = MaterialTheme.colorScheme.secondary // Custom badge color when item is unselected
            )


            ModalNavigationDrawer(
                drawerContent = {

                    ModalDrawerSheet {

                        Spacer(modifier = Modifier.height(16.dp))
                        items.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                colors = customColors,
                                label = {
                                    Text(text = item.title)
                                },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    navController.navigate(item.title)
                                    selectedItemIndex = index
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = if (index == selectedItemIndex) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary // Navy blue when selected
                                    )
                                },
                                badge = {
                                    item.badgeCount?.let {
                                        Text(text = item.badgeCount.toString())
                                    }
                                },
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)

                            )
                        }
                    }
                },
                drawerState = drawerState,
                scrimColor = MaterialTheme.colorScheme.secondary
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),

                            title = {
                                Text(text = "AutoActa")
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu"
                                    )
                                }
                            }

                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer

                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "Home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("Home") { homePage() }
                        composable("Summaries") { summariesPage() }
                        composable("Settings") { settingsPage() }
                    }
                }

            }

        }
    }





    @Composable
    fun homePage(){

        Box() {
            RecordButton() // Positioned correctly now within the Scaffold's content
        }
    }









    @Composable
    fun RecordButton() {
        var isRecording by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val activity = (context as? Activity)

        Box(
            contentAlignment = Alignment.Center, // Align the button to the center of the Box
            modifier = Modifier.fillMaxSize() // Box occupies the entire screen
        ) {

            Button(onClick = {
                if (isRecording) {
                    // Stop recording
                    isRecording = false
                    stopRecording() // Assume this function correctly stops the recording and releases the MediaRecorder
                    // Potentially navigate to a new activity or handle post-recording logic here
                } else {
                    // Start recording after checking permissions
                    activity?.let {
                        if (checkPermissions(it)) {
                            startRecording()
                            isRecording = true
                        } else {
                            // Permissions not granted, handle accordingly
                            // For instance, showing a rationale or guiding the user to the settings
                        }
                    }
                }
            }, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }
        }
    }

    private fun checkPermissions(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }



    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Assuming you're using external cache directory for simplicity
            // Consider using getExternalFilesDir(Environment.DIRECTORY_MUSIC) for production
            audioFile = File(externalCacheDir, "recorded_audio.wav")
            setOutputFile(audioFile.absolutePath)

            try {
                prepare()
                start()
                // Recording started
            } catch (e: IOException) {
                // Handle preparation or start failure
                Log.e("MainActivity", "startRecording() failed", e)
            }
        }
    }

    private fun stopRecording() {
        // Stop the recording
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null // Set mediaRecorder to null to indicate it's no longer in use

        // Here, you might want to handle the recorded audio file.
        // For example, you could start an activity to play the recording
        // or upload it to a server, depending on your app's functionality.
        // This is a placeholder for whatever next steps you need.
        handleRecordedAudio(audioFile)
    }

    private fun handleRecordedAudio(audioFile: File) {
        showMakeSummaryDialog = true
    }






}
